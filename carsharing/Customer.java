package carsharing;

public class Customer {
    String name;
    int id;
    int carID;

    public int getCarID() {
        return carID;
    }

    public void setCarID(Integer carID) {
        this.carID = carID;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Customer(int id, String name, Integer carID) {
        this.id = id;
        this.name = name;
        this.carID = carID;
    }
}
