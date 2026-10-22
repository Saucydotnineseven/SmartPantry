package com.example.smartpantry;

// Plain Java class (a "model") representing one row in the pantry table.
public class PantryItem {
    private int id;            // primary key from the database
    private String name;       // e.g. "tomato"
    private double quantity;   // e.g. 3
    private String unit;       // e.g. "pcs", "g", "ml"
    private String expiryDate; // optional, stored as text "yyyy-MM-dd" (may be empty)

    // Constructor used when READING from the database (id is known)
    public PantryItem(int id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    // Constructor used when CREATING a new item (database assigns the id)
    public PantryItem(String name, double quantity, String unit, String expiryDate) {
        this(0, name, quantity, unit, expiryDate);
    }

    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiryDate() { return expiryDate; }

    public void setName(String name) { this.name = name; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
}