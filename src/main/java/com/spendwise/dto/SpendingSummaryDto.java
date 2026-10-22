package com.spendwise.dto;

import java.math.BigDecimal;

public class SpendingSummaryDto {
    private BigDecimal todayTotal;
    private BigDecimal yesterdayTotal;
    private BigDecimal thisMonthTotal;
    private BigDecimal lastMonthTotal;
    private BigDecimal allTimeTotal;
    private long totalCount;
    private BigDecimal averageExpense;
    private String topCategory;
    private BigDecimal topCategoryAmount;

    public SpendingSummaryDto() {
        this.todayTotal = BigDecimal.ZERO;
        this.yesterdayTotal = BigDecimal.ZERO;
        this.thisMonthTotal = BigDecimal.ZERO;
        this.lastMonthTotal = BigDecimal.ZERO;
        this.allTimeTotal = BigDecimal.ZERO;
        this.totalCount = 0;
        this.averageExpense = BigDecimal.ZERO;
        this.topCategory = "None";
        this.topCategoryAmount = BigDecimal.ZERO;
    }

    public BigDecimal getTodayTotal() {
        return todayTotal;
    }

    public void setTodayTotal(BigDecimal todayTotal) {
        this.todayTotal = todayTotal;
    }

    public BigDecimal getYesterdayTotal() {
        return yesterdayTotal;
    }

    public void setYesterdayTotal(BigDecimal yesterdayTotal) {
        this.yesterdayTotal = yesterdayTotal;
    }

    public BigDecimal getThisMonthTotal() {
        return thisMonthTotal;
    }

    public void setThisMonthTotal(BigDecimal thisMonthTotal) {
        this.thisMonthTotal = thisMonthTotal;
    }

    public BigDecimal getLastMonthTotal() {
        return lastMonthTotal;
    }

    public void setLastMonthTotal(BigDecimal lastMonthTotal) {
        this.lastMonthTotal = lastMonthTotal;
    }

    public BigDecimal getAllTimeTotal() {
        return allTimeTotal;
    }

    public void setAllTimeTotal(BigDecimal allTimeTotal) {
        this.allTimeTotal = allTimeTotal;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public BigDecimal getAverageExpense() {
        return averageExpense;
    }

    public void setAverageExpense(BigDecimal averageExpense) {
        this.averageExpense = averageExpense;
    }

    public String getTopCategory() {
        return topCategory;
    }

    public void setTopCategory(String topCategory) {
        this.topCategory = topCategory;
    }

    public BigDecimal getTopCategoryAmount() {
        return topCategoryAmount;
    }

    public void setTopCategoryAmount(BigDecimal topCategoryAmount) {
        this.topCategoryAmount = topCategoryAmount;
    }
}
