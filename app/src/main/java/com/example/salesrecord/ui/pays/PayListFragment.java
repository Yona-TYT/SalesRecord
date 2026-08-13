package com.example.salesrecord.ui.pays;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.CalendUtls;
import com.example.salesrecord.utls.MoneyUtls;

import java.time.Instant;
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

    private List<EditText> mInpList =  new ArrayList<>();

    private List<Fecha> dateOrderedList;
    private List<String> mStrFecList =  new ArrayList<>();
    private Spinner mSpinn1;
    private SelecAdapter mAdapter1;
    private int currSel1 = 0;

    private List<String> nameList =  new ArrayList<>();
    private Spinner mSpinn2;
    private SelecAdapter mAdapter2;
    private int currSel2 = 0;

    private Button mBtn1;

    private ListView mListView;
    private PayAdapter mAdapter3;
    private Article crrArt;

    private TextView mView1;
    private TextView mView2;
    private TextView mView3;

    private Context contex;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());



    private double mTotal = 0.0;

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

        mBtn1 = binding.buttPays1;
        mSpinn1 = binding.paySelect1;
        mSpinn2 = binding.paySelect2;
        mListView = binding.payViewList;
        mView1 = binding.txviewPays1;
        mView2 = binding.txviewPays2;
        mView3 = binding.txviewPays3;

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
                mStrFecList.add("Hoy");
            }
            else {
                mStrFecList.add(d.strdate);
            }
        }

        Collections.reverse(mStrFecList);
        mSalList = daoSal.getUsers();

        Collections.reverse(mSalList);

        mAdapter1 = new SelecAdapter(contex, mStrFecList);
        mSpinn1.setAdapter(mAdapter1);

        Collections.reverse(dateOrderedList);

        mSpinn1.setSelection(currSel1);

        // Para el selector por Dias
        mSpinn1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currSel1 = position;

                setPayList();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cltList = StartVar.appDBall.daoClt().getUsers();

        nameList.add("<Nombre>");
        for (Cliente mC : cltList){
            nameList.add(mC.iduser);
        }

        // Para el selector por Nombre Cliente
        mAdapter2 = new SelecAdapter(contex, nameList);
        mSpinn2.setAdapter(mAdapter2);
        mSpinn2.setSelection(currSel2);

        mSpinn2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mView3.setVisibility(View.GONE);
                mBtn1.setVisibility(View.GONE);
                currSel2 = position;
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

        Fecha selFecha = dateOrderedList.get(currSel1);
        List<Object[]> mPayList = new ArrayList<>();

        mTotal = 0.0;
        double oldTasa = 0;
        for (int i = 0; i < mSalList.size(); i++) {
            Sale mPay = mSalList.get(i);
            String name = glData.saleType.get(mPay.status);
            long fecha = mPay.fecha;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                String date = CalendUtls.getShortDate(fecha);
                String time = CalendUtls.getTime(mPay.time);

                if(currSel2 == 0) {
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
                        mTotal += mPay.monto;
                    }
                }
                else if(Objects.equals(mPay.cliente, cltList.get(currSel2 - 1).cliente) && mPay.status > 0){
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
                    mTotal += mPay.monto;
                }

//                if(currSel3 > 0) {
//                    Basic.msg(mPay.cltid + " " + cltList.get(currSel3 - 1).cliente);
//                }
            }
        }

        if(mTotal == 0.0){
            mView1.setText("Cliente Sin Deudas");
            mView2.setText("");
        }
        else {

            String strTotal = "Total: ";

            if(currSel2 > 0 ) {
                strTotal = "Deuda Total: ";
                mView3.setVisibility(View.VISIBLE);
                mBtn1.setVisibility(View.VISIBLE);
            }
            mView1.setText(strTotal + Basic.getMaskConv(mTotal, oldTasa, 0) + " / " + Basic.getMaskConv(mTotal, oldTasa, 1));

            String infla = "";
            if (StartVar.mDollar > 0 && oldTasa > 0) {
                // 1. Calculamos la variación real por cada unidad monetaria
                double rateDiff = StartVar.mDollar - oldTasa;

                // 2. La pérdida total es la diferencia de tasa multiplicada por la cantidad
                // ELIMINADO: infla * StartVar.mDollar (Esto duplicaba el cálculo erróneamente)
                double loss = rateDiff * mTotal;

                loss = loss / StartVar.mDollar;

                if (loss > 0) {
                    infla = "Inflación: -" + Basic.getMaskConv(loss, 0) + " / " + Basic.getMaskConv(loss, 1);
                } else {
                    infla = "Inflación: ninguna";
                }
            }

            if (currSel2 == 0) {
                mView2.setText(infla);
            } else {
                mView2.setText("Precio Actual: " + Basic.getMaskConv(mTotal, 0) + " / " + Basic.getMaskConv(mTotal, 1));
            }
        }

        //Para configurar la lista de pagos
        mAdapter3 = new PayAdapter(contex, mPayList);
        mListView.setAdapter(mAdapter3);
        mAdapter3.getFilter().filter("");

        mBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Clip Data", glData.glTelef + "\n" + glData.glCedula + "\n" + glData.glCodeBank + "\n" + MoneyUtls.setFormatterEs(MoneyUtls.getConv(mTotal, StartVar.mDollar, 1)));
                clipboard.setPrimaryClip(clipData);
                Basic.msg("Datos de PAGO+MONTO copiados al portapapeles.");
            }
        });
    }
}