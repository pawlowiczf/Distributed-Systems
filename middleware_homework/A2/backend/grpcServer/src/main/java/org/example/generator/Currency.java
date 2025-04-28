package org.example.generator;

import java.time.LocalDateTime;

import java.time.temporal.ChronoUnit;

public class Currency {

    private CurrencyEnum currencyEnum;

    private Float valueInPLN;

    private LocalDateTime date;

    public Currency(CurrencyEnum currencyEnum, Float valueInPLN) {
        this.currencyEnum = currencyEnum;
        this.valueInPLN = valueInPLN;
        this.date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public CurrencyEnum getCurrencyEnum() {
        return currencyEnum;
    }

    public void setCurrencyEnum(CurrencyEnum currencyEnum) {
        this.currencyEnum = currencyEnum;
    }

    public Float getValueInPLN() {
        return valueInPLN;
    }

    public void setValueInPLN(Float valueInPLN) {
        this.valueInPLN = valueInPLN;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "currencyEnum=" + currencyEnum +
                ", valueInPLN=" + valueInPLN +
                ", date=" + date +
                '}';
    }
}
