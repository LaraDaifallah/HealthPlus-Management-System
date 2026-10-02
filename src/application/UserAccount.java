package application;

public class UserAccount {
    private int userID;
    private String password;
    private String role;
    private Integer employeeID;
    private Integer clientID;

    public UserAccount(int userID, String password, String role,
                       Integer employeeID, Integer clientID) {
        this.userID = userID;
        this.password = password;
        this.role = role;
        this.employeeID = employeeID;
        this.clientID = clientID;
    }

    public int getUserID() { return userID; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public Integer getEmployeeID() { return employeeID; }
    public Integer getClientID() { return clientID; }
}