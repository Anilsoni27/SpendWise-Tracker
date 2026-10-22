package com.spendwise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendwise.dto.ExpenseRequestDto;
import com.spendwise.model.Category;
import com.spendwise.model.Expense;
import com.spendwise.model.User;
import com.spendwise.repository.ExpenseRepository;
import com.spendwise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser")
class ExpenseRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
        userRepository.deleteAll();
        testUser = userRepository.save(new User("testuser", "Test User", passwordEncoder.encode("secret123"), "ROLE_USER"));
    }

    private Expense createTestExpense(String title, BigDecimal amount, Category category) {
        Expense expense = new Expense(title, amount, category, LocalDate.now(), "Card", "");
        expense.setUser(testUser);
        return expenseRepository.save(expense);
    }

    @Test
    void testGetAllExpenses() throws Exception {
        createTestExpense("Coffee", new BigDecimal("4.00"), Category.FOOD);
        createTestExpense("Bus", new BigDecimal("2.50"), Category.TRANSPORTATION);

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testCreateExpenseValid() throws Exception {
        ExpenseRequestDto dto = new ExpenseRequestDto(
                "Supermarket",
                new BigDecimal("55.20"),
                Category.GROCERIES,
                LocalDate.now(),
                "Debit Card",
                "Weekly fruit and veg"
        );

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Supermarket")))
                .andExpect(jsonPath("$.amount", is(55.20)));
    }

    @Test
    void testCreateExpenseValidationFailure() throws Exception {
        ExpenseRequestDto invalidDto = new ExpenseRequestDto(
                "", // Blank title
                new BigDecimal("-5.00"), // Negative amount
                null, // Missing category
                null,
                "",
                ""
        );

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.amount", notNullValue()));
    }

    @Test
    void testGetSummaryEndpoint() throws Exception {
        createTestExpense("Lunch", new BigDecimal("12.00"), Category.FOOD);

        mockMvc.perform(get("/api/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayTotal", is(12.00)))
                .andExpect(jsonPath("$.totalCount", is(1)));
    }

    @Test
    void testGetCategories() throws Exception {
        mockMvc.perform(get("/api/expenses/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].displayName", notNullValue()));
    }

    @Test
    void testDeleteExpense() throws Exception {
        Expense saved = createTestExpense("Cinema", new BigDecimal("18.00"), Category.ENTERTAINMENT);

        mockMvc.perform(delete("/api/expenses/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }
}
