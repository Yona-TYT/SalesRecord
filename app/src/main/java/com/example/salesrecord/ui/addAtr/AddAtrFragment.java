package com.example.salesrecord.ui.addAtr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.CurrencyEditText;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.Launcher;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.activitys.ReloadActivity;
import com.example.salesrecord.adapters.SelecAdapter;
import com.example.salesrecord.databinding.FragmentAddBinding;
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

public class AddAtrFragment extends Fragment {

    private FragmentAddBinding binding;

    // DB
    private DaoArt daoArt;

    private List<EditText> mInpList =  new ArrayList<>();

    private CurrencyEditText mInput1;
    private CurrencyEditText mInput2;
    private CurrencyEditText mInput3;

    private TextView viewTotal;

    private ImageButton mImgButt;
    private ImageView imageView;

    private SwitchCompat mSw1;
    private boolean swCurrency = false;

    private List<String> spinL1 = new ArrayList<>();
    private Spinner mSpin1;
    private int currSel1 = 0;

    private List<String> spinL2 = new ArrayList<>();
    private Spinner mSpin2;
    private int currSel2 = 0;

    private Button mBtn1;

    private FilesManager mFileM = new FilesManager();
    private String sImage = "";
    private Uri oldFile = null;
    private Uri currUri = null;

    private Context contex;
    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAddBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        contex = AppContextProvider.getContext();

        spinL1 = glData.categ;
        spinL2 = glData.unitList;

        setViwes();

        //dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void setViwes(){

        if (StartVar.appDBall == null) {
            //Satrted variables
            StartVar startVar = new StartVar();
            StartVar.setAllListDB();
        }

        mInpList.add(binding.etNombre);
        mInpList.add(binding.etDescr);

        //mInpList.add(binding.etPrecio);

        mInput1 = binding.etPrecio;
        mInput2 = binding.etMargen;
        mInput3 = binding.etTotalcount;

        viewTotal = binding.addTotal;

        mSw1 = binding.addSwBs;

        mInpList.add(binding.etMargen);

        mInpList.add(binding.etTotalcount);
        mInpList.add(binding.etIsopen);
        mInpList.add(binding.etCaduca);

        mSpin1 = binding.select1;
        mSpin2 = binding.select2;

        mBtn1 = binding.btnAceptar;

        mImgButt = binding.addBtnImage;
        imageView = binding.addImgPreview;

        daoArt = StartVar.appDBall.daoAtr();

        //Set Picker and Camera Launchers
        Launcher mLaunch = new Launcher(getActivity().getActivityResultRegistry(), getActivity().getApplicationContext(), new Launcher.OnCapture() {            @Override
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

        mSw1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swCurrency = !swCurrency;
                if(swCurrency) {
                    mInput1.setCurrencySymbol("Bs");
                    mInput1.setText(MoneyUtls.getMaskConv(mInput1.getNumericValue(), 1, false));
                }
                else{
                    mInput1.setCurrencySymbol("$");
                    mInput1.setText(MoneyUtls.getMaskConv(mInput1.getNumericValue(), 0, false));
                }
            }
        });

        mInput1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                double price = MoneyUtls.getInDollar(mInput1.getNumericValue(), StartVar.mDollar, swCurrency?1:0);
                double clcPrice = MathUtls.addPercentage(price, mInput2.getNumericValue());
                viewTotal.setText("(" + Basic.getMaskConv(clcPrice, 0) +"/" + Basic.getMaskConv(clcPrice, 1)+")");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mInput2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                double price = MoneyUtls.getInDollar(mInput1.getNumericValue(), StartVar.mDollar, swCurrency?1:0);
                double clcPrice = MathUtls.addPercentage(price, mInput2.getNumericValue());
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


        //Para el selector de tipo metrica
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
                    if (t.isEmpty()){
                        mTxList.add("0");
                    }
                    else {
                        mTxList.add(t);
                    }
                }

                //Si la validacion falla, isOk es false y terminar el linsterner
                if (!isOk){
                    return;
                }

                long currDate = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    currDate = java.time.Instant.now().toEpochMilli();
                }

                Article objA = null;
                String atrId = DatabaseUtils.generateId("atrID", daoArt);

                //Se guarda la foto en un nuevo directorio --------------------------------
                Bitmap bitmap = null;
                try {
                    // Nueva imagen elegida con el picker
                    if (currUri != null) {
                        bitmap = MediaStore.Images.Media.getBitmap(contex.getContentResolver(), currUri);
                        sImage = mFileM.SavePhoto(bitmap, atrId);
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

                double price = MoneyUtls.getInDollar(mInput1.getNumericValue(), StartVar.mDollar, swCurrency?1:0);

                objA = new Article(atrId, InputHelper.cleanText(mTxList.get(0)), InputHelper.cleanText(mTxList.get(1)),"@null", sImage,
                        (currSel1 == 0 ? (price) : 0.0),
                        (currSel1 == 1 ? (price) : 0.0),
                        (currSel1 == 2 ? (price) : 0.0),

                        mInput2.getNumericValue(),
                        mInput3.getNumericValue(), mInput3.getNumericValue(),
                        Integer.parseInt(mTxList.get(4)), currSel1,
                        currSel2, Integer.parseInt(mTxList.get(5)),

                        1, currDate, currDate
                );

                glData.setIsEdit(true);

                daoArt.insert(objA);

                //Encola al elemento a sincronizar
                GlobalData.getInstance(getContext()).getGenericQueue().enqueue(objA, 3);

                //Esto inicia las actividad Reload
                Intent mIntent = new Intent(AppContextProvider.getContext(), ReloadActivity.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mIntent);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}