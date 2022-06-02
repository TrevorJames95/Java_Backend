package carsharing;

public class Car {
    String name;
    int id;

    public int getCompany() {
        return company;
    }

    public void setCompany(int company) {
        this.company = company;
    }

    int company;
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

    public Car(int id, String name, int company) {
        this.id = id;
        this.name = name;
        this.company = company;
    }

}