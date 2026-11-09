package com.example.salesrecord.activitys;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.CurrencyEditText;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.adapters.PayAdapter;
import com.example.salesrecord.adapters.SelecAdapter;
import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.db.DatabaseUtils;
import com.example.salesrecord.db.Fecha;
import com.example.salesrecord.db.GenericQueue;
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.db.dao.DaoClt;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.drive.DriveManager;
import com.example.salesrecord.ex.Dialogs;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.CalendUtls;
import com.example.salesrecord.utls.LayoutShareHelper;
import com.example.salesrecord.utls.MoneyUtls;
import com.example.salesrecord.utls.Msg;
import com.google.android.material.button.MaterialButton;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;

public class CltDetailsActivity extends AppCompatActivity {

    private String strClt = "";
    private DaoClt daoClt;
    private DaoSal daoSal;
    private Cliente mClt;
    private List<Sale> mSalList;
    private List<String> mStrFecList =  new ArrayList<>();
    private List<LocalDate> finalDateList;

    private PayAdapter mAdapter1;
    private Spinner mSpinn1;
    private CheckBox check1;
    private ListView mListView;

    private CurrencyEditText abonoInput;
    private TextView total1;
    private TextView parcial;
    private TextView abonoText;
    private TextView datosText;

    private MaterialButton mBtton1;
    private ImageButton mBtton2;
    private ImageButton mBtton3;
    private ImageButton mBtton4;

    private Switch mSw1;
    private Switch mSw2;
    private Button mBtton5;

    private Long currDate = null;
    private double mTotal = 0.0;

    private Context contex;

    private HashMap<String, Boolean> saveSaleList = new HashMap<>();


    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Pagos por Cliente"); // Opcional: Cambia el título de la barra
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_clt_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mSpinn1 = findViewById(R.id.cltdts_select1);
        check1 = findViewById(R.id.cltdts_check_1);
        total1 = findViewById(R.id.txview_cltdts1); //TOTAL
        parcial = findViewById(R.id.txview_cltdts2);
        abonoText = findViewById(R.id.txview_cltdts3);
        datosText = findViewById(R.id.txview_cltdts4);

        abonoInput = findViewById(R.id.et_abono);

        mBtton1 = findViewById(R.id.butt_cltdts1);
        mBtton2 = findViewById(R.id.butt_cltdts2);
        mBtton3 = findViewById(R.id.butt_cltdts3);
        mBtton4 = findViewById(R.id.butt_cltdts4);

        mListView = findViewById(R.id.cltdts_viewList);

        mSw1 = findViewById(R.id.sw_cltdts1);
        mSw2 = findViewById(R.id.sw_cltdts2);
        mBtton5 = findViewById(R.id.butt_cltdts5);

