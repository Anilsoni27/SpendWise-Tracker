package com.spendwise.repository;

import com.spendwise.model.Category;
import com.spendwise.model.Expense;
import com.spendwise.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE " +
           "e.user = :user AND " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:startDate IS NULL OR e.expenseDate >= :startDate) AND " +
           "(:endDate IS NULL OR e.expenseDate <= :endDate) AND " +
           "(:search IS NULL OR TRIM(:search) = '' OR " +
           " LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.notes) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.paymentMethod) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Expense> findWithFilters(
            @Param("user") User user,
            @Param("search") String search,
            @Param("category") Category category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Sort sort
    );

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.expenseDate = :date")
    BigDecimal sumAmountByDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumAmountBetweenDates(@Param("user") User user, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user")
    BigDecimal sumTotalAmount(@Param("user") User user);

    long countByUser(User user);

    @Query("SELECT e.category, SUM(e.amount), COUNT(e) FROM Expense e " +
           "WHERE e.user = :user AND " +
           "      (:startDate IS NULL OR e.expenseDate >= :startDate) AND " +
           "      (:endDate IS NULL OR e.expenseDate <= :endDate) " +
           "GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> sumGroupedByCategory(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT e.expenseDate, SUM(e.amount), COUNT(e) FROM Expense e " +
           "WHERE e.user = :user AND " +
           "      e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY e.expenseDate ORDER BY e.expenseDate ASC")
    List<Object[]> sumGroupedByDate(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Optional<Expense> findByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);

    void deleteAllByUser(User user);
}
