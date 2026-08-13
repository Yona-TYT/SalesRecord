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
import com.example.salesrecord.DBListCreator;
import com.example.salesrecord.GlobalData;
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
    private final java.util.Set<java.util.UUID> processedWorkIds = new java.util.HashSet<>();

    private Observer<WorkInfo> workObserver; // Referencia al Observer

    public SetWorkResult(LifecycleOwner lifecycle, ExecutorService executorService, DriveManager manager) {
        this.lifecycle = lifecycle;
        this.executorService = executorService;
        this.manager = manager;
    }

    // Observar los resultados del Worker
    public void observeWorkResult() {
        Context context = AppContextProvider.getContext();
        if (context == null) return;

        observeDownloadTag(context, StartVar.WORK_TAG_DOWNLOAD);
        observeDownloadTag(context, StartVar.WORK_TAG_DOWNLOAD_IMG);
    }

    private void observeDownloadTag(Context context, String uniqueName) {
        WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkLiveData(uniqueName)
                .observe(lifecycle, workInfos -> {
                    if (workInfos == null || workInfos.isEmpty()) return;
                    handleDownloadWorkInfos(workInfos);
                });
    }

    private void handleDownloadWorkInfos(List<WorkInfo> workInfos) {
        if (workInfos == null || workInfos.isEmpty()) return;
        Context context = AppContextProvider.getContext();
        if (context == null) return;

        for (WorkInfo workInfo : workInfos) {
            if (!workInfo.getState().isFinished()) continue;

            // Evitar reprocesar el mismo WorkInfo (p.ej. el del preloader)
            if (!processedWorkIds.add(workInfo.getId())) {
                continue;
            }
            //StartVar.setmMainStart(true);

            Data outputData = workInfo.getOutputData();
            boolean preloader = outputData.getBoolean("preloader", false);
            boolean isFileOk = outputData.getBoolean("file", false);
            boolean isImg = outputData.getBoolean("img", false);
            String message = outputData.getString("result_message");
            String[] filesDownloaded = outputData.getStringArray("files_downloaded");

            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                if (isImg) {
                    // EventBus / mensaje de imágenes OK, si lo necesitas
                    android.util.Log.d("DriveSync", "Download IMG OK: " + message);
                    continue;
                }
                File mFile = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS + "/" + StartVar.dirAppName + "/" + StartVar.exportName);

                if (mFile.exists()) {
                    Uri uri = Uri.fromFile(mFile);
                    try {
                        new SetDb().set(context, outputData, uri, manager);
                    } catch (IOException e) {
                        android.util.Log.e("DriveSync", "Error en SetDb", e);
                    }
                } else {
                    Basic.msg("CSV no existe: " + message);
                    // Si era preloader y no hay archivo, igual salir del preloader
                    SetWorkResult.resetPreloader(preloader);
                }
            } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                if (isImg) {
                    android.util.Log.e("DriveSync", "Download IMG failed: " + message);
                    continue;
                }
                if (!isFileOk) {
                    // No hay archivo en Drive (o no se pudo obtener)
                    List<Article> mAccList = StartVar.appDBall.daoAtr().getUsers();
                    boolean hasLocal = mAccList != null && !mAccList.isEmpty();

                    if (hasLocal) {
                        Basic.msg("Subiendo Datos...");
                        try {
                            DBListCreator.createDbLists();
                        } catch (Exception e) {
                            android.util.Log.e("DriveSync", "Error createDbLists", e);
                        }
                        manager.uploadDataBase();

                        if (preloader) {
                            StartVar.makeUpdate = true;
                            resetPreloader(true);
                        }
                    } else {
                        // Sin datos locales ni en Drive
                        android.util.Log.w("DriveSync", "Sin archivo en Drive y sin datos locales");
                        resetPreloader(preloader);
                    }
                } else {
                    // Fallo de red/token/etc. pero el flag de archivo no indica "no encontrado"
                    android.util.Log.e("DriveSync", "Download failed: " + message);
                    resetPreloader(preloader);
                }
            }
        }
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
            // Misma política, tags distintas = no se pisan entre sí
            ExistingWorkPolicy policy = ExistingWorkPolicy.REPLACE;

            WorkManager.getInstance(appContext)
                    .enqueueUniqueWork(tag, policy, workRequest);

            android.util.Log.i("DriveSync", "✅ WorkManager encolado: " + tag + " policy=" + policy);
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

    public static void resetPreloader(boolean preloader) {
        android.util.Log.d("Preloader", "resetPreloader called | preloader=" + preloader
                + " mainStart=" + StartVar.mainStart
                + " activity=" + (StartVar.mActivity != null
                ? StartVar.mActivity.getClass().getSimpleName()
                : "null"));

        if (!preloader) {
            return;
        }

        if (StartVar.mActivity == null) {
            android.util.Log.e("Preloader", "mActivity es null");
            StartVar.setmMainStart(true);
            return;
        }

        String current = StartVar.mActivity.getClass().getSimpleName();
        if (!"Preloader".equals(current)) {
            // Ya salimos del preloader
            StartVar.setmMainStart(true);
            return;
        }

        // Seguimos en Preloader → cerrar de verdad
        StartVar.setmMainStart(true);
        Intent i = new Intent(AppContextProvider.getContext(), ReloadActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        StartVar.mActivity.startActivity(i);
        StartVar.mActivity.finish();
    }
}
