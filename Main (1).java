import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Main {
    static class InMemoryDB {
        private final Map<String, Integer> db;
        private Stack<Map<String, Integer>> txnStack;
        private boolean txnActive;

        public InMemoryDB() {
            this.db = new HashMap<>();
            this.txnStack = new Stack<>();
            this.txnActive = false;
        }

        public Integer get(String key) {
            if (txnActive && !txnStack.isEmpty()) {
                Map<String, Integer> currentTxn = txnStack.peek();
                if (currentTxn.containsKey(key)) {
                    return currentTxn.get(key);
                }
            }
            return db.get(key);
        }

        public void put(String key, int value) {
            if (!txnActive) {
                throw new IllegalStateException("Transaction not in progress.");
            }
            if (txnStack.isEmpty()) {
                txnStack.push(new HashMap<>());
            }
            txnStack.peek().put(key, value);
        }

        public void begin_transaction() {
            if (txnActive) {
                throw new IllegalStateException("Transaction is already in progress.");
            }
            txnActive = true;
            txnStack.push(new HashMap<>());
        }

        public void commit() {
            if (!txnActive || txnStack.isEmpty()) {
                throw new IllegalStateException("There is no transaction to commit.");
            }
            Map<String, Integer> changes = txnStack.pop();
            for (Map.Entry<String, Integer> entry : changes.entrySet()) {
                db.put(entry.getKey(), entry.getValue());
            }
            if (txnStack.isEmpty()) {
                txnActive = false;
            }
        }

        public void rollback() {
            if (!txnActive || txnStack.isEmpty()) {
                throw new IllegalStateException("There is no transaction to rollback.");
            }
            txnStack.pop();
            if (txnStack.isEmpty()) {
                txnActive = false;
            }
        }
    }
    // Test cases if needed - Not sure If I had to include
      public static void main(String[] args) {
        InMemoryDB db = new InMemoryDB();

        System.out.println(db.get("A")); // null
        try {
            db.put("A", 5);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage()); // Transaction is not in progress.
        }
        db.begin_transaction();
        db.put("A", 5);
        System.out.println(db.get("A")); // null
        db.put("A", 6);
        db.commit();
        System.out.println(db.get("A")); // 6
        try {
            db.commit();
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage()); // No transaction to commit.
        }
        try {
            db.rollback();
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage()); // No transaction to rollback.
        }
        System.out.println(db.get("B")); // null
        db.begin_transaction();
        db.put("B", 10);
        db.rollback();
        System.out.println(db.get("B")); // null
    }
}
