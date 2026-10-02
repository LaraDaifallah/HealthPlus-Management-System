package application;

public class Product {

    private int id;
    private String name;
    private String description;
    private double price;
    private int reorderLevel;
    private int categoryId;

    // reading from DB
    public Product(int id, String name, String description,
                   double price, int reorderLevel,
                   int categoryId) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.reorderLevel = reorderLevel;
        this.categoryId = categoryId;
    }

    public Product(String name, String description,
                   double price, int reorderLevel,
                   int categoryId) {

        this.name = name;
        this.description = description;
        this.price = price;
        this.reorderLevel = reorderLevel;
        this.categoryId = categoryId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public int getCategoryId() {
        return categoryId;
    }
}