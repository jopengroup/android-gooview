package com.example.gooview.util;

import android.graphics.Rect;

public class EvaluateUtil {
    /**
     * Integer 估值器
     * 
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public static Integer evaluateInt(float fraction, Integer startValue, Integer endValue) {
        int startInt = startValue;
        return (int) (startInt + fraction * (endValue - startInt));
    }

    /**
     * Float 估值器
     * 
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public static Float evaluateFloat(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    /**
     * Argb 估值器
     * 
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public static Object evaluateArgb(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int) ((startA + (int) (fraction * (endA - startA))) << 24)
                | (int) ((startR + (int) (fraction * (endR - startR))) << 16)
                | (int) ((startG + (int) (fraction * (endG - startG))) << 8)
                | (int) ((startB + (int) (fraction * (endB - startB))));
    }
    
    /**
     * Rect 估值器
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public static Rect evaluateRect(float fraction, Rect startValue, Rect endValue) {
        return new Rect(startValue.left + (int)((endValue.left - startValue.left) * fraction),
                startValue.top + (int)((endValue.top - startValue.top) * fraction),
                startValue.right + (int)((endValue.right - startValue.right) * fraction),
                startValue.bottom + (int)((endValue.bottom - startValue.bottom) * fraction));
    }
}