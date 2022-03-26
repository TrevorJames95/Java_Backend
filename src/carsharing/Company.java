package carsharing;

public class Company {
    String name;
    int id;
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

    public Company(int id, String name) {
        this.id = id;
        this.name = name;
    }

}
