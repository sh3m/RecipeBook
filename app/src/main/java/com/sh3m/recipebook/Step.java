package com.sh3m.recipebook;

public class Step {
    public long id;
    public long recipeId;
    public String text;
    public int stepNumber;

    public Step() {}

    public Step(long recipeId, String text, int stepNumber) {
        this.recipeId = recipeId;
        this.text = text;
        this.stepNumber = stepNumber;
    }
}
