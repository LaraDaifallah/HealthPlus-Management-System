package application;

public class Supplier {

    private int id;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String city;

    public Supplier(int id, String name, String contactPerson, String phone, String email, String city) {
        this.id = id;
        this.name = name;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.city = city;
    }

    public Supplier(String name, String contactPerson, String phone, String email, String city) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.city = city;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getCity() {
        return city;
    }
}