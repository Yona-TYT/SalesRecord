package com.example.salesrecord.activitys;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.CurrencyEditText;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.adapters.SelecAdapter;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.DatabaseUtils;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.InputHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class FullEditActivity extends AppCompatActivity {

    // DB
    private DaoArt daoArt;

    private EditText mInput1;
    private EditText mInput2;
    private CurrencyEditText mInput3;
    private CurrencyEditText mInput4;
    private CurrencyEditText mInput5;
    private EditText mInput6;
    private EditText mInput7;

    private List<EditText> mInpList =  new ArrayList<>();

    private List<String> spinL1 = new ArrayList<>();
    private Spinner mSpin1;
    private int currSel1 = 0;

    private List<String> spinL2 = new ArrayList<>();
    private Spinner mSpin2;
    private int currSel2 = 0;

    private Button mBtn1;

    private Article crrArt;

    private Context contex;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_full_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setViwes();
    }

    @SuppressLint("WrongViewCast")
    private void setViwes() {

        if (StartVar.appDBall == null) {
            //Satrted variables
            StartVar startVar = new StartVar();
            startVar.setAllListDB();
        }

        daoArt = StartVar.appDBall.daoAtr();

        mInput1 = findViewById(R.id.full_et_nombre);
        mInput2 = findViewById(R.id.full_et_descr);
        mInput3 = findViewById(R.id.full_et_precio);
        mInput4 = findViewById(R.id.full_et_margen);
        mInput5 = findViewById(R.id.full_et_totalcount);
        mInput6 = findViewById(R.id.full_et_isopen);
        mInput7 = findViewById(R.id.full_et_caduca);

        mInpList.add(mInput1);
        mInpList.add(mInput3);
        mInpList.add(mInput5);


        mSpin1 = findViewById(R.id.full_select1);
        mSpin2 = findViewById(R.id.full_select2);

        mBtn1 = findViewById(R.id.full_btn_1);

        crrArt = glData.getCurrArt();

        spinL1 = glData.categ;
        spinL2 = glData.unitList;

        if(crrArt != null){

            Double precio = crrArt.precund;
            int mType = crrArt.artipo;
            if(mType == 1){
                precio = crrArt.precpq;
            }

            if(mType == 2){
                precio = crrArt.preccj;
            }

            mInput1.setText(crrArt.nombre);
            mInput2.setText(crrArt.descr);
            mInput3.setText(Basic.setFormatterEs(precio));
            mInput4.setText(Basic.setFormatterEs(crrArt.margen));
            mInput5.setText(Basic.setFormatterEs(crrArt.totalcount));
            mInput6.setText(String.valueOf(crrArt.isopen));
            mInput7.setText(String.valueOf(crrArt.caduca));

            //Para el selector de tipo de producto
            mSpin1.setAdapter(new SelecAdapter(AppContextProvider.getContext(), spinL1));
            mSpin1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currSel1 = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            //Para el selector de metrica
            mSpin2.setAdapter(new SelecAdapter(AppContextProvider.getContext(), spinL2));
            mSpin2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currSel2 = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            mBtn1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    List<String> mTxList =  new ArrayList<>();

                    boolean isOk = true;
                    for (EditText obj : mInpList){
                        String t = obj.getText().toString();
                        boolean b = InputHelper.validateField(obj);
                        if(isOk) {
                            isOk = b;
                        }
                    }

                    //Si la validacion falla, isOk es false y terminar el linsterner
                    if (!isOk){
                        return;
                    }

                    crrArt.nombre = mInput1.getText().toString();
                    crrArt.descr = mInput2.getText().toString();

                    double precio = mInput3.getNumericValue();

                    if(currSel1 == 0) {
                        crrArt.precund = precio;
                    }
                    if(currSel1 == 0) {
                        crrArt.precpq = precio;
                    }
                    else{
                        crrArt.preccj = precio;
                    }

                    crrArt.margen = mInput4.getNumericValue();
                    crrArt.totalcount = mInput5.getNumericValue();
                    crrArt.currcount = mInput5.getNumericValue();

                    crrArt.isopen = 0;
                    crrArt.metrica = currSel2;
                    crrArt.caduca = 0;

                    daoArt.update(crrArt);

                    glData.setCurrArt(crrArt);

                    finish();
                }
            });
        }
    }
}