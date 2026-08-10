package com.example.salesrecord.db;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
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

import java.time.LocalDate;
import java.time.LocalTime;


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
            Log.d("GenericWorker", "===== INICIO doWork =====");
            String usuarioJson = getInputData().getString("usuarioJson");
            String json = getInputData().getString("objeto_json");
            String tipo = getInputData().getString("objeto_tipo");
            int mSend = getInputData().getInt("send",0);
            Gson gson = new Gson();

            if(mSend == 1){
                long currDate = 0L;
                long currTime = 0L;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    currDate = java.time.Instant.now().toEpochMilli();
                    currTime = System.currentTimeMillis();
                }
                String strDbg = TAG+": "+ CalendUtls.getShortDate(currDate)+" "+CalendUtls.getTime(currTime);
                StartVar.appDBall.daoCfg().updateDateTime(StartVar.mConfID, currDate, currTime, strDbg);
                StartVar.getConfigDB();

                DBListCreator.createDbLists(); //Actualiza la lista para exportar csv

                DriveManager manager = new DriveManager(PreferenceHelper.getInstance());
                manager.uploadDataBase();
                //Basic.msg("Aqui hay!! :) : "+);
                GlobalData.getInstance(context).getGenericQueue().clear();
            }

            else if (mSend == 2 || mSend == 3) {
                if (json == null || tipo == null) {
                    Log.e(TAG, "Datos de entrada nulos en mSend == "+mSend);
                    return Result.failure();
                }

                boolean isOk = false;
                try {
                    Class<?> claseObjetivo = Class.forName(tipo);
                    Object objeto = gson.fromJson(json, claseObjetivo);

                    if (objeto instanceof Article) {
                        isOk = processArt((Article) objeto);
                    } else if (objeto instanceof Cliente) {
                        isOk = processCliente((Cliente) objeto);
                    } else if (objeto instanceof Deuda) {
                        isOk = processDeuda((Deuda) objeto);
                    } else if (objeto instanceof Sale) {
                        isOk = processPago((Sale) objeto);
                    } else if (objeto instanceof Fecha) {
                        isOk = processFecha((Fecha) objeto);
                    } else if (objeto instanceof Conf) {
                        isOk = processConf((Conf) objeto);
                    } else {
                        Log.w(TAG, "Tipo de objeto desconocido: " + tipo);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error al deserializar o mapear clase", e);
                    return Result.retry();
                }

                if (isOk) {
                    long currDate = 0L;
                    long currTime = 0L;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        currDate = java.time.Instant.now().toEpochMilli();
                        currTime = System.currentTimeMillis();
                    }
                    String strDbg = TAG+": "+ CalendUtls.getShortDate(currDate)+" "+CalendUtls.getTime(currTime);
                    StartVar.appDBall.daoCfg().updateDateTime(StartVar.mConfID, currDate, currTime, strDbg);
                    StartVar.getConfigDB();

                    DBListCreator.createDbLists(); //Actualiza la lista para exportar csv

                    DriveManager manager = new DriveManager(PreferenceHelper.getInstance());
                    manager.uploadDataBase();
                    //Basic.msg("Aqui hay!! :) : "+gson.fromJson(queueItem.usuarioJson, Usuario.class).nombre);
                    GlobalData.getInstance(context).getGenericQueue().clear();

                    if(mSend == 2){
                        Intent mIntent = new Intent(AppContextProvider.getContext(), StartVar.mActivity.getClass());
                        StartVar.mActivity.startActivity(mIntent);
                        StartVar.mActivity.finish();
                    }
                }
                Log.d("GenericWorker", "===== SUCCESS =====");
                return Result.success();
            }
            Log.d("GenericWorker", "===== SUCCESS =====");
            return Result.success();
        } catch (Exception e) {
            Log.e("GenericWorker", "===== ERROR =====", e);   // ← esto imprime el stacktrace completo
            Basic.msg("Aqui no hay :(  "+ StartVar.sendDate);

            return Result.failure(
                    new Data.Builder()
                            .putString("error", e.getMessage() != null ? e.getMessage() : e.toString())
                            .putString("exception", e.getClass().getSimpleName())
                            .build()
            );
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

    private boolean processPago(Sale mUser) {
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