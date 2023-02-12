package com.example.budgetapp.services;

import com.example.budgetapp.model.Transaction;

public interface BudgetService {

    int getDailyBudget();

    int getBalance();

    // Метод добавления транзакции:
    long addTransaction(Transaction transaction);

    Transaction getTransaction(long id);

    Transaction editTransaction(long id, Transaction transaction);

    boolean deleteTransaction(long id);

    void deleteAllTransaction();

    // Сколько мы можем потратить согласно бюджету на сегодня:
    int getDailyBalance();

    // Метод подсчета уже потраченных средств:
    int getAllSpend();

    int getVacationBonus(int daysCount);

    // Высчитываем зарплату вместе с отпуском в месяц.
    // Нам нужно знать:
    // сколько дней в отпуске мы будем,
    // отпускные рабочие дни(т.е. из отпуска отнимаем выходные дни),
    // сколько общих рабочих дней в месяце:
    int getSalaryWithVacation(int vacationDaisCount, int vacationWorkingDaysCount, int workingDaysInMonth);
}
