package com.example.salesrecord.utls;

import android.annotation.SuppressLint;

import com.example.salesrecord.StartVar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class MoneyUtls {

    private MoneyUtls() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /** Decimales al mostrar en UI */
    private static final int SCALE_UI = 2;
    /** Decimales al convertir/guardar USD (reduce error al volver a Bs) */
    private static final int SCALE_STORE = 6;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    // -------------------------------------------------------------------------
    // Helpers internos (no cambian la API pública)
    // -------------------------------------------------------------------------

    private static BigDecimal bd(Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return BigDecimal.ZERO;
        }
        // valueOf evita peor representación que new BigDecimal(double) en muchos casos
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal rateOrDefault(Double tasa) {
        double r = (tasa == null) ? StartVar.mDollar : tasa;
        if (r <= 0 || Double.isNaN(r) || Double.isInfinite(r)) {
            r = StartVar.mDollar;
        }
        if (r <= 0) {
            return BigDecimal.ONE; // evita división por cero; no inventa conversión loca
        }
        return BigDecimal.valueOf(r);
    }

    /**
     * type == 1 → valor en USD → resultado en Bs  (× tasa)
     * type != 1 → valor en Bs  → resultado en USD (÷ tasa)
     * Misma semántica que antes; cálculo con BigDecimal.
     */
    private static BigDecimal convertByType(BigDecimal value, BigDecimal rate, int type) {
        if (value == null) value = BigDecimal.ZERO;
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return value;
        }
        if (type == 1) {
            // USD → Bs
            return value.multiply(rate).setScale(SCALE_UI, RM);
        }
        // Bs → USD (más escala al “guardar”)
        return value.divide(rate, SCALE_STORE, RM);
    }

    // -------------------------------------------------------------------------
    // API pública (firmas iguales)
    // -------------------------------------------------------------------------

    @SuppressLint("DefaultLocale")
    public static String getConverteValue(String value) {
        value = value.replaceAll("([^\\d.,])", "");
        value = value.replaceAll(",", ".");

        if (value.isEmpty()) {
            value = "0";
        }
        Double number = Double.parseDouble(value);
        return String.valueOf(getConverteValue(number));
    }

    public static Double getConverteValue(Double value) {
        if (value == null) return 0.0;
        Double precDoll = StartVar.mDollar;
        if (StartVar.mCurrency == 1) {    // Selector en Bs: el valor base está en USD → mostrar/usar Bs
            if (precDoll == null || precDoll <= 0) return value;
            return bd(value).multiply(bd(precDoll)).setScale(SCALE_UI, RM).doubleValue();
        }
        return value;
    }

    public static String getMaskConv(Double value, Double tasa, int type) {

        return getMask(getConv(value, tasa, type), type, true);
    }

    public static String getMaskConv(Double value, int type) {

        return getMask(getConv(value, null, type), type, true);
    }

    public static String getMaskConv(Double value, Double tasa, int type, boolean symb) {
        return getMask(getConv(value, tasa, type), type, symb);
    }

    public static String getMaskConv(Double value, int type, boolean symb) {
        return getMask(getConv(value, null, type), type, symb);
    }

    public static String getMask(Double nr, int type, boolean symb) {
        String format = setFormatterEs(nr);
        if (symb) {
            return getMaskInternal(format, type);
        } else {
            return format;
        }
    }

    public static String getMask(String str, int type, boolean symb) {
        String format = setFormatterEs(str);
        if (symb) {
            return getMaskInternal(format, type);
        } else {
            return format;
        }
    }

    public static String getMaskInternal(String str, int type) {
        if (type == 0) {
            str = str + " $";
        }
        if (type == 1) {
            str = str + " Bs";
        }
        return str;
    }

    @SuppressLint("DefaultLocale")
    public static String getConv(String value) {
        value = value.replaceAll("([^\\d.,])", "");
        value = value.replaceAll(",", ".");

        if (value.isEmpty()) {
            value = "0";
        }
        Double number = Double.parseDouble(value);
        return String.valueOf(getConv(number));
    }

    public static Double getConv(Double value) {
        if (value == null) return 0.0;
        Double precDoll = StartVar.mDollar;
        if (StartVar.mCurrency == 1) {    // Selector en Bs
            if (precDoll == null || precDoll <= 0) return value;
            return bd(value).multiply(bd(precDoll)).setScale(SCALE_UI, RM).doubleValue();
        }
        return value;
    }

    /**
     * type == 1: value en USD → Bs (× tasa)
     * type != 1: value en Bs  → USD (÷ tasa)
     * Sin cambio de contrato; solo precisión interna.
     */
    public static Double getConv(Double value, Double tasa, int type) {

        tasa = (tasa == null ? StartVar.mDollar : tasa);

        if (value == null) return 0.0;
        BigDecimal result = convertByType(bd(value), rateOrDefault(tasa), type);
        return result.doubleValue();
    }

    /**
     * Convierte a USD para guardar.
     * type == 1: value está en Bs → USD (÷ tasa)
     * type != 1: value ya está en USD
     *
     * Antes redondeaba a 2 decimales al dividir → mucho error al volver a Bs.
     * Ahora usa SCALE_STORE (6). La UI sigue formateando a 2 con setFormatterEs / getMask.
     */
    public static BigDecimal getInDollar(Double value, Double tasa, int type) {
        BigDecimal bigValue = bd(value);

        if (type == 1) {   // Entrada en Bs → USD
            BigDecimal precDoll = rateOrDefault(tasa);
            return bigValue.divide(precDoll, SCALE_STORE, RM);
        }
        // Ya en USD: normaliza escala de almacenamiento
        return bigValue.setScale(SCALE_STORE, RM);
    }

    public static Float floatFormat(String value) {
        String mValue = value.replaceAll("([^.\\d])", "");
        mValue = mValue.replaceAll("^.$", "0.00");

        return mValue.isEmpty() ? (float) 0 : Float.parseFloat(mValue);
    }

    public static String setFormatterEs(String value) {
        value = value.replaceAll("([^\\d.,-])", "");
        if (value.isEmpty()) {
            value = "0";
        }
        return setFormatterInternal(Double.parseDouble(value), new Locale("es", "VE"));
    }

    public static String setFormatterEn(Double value) {
        return setFormatterInternal(value, Locale.US);
    }

    public static String setFormatterEs(Double value) {
        return setFormatterInternal(value, new Locale("es", "VE"));
    }

    public static String setFormatterEs(BigDecimal value) {
        if (value == null) return setFormatterEs(0.0);
        // Evita .doubleValue() prematuro cuando se pueda: redondeo UI a 2
        return setFormatterInternal(value.setScale(SCALE_UI, RM).doubleValue(), new Locale("es", "VE"));
    }

    public static String setFormatterInternal(Double value, Locale locale) {
        if (value == null) return "";
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        df.applyPattern("#,##0.00");
        return df.format(value);
    }

    public static String formatPlainDecimal(Double value) {
        if (value == null) return "";
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("es", "VE"));
        df.applyPattern("0.00");
        return df.format(value);
    }

    public static double getQuantity(double price, double currPrice) {
        return price == 0 ? 0 : currPrice / price;
    }

    public static BigDecimal getQuantity(BigDecimal price, BigDecimal currPrice) {
        if (price == null || currPrice == null || price.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        // Cantidad de producto: 6 decimales suele ser mejor que 2 (metros, etc.)
        return currPrice.divide(price, SCALE_STORE, RM);
    }

    public static String formatDecimal(Double value) {
        if (value == null) return "";
        return getFormatDecimal().format(value);
    }

    public static String formatDecimal(Float value) {
        if (value == null) return "";
        return getFormatDecimal().format(value);
    }

    public static DecimalFormat getFormatDecimal() {
        Locale spanishLocale = Locale.forLanguageTag("es-ES");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(spanishLocale);
        DecimalFormat df = new DecimalFormat("###,###.##", symbols);
        return df;
    }
}