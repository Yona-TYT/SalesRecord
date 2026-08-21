package com.example.salesrecord.activitys;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



import net.openid.appauth.AuthState;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.GetDollar;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.db.dao.DaoClt;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.CalendUtls;
import com.example.salesrecord.utls.FilesManager;
import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.db.Fecha;
import com.example.salesrecord.drive.DriveManager;
import com.example.salesrecord.drive.SetWorkResult;
import com.example.salesrecord.ex.PreferenceHelper;
import com.example.salesrecord.utls.Msg;

public class Preloader extends AppCompatActivity {

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_preloder);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS+"/"+StartVar.dirAppName+"/");
        if(file.exists()){
            FilesManager.DeleteFile(file);
        }

        //Check valus before start main activity
        //Satrted variables
        StartVar startVar = new StartVar();
        StartVar.setAllListDB();

        DaoClt daoClt = StartVar.appDBall.daoClt();
        DaoSal daoSal = StartVar.appDBall.daoSal();
        final List<Sale> mSalList = daoSal.getUsers();
        List<Object> mList = new ArrayList<>();
        for (Cliente mC : daoClt.getUsers()){
            String mCltId = mC.cliente;
            boolean hasSal = false;
            for (Sale mS : mSalList){
                if(mS.cliente.equals(mCltId)){
                    hasSal = true;
                    break;
                }
            }

            if(!hasSal){
                mC.cliente = "@null";
                mList.add(mC);
            }
        }
        if(!mList.isEmpty()){
            Msg.m("Borrando Clientes Sin Ventas...");
            GlobalData.getInstance(this).getGenericQueue().enqueueList(mList, 3);
        }

        StartVar.setmActivity(this);
        new Basic(AppContextProvider.getContext());

        List<Fecha> listFecha = StartVar.listfec;
        if(listFecha.isEmpty()) {
            Fecha obj;
            //Inicia la fecha actual
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                long   currDate = java.time.Instant.now().toEpochMilli();
                obj = new Fecha("dateID0", CalendUtls.getShortDateYear(currDate), currDate, System.currentTimeMillis());
            }
            else {
                obj = new Fecha("dateID0","", (long)0, (long)0);
            }
            StartVar.appDBall.daoDat().insertUser(obj);
            //-------------------------------------------------------
        }

        StartVar.mLifecycle = ProcessLifecycleOwner.get();

        //Se reinicia el cursor para el gallery adapter
        PreferenceHelper.getInstance().setGalleryPosition(0, 0);

        //Se crea el directorio de la app
        FilesManager.directoryCreate();

        //Satrted variables
        Basic mBasic = new Basic(getApplicationContext());
        Msg.init(this);

        DriveManager manager = new DriveManager(PreferenceHelper.getInstance());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        SetWorkResult mWorkResult = new SetWorkResult(StartVar.mLifecycle, executorService, manager);

        AuthState authState = new AuthState();
        authState = DriveManager.getAuthState();


        AppContextProvider.runWithSafeActivity(new AppContextProvider.SafeActivityRunnable() {
            @Override
            public void onActivityReady(Activity activity) {
                //Se obtiene el precio del dolar por primera vez
                new GetDollar(activity);
                try {
                    GetDollar.urlRun();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        if(authState.isAuthorized()) {
            //En caso de estancar se forza el inicio de mainActivity
            startMainDelayErr(15000);

            if (!StartVar.mainStart) {
                Msg.m("Sincronizando Datos...");
                mWorkResult.observeWorkResult();      // primero
                manager.dataSynchronizeStarting();    // después
                return;
            }
        }
        else{
            StartVar.setmMainStart(true);
        }

        if(StartVar.mainStart) {
            startMainDelay(800);
        }
    }

    private void startMainDelay(int s){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Esto inicia las actividad Main despues de tiempo de espera del preloder
                startActivity(new Intent(Preloader.this, ReloadActivity.class));
                finish(); //Finaliza la actividad y ya no se accede mas
            }
        }, s);
    }

    private void startMainDelayErr(int s){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(StartVar.mActivity == null || StartVar.mActivity.getClass().getSimpleName().equals("Preloader")) {
                    //Esto inicia las actividad Main despues de tiempo de espera del preloder
                    startActivity(new Intent(Preloader.this, ReloadActivity.class));
                    Msg.m("Algo fallo, Inicio forzado!");
                    finish(); //Finaliza la actividad y ya no se accede mas
                }
            }
        }, s);
    }
}