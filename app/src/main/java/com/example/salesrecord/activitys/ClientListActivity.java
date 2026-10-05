package com.example.salesrecord.activitys;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.adapters.CltAdapter;
import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.db.dao.DaoClt;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.utls.MoneyUtls;
import com.example.salesrecord.utls.Msg;

import java.util.ArrayList;
import java.util.List;

public class ClientListActivity extends AppCompatActivity {

    private CheckBox check1;
    private ListView mListView1;

    private List<Sale> mSalList = new ArrayList<>();
    private List<Cliente> mCltList = new ArrayList<>();

    private Context contex;

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
        check1 = findViewById(R.id.clt_check_1);

        setViwes();
    }

    @Override
    public void onResume() {
        super.onResume();

        setViwes();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Cierra esta actividad y regresa de inmediato a la anterior
        finish();
        return true;
    }

    private void setViwes() {
        contex = AppContextProvider.getContext();

        if (StartVar.appDBall == null) {
            //Satrted variables
            StartVar.setAllListDB();
        }

        DaoClt daoClt = StartVar.appDBall.daoClt();
        DaoSal daoSal = StartVar.appDBall.daoSal();

        mSalList.clear();
        mSalList = daoSal.getUsers();

        // 1. Obtener la lista original de clientes
        mCltList.clear();
        mCltList = daoClt.getUsers();

        // 2. Ordenar de mayor a menor usando el campo float 'level'
        mCltList.sort((c1, c2) -> Float.compare(c2.level, c1.level));

        check1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setListAdapter();
            }
        });

        setListAdapter();
    }

    private void setListAdapter() {
        // 3. Llenar la lista de Strings ya ordenada
        int mLev = 1;
        List<Object[]> mObjList = new ArrayList<>();

        for (Cliente mC : mCltList) {
            double money = 0.0;
            for (Sale mS : mSalList) {
                if (mS.cliente.equals(mC.cliente) && mS.status > 0 && mS.status < 3) {
                    money += mS.monto;
                }
            }

            if (check1.isChecked() && money == 0) {
                continue;
            }

            Object[] strList = new Object[8];
            strList[0] = mC.cliente;
            strList[1] = "Puntos: " + MoneyUtls.formatPlainDecimal((double) mC.level);
            strList[2] = money;
            strList[3] = "mC.fecha";
            strList[4] = 0;
            strList[5] = "mC.fecha";
            strList[6] = money;
            strList[7] = "Nr" + mLev + ". (" + mC.nombre + ")";

            mLev++;

            mObjList.add(strList);
        }

        // 4. Asignar al adaptador
        CltAdapter adapter = new CltAdapter(this, mObjList);
        mListView1.setAdapter(adapter);
        adapter.getFilter().filter("");
    }
}