package com.spendwise.service;

import com.spendwise.dto.CategorySummaryDto;
import com.spendwise.dto.DailySpendingDto;
import com.spendwise.dto.ExpenseRequestDto;
import com.spendwise.dto.SpendingSummaryDto;
import com.spendwise.model.Category;
import com.spendwise.model.Expense;
import com.spendwise.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ExpenseServiceTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
    }

    @Test
    void testCreateExpense() {
        ExpenseRequestDto dto = new ExpenseRequestDto(
                "Coffee",
                new BigDecimal("4.50"),
                Category.FOOD,
                LocalDate.now(),
                "Credit Card",
                "Morning latte"
        );

        Expense created = expenseService.createExpense(dto);

        assertNotNull(created.getId());
        assertEquals("Coffee", created.getTitle());
        assertEquals(new BigDecimal("4.50"), created.getAmount());
        assertEquals(Category.FOOD, created.getCategory());
        assertEquals("Morning latte", created.getNotes());
    }

    @Test
    void testUpdateExpense() {
        Expense saved = expenseRepository.save(new Expense(
                "Book",
                new BigDecimal("20.00"),
                Category.EDUCATION,
                LocalDate.now(),
                "Debit Card",
                "Fiction"
        ));

        ExpenseRequestDto updateDto = new ExpenseRequestDto(
                "Updated Book",
                new BigDecimal("25.00"),
                Category.EDUCATION,
                LocalDate.now(),
                "Cash",
                "Non-fiction"
        );

        Expense updated = expenseService.updateExpense(saved.getId(), updateDto);

        assertEquals("Updated Book", updated.getTitle());
        assertEquals(new BigDecimal("25.00"), updated.getAmount());
        assertEquals("Cash", updated.getPaymentMethod());
    }

    @Test
    void testDeleteExpense() {
        Expense saved = expenseRepository.save(new Expense(
                "Lunch",
                new BigDecimal("15.00"),
                Category.FOOD,
                LocalDate.now(),
                "Cash",
                ""
        ));

        Long id = saved.getId();
        expenseService.deleteExpense(id);

        assertThrows(NoSuchElementException.class, () -> expenseService.getExpenseById(id));
    }

    @Test
    void testGetSummary() {
        LocalDate today = LocalDate.now();
        expenseRepository.save(new Expense("Breakfast", new BigDecimal("10.00"), Category.FOOD, today, "Cash", ""));
        expenseRepository.save(new Expense("Lunch", new BigDecimal("15.00"), Category.FOOD, today, "Cash", ""));
        expenseRepository.save(new Expense("Yesterday Snack", new BigDecimal("5.00"), Category.FOOD, today.minusDays(1), "Cash", ""));

        SpendingSummaryDto summary = expenseService.getSummary(today);

        assertEquals(new BigDecimal("25.00"), summary.getTodayTotal());
        assertEquals(new BigDecimal("5.00"), summary.getYesterdayTotal());
        assertEquals(3, summary.getTotalCount());
        assertEquals(new BigDecimal("30.00"), summary.getAllTimeTotal());
        assertEquals(new BigDecimal("10.00"), summary.getAverageExpense());
    }

    @Test
    void testCategoryBreakdown() {
        LocalDate today = LocalDate.now();
        expenseRepository.save(new Expense("Groceries", new BigDecimal("60.00"), Category.GROCERIES, today, "Card", ""));
        expenseRepository.save(new Expense("Dinner", new BigDecimal("40.00"), Category.FOOD, today, "Card", ""));

        List<CategorySummaryDto> breakdown = expenseService.getCategoryBreakdown(null, null);

        assertFalse(breakdown.isEmpty());
        assertEquals(2, breakdown.size());
        assertEquals("Groceries", breakdown.get(0).getDisplayName());
        assertEquals(new BigDecimal("60.00"), breakdown.get(0).getTotalAmount());
        assertEquals(60.0, breakdown.get(0).getPercentage());
    }

    @Test
    void testDailySpending() {
        LocalDate today = LocalDate.now();
        expenseRepository.save(new Expense("Dinner", new BigDecimal("35.00"), Category.FOOD, today, "Card", ""));

        List<DailySpendingDto> daily = expenseService.getDailySpending(7);

        assertEquals(7, daily.size());
        // Last element should be today
        DailySpendingDto todayDto = daily.get(daily.size() - 1);
        assertEquals(today, todayDto.getDate());
        assertEquals(new BigDecimal("35.00"), todayDto.getAmount());
    }

    @Test
    void testExportCsv() {
        expenseRepository.save(new Expense("Item 1", new BigDecimal("12.50"), Category.SHOPPING, LocalDate.now(), "Card", "Sample note"));

        StringWriter writer = new StringWriter();
        expenseService.exportToCsv(writer, null, null, null, null);

        String csv = writer.toString();
        assertTrue(csv.contains("ID,Date,Title,Category,Amount,Currency,Payment Method,Notes"));
        assertTrue(csv.contains("Item 1"));
        assertTrue(csv.contains("12.50"));
    }
}
