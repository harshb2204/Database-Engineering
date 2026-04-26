import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Main {

    // Consistent Hashing implementation
    static class HashRing {
        private final SortedMap<Integer, String> ring = new TreeMap<>();

        public void add(String node) {
            ring.put(node.hashCode(), node);
        }

        public String get(String key) {
            if (ring.isEmpty())
                return null;
            int hash = key.hashCode();
            if (!ring.containsKey(hash)) {
                SortedMap<Integer, String> tailMap = ring.tailMap(hash);
                hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
            }
            return ring.get(hash);
        }
    }

    private static final HashRing hr = new HashRing();
    private static final Map<String, Connection> clients = new HashMap<>();

    public static void main(String[] args) throws Exception {
        hr.add("5432");
        hr.add("5433");
        hr.add("5434");

        connect();

        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    if ("POST".equals(exchange.getRequestMethod())) {
                        handlePost(exchange);
                    } else if ("GET".equals(exchange.getRequestMethod())) {
                        handleGet(exchange);
                    } else {
                        exchange.sendResponseHeaders(405, -1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, -1);
                }
            }
        });

        server.setExecutor(null);
        System.out.println("Listening 8081");
        server.start();
    }

    private static void connect() {
        String[] ports = { "5432", "5433", "5434" };
        for (String port : ports) {
            try {
                // Ensure you have the PostgreSQL JDBC driver in your classpath
                String url = "jdbc:postgresql://husseinmac:" + port + "/postgres";
                Connection conn = DriverManager.getConnection(url, "postgres", "postgres");
                clients.put(port, conn);
                System.out.println("Connected to " + port);
            } catch (SQLException e) {
                System.err.println("Failed to connect to " + port + ": " + e.getMessage());
            }
        }
    }

    private static void handlePost(HttpExchange exchange) throws Exception {
        // Extract query param `url`
        String query = exchange.getRequestURI().getQuery();
        String url = null;
        if (query != null && query.startsWith("url=")) {
            url = query.substring(4);
        }

        if (url == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(url.getBytes(StandardCharsets.UTF_8));
        String hashString = Base64.getEncoder().encodeToString(hashBytes);
        String urlId = hashString.substring(0, 5);

        String serverPort = hr.get(urlId);
        Connection conn = clients.get(serverPort);

        if (conn != null) {
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO URL_TABLE (URL, URL_ID) VALUES (?, ?)")) {
                stmt.setString(1, url);
                stmt.setString(2, urlId);
                stmt.executeUpdate();
            }
        } else {
            exchange.sendResponseHeaders(500, -1);
            return;
        }

        String response = String.format("{\"urlId\":\"%s\", \"url\":\"%s\", \"server\":\"%s\"}", urlId, url,
                serverPort);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private static void handleGet(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String urlId = path.substring(1); // remove leading slash

        String serverPort = hr.get(urlId);
        Connection conn = clients.get(serverPort);

        String foundUrl = null;
        if (conn != null) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT URL FROM URL_TABLE WHERE URL_ID = ?")) {
                stmt.setString(1, urlId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        foundUrl = rs.getString("URL");
                    }
                }
            }
        }

        if (foundUrl != null) {
            String response = String.format("{\"urlId\":\"%s\", \"url\":\"%s\", \"server\":\"%s\"}", urlId, foundUrl,
                    serverPort);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }
}
