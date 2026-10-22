package com.spendwise.model;

public enum Category {
    FOOD("Food & Dining", "#d97706"),          // Warm Amber
    GROCERIES("Groceries", "#16a34a"),         // Forest Green
    TRANSPORTATION("Transportation", "#2563eb"), // Classic Blue
    HOUSING("Housing & Rent", "#7c3aed"),      // Muted Violet
    UTILITIES("Bills & Utilities", "#0891b2"), // Teal / Cyan
    ENTERTAINMENT("Entertainment", "#ea580c"), // Persimmon
    SHOPPING("Shopping", "#db2777"),           // Rose / Pink
    HEALTH("Health & Medical", "#059669"),     // Emerald
    EDUCATION("Education", "#4f46e5"),         // Indigo
    PERSONAL("Personal Care", "#9333ea"),      // Purple
    OTHER("Other", "#4b5563");                 // Neutral Slate

    private final String displayName;
    private final String color;

    Category(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public static Category fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        for (Category c : Category.values()) {
            if (c.name().equalsIgnoreCase(text) || c.displayName.equalsIgnoreCase(text)) {
                return c;
            }
        }
        return OTHER;
    }
}
