package com.example.salesrecord.ui.clients;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;

import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.adapters.CltAdapter;
import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.db.dao.DaoClt;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.utls.MoneyUtls;

import java.util.ArrayList;
import java.util.List;

public class ClientsFragment extends Fragment {

    private CheckBox check1;
    private ListView mListView1;

    private List<Sale> mSalList = new ArrayList<>();
    private List<Cliente> mCltList = new ArrayList<>();
    private Context contex;

    public ClientsFragment() {
        // Required empty public constructor
    }

    public static ClientsFragment newInstance() {
        return new ClientsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflamos el XML del fragmento
        return inflater.inflate(R.layout.fragment_clients, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Almacenamos el contexto seguro del fragmento
        contex = requireContext();

        // Vinculamos las vistas usando la vista raíz 'view' del fragmento
        mListView1 = view.findViewById(R.id.clt_viewList);
        check1 = view.findViewById(R.id.clt_check_1);

        // Configuramos el listener del CheckBox
        check1.setOnClickListener(v -> setListAdapter());

        // Inicializamos los datos gráficos
        setViwes();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refrescamos los datos cada vez que el fragmento vuelve a ser visible
        setViwes();
    }

    private void setViwes() {
        if (StartVar.appDBall == null) {
            StartVar.setAllListDB();
        }

        DaoClt daoClt = StartVar.appDBall.daoClt();
        DaoSal daoSal = StartVar.appDBall.daoSal();

        mSalList.clear();
        mSalList = daoSal.getUsers();

        mCltList.clear();
        mCltList = daoClt.getUsers();

        // Ordenar de mayor a menor por nivel
        mCltList.sort((c1, c2) -> Float.compare(c2.level, c1.level));

        setListAdapter();
    }

    private void setListAdapter() {
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

        // Asignamos el adaptador pasando el contexto del fragmento
        CltAdapter adapter = new CltAdapter(contex, mObjList);
        mListView1.setAdapter(adapter);
        adapter.getFilter().filter("");
    }
}