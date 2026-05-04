package com.example.salesrecord.db;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.DBListCreator;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.db.dao.DaoCfg;
import com.example.salesrecord.db.dao.DaoClt;
import com.example.salesrecord.db.dao.DaoDat;
import com.example.salesrecord.db.dao.DaoDeb;
import com.example.salesrecord.db.dao.DaoPay;
import com.example.salesrecord.drive.DriveManager;
import com.example.salesrecord.ex.PreferenceHelper;
import com.google.gson.Gson;

import java.time.LocalDate;
import java.time.LocalTime;

public class GenericWorker extends Worker {
    public GenericWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            String usuarioJson = getInputData().getString("usuarioJson");
            String json = getInputData().getString("objeto_json");
            String tipo = getInputData().getString("objeto_tipo");
            int mSend = getInputData().getInt("send",0);
            Gson gson = new Gson();

            if(mSend == 1){
                String currDate = "";
                String currTime = "";
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    currDate = LocalDate.now().toString();
                    currTime = LocalTime.now().toString();
                }
                StartVar.appDBall.daoCfg().updateDateTime(StartVar.mConfID, currDate, currTime);
                StartVar.getConfigDB();

                DBListCreator.createDbLists(); //Actualiza la lista para exportar csv
                
                DriveManager manager = new DriveManager(PreferenceHelper.getInstance());
                manager.uploadDataBase();
                //Basic.msg("Aqui hay!! :) : "+);
                StartVar.genericQueue.clear();
            }

            else if(mSend == 2){
                boolean isOk = false;
                if (json == null || tipo == null) return Result.failure();

                try {

                    // Convertimos el String de la clase en un objeto real
                    Class<?> claseObjetivo = Class.forName(tipo);
                    Object objeto = gson.fromJson(json, claseObjetivo);

                    // --- INICIO DE LA LÓGICA SEGÚN EL TIPO ---

                    if (objeto instanceof Article) {
                        isOk = processCuenta((Article) objeto);
                    }
                    else if (objeto instanceof Cliente) {
                        isOk = processCliente((Cliente) objeto);
                    }
                    else if (objeto instanceof Deuda) {
                        isOk = processDeuda((Deuda) objeto);
                    }
                    else if (objeto instanceof Pagos) {
                        isOk = processPago((Pagos) objeto);
                    }
                    else if (objeto instanceof Fecha) {
                        isOk = processFecha((Fecha) objeto);
                    }
                    else if (objeto instanceof Conf) {
                        isOk = processConf((Conf) objeto);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    return Result.retry(); // Reintenta si hay un error temporal
                }


                if (isOk) {
                    String currDate = "";
                    String currTime = "";
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        currDate = LocalDate.now().toString();
                        currTime = LocalTime.now().toString();
                    }
                    StartVar.appDBall.daoCfg().updateDateTime(StartVar.mConfID, currDate, currTime);
                    StartVar.getConfigDB();

                    DBListCreator.createDbLists(); //Actualiza la lista para exportar csv

                    DriveManager manager = new DriveManager(PreferenceHelper.getInstance());
                    manager.uploadDataBase();
                    //Basic.msg("Aqui hay!! :) : "+gson.fromJson(queueItem.usuarioJson, Usuario.class).nombre);
                    StartVar.genericQueue.clear();

                    Intent mIntent = new Intent(AppContextProvider.getContext(), StartVar.mActivity.getClass());
                    StartVar.mActivity.startActivity(mIntent);
                    StartVar.mActivity.finish();
                }
                return Result.success();
            }
            return Result.success();
        } catch (Exception e) {
            Basic.msg("Aqui no hay :(  "+ StartVar.sendDate);
            //LOG.error("Error procesando usuario: {}", e.getMessage(), e);
            return Result.failure();
        }
    }

    // Métodos específicos para cada lógica
    private boolean processDeuda(Deuda mUser) {
        DaoDeb mDao = StartVar.appDBall.daoDeb();
        if (mUser == null){
            Basic.msg("Error Objeto NULL");
            return false;
        }
        if (mUser.deuda.equals("@null")) {
            mDao.removerUser(mUser.deuda);
            mDao.removerUser(mUser.uid);
        }
        else {
            mDao.update(mUser);
        }
        return true;
    }

    private boolean processCliente(Cliente mUser) {
        DaoClt mDao = StartVar.appDBall.daoClt();
        if (mUser == null){
            Basic.msg("Error Objeto NULL");
            return false;
        }
        if (mUser.cliente.equals("@null")) {
            mDao.removerUser(mUser.cliente);
            mDao.removerUser(mUser.uid);
        }
        else {
            mDao.update(mUser);
        }
        return true;
    }

    private boolean processCuenta(Article mUser) {
        DaoArt mDao = StartVar.appDBall.daoAtr();
        if (mUser == null){
            Basic.msg("Error Objeto NULL");
            return false;
        }
        if (mUser.article.equals("@null")) {
            mDao.removerUser(mUser.article);
            mDao.removerUser(mUser.uid);
        }
        else {
            mDao.update(mUser);
        }
        return true;
    }

    private boolean processPago(Pagos mUser) {
        DaoPay mDao = StartVar.appDBall.daoPay();
        if (mUser == null){
            Basic.msg("Error Objeto NULL");
            return false;
        }
        if (mUser.pago.equals("@null")) {
            mDao.removerUser(mUser.pago);
            mDao.removerUser(mUser.uid);
        }
        else {
            mDao.update(mUser);
        }
        return true;
    }
    private boolean processFecha(Fecha mUser) {
        DaoDat mDao = StartVar.appDBall.daoDat();
        if (mUser == null){
            Basic.msg("Error Objeto NULL");
            return false;
        }
        if (mUser.fecha.equals("@null")) {
            mDao.removerUser(mUser.fecha);
            mDao.removerUser(mUser.uid);
        }
        else {
            mDao.update(mUser);
        }
        return true;
    }

    private boolean processConf(Conf mUser) {
        DaoCfg mDao = StartVar.appDBall.daoCfg();
        if (mUser == null){
            Basic.msg("Error Objeto NULL");
            return false;
        }
        if (mUser.config.equals("@null")) {
            mDao.removerUser(mUser.config);
            mDao.removerUser(mUser.uid);
        }
        else {
            mDao.update(mUser);
        }
        return true;
    }
}
