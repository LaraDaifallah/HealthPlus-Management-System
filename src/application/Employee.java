package application;

public class Employee {

    private int id;
    private String firstName;
    private String lastName;
    private String role;
    private String hireDate;
    private String phone;
    private double salary;
    private Integer warehouseID;

    public Employee(int id, String firstName, String lastName,
                    String role, String hireDate,
                    String phone, double salary, Integer warehouseID) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.hireDate = hireDate;
        this.phone = phone;
        this.salary = salary;
        this.warehouseID = warehouseID;
    }

    public Employee(String firstName, String lastName,
                    String role, String hireDate,
                    String phone, double salary, Integer warehouseID) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.hireDate = hireDate;
        this.phone = phone;
        this.salary = salary;
        this.warehouseID = warehouseID;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getRole() {
        return role;
    }

    public String getHireDate() {
        return hireDate;
    }

    public String getPhone() {
        return phone;
    }

    public double getSalary() {
        return salary;
    }

    public Integer getWarehouseID() {
        return warehouseID;
    }
}