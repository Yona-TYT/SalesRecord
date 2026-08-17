package com.example.salesrecord.activitys;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.salesrecord.GlobalData;
import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.db.Conf;
import com.example.salesrecord.db.dao.DaoCfg;
import com.example.salesrecord.utls.Msg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.preference.EditTextPreference;


public class ConfigFragment extends PreferenceFragmentCompat {

    private Conf mConf;
    private DaoCfg daoCfg;
    public List<String> datList = new ArrayList<>(Arrays.asList("", "", "", "", ""));

    private final Preference.OnPreferenceChangeListener campoChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            String key = preference.getKey();

            if(stringValue.isEmpty()){
                return true;
            }
            switch (key) {
                case "pm_nombre": datList.set(0, stringValue); break;
                case "pm_telefono": datList.set(1, stringValue); break;
                case "pm_cedula": datList.set(2, stringValue); break;
                case "pm_codigo_banco": datList.set(3, stringValue); break;
                case "pm_bank_name": datList.set(4, stringValue); break;
                default: return true;
            }
            return true;
        }
    };


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Vincula tu archivo XML de preferencias
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        daoCfg = StartVar.appDBall.daoCfg();
        mConf = daoCfg.getUsers(StartVar.mConfID);

        cargarDatosPagoMovil();

        // SOLUCIÓN: Enclavar los escuchadores a la interfaz visual
        vincularListenersAInputs();
    }

    // SOLUCIÓN EXTRA: Método encargado de buscar cada input del XML e inyectarle tu listener
    private void vincularListenersAInputs() {
        String[] keys = {"pm_nombre", "pm_telefono", "pm_cedula", "pm_codigo_banco", "pm_bank_name"};
        for (String key : keys) {
            EditTextPreference preference = findPreference(key);
            if (preference != null) {
                preference.setOnPreferenceChangeListener(campoChangeListener);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();


        // 2. Ejecutar Room y la cola de red fuera del hilo principal de UI
        new Thread(() -> {
            try {
                // Origen de los 5 campos (elige UNO: formulario o conf)
                // Ejemplo desde datList (UI):
                if (datList == null || datList.size() < 5) {
                    Log.w("Conf", "datList incompleta");
                    return;
                }

                String name     = safe(datList.get(0));
                String phone    = safe(datList.get(1));
                String cardId   = safe(datList.get(2));
                String bankCode = safe(datList.get(3));
                String bankName = safe(datList.get(4));

                // Opcional: si ya había datos en mConf, rellenar solo los vacíos del form
                if (mConf.datos != null && !mConf.datos.isEmpty()) {
                    String[] old = mConf.datos.split("\\|", -1); // -1 conserva vacíos
                    if (old.length >= 5) {
                        if (name.isEmpty())     name = safe(old[0]);
                        if (phone.isEmpty())    phone = safe(old[1]);
                        if (cardId.isEmpty())   cardId = safe(old[2]);
                        if (bankCode.isEmpty()) bankCode = safe(old[3]);
                        if (bankName.isEmpty()) bankName = safe(old[4]);
                    }
                }

                if (cardId.matches("^V.*")) {
                    cardId = cardId;
                }
                else if (!cardId.isEmpty()){
                    cardId = "V" + cardId;
                }

                String nuevo = name + "|" + phone + "|" + cardId + "|" + bankCode + "|" + bankName;

                // Evitar guardar/enviar completamente vacío
                if (isAllEmpty(name, phone, cardId, bankCode, bankName)) {
                    Log.w("Conf", "datos vacíos: no se guarda ni se encola");
                    return;
                }

                GlobalData.glName = name;
                GlobalData.glTelef = phone;
                GlobalData.glPhone= phone.replaceFirst("^0", "58");

                GlobalData.glCedula = cardId;
                GlobalData.glCodeBank = bankCode;
                GlobalData.glNameBank = bankName;

                // Evitar encolar si no hubo cambio
                if (nuevo.equals(mConf.datos)) {
                    Log.d("Conf", "Sin cambios en datos");
                    return;
                }

                mConf.datos = nuevo;
                daoCfg.insertUser(mConf); // o update según tu DAO
                GlobalData.getInstance(getContext()).getGenericQueue().enqueue(mConf, 3);

            } catch (Exception e) {
                Log.e("Conf", "Error guardando datos", e);
            }
        }).start();


    }

    private void cargarDatosPagoMovil() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        if(mConf != null && mConf.datos != null && !mConf.datos.isEmpty()) {
            String[] datosList = mConf.datos.split("\\|");

            if (datosList.length == 5) {
                String name = datosList[0];
                String phone = datosList[1];
                String cardId = datosList[2];
                String bankCode = datosList[3];
                String bankName = datosList[4]; // CORREGIDO: Tenías el índice 3 repetido aquí

                datList.set(0, name);
                datList.set(1, phone);
                datList.set(2, cardId);
                datList.set(3, bankCode);
                datList.set(4, bankName);

                @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("pm_nombre", name);
                editor.putString("pm_telefono", phone);
                editor.putString("pm_cedula", cardId);
                editor.putString("pm_codigo_banco", bankCode);
                editor.putString("pm_bank_name", bankName);
                editor.apply();

                forzarTextoEnUI("pm_nombre", name);
                forzarTextoEnUI("pm_telefono", phone);
                forzarTextoEnUI("pm_cedula", cardId);
                forzarTextoEnUI("pm_codigo_banco", bankCode);
                forzarTextoEnUI("pm_bank_name", bankName);
            }
        }

        String name = sharedPreferences.getString("pm_nombre", "");
        String phone = sharedPreferences.getString("pm_telefono", "");
        String cardId = sharedPreferences.getString("pm_cedula", "");
        String bankCode = sharedPreferences.getString("pm_codigo_banco", "");
        String bankName = sharedPreferences.getString("pm_bank_name", "");

        if (name.isEmpty() || phone.isEmpty() || cardId.isEmpty() || bankCode.isEmpty() || bankName.isEmpty()) {
            Msg.m("Por favor, configure sus datos de Pago Móvil");
       }
//        else {
//            String mensaje = "Datos cargados: " + name + " - " + phone;
//            Msg.m(mensaje);
//        }
    }

    // Método de asistencia para refrescar los componentes visuales del XML en tiempo de ejecución
    private void forzarTextoEnUI(String key, String value) {
        EditTextPreference preference = findPreference(key);
        if (preference != null) {
            preference.setText(value);
        }
    }

    // helpers
    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean isAllEmpty(String... parts) {
        for (String p : parts) {
            if (p != null && !p.trim().isEmpty()) return false;
        }
        return true;
    }
}
