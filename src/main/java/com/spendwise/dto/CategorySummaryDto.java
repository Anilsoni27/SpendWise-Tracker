package com.spendwise.dto;

import java.math.BigDecimal;

public class CategorySummaryDto {
    private String category;
    private String displayName;
    private String color;
    private BigDecimal totalAmount;
    private long count;
    private double percentage;

    public CategorySummaryDto() {
    }

    public CategorySummaryDto(String category, String displayName, String color, BigDecimal totalAmount, long count, double percentage) {
        this.category = category;
        this.displayName = displayName;
        this.color = color;
        this.totalAmount = totalAmount;
        this.count = count;
        this.percentage = percentage;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
