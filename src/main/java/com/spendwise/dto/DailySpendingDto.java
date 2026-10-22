package com.spendwise.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailySpendingDto {
    private LocalDate date;
    private String label; // e.g. "Oct 15"
    private BigDecimal amount;
    private long count;

    public DailySpendingDto() {
    }

    public DailySpendingDto(LocalDate date, String label, BigDecimal amount, long count) {
        this.date = date;
        this.label = label;
        this.amount = amount;
        this.count = count;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
