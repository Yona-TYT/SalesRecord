package com.example.salesrecord.activitys;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;


import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.adapters.SaleResultAdapter;
import com.example.salesrecord.adapters.SelecAdapter;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.db.dao.DaoClt;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.CalendUtls;
import com.example.salesrecord.utls.FilesManager;
import com.example.salesrecord.utls.InputHelper;
import com.example.salesrecord.utls.MathUtls;
import com.example.salesrecord.utls.MoneyUtls;
import com.example.salesrecord.utls.Msg;
import com.example.salesrecord.utls.Obj;
import com.example.salesrecord.utls.QrPagoMovilCodec;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.annotations.NonNull;

public class PayDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    // DB ----------------------------------------------------------------
    private DaoSal daoSal;
    private DaoClt daoClt;
    //--------------------------------------------------------------------

    private ListView mListView1;

    //Todos los View
    private TextView total1;
    private TextView total2;

    private TextView datos;

    private TextView mText2;
    private TextView mText3;
    private TextView mText4;
    private TextView mText5;
    private TextView mText6;

    private List<TextView> mTextList = new ArrayList<>();
    //---------------------------------------------------------------------

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch mSw;
    private boolean swDel = false;
    private String mUser = "";
    private ImageButton mBtton0;
    private ImageButton mBtton1;
    private Button mBtton2;
    private Button mBtton3;
    private ToggleButton mBtton4;

    private AutoCompleteTextView mInput1;
    private ListView mListView2;

    private ArrayAdapter<String> adapter;
    private List<String> allNamesList = new ArrayList<>();
    private List<String> filtreList = new ArrayList<>();

    private Spinner mSpinn1;
    private int currSel1 = 0;


    private ImageView mImage1;
    private String currDir = "";
    // Classs para la gestion de archivos
    private FilesManager mFileM = new FilesManager();

    private Context contex;
    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    private Sale mSale;
    private String currId;

    private double mTotal = 0.0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pay_dts);

        //Se configura el Boton nav Back -----------------------------------------------
        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                PayDetailsActivity.this.finish();
            }
        };
        onBackPressedDispatcher.addCallback(this, callback);
        //---------------------------------------------------------------------------------
        //Activate ToolBar ----------------------------------------------------------------
        Toolbar myToolbar = findViewById(R.id.toolbar_dts);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Detalles de Pago");
        actionBar.setDisplayShowHomeEnabled(true);

        myToolbar.setTitleTextColor(ContextCompat.getColor(myToolbar.getContext(), R.color.inner_button));
        //------------------------------------------------------------------------------------------

        // 3. Restaurar las variables (Opción A: Desde el onCreate)
        if (savedInstanceState != null) {
            currId = savedInstanceState.getString("KEY_SALE", "");
        }else {
            currId = glData.getCurrSalId();
        }

        //mImage1 = findViewById(R.id.image_dts1);
        mListView1 = findViewById(R.id.pay_dts_viewList);
        mListView2 = findViewById(R.id.pay_dts_viewList2);

        total1 = findViewById(R.id.txtotal_dts1); //TOTAL
        total2 = findViewById(R.id.txtotal_dts2); //TOTAL

        datos = findViewById(R.id.text_dts1);

        mInput1 = findViewById(R.id.input_dts1);

        mText2 = findViewById(R.id.txview_dts2);
        mText3 = findViewById(R.id.txview_dts3);
        mText4 = findViewById(R.id.txview_dts4);
        mText5 = findViewById(R.id.txview_dts5);
        mText6 = findViewById(R.id.txview_dts6);

        mBtton0 = findViewById(R.id.butt_dts0);
        mBtton1 = findViewById(R.id.butt_dts1);
        mBtton2 = findViewById(R.id.butt_dts2);
        mBtton3 = findViewById(R.id.butt_dts3);
        mBtton4 = findViewById(R.id.butt_dts4);

        mSw = findViewById(R.id.sw_dts1);

        mSpinn1 = findViewById(R.id.pay_dts_select1);

        mBtton0.setOnClickListener(this);
        mBtton1.setOnClickListener(this);
        mBtton2.setOnClickListener(this);
        mBtton3.setOnClickListener(this);
        //mImage1.setOnClickListener(this);
        mSw.setOnClickListener(this);

        mTextList.add(mText2);
        mTextList.add(mText3);
        mTextList.add(mText4);
        mTextList.add(mText5);
        mTextList.add(mText6);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mSw.setFocusedByDefault(false);
        }

        // Se llenan los textView
        setViwes();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // Guardamos los valores actuales en el objeto Bundle usando claves únicas (keys)
        outState.putString("KEY_SALE", currId);

        // Siempre debes llamar al método super al final
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Msg.init(this);
        setViwes();
    }

    // MenuToolbar boton back
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //------------------------------------------------------------

    @SuppressLint("SetTextI18n")
    public void setViwes() {

        if (StartVar.appDBall == null) {
            //Satrted variables
            StartVar.setAllListDB();
        }

        contex = AppContextProvider.getContext();
        daoSal = StartVar.appDBall.daoSal();

        mSale = daoSal.getUsers(currId);

        if (mSale != null) {

            if(mSale.status > 0){
                datos.setVisibility(View.VISIBLE);
                mBtton0.setVisibility(View.VISIBLE);
                mBtton1.setVisibility(View.VISIBLE);
            }

            mSpinn1.setAdapter(new SelecAdapter(contex, glData.saleType));
            mSpinn1.setSelection(mSale.status);
            mSpinn1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currSel1 = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            List<Obj> objListSal = new ArrayList<>();

            String[] artcList = mSale.artclist.split("\\|");
            String[] countList = mSale.countlist.split("\\|");
            String[] priceList = mSale.pricelist.split("\\|");
            String[] margList = mSale.marglist.split("\\|");

            DaoArt daoArt = StartVar.appDBall.daoAtr();

            mTotal = 0.0;

            for (int i = 0; i < artcList.length; i++) {
                Article crrArt = daoArt.getUsers(artcList[i]);

                if (crrArt != null) {
                    double count = Double.parseDouble(countList[i]);
                    double price = Double.parseDouble(priceList[i]);
                    double marge = Double.parseDouble(margList[i]);

                    double calc = MathUtls.addPercentage(price, marge);
                    mTotal = mTotal + (calc * count);

                    //Basic.msg(""+price+" "+count+" "+marge, true);
                    objListSal.add(setObjects(crrArt, price, count, marge));
                }
            }

            SaleResultAdapter mAdapter = new SaleResultAdapter(contex, objListSal, false);
            mListView1.setAdapter(mAdapter);

            total1.setText("Actual: " + Basic.getMaskConv(mTotal, 0) + " / " + Basic.getMaskConv(mTotal, 1));

            double oldTasa = mSale.tasa;
            String infla = "";
            if (StartVar.mDollar > 0 && oldTasa > 0) {
                // 1. Calculamos la variación real por cada unidad monetaria
                double rateDiff = StartVar.mDollar - oldTasa;

                // 2. La pérdida total es la diferencia de tasa multiplicada por la cantidad
                // ELIMINADO: infla * StartVar.mDollar (Esto duplicaba el cálculo erróneamente)
                double loss = rateDiff * mTotal;

                if (loss > 0) {
                    infla = "Fijo: " + Basic.getMaskConv(mTotal, oldTasa, 0) + " / " + Basic.getMaskConv(mTotal, oldTasa, 1);
                }
            }

            total2.setText(infla);


            mUser = mSale.sale;
            String txAlias = mSale.cliente;

            if (txAlias.startsWith("cltID")) {
                DaoClt daoClt = StartVar.appDBall.daoClt();
                Cliente mClt = daoClt.getUsers(txAlias);
                if (mClt != null) {
                    txAlias = mClt.nombre + " (" + mClt.iduser + ")";

                    mInput1.setText(mClt.iduser);
                }
            }

            daoClt = StartVar.appDBall.daoClt();

            for (Cliente mC : daoClt.getUsers()){
                if(mC != null){
                    allNamesList.add(mC.iduser);
                }
            }

            // Lista dinámica que se mostrará en el ListView
            filtreList = new ArrayList<>();

            // 3. Configurar el adaptador para el ListView
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filtreList);
            mListView2.setAdapter(adapter);

            // 4. Escuchar los cambios de texto en el AutoCompleteTextView
            mInput1.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String textoEscrito = s.toString().toLowerCase().trim();
                    filtreList.clear();

                    // Filtrar solo si el usuario ha escrito texto
                    if (!textoEscrito.isEmpty()) {
                        for (String nombre : allNamesList) {
                            if (nombre.toLowerCase().contains(textoEscrito)) {
                                filtreList.add(nombre);
                            }
                        }
                        mListView2.setVisibility(View.VISIBLE);
                    } else {
                        // Ocultar la lista si el input está vacío
                        mListView2.setVisibility(View.GONE);
                    }

                    // Notificar al adaptador para que refresque la interfaz visual
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // 5. Detectar cuándo el usuario selecciona un nombre de la lista de sugerencias
            mListView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Obtener el nombre seleccionado
                    String nombreSeleccionado = filtreList.get(position);

                    // Colocar el nombre en el input y mover el cursor al final
                    mInput1.setText(nombreSeleccionado);
                    mInput1.setSelection(mInput1.getText().length());

                    // Limpiar y ocultar el ListView de sugerencias
                    filtreList.clear();
                    adapter.notifyDataSetChanged();
                    mListView2.setVisibility(View.GONE);
                }
            });

            mBtton4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mInput1.setVisibility(View.VISIBLE);
                    } else {
                        mInput1.setVisibility(View.GONE);

                        String strRawName = mInput1.getText().toString();
                        boolean b = false;
                        String mAlias = "";
                        if(!strRawName.isEmpty()) {
                            String idUser = InputHelper.sanitizeText(strRawName);
                            for (Cliente cl : daoClt.getUsers()){
                                if(cl.iduser.equals(idUser) ){
                                    b = true;
                                    if (!mSale.cltid.equals(idUser)) {
                                        mSale.cltid = "@null";
                                        mSale.cliente = cl.cliente;
                                        daoSal.update(mSale);
                                        mAlias = cl.nombre + " (" + cl.iduser + ")";
                                        GlobalData.getInstance(contex).getGenericQueue().enqueue(mSale, 3);
                                        break;
                                    }
                                }
                            }
                        }
                        if(!b) {
                            Msg.m("UserID de cliente no VALIDO!");
                        }
                        else{
                            mTextList.get(0).setText("Alias: " + mAlias);
                        }
                    }
                }
            });

            String txConc = "Pagado";
            String txMont = mSale.monto.toString();
            String txOpt = (mSale.status == 0 ? "+ " : "- ");
            String txFech = "";
            txFech = CalendUtls.getDate(mSale.fecha);

            String txHora = CalendUtls.getTime(mSale.time);

            int i = 0;
            mTextList.get(i).setText("Alias: " + txAlias);
            i++;
            mTextList.get(i).setText("Tipo de Operación: " + glData.saleType.get(mSale.status));
            i++;
            mTextList.get(i).setText("Monto Fijo: " + txOpt + Basic.getMaskConv(mSale.monto, 0) + " / " + Basic.getMaskConv(mSale.monto, mSale.tasa, 1));
            i++;
            mTextList.get(i).setText("Tasa: " + Basic.getMask(mSale.tasa, 1));
            i++;
            mTextList.get(i).setText("Fecha y Hora: " + txFech + " " + txHora);

            currDir = mFileM.getImage(mSale.imagen, mImage1);
        }
    }

    private Obj setObjects(Article art, double mPrice, double count, double margen) {

        Obj mObj = new Obj(art.article, art.nombre, art.descr, art.image, 0, art.metrica,
                art.staus, 1, art.currcount, count, count, mPrice, margen
                , art.uid);

        return mObj;
    }

    @Override
    public void onClick(View view) {
        int itemId = view.getId();
        if (mSale != null) {

            if (itemId == R.id.sw_dts1) {
                swDel = !swDel;
                if (swDel) {
                    mBtton2.setEnabled(true);
                } else {
                    mBtton2.setEnabled(false);
                }
            }
            if (itemId == R.id.butt_dts0) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Clip Data", glData.glTelef + "\n" +
                        glData.glCedula + "\n" + MoneyUtls.setFormatterEs(MoneyUtls.getConv(mTotal, StartVar.mDollar, 1))+
                        glData.glCodeBank + "\n" + "\n" +glData.glNameBank);
                clipboard.setPrimaryClip(clipData);
                Msg.m("Datos de PAGO+MONTO copiados al portapapeles.");
            }

            if (itemId == R.id.butt_dts1) {

                Application application = (Application) contex.getApplicationContext();
                Intent mIntent = new Intent(contex, QrActivity.class);
                mIntent.putExtra("amount", MoneyUtls.getConv(mTotal, StartVar.mDollar, 1));
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                application.startActivity(mIntent);
            }

            if (itemId == R.id.butt_dts2) {

                //fmang.RemoveFile(saveImage, this.getContentResolver());

                String[] artcList = mSale.artclist.split("\\|");
                String[] countList = mSale.countlist.split("\\|");

                DaoArt daoArt = StartVar.appDBall.daoAtr();
                List<Object> mList = new ArrayList<>();
                for (int i = 0; i < artcList.length; i++) {
                    String strArtc = artcList[i];
                    Article crrArt = daoArt.getUsers(strArtc);
                    if (crrArt != null) {
                        double count = Double.parseDouble(countList[i]);
                        crrArt.totalcount += count;
                        crrArt.currcount += count;
                        daoArt.update(crrArt);
                        mList.add(crrArt);
                    }
                }

                glData.setIsEdit(true);

                //Elimina el registro selecionado
                daoSal.removerUser(mSale.uid);
                mList.add(mSale);

                GlobalData.getInstance(contex).getGenericQueue().enqueueList(mList, 3);

                finish(); //Finaliza la actividad y ya no se accede mas

            }
            if (itemId == R.id.butt_dts3) {

                mSale.status = currSel1;
                daoSal.update(mSale);

                GlobalData.getInstance(contex).getGenericQueue().enqueue(mSale, 3);

                finish(); //Finaliza la actividad y ya no se accede mas

            }
//        if(itemId == R.id.image_dts1) {
////            Intent mIntent = new Intent(this, ImageActivity.class);
////            startActivity(mIntent);
//        }
        }
    }


}