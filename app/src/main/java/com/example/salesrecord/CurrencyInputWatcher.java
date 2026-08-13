package com.example.salesrecord;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrencyInputWatcher implements TextWatcher {
    private final EditText editText;
    private final String currencySymbol;
    private final int symbolLength;
    private final boolean isSuffix;
    private final Locale locale;
    private final int maxNumberOfDecimalPlaces;
    private final float maxValue;

    private boolean hasDecimalPoint;
    private int expectedCursorPos;
    private final DecimalFormat wholeNumberDecimalFormat;
    private final DecimalFormat fractionDecimalFormat;
    final DecimalFormatSymbols decimalFormatSymbols;

    private static final String FRACTION_FORMAT_PATTERN_PREFIX = "#,##0.";

    public CurrencyInputWatcher(EditText editText, String currencySymbol, Locale locale, int maxNumberOfDecimalPlaces, boolean isSuffix, float maxValue) {
        this.editText = editText;
        this.currencySymbol = currencySymbol;
        this.isSuffix = isSuffix;
        this.locale = locale;
        this.maxNumberOfDecimalPlaces = maxNumberOfDecimalPlaces;
        this.maxValue = maxValue;

        if (maxNumberOfDecimalPlaces < 1) {
            throw new IllegalArgumentException("Maximum number of Decimal Digits must be a positive integer");
        }

        this.wholeNumberDecimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        this.wholeNumberDecimalFormat.applyPattern("#,##0");

        this.fractionDecimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        this.decimalFormatSymbols = this.wholeNumberDecimalFormat.getDecimalFormatSymbols();

		symbolLength = this.isSuffix? 0 : this.currencySymbol.length();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        this.fractionDecimalFormat.setDecimalSeparatorAlwaysShown(true);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        this.hasDecimalPoint = s.toString().contains(String.valueOf(this.decimalFormatSymbols.getDecimalSeparator()));
        String newInputString = s.toString().replace(this.currencySymbol, "");
        if(newInputString.length() > symbolLength) {
            Pattern patt = Pattern.compile("(\\D$)");
            Matcher m = patt.matcher(newInputString);
            if(m.find()){
                this.hasDecimalPoint = true;
            }
        }
    }

    private static final Pattern NON_DIGIT_END_PATTERN = Pattern.compile("(\\D$)");
    private static final Pattern DIGIT_NON_DIGIT_END_PATTERN = Pattern.compile("(\\d\\D$)");
    @SuppressLint("SetTextI18n")
    @Override
    public void afterTextChanged(Editable s) {

        String newInputString = s.toString();

        if(!newInputString.startsWith(this.currencySymbol)) {
            newInputString = newInputString.replaceAll("([^.,\\d])","");
        }

        if(newInputString.length() > symbolLength) {
            Pattern patt = NON_DIGIT_END_PATTERN;
            Matcher m = patt.matcher(newInputString);
            if (m.find()) {
                newInputString = newInputString.replaceAll("\\D$", String.valueOf(this.decimalFormatSymbols.getDecimalSeparator()));
            }
            else {
                patt = DIGIT_NON_DIGIT_END_PATTERN;
                m = patt.matcher(newInputString);
                if (m.find()) {
                    newInputString = newInputString.replaceAll("\\D$", String.valueOf(this.decimalFormatSymbols.getDecimalSeparator()));
                }
            }
        }

        boolean isParsableString;
        try {
            this.fractionDecimalFormat.parse(newInputString);
            isParsableString = true;
        } catch (ParseException e) {
            isParsableString = false;
        }

        if (newInputString.length() < symbolLength && !isParsableString) {

            String formatted = this.wholeNumberDecimalFormat.format(0);
            if (this.isSuffix) {

                this.editText.setText(formatted + this.currencySymbol);
                this.editText.setSelection(1);
            }
            else {

                this.editText.setText(this.currencySymbol + formatted);
                this.editText.setSelection(this.editText.getText().length());
            }
            return;
        }

        if (newInputString.equals(this.currencySymbol)) {

            String formatted = this.wholeNumberDecimalFormat.format(0);
            if (this.isSuffix) {

                this.editText.setText(formatted + this.currencySymbol);
                this.editText.setSelection(1);
            }
            else {

                this.editText.setText(this.currencySymbol + formatted);
                this.editText.setSelection(this.editText.getText().length());
            }
            return;
        }

        this.editText.removeTextChangedListener(this);
        int startLength = this.editText.getText().length();
        try {

            String numberWithoutGroupingSeparator = parseMoneyValue(newInputString, String.valueOf(this.decimalFormatSymbols.getGroupingSeparator()), this.currencySymbol, this.isSuffix);
            if (numberWithoutGroupingSeparator.equals(String.valueOf(this.decimalFormatSymbols.getDecimalSeparator()))) {
                numberWithoutGroupingSeparator = "0" + numberWithoutGroupingSeparator;
            }

            numberWithoutGroupingSeparator = truncateNumberToMaxDecimalDigits(numberWithoutGroupingSeparator, this.maxNumberOfDecimalPlaces, this.decimalFormatSymbols.getDecimalSeparator());

            Number parsedNumber = this.fractionDecimalFormat.parse(numberWithoutGroupingSeparator);

            // ==========================================
            // INTERCEPTOR DE VALOR MÁXIMO EN TIEMPO REAL
            // ==========================================
            if (parsedNumber.doubleValue() > this.maxValue) {
                // Reducimos el número al límite del XML
                parsedNumber = (double) this.maxValue;

                // Colocamos el globo de alerta visual en la vista
                this.editText.setError("El valor máximo permitido es " + (int) this.maxValue);

                // Sobreescribimos la variable de texto limpio para que el formateador use el tope de 100
                numberWithoutGroupingSeparator = String.valueOf((int) this.maxValue);
            }

            int selectionStartIndex = this.editText.getSelectionStart();
            if (this.hasDecimalPoint) {
                this.fractionDecimalFormat.applyPattern(FRACTION_FORMAT_PATTERN_PREFIX + getFormatSequenceAfterDecimalSeparator(numberWithoutGroupingSeparator));
                String formatted = this.fractionDecimalFormat.format(parsedNumber);
                this.editText.setText(this.isSuffix ? formatted + this.currencySymbol : this.currencySymbol + formatted);
            } else {
                String formatted = this.wholeNumberDecimalFormat.format(parsedNumber);
                this.editText.setText(this.isSuffix ? formatted + this.currencySymbol : this.currencySymbol + formatted);
            }

            int endLength = this.editText.getText().length();
            int selection = selectionStartIndex + (endLength - startLength);

            if (this.isSuffix) {
                if (selection >= 0 && selection <= this.editText.getText().length()) {
                    this.editText.setSelection(selection);
                }
                else {
                    this.editText.setSelection(this.editText.getText().length() - symbolLength);
                }
            }
            else {
                if (selection >= 0 && selection <= this.editText.getText().length()) {
                    this.editText.setSelection(selection);
                }
                else {
                    this.editText.setSelection(symbolLength);
                }
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        this.editText.addTextChangedListener(this);
    }

    private static String parseMoneyValue(String inputString, String groupingSeparator, String currencySymbol, boolean isSuffix) {  // NUEVO: + bool isSuffix
        inputString = inputString.replace(groupingSeparator, "");
        if (isSuffix) {
            // Remueve sufijo al final
            if (inputString.endsWith(currencySymbol)) {
                inputString = inputString.replace(currencySymbol, "");
            }
        }
        else {
            inputString = inputString.replace(currencySymbol, "");
        }
        return inputString;
    }

    private static String truncateNumberToMaxDecimalDigits(String numberString, int maxDecimalDigits, char decimalSeparator) {
        int decimalSeparatorIndex = numberString.indexOf(decimalSeparator);
        if (decimalSeparatorIndex == -1) {
            return numberString;
        }
        int decimalDigits = numberString.length() - decimalSeparatorIndex - 1;
        if (decimalDigits <= maxDecimalDigits) {
            return numberString;
        }
        return numberString.substring(0, decimalSeparatorIndex + maxDecimalDigits + 1);
    }

    private String getFormatSequenceAfterDecimalSeparator(String number) {
        int noOfCharactersAfterDecimalPoint = number.length() - number.indexOf(this.decimalFormatSymbols.getDecimalSeparator()) - 1;
        return "0".repeat(Math.min(noOfCharactersAfterDecimalPoint, this.maxNumberOfDecimalPlaces));
    }
}
