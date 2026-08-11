package com.example.salesrecord.ui.pays;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.adapters.PayAdapter;
import com.example.salesrecord.adapters.SelecAdapter;
import com.example.salesrecord.databinding.FragmentPaysBinding;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.db.Fecha;
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.db.dao.DaoClt;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.CalendUtls;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PayListFragment extends Fragment {

    private FragmentPaysBinding binding;

    // DB
    private DaoArt daoArt;
    private DaoSal daoSal;
    private List<Sale> mSalList = new ArrayList<>();
    private List<Cliente> cltList;

    private ListView mListView;
    private PayAdapter mAdapter1;
    private Article crrArt;

    private TextView mView1;
    private TextView mView2;

    private Context contex;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    private List<EditText> mInpList =  new ArrayList<>();

    private List<Fecha> dateOrderedList;
    private List<String> mStrFecList =  new ArrayList<>();
    private Spinner mSpinn1;
    private SelecAdapter mAdapter2;
    private int currSel2 = 0;

    private List<String> nameList =  new ArrayList<>();
    private Spinner mSpinn2;
    private SelecAdapter mAdapter3;
    private int currSel3 = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPaysBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setViwes();

        //dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        setViwes();
    }

    private void setViwes(){

        contex = AppContextProvider.getContext();


        if (StartVar.appDBall == null) {
            //Satrted variables
            StartVar startVar = new StartVar();
            StartVar.setAllListDB();
        }
        mSpinn1 = binding.paySelect1;
        mSpinn2 = binding.paySelect2;
        mListView = binding.payViewList;
        mView1 = binding.txviewPays1;
        mView2 = binding.txviewPays2;

        daoSal = StartVar.appDBall.daoSal();
        //mSalList = daoSal.getUsers();

        dateOrderedList = StartVar.appDBall.daoDat().getUsers();

        List<Fecha> listFecha = dateOrderedList;


        mStrFecList.clear();
        nameList.clear();
        long   currDate = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currDate = Instant.now().toEpochMilli();
        }
        for (Fecha d : listFecha){
            //
            // Basic.msg(d.strdate+" "+dateOrderedList.size());
            if(CalendUtls.isSameDay(currDate, d.date)){
                mStrFecList.add("Ventas de Hoy");
            }
            else {
                mStrFecList.add(d.strdate);
            }
        }

        Collections.reverse(mStrFecList);
        mSalList = daoSal.getUsers();

        Collections.reverse(mSalList);

        mAdapter2 = new SelecAdapter(contex, mStrFecList);
        mSpinn1.setAdapter(mAdapter2);

        Collections.reverse(dateOrderedList);

        mSpinn1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currSel2 = position;

                setPayList();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cltList = StartVar.appDBall.daoClt().getUsers();

        nameList.add("<Nombre Cliente>");
        for (Cliente mC : cltList){
            nameList.add(mC.iduser);
        }

        mAdapter3 = new SelecAdapter(contex, nameList);
        mSpinn2.setAdapter(mAdapter3);

        mSpinn2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currSel3 = position;
                setPayList();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setPayList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    //Configura la lista de pagos por cuenta
    @SuppressLint("SetTextI18n")
    public void setPayList(){

        //Basic.msg("currSel: "+currSel2+" "+listFecha.get(currSel2).strdate);

        Fecha selFecha = dateOrderedList.get(currSel2);
        List<Object[]> mPayList = new ArrayList<>();

        double total = 0.0;
        double oldTasa = 0;
        for (int i = 0; i < mSalList.size(); i++) {
            Sale mPay = mSalList.get(i);
            String name = glData.saleType.get(mPay.status);
            long fecha = mPay.fecha;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                String date = CalendUtls.getShortDate(fecha);
                String time = CalendUtls.getTime(mPay.time);

                if(currSel3 == 0) {
                    if (CalendUtls.isSameDay(fecha, selFecha.date)) {
                        Object[] stList = new Object[7];
                        stList[0] = mPay.sale;
                        stList[1] = name;
                        stList[2] = mPay.monto;
                        stList[3] = date;
                        stList[4] = mPay.status;
                        stList[5] = time;
                        stList[6] = mPay.tasa;
                        mPayList.add(stList);

                        oldTasa = mPay.tasa;
                        total += mPay.monto;
                    }
                }
                else if(Objects.equals(mPay.cliente, cltList.get(currSel3 - 1).cliente)){
                    Object[] stList = new Object[7];
                    stList[0] = mPay.sale;
                    stList[1] = name;
                    stList[2] = mPay.monto;
                    stList[3] = date;
                    stList[4] = mPay.status;
                    stList[5] = time;
                    stList[6] = mPay.tasa;
                    mPayList.add(stList);

                    oldTasa = mPay.tasa;
                    total += mPay.monto;
                }

//                if(currSel3 > 0) {
//                    Basic.msg(mPay.cltid + " " + cltList.get(currSel3 - 1).cliente);
//                }

            }
        }

        mView1.setText("Total: " + Basic.getMaskConv(total, oldTasa,0) +" / "+Basic.getMaskConv(total, oldTasa,1));

        String infla = "";
        if(StartVar.mDollar > 0 && oldTasa > 0) {
            // 1. Calculamos la variación real por cada unidad monetaria
            double rateDiff= StartVar.mDollar - oldTasa;

            // 2. La pérdida total es la diferencia de tasa multiplicada por la cantidad
            // ELIMINADO: infla * StartVar.mDollar (Esto duplicaba el cálculo erróneamente)
            double loss = rateDiff * total;

             loss = loss / StartVar.mDollar;

             if (loss > 0) {
                 infla = "Inflación: -" + Basic.getMaskConv(loss, 0) + " / " + Basic.getMaskConv(loss, 1);
             } else {
                 infla = "Inflación: ninguna";
             }
        }

        if(currSel3 == 0) {
            mView2.setText(infla);
        }
        else {
            mView2.setText("Precio Actual: " + Basic.getMaskConv(total,0) +" / "+Basic.getMaskConv(total,1));
        }

        //Para configurar la lista de pagos
        mAdapter1 = new PayAdapter(contex, mPayList);
        mListView.setAdapter(mAdapter1);
        mAdapter1.getFilter().filter("");
    }
}