package com.example.salesrecord.activitys;


import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.DBListCreator;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.Conf;
import com.example.salesrecord.drive.DriveManager;
import com.example.salesrecord.ex.PreferenceHelper;
import com.example.salesrecord.utls.CalendUtls;
import com.example.salesrecord.utls.Msg;

import net.openid.appauth.AuthState;

import java.util.List;

public class ReloadActivity extends AppCompatActivity {

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pre);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Satrted variables
        StartVar startVar = new StartVar();
        Basic mBasic = new Basic(getApplicationContext());

        StartVar.reloadActivity = this;

        //Recarga La lista de la DB ----------------------------
        StartVar.getFecListDB();
        //----------------------------------------------------------------------------------------------------------------------

        // Se agregan datos solo la primera vez en el primer elemento de la lista ---------------------------------------------
        List<Article> listArticle = StartVar.appDBall.daoAtr().getUsers();

        if(!listArticle.isEmpty()) {
            Conf mCfg = StartVar.appDBall.daoCfg().getUsers(StartVar.mConfID);
            int idx = 0;
            idx = mCfg.curr;
            if(idx < listArticle.size()) {
                //startVar.setCurrentTyp(listArticle.get(idx).acctipo);
                startVar.setCurrentAcc(idx);
                startVar.setCurrency(mCfg.moneda);
                //startVar.setDollar(mCfg.dolar);
                startVar.setCurrentMes(mCfg.mes);
            }
        }
        //----------------------------------------------------------------------------------------------------------------------

        boolean sync = false;
        Bundle mExtra = getIntent().getExtras() ;
        if (mExtra != null) {
            sync = mExtra.getBoolean("sync", false);
            if (sync){

                long currDate = 0;
                long currTime = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    currDate = java.time.Instant.now().toEpochMilli();
                    currTime = System.currentTimeMillis();
                }
                String strDbg = "ReloadActivity: "+ CalendUtls.getShortDate(currDate)+" "+CalendUtls.getTime(currTime);
                StartVar.appDBall.daoCfg().updateDateTime(StartVar.mConfID, currDate, currTime, strDbg);

                DBListCreator.createDbLists(); //Actualiza la lista para exportar csv

                //Envia una actulaizacion del CSV completa en este caso
                AuthState authState = new AuthState();
                authState = DriveManager.getAuthState();
                if (authState.isAuthorized()){
                    DriveManager manager = new DriveManager(PreferenceHelper.getInstance());
                    manager.uploadDataBase();
                }
                //----------------------------------------------------------------------
            }
        }

        // Si no habia sincronizacion se actualiza igualmente la lista csv
        if(!sync){
            DBListCreator.createDbLists(); //Actualiza la lista para exportar csv
        }

        //Esto inicia las actividad Main
        startActivity(new Intent(AppContextProvider.getContext(), MainActivity.class));
        finish(); //Finaliza la actividad y ya no se accede mas
    }
}