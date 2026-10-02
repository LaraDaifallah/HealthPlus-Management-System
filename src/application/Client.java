package application;

public class Client {

    private int id;
    private String name;
    private String type;
    private String phone;
    private String city;
    private double creditLimit;

    public Client(int id, String name, String type, String phone, String city, double creditLimit) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.phone = phone;
        this.city = city;
        this.creditLimit = creditLimit;
    }

    public Client(String name, String type, String phone, String city, double creditLimit) {
        this.name = name;
        this.type = type;
        this.phone = phone;
        this.city = city;
        this.creditLimit = creditLimit;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getPhone() {
        return phone;
    }

    public String getCity() {
        return city;
    }

    public double getCreditLimit() {
        return creditLimit;
    }
}