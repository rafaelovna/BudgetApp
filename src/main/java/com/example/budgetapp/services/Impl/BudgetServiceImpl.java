package com.example.budgetapp.services.Impl;

import com.example.budgetapp.model.Transaction;
import com.example.budgetapp.services.BudgetService;
import com.example.budgetapp.services.FilesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedHashMap;

import java.util.TreeMap;

@Service
public class BudgetServiceImpl implements BudgetService {
    private final FilesService filesService;

    public static final int SALARY = 30_000 - 9_750; //(Текущая З/П минус коммунальные)
    public static final int SAVING = 3_000;
    public static final int DAILY_BUDGET = (SALARY - SAVING) / LocalDate.now().lengthOfMonth(); // бюджет на день
    public static final int balance = 0;


    // Высчитываем среднюю годовую зарплату в месяц:
//    public static final int AVG_SALARY = (10000 + 10000 + 10000 + 10000 + 10000 + 15000 + 15000 + 15000 + 15000 + 15000 + 15000 + 20000) / 12;
    public static final int AVG_SALARY = SALARY;

    // Коэфициент рабочих дней установленный законом:
    public static final double AVG_DAYS = 29.3;
    private static TreeMap<Month, LinkedHashMap<Long, Transaction>> transactions = new TreeMap<>();
    private static long lastId = 0;

    public BudgetServiceImpl(FilesService filesService) {
        this.filesService = filesService;
    }
@PostConstruct
    private void init() {
    readToFile();
    }

    @Override
    public int getDailyBudget() {
        return DAILY_BUDGET;
    }


    // Сколько денег всего осталось в кошельке:
    @Override
    public int getBalance() {
        return SALARY - SAVING - getAllSpend();
    }


    // Метод добавления транзакции:
    @Override
    public long addTransaction(Transaction transaction) {
        LinkedHashMap<Long, Transaction> monthTransactions = transactions.getOrDefault(LocalDate.now().getMonth(), new LinkedHashMap<>());
        monthTransactions.put(lastId, transaction);
        transactions.put(LocalDate.now().getMonth(), monthTransactions);
        saveToFile();
        return lastId++;
    }

    // Метод получения транзакции:
    @Override
    public Transaction getTransaction(long id) {
        for (LinkedHashMap<Long, Transaction> transactionsByMonth : transactions.values()) {
            Transaction transaction = transactionsByMonth.get(id);
            if (transaction != null) {
                return transaction;
            }
        }
        return null;
    }

    @Override
    public Transaction editTransaction(long id, Transaction transaction) {
        for (LinkedHashMap<Long, Transaction> transactionsByMonth : transactions.values()) {
            if (transactionsByMonth.containsKey(id)) {
                transactionsByMonth.put(id, transaction);
                saveToFile();
                return transaction;
            }
        }
        return null;
    }

    @Override
    public boolean deleteTransaction(long id) {
        for (LinkedHashMap<Long, Transaction> transactionsByMonth : transactions.values()) {
            if (transactionsByMonth.containsKey(id)) {
                transactionsByMonth.remove(id);
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteAllTransaction() {
        transactions = new TreeMap<>();
    }

    // Сколько мы можем потратить согласно бюджету на сегодня:
    @Override
    public int getDailyBalance() {
        return DAILY_BUDGET * LocalDate.now().getDayOfMonth() - getAllSpend();
    }

    // Метод подсчета уже потраченных средств:
    @Override
    public int getAllSpend() {
        LinkedHashMap<Long, Transaction> monthTransactions = transactions.getOrDefault(LocalDate.now().getMonth(), new LinkedHashMap<>());
        int sum = 0;
        for (Transaction transaction : monthTransactions.values()) {
            sum += transaction.getSum();
        }
        return sum;
    }

    //Отпускные. Метод, который высчитывает зарплату в отпуске в днях:
    @Override
    public int getVacationBonus(int daysCount) {
        double avgDaySalary = AVG_SALARY / AVG_DAYS;
        return (int) (daysCount * avgDaySalary);
    }

    // Высчитываем зарплату вместе с отпуском в месяц.
    // Нам нужно знать:
    // сколько дней в отпуске мы будем,
    // отпускные рабочие дни(т.е. из отпуска отнимаем выходные дни),
    // сколько общих рабочих дней в месяце:
    @Override
    public int getSalaryWithVacation(int vacationDaysCount, int vacationWorkingDaysCount, int workingDaysInMonth) {
        int salary = SALARY / workingDaysInMonth * (workingDaysInMonth - vacationWorkingDaysCount);
        return salary + getVacationBonus(vacationDaysCount);
    }

    private void saveToFile() {
        try {
            String json = new ObjectMapper().writeValueAsString(transactions);
            filesService.saveToFile(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void readToFile() {
        try {
            String json = filesService.readToFile();
           transactions = new ObjectMapper().readValue(json, new TypeReference<TreeMap<Month, LinkedHashMap<Long, Transaction>>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
