package com.example.salesrecord;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.graphics.Rect;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import android.view.ActionMode;

import com.example.salesrecord.utls.Basic;

public class CurrencyEditText extends AppCompatEditText {
    private String currencySymbolStr = null;
    private CurrencyInputWatcher textWatcher;
    private Locale locale = Locale.forLanguageTag("ES");//locale; //Esto es un experimentoooooo!!!!!!!1//Locale.getDefault();
    private int maxDP;
    private boolean isTouch = false;
    private Context mContex;
    private GestureDetector gestureDetector;
    private List<Integer> viewIdsToHide = new ArrayList<>();  // NUEVO: IDs de views a ocultar
    private boolean keepFocusOnKeyboardClose;
    private boolean currencySymbolSuffix;  // NUEVO: Símbolo como sufijo

    @SuppressLint({"PrivateResource", "DiscouragedApi"})
    public CurrencyEditText(Context mContext, AttributeSet attrs) {
        super(mContext, attrs);

        this.mContex = mContext;

        //setBackgroundResource(R.drawable.edittext_outline);

        boolean useCurrencySymbolAsHint = false;
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setKeyListener(DigitsKeyListener.getInstance("0123456789.,"));

        String localeTag = null;
        String prefix;

        int[] styleable = R.styleable.CurrencyEditText;
        TypedArray a = mContext.getTheme().obtainStyledAttributes(attrs, styleable, 0, 0);
        try {
            prefix = a.getString(R.styleable.CurrencyEditText_currencySymbol);
            if (prefix == null) prefix = "";
            localeTag = a.getString(R.styleable.CurrencyEditText_localeTag);
            useCurrencySymbolAsHint = a.getBoolean(R.styleable.CurrencyEditText_useCurrencySymbolAsHint, false);
            maxDP = a.getInt(R.styleable.CurrencyEditText_maxNumberOfDecimalDigits, 2);

            String viewsToHideStr = a.getString(R.styleable.CurrencyEditText_viewsToHideIds);
            List<Integer> viewIdsToHide = new ArrayList<>();
            if (viewsToHideStr != null && !viewsToHideStr.trim().isEmpty()) {
                String[] idNames = viewsToHideStr.split(",");
                for (String idName : idNames) {
                    idName = idName.trim();
                    if (!idName.isEmpty()) {
                        int id = getResources().getIdentifier(idName, "id", getContext().getPackageName());
                        if (id != 0) {  // ID válido
                            viewIdsToHide.add(id);
                        } else {
                            Log.w("CurrencyEditText", "ID no encontrado: " + idName);  // Warning si inválido
                        }
                    }
                }
            }
            this.viewIdsToHide = viewIdsToHide;
            this.keepFocusOnKeyboardClose = a.getBoolean(R.styleable.CurrencyEditText_keepFocusOnKeyboardClose, false);
            // NUEVO: Parsea si símbolo es sufijo (default false para prefix)
            this.currencySymbolSuffix = a.getBoolean(R.styleable.CurrencyEditText_currencySymbolSuffix, false);

        } finally {
            a.recycle();
        }

        if(currencySymbolSuffix){
            currencySymbolStr = prefix.isEmpty() ? "" : " "+prefix;
        }
        else {
            currencySymbolStr = prefix.isEmpty() ? "" : prefix + " ";
        }

        if (useCurrencySymbolAsHint) setHint(currencySymbolStr);
        if (Basic.isLollipopAndAbove() && localeTag != null && !localeTag.isEmpty()) locale = getLocaleFromTag(localeTag);
        textWatcher = new CurrencyInputWatcher(this, currencySymbolStr, locale, maxDP, currencySymbolSuffix);
        addTextChangedListener(textWatcher);

        // Inicializa detector para double-tap y long-press
        gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                selectNumericPart();  // Método helper común (ver abajo)
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                // Aplica selección numérica con delay para corregir handles del sistema
                post(() -> selectNumericPart());
            }
        });
        setCustomSelectionActionModeCallback(new CustomSelectionCallback());
    }

    private void selectNumericPart() {
        String text = getText().toString();
        int start, end;

        if (currencySymbolSuffix) {
            if (text.endsWith(currencySymbolStr) && !text.equals(currencySymbolStr)) {
                start = 0;
                end = text.length() - currencySymbolStr.length();
            } else {
                start = Math.max(0, text.length() - currencySymbolStr.length());
                end = start;  // Cursor antes del sufijo si vacío
            }
        } else {
            if (text.startsWith(currencySymbolStr) && !text.equals(currencySymbolStr)) {
                start = currencySymbolStr.length();
                end = text.length();
            } else {
                start = currencySymbolStr.length();
                end = start;  // Cursor después del prefijo si vacío
            }
        }

        if (start <= end && end <= text.length()) {
            setSelection(start, end);
        }
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        invalidateTextWatcher();
    }

    public void setLocale(String localeTag) {
        locale = Locale.forLanguageTag(localeTag);
        invalidateTextWatcher();
    }

    public void setCurrencySymbol(String currencySymbol) {
        currencySymbolStr = currencySymbol.isEmpty() ? "" : (this.currencySymbolSuffix ? " " + currencySymbol : currencySymbol + " ");
        invalidateTextWatcher();
    }

    public void setCurrencySymbolAsHint( boolean useCurrencySymbolAsHint ){
        if (useCurrencySymbolAsHint) setHint(currencySymbolStr);
        invalidateTextWatcher();
    }

    public void setCurrencySymbolSuffix(boolean isSuffix){
        this.currencySymbolSuffix = isSuffix;
        invalidateTextWatcher();
    }

    public void setMaxNumberOfDecimalDigits(int maxDP) {
        this.maxDP = maxDP;
        invalidateTextWatcher();
    }

    private void invalidateTextWatcher() {
        removeTextChangedListener(textWatcher);
        textWatcher = new CurrencyInputWatcher(this, currencySymbolStr, locale, maxDP, currencySymbolSuffix);
        addTextChangedListener(textWatcher);
    }

    public double getNumericValue() {
        return parseMoneyValueWithLocale(locale, getText().toString(),
                textWatcher.decimalFormatSymbols.getGroupingSeparator() + "",
                currencySymbolStr, currencySymbolSuffix).doubleValue();
    }

    public BigDecimal getNumericValueBigDecimal() {
        return parseMoneyValueWithLocale(locale, getText().toString(),
                textWatcher.decimalFormatSymbols.getGroupingSeparator() + "",
                currencySymbolStr, currencySymbolSuffix);
    }

    public String getCurrencySymbol() {
        return currencySymbolStr;
    }

    private class CustomSelectionCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.clear(); // Limpia cualquier cosa previa

            mode.setTitle("Opciones de Monto");

            // Ítems nativos con iconos en fila
            menu.add(Menu.NONE, android.R.id.cut, 0, "Cortar")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            menu.add(Menu.NONE, android.R.id.copy, 1, "Copiar")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            menu.add(Menu.NONE, android.R.id.paste, 2, "Pegar")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            menu.add(Menu.NONE, android.R.id.selectAll, 3, "Seleccionar todo")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

            // Custom
            MenuItem clearItem = menu.add(Menu.NONE, 1001, 4, "Limpiar a 0");
            clearItem.setIcon(android.R.drawable.ic_menu_delete);
            clearItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int mCut = android.R.id.cut;
            int mCopy = android.R.id.copy;
            int mPaste = android.R.id.paste;
            int mSelAll = android.R.id.selectAll;
            int mClear = 1001;

            // Remueve ítems no deseados del sistema
            for (int i = menu.size() - 1; i >= 0; i--) {
                MenuItem item = menu.getItem(i);
                if (item.getItemId() != mCut && item.getItemId() != mCopy &&
                        item.getItemId() != mPaste && item.getItemId() != mSelAll &&
                        item.getItemId() != mClear) {
                    menu.removeItem(item.getItemId());
                    // Log para debug (remueve en producción)
                    Log.d("CurrencyEditText", "Removido ítem no deseado: " + (item.getTitle() != null ? item.getTitle() : "Sin título"));
                }
            }

            boolean hasText = !getText().toString().trim().isEmpty();
            boolean hasSelection = getSelectionStart() != getSelectionEnd();

            // NUEVO: Checks de null para robustez (opcional, pero seguro)
            MenuItem cutItem = menu.findItem(mCut);
            if (cutItem != null) cutItem.setEnabled(hasSelection);

            MenuItem copyItem = menu.findItem(mCopy);
            if (copyItem != null) copyItem.setEnabled(hasSelection);

            MenuItem pasteItem = menu.findItem(mPaste);
            if (pasteItem != null) pasteItem.setEnabled(hasSelection && clipboardHasText());

            MenuItem selAllItem = menu.findItem(mSelAll);
            if (selAllItem != null) selAllItem.setEnabled(!hasSelection);

            MenuItem clearItem = menu.findItem(mClear);
            if (clearItem != null) clearItem.setEnabled(hasText);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();

            if (id == 1001) {
                setText("0");
                mode.finish();
                return true;
            }

            if (id == android.R.id.cut) {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("monto", getSelectedText()));
                deleteSelectedText();
                mode.finish();
                return true;
            }

            if (id == android.R.id.copy) {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("monto", getSelectedText()));
                mode.finish();
                return true;
            }

            if (id == android.R.id.selectAll) {
                selectAllText();
                return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Nada especial, el sistema lo maneja
        }

        // Helpers para cortar/copiar/seleccionar
        private CharSequence getSelectedText() {
            return getText().subSequence(getSelectionStart(), getSelectionEnd());
        }

        private void deleteSelectedText() {
            int start = getSelectionStart();
            int end = getSelectionEnd();
            getText().delete(start, end);
        }

        private void selectAllText() {
            setSelection(0, getText().length());
        }
    }

    private boolean clipboardHasText() {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        return clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        isTouch = false;
        super.setText(text, type);
        if (getText() != null) setSelection(getText().length());
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        String currentText = getText().toString();
        if (focused) {
            if (currentText.isEmpty()) {
                setText(currencySymbolStr);
            }
            // Auto-seleccionar todo el texto numérico
            post(() -> {
                String text = getText().toString();
                if (currencySymbolSuffix) {
                    if (text.endsWith(currencySymbolStr) && !text.equals(currencySymbolStr)) {
                        int start = 0;
                        int end = text.length() - currencySymbolStr.length();
                        setSelection(start, end);
                    } else {
                        //Basic.msg(""+currencySymbolStr.length());
                        // FIXED: Clampa end >= 0 para evitar IndexOutOfBounds
                        int end = Math.max(0, (currencySymbolStr != null ? text.length() - currencySymbolStr.length() : 0));
                        setSelection(end);  // Cursor al inicio (antes del sufijo si vacío)
                    }
                } else {
                    if (text.startsWith(currencySymbolStr) && !text.equals(currencySymbolStr)) {
                        int start = currencySymbolStr.length();
                        int end = text.length();
                        setSelection(start, end);
                    } else {
                        int start = currencySymbolStr != null ? currencySymbolStr.length() : 0;
                        setSelection(start);  // Cursor después del prefijo si vacío
                    }
                }
            });

            setupKeyboardListener();
            View parent = getParent() instanceof View ? (View) getParent() : null;
            List<View> views = new ArrayList<>();
            if (parent != null && !viewIdsToHide.isEmpty()) {
                for (int id : viewIdsToHide) {
                    View view = parent.findViewById(id);
                    if (view != null) {
                        views.add(view);
                    }
                }
            }
            addViewsToHide(views);  // Tu método existente

        } else {
            if (currentText.equals(currencySymbolStr)) {
                setText("");
            }
            // Ocultar teclado
            InputMethodManager imm = (InputMethodManager) mContex.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }

            // FIXED: Fuerza pérdida de foco y oculta cursor/selección
            clearFocus();  // Pierde foco explícitamente
            setSelection(getSelectionStart());

            // Tu cleanup de listener y toggleViewsVisibility(false); sin cambios
            if (keyboardListener != null) {
                getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
                keyboardListener = null;
            }
            toggleViewsVisibility(false);
            isKeyboardVisible = false;
            viewsToHide.clear();
        }
    }

    @Override
    public void onSelectionChanged(int selStart, int selEnd) {
        if (currencySymbolStr == null || currencySymbolStr.isEmpty()) {
            super.onSelectionChanged(selStart, selEnd);
            return;
        }

        String text = getText().toString();
        int textLength = text.length();
        int symbolLength = currencySymbolStr.length();

        int newStart;
        int newEnd;

        if (currencySymbolSuffix) {
            int limit = textLength - symbolLength;
            if (limit < 0) {
                super.onSelectionChanged(selStart, selEnd);
                return;
            }
            newStart = Math.max(0, Math.min(selStart, limit));
            newEnd = Math.max(0, Math.min(selEnd, limit));
        } else {
            if (textLength < symbolLength) {
                super.onSelectionChanged(selStart, selEnd);
                return;
            }
            newStart = Math.max(symbolLength, selStart);
            newEnd = Math.max(symbolLength, selEnd);
        }

        if (newStart != selStart || newEnd != selEnd) {
            setSelection(newStart, newEnd);
        } else {
            super.onSelectionChanged(newStart, newEnd);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isTouch = !isTouch;
        }

        // NUEVO: Delega al detector para manejar double-tap
        gestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }


    private List<View> viewsToHide = new ArrayList<>();  // Lista de views a ocultar al abrir teclado
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;  // Listener para detectar teclado
    private boolean isKeyboardVisible = false;  // Estado del teclado

    // Método público para agregar views a ocultar
    public void addViewsToHide(List<View> views) {
        this.viewsToHide.addAll(views);
    }

    // Método privado para ocultar/restaurar views
    private void toggleViewsVisibility(boolean hide) {
        for (View view : viewsToHide) {
            if (view != null) {
                view.setVisibility(hide ? View.GONE : View.VISIBLE);
            }
        }
    }

    // Método para detectar visibilidad del teclado
    private void setupKeyboardListener() {
        if (keyboardListener == null) {
            keyboardListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect r = new Rect();
                    getRootView().getWindowVisibleDisplayFrame(r);
                    int screenHeight = getRootView().getHeight();
                    int keypadHeight = screenHeight - r.bottom;

                    boolean keyboardShown = keypadHeight > screenHeight * 0.15;  // Umbral ~15% para detectar teclado
                    if (keyboardShown != isKeyboardVisible) {
                        isKeyboardVisible = keyboardShown;
                        toggleViewsVisibility(keyboardShown);  // Oculta si visible, muestra si no

                        if (!keyboardShown) {  // FIXED: Al cerrar teclado
                            if (keepFocusOnKeyboardClose) {
                                // Mantén foco si attr=true
                                post(() -> requestFocus());
                            } else {
                                // FIXED: Fuerza pérdida de foco si attr=false (oculta cursor)
                                post(() -> {
                                    clearFocus();
                                    setSelection(getSelectionStart());
                                });
                            }
                        }
                    }
                }
            };
            getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);
        }
    }

    private static Locale getLocaleFromTag(String localeTag) {
        String[] parts = localeTag.split("-");
        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else if (parts.length == 2) {
            return new Locale(parts[0], parts[1]);
        } else if (parts.length == 3) {
            return new Locale(parts[0], parts[1], parts[2]);
        } else {
            throw new IllegalArgumentException("Invalid locale tag: " + localeTag);
        }
    }

    private static BigDecimal parseMoneyValueWithLocale(Locale locale, String value, String groupingSeparator, String currencySymbol, boolean isSuffix) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        symbols.setGroupingSeparator(groupingSeparator.charAt(0));
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(symbols);
        format.setParseBigDecimal(true);

        // Remueve símbolo según posición
        if (isSuffix && value.endsWith(currencySymbol)) {
            value = value.substring(0, value.length() - currencySymbol.length()).trim();
        } else {
            value = value.replace(currencySymbol, "").trim();
        }
        // Remueve grouping separators
        value = value.replace(groupingSeparator, "");

        try {
            return (BigDecimal) format.parse(value);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
