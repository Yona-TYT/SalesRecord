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


    //Test--------------------------------


    // Encolar CUALQUIER objeto
    public void enqueue(Object objeto) {
        String json = gson.toJson(objeto);
        String tipoClase = objeto.getClass().getName(); // "com.package.Deuda"

        long order = System.currentTimeMillis();
        QueueItem item = new QueueItem(json, tipoClase, order);

        // Ejecutar en hilo secundario (Importante para evitar el crash anterior)
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AuthState authState = DriveManager.getAuthState();
                if (authState == null || !authState.isAuthorized()) {
                    Log.w("Queue", "No autorizado en Google Drive. No se inserta en cola.");
                    return;                    // Sale inmediatamente del hilo
                }
                // Si está autorizado, continuamos
                queueItemDao.insert(item);

                // Sincronización con Drive
                synchronizeCheck();

                // Añadir a la cola en memoria (en el hilo principal)
                new Handler(Looper.getMainLooper()).post(() -> {
                    queue.add(objeto);
                    Log.d("Queue", "Objeto añadido a la cola en memoria");
                });
            } catch (Exception e) {
                Log.e("Queue", "Error al procesar item en cola", e);
            }
            // El hilo se termina automáticamente aquí
        });
    }

    // Cargar desde DB reconociendo el tipo
    public void loadQueueFromDatabase(int send) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<QueueItem> items = queueItemDao.getAllQueueItems();

            // Limpiar cola actual para evitar duplicados al recargar
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

            //Una vez cargada la lista, iniciamos el proceso en el hilo principal
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!queue.isEmpty()) {
                    processNext(send);
                } else {
                    Basic.msg("No hay elementos en cola");
                }
            });
        });
    }

    //---------------------------------------------------------------------

    // Cargar la cola desde Room
    public void startUsuarioQueue(int send) {
        loadQueueFromDatabase(send);
    }

    private void synchronizeCheck(){
        DriveManager manager = new DriveManager(PreferenceHelper.getInstance());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        StartVar.mWorkResult = new SetWorkResult( lifecycle, executorService, manager);

        manager.dataSynchronizeCheck();
    }

    // Procesar el siguiente elemento de la cola
    private void processNext(int sendOpt) {

        if (queue.isEmpty()) {
            //Basic.msg("Empty: "+sendOpt);
            return;
        }

        Object objetoActual = queue.peek();

        //Basic.msg("Not Empty: "+sendOpt);

        // Encolar un trabajo en WorkManager
        Data inputData = new Data.Builder()
                .putString("objeto_json", gson.toJson(objetoActual))
                .putString("objeto_tipo", objetoActual.getClass().getName()) // Ej: "com.package.Deuda"
                .putInt("send", sendOpt)
                .build();


        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(GenericWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context)
                .getWorkInfoByIdLiveData(workRequest.getId())
                .observe(lifecycle, workInfo -> {

                    if (workInfo != null && workInfo.getState().isFinished()) {

                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            // Eliminar el elemento procesado
                            queue.poll();
                            QueueItem queueItem = queueItemDao.getFirstQueueItem();
                            if (queueItem != null) {
                                queueItemDao.delete(queueItem);
                            }
                            // Procesar el siguiente
                            processNext(sendOpt);

                        } else {
                            Basic.msg("Aqui fallloooo: "+StartVar.sendDate);
                            //Log.e("UsuarioQueue", "Error procesando usuario: " + workInfo.getState());
                        }
                    }
                });

        WorkManager.getInstance(context).enqueue(workRequest);
    }

    // Obtener el tamaño de la cola
    public int size() {
        return queue.size();
    }

    // Limpiar la cola (opcional)
    public void clear() {
        queue.clear();
        queueItemDao.deleteAll();
    }

    public void poll() {
        queue.poll();
    }
}
