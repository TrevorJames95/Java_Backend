package carsharing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class H2Dao {
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:file:C:\\Users\\trevo\\Desktop\\Car Sharing" +
            "\\Car Sharing\\task\\src\\carsharing\\db\\";
    Connection conn;
    static Statement st;

    public void initDatabase(String args){
        try {
            Class.forName (JDBC_DRIVER);
            conn = DriverManager.getConnection (DB_URL + args);
            st = conn.createStatement();
            conn.setAutoCommit(true);

            //st.execute("DROP TABLE IF EXISTS CUSTOMER, CAR, COMPANY");
            st.execute("CREATE TABLE IF NOT EXISTS COMPANY (" +
                    "ID INT AUTO_INCREMENT PRIMARY KEY," +
                    "NAME VARCHAR(30) UNIQUE NOT NULL);");

            st.execute("CREATE TABLE IF NOT EXISTS CAR (\n" +
                    "ID INT AUTO_INCREMENT PRIMARY KEY," +
                    "NAME VARCHAR(30) UNIQUE NOT NULL," +
                    "COMPANY_ID INT NOT NULL," +
                    "CONSTRAINT FK_COMPANY FOREIGN KEY(COMPANY_ID)" +
                    "REFERENCES COMPANY(ID));");

            st.execute("CREATE TABLE IF NOT EXISTS CUSTOMER (\n" +
                    "ID INT AUTO_INCREMENT PRIMARY KEY," +
                    "NAME VARCHAR(30) UNIQUE NOT NULL," +
                    "RENTED_CAR_ID INT," +
                    "CONSTRAINT FK_CAR FOREIGN KEY(RENTED_CAR_ID)" +
                    "REFERENCES CAR(ID));");

            st.execute("ALTER TABLE company ALTER COLUMN id RESTART WITH 1");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Company> getAllCompanies() {
        ArrayList<Company> companies = new ArrayList<>();
        try {
            var result = st.executeQuery("SELECT * FROM COMPANY");
            while (result.next()) {
                companies.add(new Company(result.getInt("ID"), result.getString("NAME")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return companies;
    }

    public static ArrayList<Car> getAllCars(int id) {
        ArrayList<Car> cars = new ArrayList<>();
        try {
            var result = st.executeQuery("SELECT * FROM CAR WHERE COMPANY_ID = " + id);
            while (result.next()) {
                cars.add(new Car(result.getInt("ID"), result.getString("NAME"),
                        result.getInt("COMPANY_ID")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    public static ArrayList<Customer> getAllCustomers() {
        ArrayList<Customer> customers = new ArrayList<>();
        try {
            var result = st.executeQuery("SELECT * FROM CUSTOMER");
            while (result.next()) {
                customers.add(new Customer(result.getInt("ID"), result.getString("NAME"),
                        result.getInt("RENTED_CAR_ID")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    public static void createCompany(String name) {
        try {
            st.executeUpdate("INSERT INTO COMPANY (NAME)" +
                    " VALUES ('" + name + "');");
            System.out.println("The company was created!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createCar(String name, int companyID) {
        try {
            st.executeUpdate("INSERT INTO CAR (NAME, COMPANY_ID) " +
                    "VALUES('" + name + "', " + companyID + ");");
            System.out.println("The car was created!");

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createCustomer(String name) {
        try {
            st.executeUpdate("INSERT INTO CUSTOMER (NAME, RENTED_CAR_ID) " +
                    "VALUES('" + name + "', NULL" + ")");
            System.out.println("The customer was created!");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateCustomer(int carID, int customerID) {
        try {
            st.executeUpdate("UPDATE CUSTOMER " +
                    "SET RENTED_CAR_ID = " + carID +
                    " WHERE ID = " + customerID);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateCustomer(int customerID) {
        try {
            st.executeUpdate("UPDATE CUSTOMER " +
                    "SET RENTED_CAR_ID = " + null +
                    " WHERE ID = " + customerID);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

}
