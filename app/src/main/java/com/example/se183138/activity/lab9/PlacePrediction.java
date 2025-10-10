package com.example.se183138.activity.lab9;

public class PlacePrediction {
    private String description;
    private String placeId;
    private String mainText;
    private String secondaryText;
    
    public PlacePrediction(String description, String placeId, String mainText, String secondaryText) {
        this.description = description;
        this.placeId = placeId;
        this.mainText = mainText;
        this.secondaryText = secondaryText;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getPlaceId() {
        return placeId;
    }
    
    public String getMainText() {
        return mainText;
    }
    
    public String getSecondaryText() {
        return secondaryText;
    }
}

