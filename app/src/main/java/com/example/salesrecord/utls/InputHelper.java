package com.example.salesrecord.utls;


import android.text.InputType;
import android.widget.EditText;

public class InputHelper {

    // Constantes para identificar los tipos de entrada de forma clara
    public static final int TYPE_NUMBER = 0;
    public static final int TYPE_PHONE = 1;
    public static final int TYPE_TEXT = 2;
    public static final int TYPE_UNKNOWN = 3;

    /**
     * Valida si un campo EditText cumple con las condiciones básicas de llenado.
     * Usa el Tag del componente como mensaje de error personalizado.
     *
     * @param input El componente EditText a validar.
     * @return true si es válido, false si contiene errores.
     */
    public static boolean validateField(EditText input) {
        if (input == null) return false;

        Object tag = input.getTag();
        if (tag != null) {
            String s = input.getText().toString().trim();
            String errorMsg = tag.toString();

            // 1. Validación de campo vacío
            if (s.isEmpty()) {
                input.setError(errorMsg);
                return false;
            }

            // 2. Validación específica para números (Evita valores en cero o negativos)
            if (getInputType(input) == TYPE_NUMBER) {
                String cleanDigits = s.replaceAll("\\D", ""); // Deja solo los dígitos

                // Protección contra caídas: Si se queda vacío tras limpiar letras o símbolos, es inválido
                if (cleanDigits.isEmpty()) {
                    input.setError(errorMsg);
                    return false;
                }

                if (Double.parseDouble(cleanDigits) <= 0) {
                    input.setError(errorMsg);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Analiza el InputType del componente para clasificarlo.
     *
     * @param input El componente EditText a analizar.
     * @return Código numérico correspondiente al tipo (0, 1, 2 o 3).
     */
    public static int getInputType(EditText input) {
        if (input == null) return TYPE_UNKNOWN;

        int type = input.getInputType();
        int inputClass = type & InputType.TYPE_MASK_CLASS;

        if (inputClass == InputType.TYPE_CLASS_NUMBER) {
            return TYPE_NUMBER;
        }

        if (inputClass == InputType.TYPE_CLASS_PHONE) {
            return TYPE_PHONE;
        }

        if (inputClass == InputType.TYPE_CLASS_TEXT) {
            return TYPE_TEXT;
        }

        return TYPE_UNKNOWN;
    }
}