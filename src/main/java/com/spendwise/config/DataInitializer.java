package com.spendwise.config;

import com.spendwise.service.ExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final ExpenseService expenseService;

    public DataInitializer(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Override
    public void run(String... args) {
        log.info("Checking SpendWise database initialization...");
        expenseService.seedInitialDataIfEmpty();
        log.info("SpendWise data initialization check complete.");
    }
}
