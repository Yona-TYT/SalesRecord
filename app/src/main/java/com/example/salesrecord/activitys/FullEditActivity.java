package com.example.salesrecord.activitys;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.CurrencyEditText;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.Launcher;
import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.adapters.SelecAdapter;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.DatabaseUtils;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.FilesManager;
import com.example.salesrecord.utls.InputHelper;
import com.example.salesrecord.utls.MathUtls;
import com.example.salesrecord.utls.MoneyUtls;
import com.example.salesrecord.utls.Msg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FullEditActivity extends AppCompatActivity {

    // DB
    private DaoArt daoArt;
    private Article crrArt;
    private Article oldArt;

    private EditText mInput1;
    private EditText mInput2;
    private CurrencyEditText mInput3;
    private CurrencyEditText mInput4;
    private CurrencyEditText mInput5;
    private EditText mInput6;
    private EditText mInput7;

    private TextView viewTotal;

    private SwitchCompat mSw1;
    private boolean swCurrency = false;

    private ImageButton mImgButt;
    private ImageView imageView;

    private List<EditText> mInpList =  new ArrayList<>();

    private List<String> spinL1 = new ArrayList<>();
    private Spinner mSpin1;
    private int currSel1 = 0;

    private List<String> spinL2 = new ArrayList<>();
    private Spinner mSpin2;
    private int currSel2 = 0;

    private Button mBtn1;

    private Context contex;

    private FilesManager mFileM = new FilesManager();
    private String sImage = "";
    private Uri oldFile = null;
    private Uri currUri = null;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contex = this;
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
            StartVar.setAllListDB();
        }

        daoArt = StartVar.appDBall.daoAtr();

        mInput1 = findViewById(R.id.full_et_nombre);
        mInput2 = findViewById(R.id.full_et_descr);
        mInput3 = findViewById(R.id.full_et_precio);
        mInput4 = findViewById(R.id.full_et_margen);
        mInput5 = findViewById(R.id.full_et_totalcount);
        mInput6 = findViewById(R.id.full_et_isopen);
        mInput7 = findViewById(R.id.full_et_caduca);

        viewTotal = findViewById(R.id.edit_total);

        mSw1 = findViewById(R.id.edit_sw_bs);

        mImgButt = findViewById(R.id.full_btn_image);
        imageView = findViewById(R.id.full_img_preview);

        mInpList.add(mInput1);
        mInpList.add(mInput3);
        mInpList.add(mInput5);


        mSpin1 = findViewById(R.id.full_select1);
        mSpin2 = findViewById(R.id.full_select2);

        mBtn1 = findViewById(R.id.full_btn_1);

        crrArt = glData.getCurrArt();
        oldArt = new Article(crrArt);

        spinL1 = glData.categ;
        spinL2 = glData.unitList;

        if(crrArt != null){

            FilesManager.setImageView(crrArt.image, imageView);

            //Set Picker and Camera Launchers
            Launcher mLaunch = new Launcher(this.getActivityResultRegistry(), this.getApplicationContext(), new Launcher.OnCapture() {
                @Override
                public void invoke(List<Uri> uris) {
                    if (!uris.isEmpty()) {
                        Uri uri = uris.get(0);
                        try {
                            Log.d("PhotoPicker", "Selected URI: " + uri);
                            if(imageView != null){
                                imageView.setImageURI(uri);
                            }
                            currUri = uri;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Msg.m("No hay imagen seleccionada!");
                    }
                }
            });

            getLifecycle().addObserver(mLaunch);

            // Adjunta al botón para el picker
            mLaunch.attachToViewPicker(mImgButt, false, false);

            // Adjunta al botón para la camara
            mLaunch.attachToViewCam(mImgButt, true);

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

            mSw1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    swCurrency = !swCurrency;
                    if(swCurrency) {
                        mInput3.setCurrencySymbol("Bs");
                        mInput3.setText(MoneyUtls.getMaskConv(mInput3.getNumericValue(), 1, false));
                    }
                    else{
                        mInput3.setCurrencySymbol("$");
                        mInput3.setText(MoneyUtls.getMaskConv(mInput3.getNumericValue(), 0, false));
                    }
                }
            });

            mInput3.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    double price = MoneyUtls.getInDollar(mInput3.getNumericValue(), StartVar.mDollar, swCurrency?1:0);
                    double clcPrice = MathUtls.addPercentage(price, mInput4.getNumericValue());
                    viewTotal.setText("(" + Basic.getMaskConv(clcPrice, 0) +"/" + Basic.getMaskConv(clcPrice, 1)+")");
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            mInput4.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    double price = MoneyUtls.getInDollar(mInput3.getNumericValue(), StartVar.mDollar, swCurrency?1:0);
                    double clcPrice = MathUtls.addPercentage(price, mInput4.getNumericValue());
                    viewTotal.setText("(" + Basic.getMaskConv(clcPrice, 0) +"/" + Basic.getMaskConv(clcPrice, 1)+")");
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


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
            mSpin2.setSelection(crrArt.metrica);
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

                    crrArt.nombre = InputHelper.cleanText(mInput1.getText().toString());
                    crrArt.descr = InputHelper.cleanText(mInput2.getText().toString());

                    double price = MoneyUtls.getInDollar(mInput3.getNumericValue(), StartVar.mDollar, swCurrency?1:0);

                    if(currSel1 == 0) {
                        crrArt.precund = price;
                    }
                    if(currSel1 == 0) {
                        crrArt.precpq = price;
                    }
                    else{
                        crrArt.preccj = price;
                    }

                    crrArt.margen = mInput4.getNumericValue();
                    crrArt.totalcount = mInput5.getNumericValue();
                    crrArt.currcount = mInput5.getNumericValue();

                    crrArt.isopen = 0;
                    crrArt.metrica = currSel2;
                    crrArt.caduca = 0;

                    //Se guarda la foto en un nuevo directorio --------------------------------
                    Bitmap bitmap = null;
                    try {
                        // Nueva imagen elegida con el picker
                        if (currUri != null) {
                            bitmap = MediaStore.Images.Media.getBitmap(contex.getContentResolver(), currUri);
                            sImage = mFileM.SavePhoto(bitmap, crrArt.article);
                        } else if (sImage != null && !sImage.isEmpty()) {
                            // Se mantiene la imagen anterior
                            oldFile = Uri.parse(sImage);
                        } else {
                            sImage = "";
                        }
                    } catch (IOException e) {
                        Msg.m("Error al guardar la IMAGEN!");
                        e.printStackTrace();
                        sImage = "";
                    }
                    if (!sImage.isEmpty()) {
                        crrArt.image = sImage;
                    }
                    //-------------------------------------------------------------------

                    Log.d("DB_INSTANCE", "Hash: " + System.identityHashCode(StartVar.appDBall));

                    glData.setCurrArt(crrArt);
                    glData.setIsEdit(true);
                    GlobalData.shouldReload = false;
                    // Evitar encolar si no hubo cambio
                    if (DatabaseUtils.isIdentical(crrArt, oldArt)) {
                        //Msg.d("Art", "Sin cambios en datos");
                        finish();
                        return;
                    }

                    daoArt.update(crrArt);

                    //Basic.msg(daoArt.getUsers(crrArt.uid).nombre, true);

                    //Encola al elemento a sincronizar
                    GlobalData.getInstance(contex).getGenericQueue().enqueue(crrArt, 3);

                    finish();
                }
            });
        }
    }
}