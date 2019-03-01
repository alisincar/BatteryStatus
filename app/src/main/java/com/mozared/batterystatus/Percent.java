package com.mozared.batterystatus;

/**
 * Created by kullanc on 5.10.2018.
 * For BatteryStatus
 */
import java.math.BigDecimal;

public class Percent {

    public static final BigDecimal DIVISOR_PERCENT = new BigDecimal(100);

    private final int value;

    public Percent(int value) {
        if(value < 0 || value > 100){
            throw new IllegalArgumentException("Percentage value must be in <0;100> range");
        }
        this.value = value;
    }

    public int asIntValue() {
        return value;
    }

    public BigDecimal asBigDecimal() {
        return new BigDecimal(value).divide(DIVISOR_PERCENT);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Percent{");
        sb.append("value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}