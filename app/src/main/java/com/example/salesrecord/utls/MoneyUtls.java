package com.example.salesrecord.utls;

import android.annotation.SuppressLint;

import com.example.salesrecord.StartVar;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class MoneyUtls {

    private MoneyUtls() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }


    @SuppressLint("DefaultLocale")
    public static String getConverteValue(String value) {
        value = value.replaceAll("([^\\d.,])","");
        value = value.replaceAll(",",".");

        if (value.isEmpty()){
            value = "0";
        }
        Double number = Double.parseDouble(value);
        return String.valueOf(getConverteValue(number));
    }

    public static Double getConverteValue(Double value) {
        Double precDoll = StartVar.mDollar;
        if (StartVar.mCurrency == 1) {    //Selector en Bs
            value = value * precDoll;
        }
        return value;
    }

    public static String getMaskConv(Double value, Double tasa, int type, boolean symb) {
        return getMask(getConv(value, tasa, type), type,symb);
    }
    public static String getMaskConv(Double value, int type, boolean symb) {
        return getMask(getConv(value, null, type), type, symb);
    }

    public static String getMask(Double nr, int type, boolean symb) {
        String format = setFormatterEs(nr);
        if(symb) {
            return getMaskInternal(format, type);
        }
        else {
            return format;
        }
    }
    public static String getMask(String str, int type, boolean symb) {
        String format = setFormatterEs(str);
        if(symb) {
            return getMaskInternal(format, type);
        }
        else {
            return format;
        }
    }
    public static String getMaskInternal(String str, int type) {
        if(type == 0){
            str = str + " $";
        }
        if(type == 1){
            str = str + " Bs";
        }
        return str;
    }

    @SuppressLint("DefaultLocale")
    public static String getConv(String value) {
        value = value.replaceAll("([^\\d.,])","");
        value = value.replaceAll(",",".");

        if (value.isEmpty()){
            value = "0";
        }
        Double number = Double.parseDouble(value);
        return String.valueOf(getConv(number));
    }

    public static Double getConv(Double value) {

        Double precDoll = StartVar.mDollar;
        if (StartVar.mCurrency == 1) {    //Selector en Bs
            value = value * precDoll;
        }
        return value;
    }

    public static Double getConv(Double value, Double tasa, int type) {

        Double precDoll = (tasa == null ? StartVar.mDollar : tasa);
        if(type == 1) {   //Selector en Bs
            return value * precDoll;
        }
        return value / precDoll;
    }

    public static Double getInDollar(Double value, Double tasa, int type) {

        Double precDoll = (tasa == null ? StartVar.mDollar : tasa);
        if(type == 1) {   //Selector en Bs
            return value / precDoll;
        }
        return value;
    }

    public static Float floatFormat(String value) {
        String mValue = value.replaceAll("([^.\\d])", "");
        mValue = mValue.replaceAll("^.$", "0.00");

        return mValue.isEmpty() ? (float)0 : Float.parseFloat(mValue);
    }

    public static String setFormatterEs(String value){
        value = value.replaceAll("([^\\d.,-])","");
        if (value.isEmpty()){
            value = "0";
        }
        return setFormatterInternal(Double.parseDouble(value), new Locale("es", "VE"));
    }

    public static String setFormatterEn(Double value) {
        return setFormatterInternal(value, Locale.US);           // 1,234.56
    }

    public static String setFormatterEs(Double value) {
        return setFormatterInternal(value, new Locale("es", "VE"));
    }

    public static String setFormatterInternal(Double value, Locale locale){
        if (value == null) return "";
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        df.applyPattern("#,##0.00");
        return df.format(value);
    }

    public static double getQuantity(double price, double currPrice ){
        return price == 0? 0 : currPrice/price;
    }
}
