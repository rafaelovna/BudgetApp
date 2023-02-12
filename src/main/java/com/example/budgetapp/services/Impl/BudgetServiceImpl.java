package com.example.budgetapp.services.Impl;

import com.example.budgetapp.model.Transaction;
import com.example.budgetapp.services.BudgetService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class BudgetServiceImpl implements BudgetService {

    public static final int SALARY = 30_000 - 9_750; //(Текущая З/П минус коммунальные)
    public static final int SAVING = 3_000;
    public static final int DAILY_BUDGET = (SALARY - SAVING) / LocalDate.now().lengthOfMonth(); // бюджет на день
    public static final int balance = 0;


    // Высчитываем среднюю годовую зарплату в месяц:
//    public static final int AVG_SALARY = (10000 + 10000 + 10000 + 10000 + 10000 + 15000 + 15000 + 15000 + 15000 + 15000 + 15000 + 20000) / 12;
    public static final int AVG_SALARY = SALARY;

    // Коэфициент рабочих дней установленный законом:
    public static final double AVG_DAYS = 29.3;
    private static Map<Month, Map<Long, Transaction>> transactions = new TreeMap<>();
    private static long lastId = 0;

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
        Map<Long, Transaction> monthTransactions = transactions.getOrDefault(LocalDate.now().getMonth(), new LinkedHashMap<>());
        monthTransactions.put(lastId, transaction);
        return lastId++;
    }

    // Метод получения транзакции:
    @Override
    public Transaction getTransaction(long id) {
        for (Map<Long, Transaction> transactionsByMonth : transactions.values()) {
            Transaction transaction = transactionsByMonth.get(id);
            if (transaction != null) {
                return transaction;
            }
        }
        return null;
    }

    @Override
    public Transaction editTransaction(long id, Transaction transaction) {
        for (Map<Long, Transaction> transactionsByMonth : transactions.values()) {
            if (transactionsByMonth.containsKey(id)) {
                transactionsByMonth.put(id, transaction);
                return transaction;
            }
        }
        return null;
    }

    @Override
    public boolean deleteTransaction(long id) {
        for (Map<Long, Transaction> transactionsByMonth : transactions.values()) {
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
        Map<Long, Transaction> monthTransactions = transactions.getOrDefault(LocalDate.now().getMonth(), new LinkedHashMap<>());
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
}
