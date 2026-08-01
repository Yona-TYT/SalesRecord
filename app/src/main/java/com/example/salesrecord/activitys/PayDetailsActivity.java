package com.example.salesrecord.activitys;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

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
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.CalendUtls;
import com.example.salesrecord.utls.FilesManager;
import com.example.salesrecord.utls.MathUtls;


import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.annotations.NonNull;

public class PayDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    // DB ----------------------------------------------------------------
    private DaoSal daoSal;
    //--------------------------------------------------------------------

    //Todos los View
    private TextView mText1;
    private TextView mText2;
    private TextView mText3;
    private TextView mText4;
    private TextView mText5;
    private List<TextView> mTextList = new ArrayList<>();
    //---------------------------------------------------------------------

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch mSw;
    private boolean swDel = false;
    private String mUser = "";
    private Button mBtton1;
    private Button mBtton2;

    public String payId = StartVar.currPayId;
    public int accIndex = StartVar.accSelect;

    private List<String> mCurrencyList= Arrays.asList("$", "Bs");
    private int mCindex = StartVar.mCurrency;

    private ImageView mImage1;
    private String currDir = "";
    // Classs para la gestion de archivos
    private FilesManager mFileM = new FilesManager();

    private Context contex;
    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pay_dts);

        contex = AppContextProvider.getContext();
        daoSal = StartVar.appDBall.daoSal();

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

        mImage1 = findViewById(R.id.image_dts1);

        mText1 = findViewById(R.id.txview_dts1);
        mText2 = findViewById(R.id.txview_dts2);
        mText3 = findViewById(R.id.txview_dts3);
        mText4 = findViewById(R.id.txview_dts4);
        mText5 = findViewById(R.id.txview_dts5);

        mBtton1 = findViewById(R.id.butt_dts1);
        mBtton2  = findViewById(R.id.butt_dts2);
        mSw = findViewById(R.id.sw_dts1);

        mBtton1.setOnClickListener(this);
        mBtton2.setOnClickListener(this);
        mImage1.setOnClickListener(this);
        mSw.setOnClickListener(this);

        mTextList.add(mText1);
        mTextList.add(mText2);
        mTextList.add(mText3);
        mTextList.add(mText4);
        mTextList.add(mText5);

        // Se llenan los textView
        setTextViewList();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mSw.setFocusedByDefault(false);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    // MenuToolbar boton back
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == android.R.id.home){
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //------------------------------------------------------------

    @SuppressLint("SetTextI18n")
    public void setTextViewList(){

        Sale mPay = daoSal.getUsers(glData.getCurrSalId());

        if(mPay != null) {
            CalendUtls cale = new CalendUtls();
            mUser = mPay.sale;
            String txName = "";
            String txAlias = "";

            String txConc = "Pagado";
            String txMont = mPay.monto.toString();
            String txOpt = (mPay.status==0?"+ ":"- ");
            String txFech = "";
            txFech = CalendUtls.getDate(mPay.fecha);

            String txHora = CalendUtls.getTime(mPay.time);

            int i = 0;
            mTextList.get(i).setText("Tipo de Operación: " + glData.saleType.get(mPay.status));
            i++;
            mTextList.get(i).setText("Monto: "+txOpt+ Basic.getMaskConv(mPay.monto, 0) +" / " + Basic.getMaskConv(mPay.monto, mPay.tasa, 1));
            i++;
            mTextList.get(i).setText("Tasa: " + Basic.getMask(mPay.tasa, 1));
            i++;
            mTextList.get(i).setText("Fecha: "+ txFech);
            i++;
            mTextList.get(i).setText("Hora: "+ txHora);

            currDir = mFileM.getImage(mPay.imagen, mImage1);
        }
    }

    @Override
    public void onClick(View view) {
        int itemId = view.getId();
        if (itemId == R.id.sw_dts1){
//            swDel = !swDel;
//            if(swDel) {
//                mBtton1.setEnabled(true);
//            }
//            else{
//                mBtton1.setEnabled(false);
//            }
        }
        if (itemId == R.id.butt_dts1){
//            //fmang.RemoveFile(saveImage, this.getContentResolver());
//
//            //Elimina el registro selecionado
//            //appDBregistro.get(accIndex).daoUser().removerUser(mUser);
//            StartVar mVars = new StartVar();
//            //Recarga La lista de la DB ----------------------------
//            mVars.getCltListDB();
//            //-------------------------------------------------------
//
//            Intent mIntent = new Intent(this, MainActivity.class);
//            startActivity(mIntent);
//            finish(); //Finaliza la actividad y ya no se accede mas
        }
        if (itemId == R.id.butt_dts2){
//            Intent mIntent = new Intent(this, PayEditActivity.class);
//            this.finish();
//            startActivity(mIntent);
        }
        if(itemId == R.id.image_dts1) {
//            Intent mIntent = new Intent(this, ImageActivity.class);
//            startActivity(mIntent);
        }
    }
}