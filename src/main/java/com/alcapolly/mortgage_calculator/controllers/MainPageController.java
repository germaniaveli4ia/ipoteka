package com.alcapolly.mortgage_calculator.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainPageController {

    @GetMapping("/")
    public String mainPage(Model model) {
        return "mortgageForm";
    }

    @PostMapping("/calculate")
    public String calculate(@RequestParam("apartmentCost") double apartmentCost,
                            @RequestParam("advanceMoney") int advanceMoney,
                            @RequestParam("interestRate") double interestRate,
                            @RequestParam("period") double period,
                            @RequestParam("rentCost") double rentCost,
                            @RequestParam("rentPeriod") double rentPeriod,
                            @RequestParam("creditAmount") double creditAmount,
                            @RequestParam("creditInterestRate") double creditInterestRate,
                            @RequestParam("creditPeriod") double creditPeriod,
                            Model model) {

        double firstPayment = apartmentCost / 100 * advanceMoney;
        double mortgageAmount = apartmentCost - firstPayment;
        double mortgagePeriodInMonths = period * 12;
        double mortgageMonthlyInterest = interestRate / 12 / 100;
        double monthlyPayment = calculateMonthlyPayment(mortgageAmount, mortgagePeriodInMonths, mortgageMonthlyInterest);
        double totalPayment = monthlyPayment * mortgagePeriodInMonths;
        double overPayment = totalPayment - mortgageAmount;
        model.addAttribute("firstPayment", (int)firstPayment);
        model.addAttribute("monthlyPayment", (int)monthlyPayment);
        model.addAttribute("overPayment", (int)overPayment);
        model.addAttribute("totalPayment", (int)totalPayment);

        //Платежи по кредиту
        double creditPeriodInMonths = creditPeriod * 12;
        double creditMonthlyInterest = creditInterestRate / 12 / 100;
        double creditMonthlyPayment = calculateMonthlyPayment(creditAmount, creditPeriodInMonths, creditMonthlyInterest);
        double totalCredit = creditMonthlyPayment * creditPeriod;

        double totalRent = rentCost * rentPeriod;

        double totalPaymentForEverything = totalPayment + totalCredit + totalRent;
        model.addAttribute("totalPaymentForEverything", (int)totalPaymentForEverything);


        // график платежей
        String[][] table = new String[(int) mortgagePeriodInMonths + 1][8];
        table[0][0] = "Месяц";
        table[0][1] = "Проценты";
        table[0][2] = "Основной долг";
        table[0][3] = "Всего по ипотеке";
        table[0][4] = "Кредит";
        table[0][5] = "Аренда квартиры";
        table[0][6] = "Итого в месяц";
        table[0][7] = "Остаток по ипотеке";
        double remainingDebt = mortgageAmount;

        for (int i = 1; i <= mortgagePeriodInMonths ; i++) {
            double interests = remainingDebt * mortgageMonthlyInterest;
            double mainDebt = monthlyPayment - interests;
            remainingDebt = remainingDebt - mainDebt;
            table[i][0] = String.valueOf(i);
            table[i][1] = String.valueOf((int)interests);
            table[i][2] = String.valueOf((int)mainDebt);
            table[i][3] = String.valueOf((int)(interests + mainDebt));
            if (i <= creditPeriodInMonths) {
                table[i][4] = String.valueOf((int)creditMonthlyPayment);
            } else {
                table[i][4] = String.valueOf(0);
            }
            if (i <= rentPeriod) {
                table[i][5] = String.valueOf((int)rentCost);
            } else {
                table[i][5] = String.valueOf(0);
            }
            table[i][6] = String.valueOf((int)(interests + mainDebt + Integer.parseInt(table[i][4]) + Integer.parseInt(table[i][5])));
            table[i][7] = String.valueOf((int) remainingDebt);

        }

        model.addAttribute("paymentsTable", table);


        return "mainPage";
    }

    private double calculateMonthlyPayment(double creditAmount, double periodInMonths, double monthlyInterest) {

        double commonRate = Math.pow(1 + monthlyInterest, periodInMonths);
        double monthlyPayment = creditAmount * monthlyInterest * commonRate / (commonRate - 1);
        return monthlyPayment;
    }
}
