package com.sh3m.recipebook;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    public long id;
    public String name;
    public String description;
    public String imagePath;
    public long createdAt;
    public long updatedAt;

    public List<Ingredient> ingredients = new ArrayList<>();
    public List<Step> steps = new ArrayList<>();

    public Recipe() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
    }
}
