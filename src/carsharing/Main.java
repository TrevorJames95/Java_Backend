package carsharing;

public class Main {

    public static void main(String[] args) {
        // write your code here
        H2Dao db = new H2Dao();
        db.initDatabase(args[1]);
        MenuManager.start();
        db.closeConnection();
    }
}
