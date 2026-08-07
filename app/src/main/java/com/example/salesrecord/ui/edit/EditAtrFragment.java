package com.example.salesrecord.ui.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.CurrencyEditText;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.activitys.FullEditActivity;
import com.example.salesrecord.adapters.SummaryAdapter;
import com.example.salesrecord.databinding.FragmentEditBinding;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.MathUtls;
import com.example.salesrecord.utls.MoneyUtls;
import com.example.salesrecord.utls.Obj;
import com.example.salesrecord.utls.StringsUtls;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditAtrFragment extends Fragment {

    private FragmentEditBinding binding;

    // DB
    private DaoArt daoArt;
    private List<Article> mArtList =  new ArrayList<>();

    private SummaryAdapter mAdapter1;
    private ListView mListView;
    private List<Obj> objListSal = new ArrayList<>();
    private int currSel1 = 0;

    private TextInputEditText mInput1;
    private CurrencyEditText mInput2;
    private CurrencyEditText mInput3;
    private CurrencyEditText mInput4;

    private TextInputLayout mTil1;
    private TextInputLayout mTil2;

    private SwitchMaterial mSw1;
    private SwitchMaterial mSw2;

    private boolean swCurrency = false;

    private Button editButt;
    private Button acepButt;

    private Article crrArt;

    private Context contex;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentEditBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        contex = AppContextProvider.getContext();

        mListView = binding.editViewList;

        mInput1 = binding.etNombre;
        mInput2 = binding.etMonto;
        mInput3 = binding.etReponer;
        mInput4 = binding.etMargen;

        mTil1 = binding.tilReponer;
        mTil2 = binding.tilMargen;

        mSw1 = binding.swBolivares;
        mSw2 = binding.swDisponible;

        editButt = binding.buttHome3;
        acepButt = binding.buttHome2;

        setViwes();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        swCurrency = false;
        mSw1.setChecked(false);

        if(crrArt != null) {

            objListSal.clear();

            mArtList = daoArt.getUsers();

            for (Article obj : mArtList) {
                objListSal.add(setGalleryArray(obj));
            }
            mAdapter1.notifyDataSetChanged();

            if (!objListSal.isEmpty() && currSel1 < objListSal.size()) {

                // Mueve el scroll visualmente hacia el ítem
                mListView.setSelection(currSel1);

                // SIMULA EL CLIC: Ejecuta el código dentro de tu onItemClick
                mListView.performItemClick(
                        mListView.getAdapter().getView(currSel1, null, mListView),  // La vista del ítem
                        currSel1,                                                              // La posición del ítem
                        mListView.getAdapter().getItemId(currSel1)                             // El ID único del ítem
                );
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setViwes() {
        objListSal.clear();

        if (StartVar.appDBall == null) {
            //Satrted variables
            StartVar startVar = new StartVar();
            StartVar.setAllListDB();
        }

        daoArt = StartVar.appDBall.daoAtr();
        mArtList = daoArt.getUsers();

        //Para la lista de Articulos ----------------------------
        //Para la lista de todos los productos
        for (Article obj : mArtList) {
            objListSal.add(setGalleryArray(obj));
        }

        mAdapter1 = new SummaryAdapter(contex, objListSal);
        //-----------------------------------------------------
        mListView.setAdapter(mAdapter1);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Obj item = (Obj) parent.getAdapter().getItem(position);

                mInput1.setText("");
                mInput2.setText("");
                mInput3.setText("");
                mInput4.setText("");

                mTil1.setHint("Reponer");
                mTil2.setHint("Ganancia");

                currSel1 = position;

                mAdapter1.setSelectedPos(position);

                if (item != null){
                    crrArt = daoArt.getUsers(item.id);

                    if (crrArt != null){

                        Double precio = crrArt.precund;
                        int mType = crrArt.artipo;
                        if(mType == 1){
                            precio = crrArt.precpq;
                        }

                        if(mType == 2){
                            precio = crrArt.preccj;
                        }

                        mInput1.setText(StringsUtls.capitalize(crrArt.nombre));
                        mInput2.setText(Basic.setFormatterEs(precio));
                        //mInput3.setText(Basic.setFormatterEn((double)crrArt.currcount) );
                        if(crrArt.margen > 0) {
                            mInput4.setText(Basic.setFormatterEs(crrArt.margen));
                        }

                        mInput1.setEnabled(true);
                        mInput2.setEnabled(true);
                        mInput3.setEnabled(true);
                        mInput4.setEnabled(true);

                        mSw1.setEnabled(true);
                        mSw2.setEnabled(true);
                        editButt.setEnabled(true);
                        acepButt.setEnabled(true);

                        mSw2.setChecked(crrArt.staus != 0);

                        //Termina el proceso
                        return;
                    }
                }
                mSw1.setChecked(false);
                mSw1.setEnabled(false);
                mSw2.setChecked(false);
                mSw2.setEnabled(false);
                editButt.setEnabled(false);

            }
        });

        mInput2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(crrArt != null) {
                    double price = MoneyUtls.getInDollar(mInput2.getNumericValue(), StartVar.mDollar, swCurrency?1:0);
                    double clcPrice = MathUtls.addPercentage(price, mInput4.getNumericValue());
                    mTil2.setHint("(" + Basic.getMaskConv(clcPrice, 0) +"/" + Basic.getMaskConv(clcPrice, 1)+")");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mInput3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(crrArt != null) {
                    mTil1.setHint("Total: " + Basic.formatDecimal(mInput3.getNumericValue()+crrArt.totalcount));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mInput4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(crrArt != null) {
                    double price = MoneyUtls.getInDollar(mInput2.getNumericValue(), StartVar.mDollar, swCurrency?1:0);
                    double clcPrice = MathUtls.addPercentage(price, mInput4.getNumericValue());

                    mTil2.setHint("(" + Basic.getMaskConv(clcPrice, 0) +"/" + Basic.getMaskConv(clcPrice, 1)+")");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSw1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swCurrency = !swCurrency;
                if(swCurrency) {
                    mInput2.setCurrencySymbol("Bs");
                    mInput2.setText(MoneyUtls.getMaskConv(mInput2.getNumericValue(), 1, false));
                }
                else{
                    mInput2.setCurrencySymbol("$");
                    mInput2.setText(MoneyUtls.getMaskConv(mInput2.getNumericValue(), 0, false));
                }
            }
        });

        editButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(crrArt == null){
                    Basic.msg("Seleccione un producto primero!");
                    return;
                }
                else {
                    glData.setCurrArt(crrArt);

                    Intent intent = new Intent(contex, FullEditActivity.class);
                    startActivity(intent);
                }
            }
        });

        acepButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(crrArt == null){
                    Basic.msg("Seleccione un producto primero!");
                    return;
                }


                String nombre = Objects.requireNonNull(mInput1.getText()).toString();

                if(nombre.isEmpty()){
                    Basic.msg("Nombre no debe estar VACIO!");
                    return;
                }

                crrArt.nombre = nombre;

                int mType = crrArt.artipo;
                double price = MoneyUtls.getInDollar(mInput2.getNumericValue(), StartVar.mDollar, swCurrency?1:0);

                if(mType == 0){
                    crrArt.precund = price;
                }
                if(mType == 1){
                    crrArt.precpq = price;
                }

                if(mType == 2){
                    crrArt.preccj = price;
                }

                crrArt.currcount = crrArt.currcount + mInput3.getNumericValue();
                crrArt.totalcount = crrArt.currcount;

                crrArt.margen = mInput4.getNumericValue();

                crrArt.staus = (mSw2.isChecked() ? 1 : 0);

                daoArt.update(crrArt);

                //Encola al elemento a sincronizar
                StartVar.genericQueue.enqueue(crrArt, 3);

                //Limpia y Desactiva los inputs
                mInput1.setText("");
                mInput2.setText("");
                mInput3.setText("");
                mInput4.setText("");

                mTil1.setHint("Reponer");
                mTil2.setHint("Ganancia");

                mInput1.setEnabled(false);
                mInput2.setEnabled(false);
                mInput3.setEnabled(false);
                mInput4.setEnabled(false);

                mSw1.setChecked(false);
                mSw1.setEnabled(false);
                mSw2.setChecked(false);
                mSw2.setEnabled(false);
                editButt.setEnabled(false);
                acepButt.setEnabled(false);

                mInput2.setCurrencySymbol("$");

                swCurrency = false;
                crrArt = null;

                mArtList = daoArt.getUsers();

                objListSal.clear();

                mAdapter1.setSelectedPos(-1);

                //Para la lista de Articulos ----------------------------
                //Para la lista de todos los productos
                for (Article obj : mArtList) {
                    objListSal.add(setGalleryArray(obj));
                }
                mAdapter1.notifyDataSetChanged();
            }
        });
    }

    private Obj setGalleryArray(Article art){
        double mPrice;
        int type = art.artipo;
        if(type == 0) {
            mPrice = art.precund;
        }
        else if(type == 1){
            mPrice = art.precpq;
        }
        else {
            mPrice = art.preccj;
        }
        Obj mObj = new Obj(art.article, art.nombre, art.descr, art.image, 0, art.metrica,
                art.staus, art.currcount, art.totalcount, 0, mPrice, art.margen
                , art.uid);

        return mObj;

    }
}