        setViwes();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Cierra esta actividad y regresa de inmediato a la anterior
        finish();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        setViwes();

    }

    @SuppressLint("SetTextI18n")
    private void setViwes() {

        contex = AppContextProvider.getContext();

        if (StartVar.appDBall == null) {
            //Satrted variables
            StartVar.setAllListDB();
        }

        daoClt = StartVar.appDBall.daoClt();
        daoSal =  StartVar.appDBall.daoSal();
        strClt = glData.getCurrCltId();
        mClt = daoClt.getUsers(strClt);
        if (mClt == null){
            finish();
            return;
        }
        mSalList = daoSal.getSalesByClient(strClt);

        mBtton1.addOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                abonoText.setVisibility(View.GONE);
                abonoInput.setVisibility(View.VISIBLE);
            } else {
                abonoText.setVisibility(View.VISIBLE);
                abonoInput.setVisibility(View.GONE);

                double money = MoneyUtls.getInDollar(abonoInput.getNumericValue(), StartVar.mDollar, 1).doubleValue();

                if(money != 0){

                    long currDate = 0;
                    long currTime = 0;

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        currDate = java.time.Instant.now().toEpochMilli();
                        currTime = System.currentTimeMillis();
                    }
                    String strId = DatabaseUtils.generateId("salID", daoSal);

                    int cltNr = 1;
                    for (Sale sal : daoSal.getUsers()) {
                        if (CalendUtls.isSameDay(sal.fecha, currDate)) {
                            cltNr++;
                        }
                    }

                    Sale mSal = new Sale(
                            strId, strClt, "@null", "@null", "@null", "@null",
                            money, StartVar.mDollar, 0, "", currTime, "strCltId", cltNr, 1, currDate
                    );

                    double mResto = mClt.resto;

                    mResto = mResto + money;

                    List<Object> mList = new ArrayList<>();

                    for (Sale mS : mSalList){
                        if (mS.status >0 && mS.status < 3){
                            if(mResto >= mS.monto){
                                mResto = mResto - mS.monto;
                                mS.status = 0;
                                daoSal.update(mS);
                                mList.add(mS);

                            }
                        }
                    }

                    mClt.resto = mResto;

                    daoClt.update(mClt);

                    mList.add(mClt);


                    daoSal.insertUser(mSal);
                    mList.add(mSal);

                    //Se actualiza la lista de fechas si esta no existe
                    Fecha objB = CalendUtls.getAndCreateDate(contex, 0);
                    mList.add(objB);

                    mSalList = daoSal.getSalesByClient(strClt);

                    syncWithDrive(mList);

                }
                abonoInput.setText("0.00");
            }
        });

        // 1. Usamos un Set para eliminar duplicados ignorando las horas
        Set<LocalDate> uniqueDatesSet = new LinkedHashSet<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            for (Sale mS : mSalList) {
                if (mS.fecha != null) {
                    // Convertimos el Long a fecha pura (Año-Mes-Día) sin hora
                    LocalDate dateOnly = null;
                    dateOnly = Instant.ofEpochMilli(mS.fecha)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    uniqueDatesSet.add(dateOnly);
                }
            }
        }

        // 2. Convertimos el Set a una lista tradicional para poder ordenarla
        finalDateList = new ArrayList<>(uniqueDatesSet);

        // 3. SOLUCIÓN: Ordenamos de la más reciente a la más vieja (Orden Descendente)
        finalDateList.sort(Collections.reverseOrder());

        finalDateList.add(0, null);
        mStrFecList.clear();
        for(LocalDate date : finalDateList){
            if(date == null){
                mStrFecList.add(0,"<Todos>");
                continue;
            }
            mStrFecList.add(CalendUtls.getShortDateYear(date));
        }

        mSpinn1.setAdapter(new SelecAdapter(contex, mStrFecList));
        mSpinn1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                LocalDate date = finalDateList.get(position);
                if(date == null){
                    currDate = null;
                }
                else {
                    currDate = CalendUtls.localDateToTimestamp(date);

                }
                setListAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        check1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setListAdapter();
            }
        });


        mSw1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                glData.clearChecksState();
                if(!mSw1.isChecked() && mAdapter1 != null){
                    mAdapter1.clearSelections();
                }
                setListAdapter();
            }
        });

        mBtton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shredText = GlobalData.glTelef + "\n" +
                        GlobalData.glCedula + "\n" + MoneyUtls.formatPlainDecimal(MoneyUtls.getConv(mTotal, StartVar.mDollar, 1))+
                        "\n"+ GlobalData.glCodeBank + "\n" +GlobalData.glNameBank;

                LayoutShareHelper.sharePlainText(CltDetailsActivity.this, shredText, "Share text via:");
            }
        });

        mBtton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) contex.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Clip Data", GlobalData.glTelef + "\n" +
                        GlobalData.glCedula + "\n" + MoneyUtls.formatPlainDecimal(MoneyUtls.getConv(mTotal, StartVar.mDollar, 1))+ "\n"+
                        GlobalData.glCodeBank + "\n" + GlobalData.glNameBank);
                clipboard.setPrimaryClip(clipData);
                Msg.m("Datos de PAGO+MONTO copiados al portapapeles.");
            }
        });

        mBtton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!GlobalData.glPhone.isEmpty() && !GlobalData.glCedula.isEmpty() && !GlobalData.glCodeBank.isEmpty()) {

                    Application application = (Application) contex.getApplicationContext();
                    Intent mIntent = new Intent(contex, QrActivity.class);
                    mIntent.putExtra("amount", MoneyUtls.getConv(mTotal, StartVar.mDollar, 1));

                    //Deudas Selecionadas
                    if(mSw1.isChecked()) {
                        mIntent.putExtra("type", 3);

                    }
                    //Todas las deudas
                    else {
                        glData.saveChecksState(saveSaleList);
                        mIntent.putExtra("type", 2);
                    }

                    mIntent.putExtra("islist", true);

                    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    application.startActivity(mIntent);
                }
                else {
                    Msg.m("Los datos de pago estan incompletos!.");
                }
            }
        });

        setListAdapter();

        mSw2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtton5.setEnabled(mSw2.isChecked());
            }
        });

        mBtton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSw2.isChecked()){
                    List<Object> mList = new ArrayList<>();

                    double mPrice = 0.0;
                    for (Sale mS : mSalList) {
                        if (mS != null && mS.status != 0 && mS.status != 3) {

                            mPrice += mS.monto;

                            mList.addAll(updatePointsStatus( mS.status, 3,  mS, mPrice));
                        }
                    }
                    syncWithDrive(mList);
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setListAdapter() {
        setListAdapterInternal(true);
    }
    private void  setListAdapterNotReload(){
        setListAdapterInternal(false);
    }
    @SuppressLint("SetTextI18n")
    private void setListAdapterInternal(boolean reload){
        List<Object[]> mPayList = new ArrayList<>();

        mTotal = 0.0;
        saveSaleList.clear();

        parcial.setText("Sin Descontar: " + Basic.getMaskConv(mClt.resto, 0) + " / " + Basic.getMaskConv(mClt.resto, 1));

        for (int idx = 0; idx < mSalList.size(); idx ++) {
            Sale mS = mSalList.get(idx);

            boolean multiSel = (mAdapter1 != null && mAdapter1.isItemChecked(mS.sale));
            int stus = mS.status;

            if (check1.isChecked() && ( stus == 0 || stus == 3)){
                continue;
            }

            long fecha = mS.fecha;

            if(currDate != null){
                if(!CalendUtls.isSameDay(fecha, currDate)){
                    continue;
                }
            }

            if(stus > 0 && stus < 3){
                if(!mSw1.isChecked()){
                    saveSaleList.put(mS.sale, true);
                    mTotal += mS.monto;
                }
                else if (multiSel){
                    mTotal += mS.monto;
                }
            }
            if(reload) {

                String status =  glData.saleType.get(stus);
                if(mS.type == 1){
                    status = "Pago Parcial";
                }
                else if(mS.type == 2){
                    status = "Total deudas Pagadas";
                }
                else if(mS.type == 3){
                    status = "Parcial deudas Pagadas";
                }

                Long longDate = mS.fecha;
                String date = CalendUtls.getShortDate(fecha);
                String time = CalendUtls.getTime(mS.time);

                Object[] strList = new Object[9];
                strList[0] = mS.sale;
                strList[1] = status;
                strList[2] = mS.monto;
                strList[3] = date;
                strList[4] = stus;
                strList[5] = time;
                strList[6] = mS.tasa;
                strList[7] = mClt.nombre;
                strList[8] = longDate;

                mPayList.add(strList);
            }
        }

        mTotal = mTotal - mClt.resto;

        mPayList.sort((o1, o2) -> {
            Long fecha1 = (Long) o1[8];
            Long fecha2 = (Long) o2[8];
            return fecha2.compareTo(fecha1); // Al revés (fecha2 vs fecha1) da orden reciente a antiguo
        });


        if(mTotal > 0){
            total1.setText("Deuda Total: " + Basic.getMaskConv(mTotal, 0) + " / " + Basic.getMaskConv(mTotal, 1));
            datosText.setVisibility(View.VISIBLE);
            mBtton2.setVisibility(View.VISIBLE);
            mBtton3.setVisibility(View.VISIBLE);
            mBtton4.setVisibility(View.VISIBLE);
        }
        else {
            total1.setText( mSw1.isChecked()?"Deuda Total: SELECIONE ELEMENTOS" :"Deuda Total: SIN DEUDAS");

            datosText.setVisibility(View.GONE);
            mBtton2.setVisibility(View.GONE);
            mBtton3.setVisibility(View.GONE);
            mBtton4.setVisibility(View.GONE);
        }

        //Para configurar la lista de pagos
        if(reload) {
            mAdapter1 = new PayAdapter(contex, mPayList, mSw1.isChecked());
            mListView.setAdapter(mAdapter1);
            mAdapter1.getFilter().filter("");
        }


        // 1. CLICK NORMAL EN LA FILA (Tu código de siempre)
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Msg.m();
            }
        });

        // 2. CLICK ESPECÍFICO EN EL CHECKBOX (Capturado desde la Activity)
        mAdapter1.setOnCheckClickListener(new PayAdapter.OnCheckClickListener() {
            @Override
            public void onCheckClick(int position, boolean isChecked) {
                glData.saveChecksState(mAdapter1.checkedList());
                setListAdapterNotReload();
                //mAdapter1.notifyDataSetChanged();
            }
        });

    }

    private List<Object> updatePointsStatus(int oldOpt, int opt, Sale mSale, double price){
        List<Object> mList = new ArrayList<>();

        if (oldOpt != opt){

            Cliente mClt = daoClt.getUsers(mSale.cliente);

            if (mClt != null) {
                float oldPoints = 0f;
                float newPoints = 0f;

                // 1. Puntos que se habían otorgado con el estatus anterior
                switch (oldOpt) {
                    case 0: // Pagado
                        oldPoints = (float) (price * GlobalData.pointPay);
                        break;
                    case 1: // No pagado
                        oldPoints = (float) (price * GlobalData.pointNoPay);
                        break;
                    case 2: // Perdido
                        oldPoints = (float) (price * GlobalData.pointLost);
                        break;
                    case 3: // Exonerar → no se habían sumado puntos
                        oldPoints = 0f;
                        break;
                }

                // 2. Puntos que corresponden con el nuevo estatus
                switch (opt) {
                    case 0: // Pagado
                        newPoints = (float) (price * GlobalData.pointPay);
                        break;
                    case 1: // No pagado
                        newPoints = (float) (price * GlobalData.pointNoPay);
                        break;
                    case 2: // Perdido
                        newPoints = (float) (price * GlobalData.pointLost);
                        break;
                    case 3: // Exonerar → se restan los puntos anteriores y no se agrega nada
                        newPoints = 0f;
                        break;
                }

                // 3. Matemática segura
                float points = mClt.level - oldPoints + newPoints;
                mClt.level = points > 0f ? points : newPoints;

                daoClt.insertUser(mClt);
                mList.add(mClt);
            }
            mSale.status = opt;
            mList.add(mSale);
        }
        return mList;
    }

    private void syncWithDrive(List<Object> mList) {
        if (!DriveManager.getAuthState().isAuthorized()) {
            Msg.m("No autorizado en Google Drive");
            return;
        }

        if (GlobalData.shouldReload && (mList == null || mList.isEmpty())) {
            return;
        }

        GlobalData.shouldReload = false;

        Dialogs.progress(this, "Sincronizando con Google Drive");

        GenericQueue queue = GlobalData.getInstance(AppContextProvider.getContext()).getGenericQueue();

        final boolean[] completed = {false};   // bandera

        queue.setOnSyncCompleteListener(() -> {
            completed[0] = true;
            Dialogs.hideProgress();
            queue.setOnSyncCompleteListener(null);
            Msg.m("Sincronización exitosa");
            setListAdapter();
        });

        queue.enqueueList(mList, 3);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!completed[0]) {                 // solo si todavía no terminó
                Dialogs.hideProgress();
                queue.setOnSyncCompleteListener(null);
                Log.w("Ventas", "Timeout de red alcanzado. Liberando pantalla.");
                Msg.m("Sincronización en segundo plano");
            }
        }, 8000);
    }
}
