package application;

public class Warehouse {

    private int id;
    private String name;
    private String address;
    private String city;
    private String phone;
    private int capacity;

    // reading from DB
    public Warehouse(int id, String name, String address, String city, String phone, int capacity) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.phone = phone;
        this.capacity=capacity;
    }

    public Warehouse(String name, String address, String city, String phone, int capacity) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.phone = phone;
        this.capacity=capacity;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getPhone() {
        return phone;
    }
    public int getCapacity() {
        return capacity;
    }
}