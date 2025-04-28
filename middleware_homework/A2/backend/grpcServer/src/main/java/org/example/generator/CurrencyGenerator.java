package org.example.generator;

import java.util.Random;


public class CurrencyGenerator {
    private static Random random = new Random();


    public static Currency generateValue(){
        CurrencyEnum currencyEnum = getRandomCurrencyEnum();
        return new Currency(currencyEnum, getRandomizedValue(currencyEnum));
    }


    private static CurrencyEnum getRandomCurrencyEnum(){
        int min = 0;
        int max = CurrencyEnum.values().length - 1;
        int randomNum = (int)(Math.random() * (max - min + 1)) + min;

        return CurrencyEnum.values()[randomNum];
    }

    private static Float getRandomizedValue(CurrencyEnum currencyEnum){
        float defaultValue = currencyEnum.getDefaultCurrencyValue();
        float increaseDecreaseRanges = (float) (defaultValue * 0.1);

        float valueToAdd = -increaseDecreaseRanges
                + random.nextFloat() * (1 * increaseDecreaseRanges);

        return defaultValue + valueToAdd;
    }

}
