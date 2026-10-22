package com.spendwise.controller;

import com.spendwise.dto.CategoryOptionDto;
import com.spendwise.dto.CategorySummaryDto;
import com.spendwise.dto.DailySpendingDto;
import com.spendwise.dto.ExpenseRequestDto;
import com.spendwise.dto.SpendingSummaryDto;
import com.spendwise.model.Category;
import com.spendwise.model.Expense;
import com.spendwise.service.ExpenseService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseRestController {

    private final ExpenseService expenseService;

    public ExpenseRestController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        List<Expense> expenses = expenseService.getExpenses(search, category, startDate, endDate, sortBy, sortDir);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(@Valid @RequestBody ExpenseRequestDto dto) {
        Expense created = expenseService.createExpense(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseRequestDto dto) {
        Expense updated = expenseService.updateExpense(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Expense deleted successfully", "id", id));
    }

    @GetMapping("/summary")
    public ResponseEntity<SpendingSummaryDto> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(expenseService.getSummary(date));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryOptionDto>> getCategories() {
        return ResponseEntity.ok(expenseService.getAllCategories());
    }

    @GetMapping("/charts/categories")
    public ResponseEntity<List<CategorySummaryDto>> getCategoryBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(expenseService.getCategoryBreakdown(startDate, endDate));
    }

    @GetMapping("/charts/daily")
    public ResponseEntity<List<DailySpendingDto>> getDailySpending(
            @RequestParam(defaultValue = "14") int days
    ) {
        return ResponseEntity.ok(expenseService.getDailySpending(days));
    }

    @GetMapping("/export")
    public void exportToCsv(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response
    ) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"spendwise-expenses.csv\"");
        expenseService.exportToCsv(response.getWriter(), search, category, startDate, endDate);
    }

    @PostMapping("/reset-demo")
    public ResponseEntity<Map<String, String>> resetDemo() {
        expenseService.resetToSampleData();
        return ResponseEntity.ok(Map.of("message", "Sample data reset successfully"));
    }
}
