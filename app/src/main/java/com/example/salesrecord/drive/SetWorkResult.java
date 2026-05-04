package com.example.salesrecord.drive;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Environment;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.activitys.ReloadActivity;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.ex.PreferenceHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class SetWorkResult {
    private static final Log log = LogFactory.getLog(SetWorkResult.class);
    private LifecycleOwner lifecycle;
    private ExecutorService executorService;
    private DriveManager manager;
    private Observer<WorkInfo> workObserver; // Referencia al Observer

    public SetWorkResult(LifecycleOwner lifecycle, ExecutorService executorService, DriveManager manager) {
        this.lifecycle = lifecycle;
        this.executorService = executorService;
        this.manager = manager;
    }

    //Debug
//    public void observeWorkResult() {
//        android.util.Log.d("QueueManager", "Iniciando observador para WORK_TAG_CONFDB: " + StartVar.WORK_TAG_CONFDB);
//        WorkManager.getInstance(StartVar.mContex)
//                .getWorkInfosForUniqueWorkLiveData(StartVar.WORK_TAG_CONFDB)
//                .observe(lifecycle, workInfos -> {
//                    android.util.Log.d("WorkerStatus", "Recibidos " + workInfos.size() + " WorkInfos");
//                    for (WorkInfo workInfo : workInfos) {
//                        android.util.Log.d("WorkerStatus", "Estado: " + workInfo.getState() + ", ID: " + workInfo.getId());
//                        if (workInfo.getState().isFinished()) {
//                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
//                                String result = workInfo.getOutputData().getString("result");
//                                android.util.Log.d("WorkerResult", "Éxito: " + result);
//                            } else if (workInfo.getState() == WorkInfo.State.FAILED) {
//                                android.util.Log.d("WorkerResult", "Fallo en Worker");
//                            } else if (workInfo.getState() == WorkInfo.State.CANCELLED) {
//                                android.util.Log.d("WorkerResult", "Worker cancelado");
//                            }
//                        } else {
//                            android.util.Log.d("WorkerStatus", "Worker en curso: " + workInfo.getState());
//                        }
//                    }
//                });
//    }
//
    // Observar los resultados del Worker
    public void observeWorkResult() {
        Context context = AppContextProvider.getContext();

        if (context == null) {
            android.util.Log.e("DriveSync", "❌ Context null en observeWorkResult(). No se puede observar WorkManager.");
            return;
        }

        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(StartVar.WORK_TAG_DOWNLOAD)
            .observe(lifecycle, workInfos -> {
                for (WorkInfo workInfo : workInfos) {
                    if (workInfo.getState().isFinished()) {
                        StartVar.setmMainStart(true);

                        Data outputData = workInfo.getOutputData();
                        String message = outputData.getString("result_message");
                        boolean preloader = outputData.getBoolean("preloader", false);
                        boolean isFileOk = outputData.getBoolean("file", false);
                        boolean isImg = outputData.getBoolean("img", false);

                        //Basic.msg("!!!!---0 !: "+ isCheck);

                        String[] filesDownloaded = outputData.getStringArray("files_downloaded");

                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {

                            String displayMessage = message != null ? message : "Descarga completada";
                            if (filesDownloaded != null && filesDownloaded.length > 0) {
                                displayMessage += ": " + String.join(", ", filesDownloaded);
                            }
                            if(isImg){
                                return;
                            }
                            File mFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS+"/"+StartVar.dirAppName+"/"+StartVar.exportName);
                            if(mFile.exists()){
                                //Aqui se analizan los nuevos datos y se comparan con los existentes
                                Uri uri = Uri.fromFile(mFile);
                                try {
                                    SetDb.set(context, outputData, uri,  manager);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                //------------------------------------------------------------------
                            }
                            else {
                                Basic.msg("CVS no Existe 1 !: "+displayMessage);
                            }
                        }
                        else if (workInfo.getState() == WorkInfo.State.FAILED) {
                            String displayMessage = message != null ? message : "Error en la descarga";
                            //Basic.msg("CVS no Existe 2 !: "+displayMessage);
                            //Basic.msg("Aqui hay ? "+displayMessage,true);
                            if (!isFileOk) {
                                List<Article> mAccList = StartVar.appDBall.daoAtr().getUsers();
                                if(!mAccList.isEmpty()) {
                                    if (preloader) {
                                        resetPreloader(true);
                                        StartVar.makeUpdate = true;
                                    } else {
                                        Basic.msg("Subiendo Datos...");
                                        manager.uploadDataBase();
                                    }
                                }
                            }
                        }
                    }
                }
            });
    }

    public static void startWorkManagerRequest(Class<? extends ListenableWorker> workerClass, HashMap<String, Object> dataMap, String tag) {
        // 1. Usar el contexto proporcionado o el global como respaldo
        Context appContext = AppContextProvider.getContext();

        if (appContext == null) {
            android.util.Log.e("DriveSync", "❌ Sin contexto disponible.");
            return;
        }

        // 2. Datos
        Data data = new Data.Builder().putAll(dataMap).build();

        // 3. Constraints simplificadas (Evita DeadObject en MIUI)
        boolean soloWifi = PreferenceHelper.getInstance().shouldAutoSendOnWifiOnly();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(soloWifi ? NetworkType.UNMETERED : NetworkType.CONNECTED)
                .build();

        // 4. Request
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(workerClass)
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.SECONDS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(tag)
                .build();

        // 5. Verificar conexión (usando la versión segura)
        if (!isNetworkAvailable(appContext)) {
            android.util.Log.w("DriveSync", "Sin conexión a internet. Se encolará cuando vuelva la conexión.");
            // Solo forzamos el preloader si es el flujo inicial
            if (!StartVar.mainStart) {
                StartVar.setmMainStart(true);
                resetPreloader(true);
            }
            // Puedes decidir si quieres encolar igual o no. WorkManager lo manejará con las constraints.
        }

        // 6. Encolar con política conservadora
        try {
            WorkManager.getInstance(appContext)
                    .enqueueUniqueWork(tag, ExistingWorkPolicy.KEEP, workRequest);
            android.util.Log.i("DriveSync", "✅ WorkManager encolado: " + tag);
        } catch (Exception e) {
            android.util.Log.e("DriveSync", "❌ Error Binder/WorkManager", e);
        }
    }
    public static boolean isNetworkAvailable(Context context) {

        if (context == null) {
            android.util.Log.e("NetworkCheck", "Context pasado es null");
            return false;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    public static void resetPreloader(boolean preloader){
        if(preloader){
            if(StartVar.mActivity != null){
                Intent mIntent = new Intent(AppContextProvider.getContext(),  ReloadActivity.class);
                StartVar.mActivity.startActivity(mIntent);
                StartVar.mActivity.finish();
            }
        }
    }
}
