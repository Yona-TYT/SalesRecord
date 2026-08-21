package com.example.salesrecord.activitys;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.db.dao.DaoClt;

import java.util.ArrayList;

public class ClientListActivity extends AppCompatActivity {

    private ListView mListView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Lista Clietes"); // Opcional: Cambia el título de la barra
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_client_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mListView1 = findViewById(R.id.clt_viewList);

        setViwes();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Cierra esta actividad y regresa de inmediato a la anterior
        finish();
        return true;
    }

    private void setViwes() {
        if (StartVar.appDBall == null) {
            //Satrted variables
            StartVar.setAllListDB();
        }

        DaoClt daoClt = StartVar.appDBall.daoClt();
        ArrayList<String> mStrList = new ArrayList<>();

        for (Cliente mC : daoClt.getUsers()){
            mStrList.add(mC.nombre+", Puntos: "+mC.level+" Count:"+mC.count);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mStrList);
        mListView1.setAdapter(adapter);

    }


}