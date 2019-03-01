package com.mozared.batterystatus;

/**
 * Created by kullanc on 5.10.2018.
 * For BatteryStatus
 */


import android.content.Context;
import android.util.AttributeSet;

import java.math.BigDecimal;

public class VerticalProgressBar extends android.support.v7.widget.AppCompatImageView {

    /**
     * @see <a href="http://developer.android.com/reference/android/graphics/drawable/ClipDrawable.html">ClipDrawable</a>
     */
    private static final BigDecimal MAX = BigDecimal.valueOf(10000);

    public VerticalProgressBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setImageResource(R.drawable.progress_vbar);
    }

    public void setCurrentValue(Percent percent){
        int cliDrawableImageLevel = percent.asBigDecimal().multiply(MAX).intValue();
        setImageLevel(cliDrawableImageLevel);
    }
}