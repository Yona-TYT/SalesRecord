package com.example.salesrecord.db;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;


import com.example.salesrecord.DBListCreator;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.db.dao.QueueItemDao;
import com.example.salesrecord.drive.DriveManager;
import com.example.salesrecord.drive.SetWorkResult;
import com.example.salesrecord.ex.PreferenceHelper;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.utls.CalendUtls;
import com.google.gson.Gson;

import net.openid.appauth.AuthState;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GenericQueue {
    private final LinkedList<Object> queue;
    private QueueItemDao queueItemDao;
    private final Context context;
    private final Gson gson;

    public GenericQueue(Context context) {
        this.context = context.getApplicationContext();
        this.queue = new LinkedList<>();
        this.gson = new Gson();
    }

    // 3. Agrega este método para asegurar que el DAO se obtenga solo cuando se necesite
    private QueueItemDao getQueueItemDao() {
        if (queueItemDao == null) {
            // Si la app despertó en segundo plano y StartVar no se ha inicializado, lo forzamos
            if (StartVar.appDBall == null) {
                Log.w("Queue", "La BD en StartVar es null. Inicializando contenedores...");
                StartVar.setAllListDB();
            }
            queueItemDao = StartVar.appDBall.daoQueue();
        }
        return queueItemDao;
    }

    // Encolar con mSend = 2 por defecto
    public void enqueue(Object objeto) {
        enqueue(objeto, 2);
    }

    // Encolar con mSend personalizado
    public void enqueue(Object objeto, int mSend) {
        String json = gson.toJson(objeto);
        String tipoClase = objeto.getClass().getName();

        long order = System.currentTimeMillis();
        QueueItem item = new QueueItem(json, tipoClase, order);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {

                getQueueItemDao().insert(item);

                // Sincronización con Drive
                AuthState authState = DriveManager.getAuthState();
                if (authState != null && authState.isAuthorized()) {
                    synchronizeCheck();
                }

                // Añadir a la cola en memoria y arrancar el procesamiento con mSend
                new Handler(Looper.getMainLooper()).post(() -> {
                    queue.add(objeto);
                    Log.d("Queue", "Objeto añadido a la cola en memoria");

                    // Arranca el Worker con el mSend indicado (siempre 2 desde el fragment)
                    startUsuarioQueue(mSend);
                });

            } catch (Exception e) {
                Log.e("Queue", "Error al procesar item en cola", e);
            }
        });
    }

    public void enqueueList(List<Object> objList) {
        enqueueList(objList, 2);
    }

    public void enqueueList(List<Object> objList, int mSend) {
        if (objList == null || objList.isEmpty()) {
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {

                long baseOrder = System.currentTimeMillis();

                // Convertir y guardar secuencialmente en la base de datos
                for (int i = 0; i < objList.size(); i++) {
                    Object objeto = objList.get(i);
                    String json = gson.toJson(objeto);
                    String tipoClase = objeto.getClass().getName();

                    // Sumamos el índice para mantener el orden exacto de llegada
                    QueueItem item = new QueueItem(json, tipoClase, baseOrder + i);
                    getQueueItemDao().insert(item);
                }

                // Sincronización con Drive
                AuthState authState = DriveManager.getAuthState();
                if (authState != null && authState.isAuthorized()) {
                    synchronizeCheck();
                }

                // Pasar al hilo principal para actualizar memoria e iniciar Workers
                new Handler(Looper.getMainLooper()).post(() -> {
                    queue.addAll(objList);
                    Log.d("Queue", objList.size() + " objetos añadidos a la memoria.");
                });

            } catch (Exception e) {
                Log.e("Queue", "Error al procesar lista en cola", e);
            }
        });
    }
    public void loadQueueFromDatabase(int send) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<QueueItem> items = getQueueItemDao().getAllQueueItems();

            queue.clear();

            for (QueueItem item : items) {
                try {
                    Class<?> clazz = Class.forName(item.tipo);
                    Object objeto = gson.fromJson(item.json, clazz);
                    queue.add(objeto);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                if (!queue.isEmpty()) {
                    processNext(send);
                } //else {
                    ///Basic.msg("No hay elementos en cola");
                //}
            });
        });
    }

    public void startUsuarioQueue(int send) {
        loadQueueFromDatabase(send);
    }

    private void synchronizeCheck() {
        // LiveData.observe DEBE ejecutarse en el hilo principal
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                DriveManager manager = new DriveManager(PreferenceHelper.getInstance());
                ExecutorService executorService = Executors.newSingleThreadExecutor();

                StartVar.mWorkResult = new SetWorkResult(
                        ProcessLifecycleOwner.get(),
                        executorService,
                        manager
                );

                StartVar.mWorkResult.observeWorkResult();
                manager.dataSynchronizeCheck();
            } catch (Exception e) {
                Log.e("Queue", "Error en synchronizeCheck", e);
            }
        });
    }

    private void processNext(int sendOpt) {
        if (queue.isEmpty()) {
            if (sendOpt == 1 || sendOpt == 2 || sendOpt == 3) {
                performFinalUpload(sendOpt);
            }
            return;
        }

        Object current = queue.peek();

        Data inputData = new Data.Builder()
                .putString("objeto_json", gson.toJson(current))
                .putString("objeto_tipo", current.getClass().getName())
                .putInt("send", sendOpt)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(GenericWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context)
                .getWorkInfoByIdLiveData(request.getId())
                .observe(ProcessLifecycleOwner.get(), workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            queue.poll();

                            QueueItem item = getQueueItemDao().getFirstQueueItem();
                            if (item != null) getQueueItemDao().delete(item);

                            processNext(sendOpt);   // siguiente
                        } else {
                            Log.e("Queue", "Worker falló: " + workInfo.getState());
                        }
                    }
                });

        WorkManager.getInstance(context).enqueue(request);
    }

    private void performFinalUpload(int sendOpt) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                updateConfigTimestamp();                 // nueva fecha local
                DBListCreator.createDbLists();           // CSV actualizados (ya fusionados)
                new DriveManager(PreferenceHelper.getInstance()).uploadDataBase();
                clear();

                if (sendOpt == 2) {
                    // Reiniciar activity si es necesario
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (StartVar.mActivity != null) {
                            Intent i = new Intent(context, StartVar.mActivity.getClass());
                            StartVar.mActivity.startActivity(i);
                            StartVar.mActivity.finish();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("Queue", "Error en subida final", e);
            }
        });
    }

    public int size() {
        return queue.size();
    }

    public void clear() {
        queue.clear();
        getQueueItemDao().deleteAll();
    }

    public void poll() {
        queue.poll();
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

    public boolean hasPendingQueueItems() {
        try {
            List<QueueItem> items = getQueueItemDao().getAllQueueItems();
            return items != null && !items.isEmpty();
        } catch (Exception e) {
            return !queue.isEmpty();
        }
    }
}
