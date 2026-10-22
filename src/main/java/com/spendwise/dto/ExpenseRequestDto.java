package com.spendwise.dto;

import com.spendwise.model.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title cannot exceed 120 characters")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String currency = "INR";

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Date is required")
    private LocalDate expenseDate;

    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method cannot exceed 50 characters")
    private String paymentMethod;

    @Size(max = 255, message = "Notes cannot exceed 255 characters")
    private String notes;

    public ExpenseRequestDto() {
    }

    public ExpenseRequestDto(String title, BigDecimal amount, String currency, Category category, LocalDate expenseDate, String paymentMethod, String notes) {
        this.title = title;
        this.amount = amount;
        this.currency = (currency != null && !currency.trim().isEmpty()) ? currency.trim().toUpperCase() : "INR";
        this.category = category;
        this.expenseDate = expenseDate;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }

    public ExpenseRequestDto(String title, BigDecimal amount, Category category, LocalDate expenseDate, String paymentMethod, String notes) {
        this(title, amount, "INR", category, expenseDate, paymentMethod, notes);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = (currency != null && !currency.trim().isEmpty()) ? currency.trim().toUpperCase() : "INR";
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
