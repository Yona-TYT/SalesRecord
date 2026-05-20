package com.example.salesrecord.utls;

import static android.widget.GridLayout.spec;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.IllformedLocaleException;
import java.util.Locale;

public class Basic {
    private static Context mContex;
    public static boolean isDow = true;
    public static boolean isUp = false;

    private static final String ACTION_APP_EVENT = "com.example.cow_data.EVENT";
    private static final String EXTRA_EVENT_TYPE = "cow_data_event";
    private static final String EXTRA_FILE_PATHS = "file_paths";
    private static final String EXTRA_SENDER_TYPE = "sender_type";
    private static final String EVENT_FILE_UPLOADED = "file_uploaded";

    private static String oldMsg = "";
    private static long lastShowTime = 0;

    public Basic(Context mContex) {
        this.mContex = mContex;
    }

    public int getPixelSiz(int id) {
        return mContex.getResources().getDimensionPixelSize(id);
    }

    public float getFloatSiz(int id) {
        DisplayMetrics metrics = new DisplayMetrics();
        float scaledDensity = mContex.getResources().getDisplayMetrics().scaledDensity;
        return getPixelSiz(id) / scaledDensity;
    }

    public static Float parseFloat(String value){
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("0.##");
        format.setDecimalFormatSymbols(symbols);

        try {
            return format.parse(value).floatValue();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return (float)0.00;
    }

    public static String setFormatter(String value){
        value = value.replaceAll("([^\\d.,-])","");
        if (value.isEmpty()){
            value = "0";
        }
        return setFormatter(Double.parseDouble(value));
    }
    public static String setFormatter(Double value){
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("ES"));
        DecimalFormat formatter = (DecimalFormat) nf;
        formatter.applyPattern("###,##0.00");
        return formatter.format(value);
    }

    @SuppressLint("DefaultLocale")
    public static String setValue(String value) {
        value = value.replaceAll("([^\\d.,])","");
        if (value.isEmpty()){
            value = "0";
        }
        Double precDoll = StartVar.mDollar;
        Double number = Double.parseDouble(value);
        if (StartVar.mCurrency == 1) {    //Selector en Bs
            number = number / precDoll;
        }
        return String.valueOf(number);
    }

