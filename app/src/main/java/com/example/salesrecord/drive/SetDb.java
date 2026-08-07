package com.example.salesrecord.drive;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.work.Data;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.CalendUtls;
import com.example.salesrecord.DBListCreator;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.Conf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class SetDb {
    private static final String TAG = "SetDb";

    public void set(Context context, Data outputData, Uri uri, DriveManager manager) throws IOException {

        boolean preloader = outputData.getBoolean("preloader", false);
        boolean newObj    = outputData.getBoolean("newobj", false);
        boolean isCheck   = outputData.getBoolean("check", false);
        boolean isId      = outputData.getBoolean("isId", false);

        DBListCreator mListCreator = new DBListCreator(context);

        // Respaldo completo → reemplaza todo
        if (isId) {
            mListCreator.cvsToDB(StartVar.mActivity, uri, 1, "Restaurando respaldo...");
            SetWorkResult.resetPreloader(preloader);
            return;
        }

        // ----- Leer timestamps del CSV -----
        String hexID = "";
        long remoteDate = 0L;
        long remoteTime = 0L;
        boolean confLineFound = false;

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\"", "");
                String[] spl = line.split(",");

                if (spl.length >= 7 && "confID0".equals(spl[0])) {
                    hexID = spl[2];
                    try {
                        remoteDate = Long.parseLong(spl[5].trim());
                        remoteTime = Long.parseLong(spl[6].trim());
                        confLineFound = true;
                    } catch (NumberFormatException e) {
                        Basic.msg("Error: El CSV no contiene timestamps válidos");
                        Log.e(TAG, "CSV date/time inválidos: " + spl[5] + " / " + spl[6], e);
                        SetWorkResult.resetPreloader(preloader);
                        return;
                    }
                    break;
                }
            }
        }

        if (!confLineFound) {
            Basic.msg("Error: No se encontró configuración en el CSV");
            Log.e(TAG, "Línea confID0 no encontrada");
            SetWorkResult.resetPreloader(preloader);
            return;
        }

        Conf mConf = StartVar.appDBall.daoCfg().getUsers(StartVar.mConfID);
        List<Article> mAccList = StartVar.appDBall.daoAtr().getUsers();

        if (mConf == null) {
            Basic.msg("Error: No hay configuración local");
            SetWorkResult.resetPreloader(preloader);
            return;
        }

        // ----- IDs no coinciden -----
        if (!mConf.hexid.equals(hexID)) {
            if (mAccList == null || mAccList.isEmpty()) {
                mListCreator.cvsToDB(StartVar.mActivity, uri, 1, "Los datos locales están vacios");
            } else {
                Basic.msg("Error: Los IDs de las DB no coinciden: " + hexID + " , " + mConf.hexid, true);
            }
            SetWorkResult.resetPreloader(preloader);
            return;
        }

        // ----- Base local vacía -----
        if (mAccList == null || mAccList.isEmpty()) {
            mListCreator.cvsToDB(StartVar.mActivity, uri, 1, "Los datos locales están vacios");
            SetWorkResult.resetPreloader(preloader);
            return;
        }

        // ----- Timestamps locales -----
        if (mConf.date == null || mConf.time == null) {
            Basic.msg("Error: Datos de fecha/hora locales incompletos");
            SetWorkResult.resetPreloader(preloader);
            return;
        }

        // Comparación: primero date, si empatan usa time
        int comparison;
        if (mConf.date.equals(remoteDate)) {
            comparison = Long.compare(mConf.time, remoteTime);
        } else {
            comparison = Long.compare(mConf.date, remoteDate);
        }

        Log.d(TAG, "Comparación → local(" + mConf.date + "," + mConf.time
                + ") vs remote(" + remoteDate + "," + remoteTime
                + ") = " + comparison
                + " | newObj=" + newObj + " isCheck=" + isCheck);

        if (comparison > 0) {
            // =====================================================
            // A) LOCAL más nuevo
            // =====================================================
            if (newObj) {
                // El usuario acaba de guardar algo → subir DB completa
                manager.uploadDataBase();
            }
            if (isCheck) {
                // Hay elementos en cola → procesarlos (send = 1)
                StartVar.genericQueue.startUsuarioQueue(1);
            }

        } else if (comparison < 0) {
            // =====================================================
            // B) REMOTO más nuevo
            // =====================================================
            if (newObj) {
                Basic.msg("Error: Existen cambios más recientes en la red. Sincronizando...");
            }

            if (isCheck) {
                // Descargar sin cerrar y luego procesar cola con send = 2
                mListCreator.cvsToDbNotFinish(StartVar.mActivity, uri, 1, "");
                StartVar.genericQueue.startUsuarioQueue(2);
            } else {
                // Descarga completa (preloader, check manual, etc.)
                mListCreator.cvsToDB(StartVar.mActivity, uri, 1, "");
            }

        } else {
            // =====================================================
            // C) IGUALES
            // =====================================================
            if (newObj) {
                // Mismo versionado → actualizar timestamps y subir
                long now = System.currentTimeMillis();
                String strDbg = TAG + ": " + CalendUtls.getShortDate(now) + " " + CalendUtls.getTime(now);

                StartVar.appDBall.daoCfg().updateDateTime(StartVar.mConfID, now, now, strDbg);
                StartVar.getConfigDB();
                manager.uploadDataBase();
            } else if (!isCheck) {
                Basic.msg("La base de datos está actualizada");
            }

            if (isCheck) {
                StartVar.genericQueue.startUsuarioQueue(1);
            }
        }

        // Siempre se llama al final (ya no hay return anticipado sin esto)
        SetWorkResult.resetPreloader(preloader);
    }
}