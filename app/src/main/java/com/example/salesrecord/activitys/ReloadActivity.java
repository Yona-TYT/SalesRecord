package com.example.salesrecord.activitys;

import static com.example.salesrecord.StartVar.appDBall;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.DBListCreator;
import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.Conf;
import com.example.salesrecord.drive.DriveManager;
import com.example.salesrecord.ex.PreferenceHelper;

import net.openid.appauth.AuthState;

import java.util.List;

public class ReloadActivity extends AppCompatActivity {

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
        startVar.getFecListDB();
        //----------------------------------------------------------------------------------------------------------------------

        // Se agregan datos solo la primera vez en el primer elemento de la lista ---------------------------------------------
        List<Article> listArticle = appDBall.daoAtr().getUsers();

        if(!listArticle.isEmpty()) {
            Conf mCfg = StartVar.appDBall.daoCfg().getUsers(StartVar.mConfID);
            int idx = 0;
            idx = mCfg.curr;
            if(idx < listArticle.size()) {
                //startVar.setCurrentTyp(listArticle.get(idx).acctipo);
                startVar.setCurrentAcc(idx);
                startVar.setCurrency(mCfg.moneda);
                startVar.setDollar(mCfg.dolar);
                startVar.setCurrentMes(mCfg.mes);
            }
        }
        //----------------------------------------------------------------------------------------------------------------------

        DBListCreator.createDbLists(); //Actualiza la lista para exportar csv

        Bundle mExtra = getIntent().getExtras() ;
        if (mExtra != null) {
            boolean sync = mExtra.getBoolean("sync", false);
            if (sync){
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

        //Esto inicia las actividad Main
        startActivity(new Intent(AppContextProvider.getContext(), MainActivity.class));
        finish(); //Finaliza la actividad y ya no se accede mas
    }
}