package com.example.salesrecord.db;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;


import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.db.dao.QueueItemDao;
import com.example.salesrecord.drive.DriveManager;
import com.example.salesrecord.drive.SetWorkResult;
import com.example.salesrecord.ex.PreferenceHelper;
import com.example.salesrecord.StartVar;
import com.google.gson.Gson;

import net.openid.appauth.AuthState;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GenericQueue {
    private LifecycleOwner lifecycle;
    private final LinkedList<Object> queue;
    private final QueueItemDao queueItemDao;
    private final Context context;
    private final Gson gson;

    public GenericQueue(LifecycleOwner lifecycle, Context context) {
        this.lifecycle = lifecycle;
        this.context = context.getApplicationContext();
        this.queue = new LinkedList<>();
        this.queueItemDao = StartVar.appDBall.daoQueue();
        this.gson = new Gson();
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
                AuthState authState = DriveManager.getAuthState();
                if (authState == null || !authState.isAuthorized()) {
                    Log.w("Queue", "No autorizado en Google Drive. No se inserta en cola.");
                    return;
                }

                queueItemDao.insert(item);

                // Sincronización con Drive
                synchronizeCheck();

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

    public void loadQueueFromDatabase(int send) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<QueueItem> items = queueItemDao.getAllQueueItems();

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
                } else {
                    Basic.msg("No hay elementos en cola");
                }
            });
        });
    }

    public void startUsuarioQueue(int send) {
        loadQueueFromDatabase(send);
    }

    private void synchronizeCheck() {
        DriveManager manager = new DriveManager(PreferenceHelper.getInstance());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        StartVar.mWorkResult = new SetWorkResult(lifecycle, executorService, manager);
        manager.dataSynchronizeCheck();
    }

    private void processNext(int sendOpt) {
        if (queue.isEmpty()) {
            return;
        }

        Object objetoActual = queue.peek();

        Data inputData = new Data.Builder()
                .putString("objeto_json", gson.toJson(objetoActual))
                .putString("objeto_tipo", objetoActual.getClass().getName())
                .putInt("send", sendOpt)   // ← aquí llega el mSend al GenericWorker
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(GenericWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context)
                .getWorkInfoByIdLiveData(workRequest.getId())
                .observe(lifecycle, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            queue.poll();

                            QueueItem queueItem = queueItemDao.getFirstQueueItem();
                            if (queueItem != null) {
                                queueItemDao.delete(queueItem);
                            }

                            processNext(sendOpt);
                        } else {
                            Basic.msg("Aqui fallloooo: " + StartVar.sendDate);

                            WorkInfo.State state = workInfo.getState();
                            String output = workInfo.getOutputData().toString();

                            Log.e("Queue", "Work falló. Estado = " + state);
                            Log.e("Queue", "OutputData = " + output);

                            String errorMsg = workInfo.getOutputData().getString("error");
                            if (errorMsg != null) {
                                Log.e("Queue", "Error del Worker: " + errorMsg);
                                Basic.msg("Error Worker: " + errorMsg);
                            }
                        }
                    }
                });

        WorkManager.getInstance(context).enqueue(workRequest);
    }

    public int size() {
        return queue.size();
    }

    public void clear() {
        queue.clear();
        queueItemDao.deleteAll();
    }

    public void poll() {
        queue.poll();
    }
}