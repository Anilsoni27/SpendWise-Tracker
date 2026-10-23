package com.spendwise.service;

import com.spendwise.dto.CategoryOptionDto;
import com.spendwise.dto.CategorySummaryDto;
import com.spendwise.dto.DailySpendingDto;
import com.spendwise.dto.ExpenseRequestDto;
import com.spendwise.dto.SpendingSummaryDto;
import com.spendwise.model.Category;
import com.spendwise.model.Expense;
import com.spendwise.model.User;
import com.spendwise.repository.ExpenseRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;
    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");

    public ExpenseService(ExpenseRepository expenseRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpenses(String search, Category category, LocalDate startDate, LocalDate endDate,
                                    String sortBy, String sortDir) {
        return getExpenses(userService.getCurrentAuthenticatedUser(), search, category, startDate, endDate, sortBy, sortDir);
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpenses(User user, String search, Category category, LocalDate startDate, LocalDate endDate,
                                    String sortBy, String sortDir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String property = "expenseDate";
        if ("amount".equalsIgnoreCase(sortBy)) {
            property = "amount";
        } else if ("title".equalsIgnoreCase(sortBy)) {
            property = "title";
        } else if ("category".equalsIgnoreCase(sortBy)) {
            property = "category";
        }

        Sort sort = Sort.by(direction, property);
        if (!"id".equalsIgnoreCase(property)) {
            sort = sort.and(Sort.by(Sort.Direction.DESC, "id"));
        }

        Specification<Expense> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), endDate));
            }
            if (search != null && !search.trim().isEmpty()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), term);
                Predicate notesMatch = cb.like(cb.lower(cb.coalesce(root.get("notes"), "")), term);
                Predicate paymentMatch = cb.like(cb.lower(cb.coalesce(root.get("paymentMethod"), "")), term);
                predicates.add(cb.or(titleMatch, notesMatch, paymentMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return expenseRepository.findAll(spec, sort);
    }

    @Transactional(readOnly = true)
    public Expense getExpenseById(Long id) {
        return getExpenseById(userService.getCurrentAuthenticatedUser(), id);
    }

    @Transactional(readOnly = true)
    public Expense getExpenseById(User user, Long id) {
        return expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NoSuchElementException("Expense not found with id: " + id));
    }

    public Expense createExpense(ExpenseRequestDto dto) {
        return createExpense(userService.getCurrentAuthenticatedUser(), dto);
    }

    public Expense createExpense(User user, ExpenseRequestDto dto) {
        Expense expense = new Expense(
                dto.getTitle().trim(),
                dto.getAmount(),
                (dto.getCurrency() != null && !dto.getCurrency().trim().isEmpty()) ? dto.getCurrency().trim().toUpperCase() : "INR",
                dto.getCategory(),
                dto.getExpenseDate() != null ? dto.getExpenseDate() : LocalDate.now(),
                dto.getPaymentMethod().trim(),
                dto.getNotes() != null ? dto.getNotes().trim() : null
        );
        expense.setUser(user);
        return expenseRepository.save(expense);
    }

    public Expense updateExpense(Long id, ExpenseRequestDto dto) {
        return updateExpense(userService.getCurrentAuthenticatedUser(), id, dto);
    }

    public Expense updateExpense(User user, Long id, ExpenseRequestDto dto) {
        Expense expense = getExpenseById(user, id);
        expense.setTitle(dto.getTitle().trim());
        expense.setAmount(dto.getAmount());
        if (dto.getCurrency() != null && !dto.getCurrency().trim().isEmpty()) {
            expense.setCurrency(dto.getCurrency().trim().toUpperCase());
        }
        expense.setCategory(dto.getCategory());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setPaymentMethod(dto.getPaymentMethod().trim());
        expense.setNotes(dto.getNotes() != null ? dto.getNotes().trim() : null);
        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        deleteExpense(userService.getCurrentAuthenticatedUser(), id);
    }

    public void deleteExpense(User user, Long id) {
        if (!expenseRepository.existsByIdAndUser(id, user)) {
            throw new NoSuchElementException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public SpendingSummaryDto getSummary(LocalDate referenceDate) {
        return getSummary(userService.getCurrentAuthenticatedUser(), referenceDate);
    }

    @Transactional(readOnly = true)
    public SpendingSummaryDto getSummary(User user, LocalDate referenceDate) {
        LocalDate today = (referenceDate != null) ? referenceDate : LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        YearMonth currentYearMonth = YearMonth.from(today);
        LocalDate startOfCurrentMonth = currentYearMonth.atDay(1);
        LocalDate endOfCurrentMonth = currentYearMonth.atEndOfMonth();

        YearMonth prevYearMonth = currentYearMonth.minusMonths(1);
        LocalDate startOfPrevMonth = prevYearMonth.atDay(1);
        LocalDate endOfPrevMonth = prevYearMonth.atEndOfMonth();

        SpendingSummaryDto summary = new SpendingSummaryDto();
        summary.setTodayTotal(expenseRepository.sumAmountByDate(user, today));
        summary.setYesterdayTotal(expenseRepository.sumAmountByDate(user, yesterday));
        summary.setThisMonthTotal(expenseRepository.sumAmountBetweenDates(user, startOfCurrentMonth, endOfCurrentMonth));
        summary.setLastMonthTotal(expenseRepository.sumAmountBetweenDates(user, startOfPrevMonth, endOfPrevMonth));

        BigDecimal allTime = expenseRepository.sumTotalAmount(user);
        summary.setAllTimeTotal(allTime);
        long totalCount = expenseRepository.countByUser(user);
        summary.setTotalCount(totalCount);

        if (totalCount > 0 && allTime.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal avg = allTime.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
            summary.setAverageExpense(avg);
        } else {
            summary.setAverageExpense(BigDecimal.ZERO);
        }

        // Top category this month
        List<Object[]> topCats = expenseRepository.sumGroupedByCategoryBetweenDates(user, startOfCurrentMonth, endOfCurrentMonth);
        if (!topCats.isEmpty()) {
            Category topCat = (Category) topCats.get(0)[0];
            BigDecimal topAmount = (BigDecimal) topCats.get(0)[1];
            summary.setTopCategory(topCat.getDisplayName());
            summary.setTopCategoryAmount(topAmount);
        } else {
            List<Object[]> allTop = expenseRepository.sumGroupedByCategoryAllTime(user);
            if (!allTop.isEmpty()) {
                Category topCat = (Category) allTop.get(0)[0];
                BigDecimal topAmount = (BigDecimal) allTop.get(0)[1];
                summary.setTopCategory(topCat.getDisplayName());
                summary.setTopCategoryAmount(topAmount);
            }
        }

        return summary;
    }

    @Transactional(readOnly = true)
    public List<CategorySummaryDto> getCategoryBreakdown(LocalDate startDate, LocalDate endDate) {
        return getCategoryBreakdown(userService.getCurrentAuthenticatedUser(), startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<CategorySummaryDto> getCategoryBreakdown(User user, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows;
        if (startDate != null && endDate != null) {
            rows = expenseRepository.sumGroupedByCategoryBetweenDates(user, startDate, endDate);
        } else {
            rows = expenseRepository.sumGroupedByCategoryAllTime(user);
        }
        BigDecimal totalSum = BigDecimal.ZERO;
        for (Object[] row : rows) {
            totalSum = totalSum.add((BigDecimal) row[1]);
        }

        List<CategorySummaryDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            Category category = (Category) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            long count = ((Number) row[2]).longValue();

            double percentage = 0.0;
            if (totalSum.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.multiply(BigDecimal.valueOf(100))
                        .divide(totalSum, 1, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            result.add(new CategorySummaryDto(
                    category.name(),
                    category.getDisplayName(),
                    category.getColor(),
                    amount,
                    count,
                    percentage
            ));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<DailySpendingDto> getDailySpending(int days) {
        return getDailySpending(days, null);
    }

    @Transactional(readOnly = true)
    public List<DailySpendingDto> getDailySpending(int days, LocalDate clientEndDate) {
        return getDailySpending(userService.getCurrentAuthenticatedUser(), days, clientEndDate);
    }

    @Transactional(readOnly = true)
    public List<DailySpendingDto> getDailySpending(User user, int days) {
        return getDailySpending(user, days, null);
    }

    @Transactional(readOnly = true)
    public List<DailySpendingDto> getDailySpending(User user, int days, LocalDate clientEndDate) {
        if (days < 1) days = 8;
        if (days > 90) days = 90;

        LocalDate endDate = (clientEndDate != null) ? clientEndDate : LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Object[]> rawList = expenseRepository.sumGroupedByDate(user, startDate, endDate);
        Map<LocalDate, Object[]> map = new HashMap<>();
        for (Object[] row : rawList) {
            LocalDate date = (LocalDate) row[0];
            map.put(date, row);
        }

        List<DailySpendingDto> result = new ArrayList<>();
        LocalDate curr = startDate;
        while (!curr.isAfter(endDate)) {
            BigDecimal amt = BigDecimal.ZERO;
            long count = 0;
            if (map.containsKey(curr)) {
                amt = (BigDecimal) map.get(curr)[1];
                count = ((Number) map.get(curr)[2]).longValue();
            }
            result.add(new DailySpendingDto(
                    curr,
                    curr.format(LABEL_FORMATTER),
                    amt,
                    count
            ));
            curr = curr.plusDays(1);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<CategoryOptionDto> getAllCategories() {
        List<CategoryOptionDto> list = new ArrayList<>();
        for (Category cat : Category.values()) {
            list.add(new CategoryOptionDto(cat.name(), cat.getDisplayName(), cat.getColor()));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public void exportToCsv(Writer writer, String search, Category category, LocalDate startDate, LocalDate endDate) {
        exportToCsv(userService.getCurrentAuthenticatedUser(), writer, search, category, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public void exportToCsv(User user, Writer writer, String search, Category category, LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = getExpenses(user, search, category, startDate, endDate, "expenseDate", "desc");
        PrintWriter printWriter = new PrintWriter(writer);
        printWriter.println("ID,Date,Title,Category,Amount,Currency,Payment Method,Notes");

        for (Expense e : expenses) {
            printWriter.printf("%d,%s,\"%s\",\"%s\",%.2f,\"%s\",\"%s\",\"%s\"%n",
                    e.getId(),
                    e.getExpenseDate(),
                    escapeCsv(e.getTitle()),
                    e.getCategory().getDisplayName(),
                    e.getAmount(),
                    e.getCurrency() != null ? e.getCurrency() : "INR",
                    escapeCsv(e.getPaymentMethod()),
                    escapeCsv(e.getNotes() != null ? e.getNotes() : "")
            );
        }
        printWriter.flush();
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        return input.replace("\"", "\"\"");
    }

    public void seedInitialDataIfEmpty() {
        User demoUser = userService.seedDefaultDemoUserIfEmpty();
        if (expenseRepository.countByUser(demoUser) == 0) {
            seedSampleExpensesForUser(demoUser);
        }
    }

    public void resetToSampleData() {
        resetToSampleData(userService.getCurrentAuthenticatedUser());
    }

    public void resetToSampleData(User user) {
        expenseRepository.deleteAllByUser(user);
        seedSampleExpensesForUser(user);
    }

    public void seedSampleExpensesForUser(User user) {
        LocalDate today = LocalDate.now();

        List<Expense> list = List.of(
                createSample(user, "Vegetables & Groceries", new BigDecimal("850.00"), "INR", Category.GROCERIES, today, "UPI / Wallet", "Fresh vegetables and household essentials"),
                createSample(user, "Chai & Samosa Snacks", new BigDecimal("120.00"), "INR", Category.FOOD, today, "Cash", "Evening tea and snacks with friends"),
                createSample(user, "Metro Card Recharge", new BigDecimal("500.00"), "INR", Category.TRANSPORTATION, today.minusDays(1), "UPI / Wallet", "Monthly metro smart card top-up"),
                createSample(user, "Family Dinner at Restaurant", new BigDecimal("1850.00"), "INR", Category.FOOD, today.minusDays(1), "Debit Card", "Weekend family dinner"),
                createSample(user, "Broadband Internet Bill", new BigDecimal("799.00"), "INR", Category.UTILITIES, today.minusDays(3), "Bank Transfer", "High-speed optical fiber monthly recharge"),
                createSample(user, "Pharmacy - Medicines", new BigDecimal("380.00"), "INR", Category.HEALTH, today.minusDays(4), "UPI / Wallet", "Vitamins and first aid essentials"),
                createSample(user, "Movie Tickets (PVR)", new BigDecimal("650.00"), "INR", Category.ENTERTAINMENT, today.minusDays(5), "Credit Card", "Weekend movie tickets with popcorn"),
                createSample(user, "Cotton Casual Shirts", new BigDecimal("1499.00"), "INR", Category.SHOPPING, today.minusDays(6), "Credit Card", "Online shopping summer festival sale"),
                createSample(user, "Petrol / Fuel Fill-up", new BigDecimal("1200.00"), "INR", Category.TRANSPORTATION, today.minusDays(7), "UPI / Wallet", "Bike and car fuel refill"),
                createSample(user, "House Rent Contribution", new BigDecimal("12500.00"), "INR", Category.HOUSING, today.minusDays(10), "Bank Transfer", "Monthly room / apartment rent share"),
                createSample(user, "Java Coding Reference Book", new BigDecimal("650.00"), "INR", Category.EDUCATION, today.minusDays(11), "Debit Card", "Programming and algorithm book"),
                createSample(user, "Salon & Haircut", new BigDecimal("250.00"), "INR", Category.PERSONAL, today.minusDays(12), "Cash", "Haircut and styling"),
                createSample(user, "Dairy Milk & Fresh Fruits", new BigDecimal("320.00"), "INR", Category.GROCERIES, today.minusDays(13), "UPI / Wallet", "Milk, yogurt, and bananas")
        );

        expenseRepository.saveAll(list);
    }

    private Expense createSample(User user, String title, BigDecimal amount, String currency, Category category,
                                 LocalDate expenseDate, String paymentMethod, String notes) {
        Expense expense = new Expense(title, amount, currency, category, expenseDate, paymentMethod, notes);
        expense.setUser(user);
        return expense;
    }
}
