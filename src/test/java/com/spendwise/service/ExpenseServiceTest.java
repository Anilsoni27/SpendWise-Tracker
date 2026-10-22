package com.spendwise.service;

import com.spendwise.dto.CategorySummaryDto;
import com.spendwise.dto.DailySpendingDto;
import com.spendwise.dto.ExpenseRequestDto;
import com.spendwise.dto.SpendingSummaryDto;
import com.spendwise.model.Category;
import com.spendwise.model.Expense;
import com.spendwise.model.User;
import com.spendwise.repository.ExpenseRepository;
import com.spendwise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@WithMockUser(username = "testuser")
class ExpenseServiceTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
        userRepository.deleteAll();
        testUser = userRepository.save(new User("testuser", "Test User", passwordEncoder.encode("secret123"), "ROLE_USER"));
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
        assertEquals("testuser", created.getUser().getUsername());
    }

    @Test
    void testUpdateExpense() {
        Expense expense = new Expense(
                "Book",
                new BigDecimal("20.00"),
                Category.EDUCATION,
                LocalDate.now(),
                "Debit Card",
                "Fiction"
        );
        expense.setUser(testUser);
        Expense saved = expenseRepository.save(expense);

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
        Expense expense = new Expense(
                "Lunch",
                new BigDecimal("15.00"),
                Category.FOOD,
                LocalDate.now(),
                "Cash",
                ""
        );
        expense.setUser(testUser);
        Expense saved = expenseRepository.save(expense);

        Long id = saved.getId();
        expenseService.deleteExpense(id);

        assertThrows(NoSuchElementException.class, () -> expenseService.getExpenseById(id));
    }

    @Test
    void testGetSummaryCalculation() {
        LocalDate today = LocalDate.now();

        Expense e1 = new Expense("Groceries", new BigDecimal("100.00"), Category.GROCERIES, today, "Card", "");
        e1.setUser(testUser);
        Expense e2 = new Expense("Snacks", new BigDecimal("20.00"), Category.FOOD, today, "Cash", "");
        e2.setUser(testUser);
        Expense e3 = new Expense("Yesterday dinner", new BigDecimal("50.00"), Category.FOOD, today.minusDays(1), "Card", "");
        e3.setUser(testUser);

        expenseRepository.saveAll(List.of(e1, e2, e3));

        SpendingSummaryDto summary = expenseService.getSummary(today);

        assertEquals(new BigDecimal("120.00"), summary.getTodayTotal());
        assertEquals(new BigDecimal("50.00"), summary.getYesterdayTotal());
        assertEquals(new BigDecimal("170.00"), summary.getThisMonthTotal());
        assertEquals(new BigDecimal("170.00"), summary.getAllTimeTotal());
        assertEquals(3, summary.getTotalCount());
    }

    @Test
    void testCategoryBreakdown() {
        LocalDate today = LocalDate.now();
        Expense e1 = new Expense("Groceries 1", new BigDecimal("60.00"), Category.GROCERIES, today, "Card", "");
        e1.setUser(testUser);
        Expense e2 = new Expense("Groceries 2", new BigDecimal("40.00"), Category.GROCERIES, today, "Cash", "");
        e2.setUser(testUser);
        Expense e3 = new Expense("Dinner", new BigDecimal("100.00"), Category.FOOD, today, "Card", "");
        e3.setUser(testUser);

        expenseRepository.saveAll(List.of(e1, e2, e3));

        List<CategorySummaryDto> breakdown = expenseService.getCategoryBreakdown(today.minusDays(5), today);

        assertEquals(2, breakdown.size());
        assertEquals("FOOD", breakdown.get(0).getCategory());
        assertEquals(new BigDecimal("100.00"), breakdown.get(0).getTotalAmount());
        assertEquals(50.0, breakdown.get(0).getPercentage());
    }

    @Test
    void testDailySpendingChartData() {
        LocalDate today = LocalDate.now();
        Expense e = new Expense("Taxi", new BigDecimal("35.00"), Category.TRANSPORTATION, today, "Card", "");
        e.setUser(testUser);
        expenseRepository.save(e);

        List<DailySpendingDto> daily = expenseService.getDailySpending(7);

        assertEquals(7, daily.size());
        DailySpendingDto todayDto = daily.get(daily.size() - 1);
        assertEquals(today, todayDto.getDate());
        assertEquals(new BigDecimal("35.00"), todayDto.getAmount());
        assertEquals(1, todayDto.getCount());
    }

    @Test
    void testExportCsv() {
        LocalDate today = LocalDate.now();
        Expense e = new Expense("Movie", new BigDecimal("15.50"), Category.ENTERTAINMENT, today, "Card", "Popcorn included");
        e.setUser(testUser);
        expenseRepository.save(e);

        StringWriter sw = new StringWriter();
        expenseService.exportToCsv(sw, null, null, null, null);

        String csvOutput = sw.toString();
        assertTrue(csvOutput.contains("ID,Date,Title,Category,Amount,Currency,Payment Method,Notes"));
        assertTrue(csvOutput.contains("Movie"));
        assertTrue(csvOutput.contains("15.50"));
    }
}
