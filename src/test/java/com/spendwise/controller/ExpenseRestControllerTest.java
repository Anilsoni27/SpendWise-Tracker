package com.spendwise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendwise.dto.ExpenseRequestDto;
import com.spendwise.model.Category;
import com.spendwise.model.Expense;
import com.spendwise.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
    }

    @Test
    void testGetAllExpenses() throws Exception {
        expenseRepository.save(new Expense("Coffee", new BigDecimal("4.00"), Category.FOOD, LocalDate.now(), "Card", ""));
        expenseRepository.save(new Expense("Bus", new BigDecimal("2.50"), Category.TRANSPORTATION, LocalDate.now(), "Cash", ""));

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
        expenseRepository.save(new Expense("Lunch", new BigDecimal("12.00"), Category.FOOD, LocalDate.now(), "Cash", ""));

        mockMvc.perform(get("/api/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayTotal", is(12.00)))
                .andExpect(jsonPath("$.totalCount", is(1)));
    }

    @Test
    void testGetCategoriesEndpoint() throws Exception {
        mockMvc.perform(get("/api/expenses/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].displayName", notNullValue()));
    }

    @Test
    void testDeleteExpenseEndpoint() throws Exception {
        Expense saved = expenseRepository.save(new Expense("Snack", new BigDecimal("3.00"), Category.FOOD, LocalDate.now(), "Cash", ""));

        mockMvc.perform(delete("/api/expenses/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }
}
