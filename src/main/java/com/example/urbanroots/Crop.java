package com.example.urbanroots;

public class Crop {
    private String cropId;
    private String cropName;
    private double price;
    private String description;
    private String status;

    // Required for Firestore
    public Crop() {}

    public Crop(String cropId, String cropName, double price, String description, String status) {
        this.cropId = cropId;
        this.cropName = cropName;
        this.price = price;
        this.description = description;
        this.status = status;
    }

    // Getters and Setters
    public String getCropId() { return cropId; }
    public void setCropId(String cropId) { this.cropId = cropId; }
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}