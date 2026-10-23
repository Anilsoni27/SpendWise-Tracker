package com.spendwise.repository;

import com.spendwise.model.Expense;
import com.spendwise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.expenseDate = :date")
    BigDecimal sumAmountByDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumAmountBetweenDates(@Param("user") User user, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user")
    BigDecimal sumTotalAmount(@Param("user") User user);

    long countByUser(User user);

    @Query("SELECT e.category, SUM(e.amount), COUNT(e) FROM Expense e " +
           "WHERE e.user = :user AND " +
           "      e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> sumGroupedByCategoryBetweenDates(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT e.category, SUM(e.amount), COUNT(e) FROM Expense e " +
           "WHERE e.user = :user " +
           "GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> sumGroupedByCategoryAllTime(
            @Param("user") User user
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
