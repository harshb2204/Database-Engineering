import java.util.SortedMap;
import java.util.TreeMap;

public class Test {
    // Basic implementation of a HashRing for testing purposes
    static class HashRing {
        private final SortedMap<Integer, String> ring = new TreeMap<>();

        public void add(String node, int weight) {
            // Adding a node with weight (simplification by adding multiple virtual nodes)
            for (int i = 0; i < weight; i++) {
                String virtualNode = node + "#" + i;
                ring.put(virtualNode.hashCode(), node);
            }
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

    public static void main(String[] args) {
        HashRing ring = new HashRing();
        ring.add("127.0.0.1:11211", 200);
        ring.add("127.0.0.2:11211", 200);
        ring.add("127.0.0.3:11211", 200);

        System.out.println(ring.get("fasdfdf"));
    }
}
