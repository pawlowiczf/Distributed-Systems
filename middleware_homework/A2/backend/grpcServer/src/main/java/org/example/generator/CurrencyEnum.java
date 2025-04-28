package org.example.generator;

public enum CurrencyEnum {
    DOL(3.75F),
    EUR(4.29F);

    private float defaultCurrencyValue;

    CurrencyEnum(float defaultCurrencyValue) {
        this.defaultCurrencyValue = defaultCurrencyValue;
    }

    public float getDefaultCurrencyValue() {
        return defaultCurrencyValue;
    }
}