    public static Double setValue(double value) {
        Double precDoll = StartVar.mDollar;
        if (StartVar.mCurrency == 1) {    //Selector en Bs
            value = value / precDoll;
        }
        return value;
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

    public static Double getConv(Double value, int symb) {

        Double precDoll = StartVar.mDollar;
        if(symb == 1) {   //Selector en Bs
            value = value * precDoll;
        }
        return value;
    }

    public static String getMaskConv(Double value, int symb) {

        return getMask(getConv(value, symb), symb);
    }

    public static String getMask(String value, int symb) {
        value = setFormatter(value);

        if(symb == 0){   //Selector en $
            value = value + " $";
        }
        if(symb == 1){   //Selector en Bs
            value = value + " Bs";

        }

        return value;
    }

    public static String getMask(Double nr, int sing) {
        String value = setFormatter(nr);

        if(sing == 0){
            value = value + " $";
        }
        if(sing == 1){
            value = value + " Bs";

        }

        return value;
    }

    @SuppressLint("DefaultLocale")
    public static String getValueFormatter(String value) {
        return setFormatter(getConv(value));
    }
    public static String getValueFormatter(Double value) {
        return setFormatter(getConv(value).toString());
    }

    public static Float floatFormat(String value) {
        String mValue = value.replaceAll("([^.\\d])", "");
        mValue = mValue.replaceAll("^.$", "0.00");

        return mValue.isEmpty() ? (float)0 : Float.parseFloat(mValue);
    }

    public static Double getDebt(int mult, Double mont, Double debt) {
//        mont = mont.replaceAll("([^.0-9]+)", "");
//        debt = debt.replaceAll("([^.0-9]+)", "");

        double numA = mont;
        double numB = debt;

        double result = numA*mult;

        result -= numB;

        return result;

    }

    public static String nameProcessor(String value){
        String text = value.replaceAll("([^\\s0-9a-zA-Z]+)", "");
        text = text.replaceAll("(\\s{2,})", " ");
        text = text.replaceAll("(^\\s)|(\\s$)", "");
        return text;
    }

    public static String inputProcessor(String value){
        return value.replaceAll("([;,\"<>]+)", "");
    }

    public static void msg(String msg){
        msgInternal(msg, false);
    }

    public static void msg(String msg, boolean isClipboard){
        msgInternal(msg, isClipboard);
    }

    public static void msgInternal(String msg, boolean isClipboard)
    {
        new Handler(Looper.getMainLooper()).post(() -> {
            long currentTime = System.currentTimeMillis();
            long fiveSecondsAgo = currentTime - 3000;  // 5s en ms

            if (oldMsg.equals(msg) && lastShowTime > fiveSecondsAgo) {
                return;  // No mostrar
            }
            // Actualiza el mensaje anterior y el tiempo de muestra
            oldMsg = msg;
            lastShowTime = currentTime;

            TextView text = new TextView(mContex);
            // Se ajustan los parametros del Texto ----------------------------------
            text.setText(msg);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setGravity(Gravity.CENTER);
            text.setWidth(R.dimen.spinner_w1);
            text.setMaxLines(1);
            text.setTextColor(ContextCompat.getColor(text.getContext(), R.color.text_color1));
            text.setBackgroundColor(ContextCompat.getColor(text.getContext(), R.color.text_background2));
            text.setPadding(10, 5, 10, 5);

            CardView cardView = new CardView(mContex);
            cardView.setLayoutParams(new GridLayout.LayoutParams(spec(140), spec(150)));
            cardView.addView(text);
            cardView.setRadius(10f);

            Toast mToast = new Toast(mContex);
            mToast.setView(cardView);
            mToast.setDuration(Toast.LENGTH_LONG);
            mToast.show();

            //Copiar al portapapeles
            if(isClipboard){
                ClipboardManager clipboard = (ClipboardManager) mContex.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Texto Extraído", msg);
                clipboard.setPrimaryClip(clip);
            }
        });
    }


    public static String parseMoneyValue(String value, String groupingSeparator, String currencySymbol) {
        return value.replace(groupingSeparator, "").replace(currencySymbol, "");
    }

    public static Number parseMoneyValueWithLocale(Locale locale, String value, String groupingSeparator, String currencySymbol) {
        String valueWithoutSeparator = parseMoneyValue(value, groupingSeparator, currencySymbol);
        try {
            return NumberFormat.getInstance(locale).parse(valueWithoutSeparator);
        } catch (ParseException exception) {
            return 0;
        }
    }

    public static Locale getLocaleFromTag(String localeTag) {
        try {
            return new Locale.Builder().setLanguageTag(localeTag).build();
        } catch (IllformedLocaleException e) {
            return Locale.getDefault();
        }
    }

    public static boolean isLollipopAndAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static void sendFileUploadedBroadcast(Context context, String[] filePaths, String senderType) {
        //LOG.debug("Sending file uploaded broadcast para: " + senderType);
        Intent intent = new Intent(ACTION_APP_EVENT);
        intent.putExtra(EXTRA_EVENT_TYPE, EVENT_FILE_UPLOADED);
        intent.putExtra(EXTRA_FILE_PATHS, filePaths);
        intent.putExtra(EXTRA_SENDER_TYPE, senderType);
        context.sendBroadcast(intent);
    }

    // También para errores
    public static void sendUploadErrorBroadcast(Context context, String errorMessage, String senderType) {
        //LOG.debug("Sending upload error broadcast: " + errorMessage);
        Intent intent = new Intent(ACTION_APP_EVENT);
        intent.putExtra(EXTRA_EVENT_TYPE, "upload_error");
        intent.putExtra("error_message", errorMessage);
        intent.putExtra(EXTRA_SENDER_TYPE, senderType);
        context.sendBroadcast(intent);
    }
}
