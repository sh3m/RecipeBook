package com.sh3m.recipebook;

public class Ingredient {
    public long id;
    public long recipeId;
    public String ingredient;
    public String amount;
    public int sortOrder;

    public Ingredient() {}

    public Ingredient(long recipeId, String ingredient, String amount, int sortOrder) {
        this.recipeId = recipeId;
        this.ingredient = ingredient;
        this.amount = amount;
        this.sortOrder = sortOrder;
    }
}
