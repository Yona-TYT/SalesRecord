package com.example.salesrecord.db;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.DBListCreator;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.db.dao.DaoCfg;
import com.example.salesrecord.db.dao.DaoClt;
import com.example.salesrecord.db.dao.DaoDat;
import com.example.salesrecord.db.dao.DaoDeb;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.drive.DriveManager;
import com.example.salesrecord.ex.PreferenceHelper;
import com.example.salesrecord.utls.CalendUtls;
import com.google.gson.Gson;


public class GenericWorker extends Worker {
    private static final String TAG = "GenericWorker";
    private Context context;
    public GenericWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            int mSend = getInputData().getInt("send", 0);
            String json = getInputData().getString("objeto_json");
            String tipo = getInputData().getString("objeto_tipo");

            if (mSend == 1 || mSend == 2 || mSend == 3) {
                if (json == null || tipo == null) {
                    Log.e(TAG, "Datos de entrada nulos en mSend == " + mSend);
                    return Result.failure();
                }

                boolean isOk = applyQueueObject(json, tipo);
                if (!isOk) {
                    return Result.retry();
                }

                // NO subir ni hacer clear aquí.
                // La subida única la hace GenericQueue.performFinalUpload()
                // cuando la cola quede vacía.
                return Result.success();
            }

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error en GenericWorker", e);
            return Result.failure(new Data.Builder()
                    .putString("error", e.getMessage())
                    .build());
        }
    }

    private void updateConfigTimestamp() {
        long currDate = 0L;
        long currTime = System.currentTimeMillis();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currDate = java.time.Instant.now().toEpochMilli();
        } else {
            currDate = currTime; // fallback para versiones antiguas
        }

        String strDbg = "GenericWorker: " +
                CalendUtls.getShortDate(currDate) + " " +
                CalendUtls.getTime(currTime);

        StartVar.appDBall.daoCfg().updateDateTime(
                StartVar.mConfID,
                currDate,
                currTime,
                strDbg
        );

        StartVar.getConfigDB(); // refresca la configuración en memoria
    }

    private boolean applyQueueObject(String json, String tipo) {
        try {
            Class<?> clazz = Class.forName(tipo);
            Object obj = new Gson().fromJson(json, clazz);

            if (obj instanceof Article) {
                return processArt((Article) obj);
            } else if (obj instanceof Cliente) {
                return processCliente((Cliente) obj);
            } else if (obj instanceof Sale) {
                return processSale((Sale) obj);
            } else if (obj instanceof Fecha) {
                return processFecha((Fecha) obj);
            } else if (obj instanceof Deuda) {
                return processDeuda((Deuda) obj);
            } else if (obj instanceof Conf) {
                return processConf((Conf) obj);
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error aplicando objeto de cola", e);
            return false;
        }
    }

    // Métodos específicos corregidos sin alertas UI que rompan el hilo secundario
    private boolean processDeuda(Deuda mUser) {
        DaoDeb mDao = StartVar.appDBall.daoDeb();
        if (mUser == null) return false;

        if ("@null".equals(mUser.deuda)) {
            mDao.removerUser(mUser.deuda);
            mDao.removerUser(mUser.uid);
        } else {
            mDao.update(mUser);
        }
        return true;
    }

    private boolean processCliente(Cliente mUser) {
        DaoClt mDao = StartVar.appDBall.daoClt();
        if (mUser == null) return false;

        if ("@null".equals(mUser.cliente)) {
            mDao.removerUser(mUser.cliente);
            mDao.removerUser(mUser.uid);
        } else {
            mDao.update(mUser);
        }
        return true;
    }

    private boolean processArt(Article mUser) {
        DaoArt mDao = StartVar.appDBall.daoAtr();
        if (mUser == null) return false;

        if ("@null".equals(mUser.article)) {
            mDao.removerUser(mUser.article);
            mDao.removerUser(mUser.uid);
        } else {
            mDao.update(mUser);
        }
        return true;
    }

    private boolean processSale(Sale mUser) {
        DaoSal mDao = StartVar.appDBall.daoSal();
        if (mUser == null) return false;

        if ("@null".equals(mUser.sale)) {
            mDao.removerUser(mUser.sale);
            mDao.removerUser(mUser.uid);
        } else {
            mDao.update(mUser);
        }
        return true;
    }

    private boolean processFecha(Fecha mUser) {
        DaoDat mDao = StartVar.appDBall.daoDat();
        if (mUser == null) return false;

        if ("@null".equals(mUser.fecha)) {
            mDao.removerUser(mUser.fecha);
            mDao.removerUser(mUser.uid);
        } else {
            mDao.update(mUser);
        }
        return true;
    }

    private boolean processConf(Conf mUser) {
        DaoCfg mDao = StartVar.appDBall.daoCfg();
        if (mUser == null) return false;

        if ("@null".equals(mUser.config)) {
            mDao.removerUser(mUser.config);
            mDao.removerUser(mUser.uid);
        } else {
            mDao.update(mUser);
        }
        return true;
    }
}