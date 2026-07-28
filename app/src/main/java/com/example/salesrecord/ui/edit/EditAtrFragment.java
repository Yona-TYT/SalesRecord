package com.example.salesrecord.ui.edit;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.example.salesrecord.adapters.SummaryAdapter;
import com.example.salesrecord.databinding.FragmentEditBinding;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.MathUtls;
import com.example.salesrecord.utls.Obj;
import com.example.salesrecord.utls.StringsUtls;
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

    private SummaryAdapter mAdapter2;
    private ListView mListView;
    private List<Obj> objListSal = new ArrayList<>();

    private TextInputEditText mInput1;
    private CurrencyEditText mInput2;
    private CurrencyEditText mInput3;
    private CurrencyEditText mInput4;

    private TextInputLayout mTil1;
    private TextInputLayout mTil2;

    private Button acepButt;

    private Article crrArt;

    private Context contex;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

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

        acepButt = binding.buttHome2;

        setViwes();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setViwes() {
        objListSal.clear();
        daoArt = StartVar.appDBall.daoAtr();
        mArtList = daoArt.getUsers();

        //Para la lista de Articulos ----------------------------
        //Para la lista de todos los productos
        for (Article obj : mArtList) {
            objListSal.add(setGalleryArray(obj));
        }

        mAdapter2 = new SummaryAdapter(contex, objListSal);
        //-----------------------------------------------------
        mListView.setAdapter(mAdapter2);
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
                            mInput4.setText(Basic.setFormatterEn(crrArt.margen));
                        }

                        mInput1.setEnabled(true);
                        mInput2.setEnabled(true);
                        mInput3.setEnabled(true);
                        mInput4.setEnabled(true);




                    }
                }

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
                    double clcPrice = MathUtls.addPercentage(mInput2.getNumericValue(), mInput4.getNumericValue());

                    mTil2.setHint("(" + Basic.getMaskConv(clcPrice, 0) +"/" + Basic.getMaskConv(clcPrice, 1)+")");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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

                if(mType == 0){
                    crrArt.precund = mInput2.getNumericValue();
                }
                if(mType == 1){
                    crrArt.precpq = mInput2.getNumericValue();
                }

                if(mType == 2){
                    crrArt.preccj = mInput2.getNumericValue();
                }

                crrArt.currcount = crrArt.currcount + (float)mInput3.getNumericValue();
                crrArt.totalcount = crrArt.currcount;

                crrArt.margen = mInput4.getNumericValue();

                daoArt.update(crrArt);

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


                crrArt = null;

                mArtList = daoArt.getUsers();

                objListSal.clear();

                //Para la lista de Articulos ----------------------------
                //Para la lista de todos los productos
                for (Article obj : mArtList) {
                    objListSal.add(setGalleryArray(obj));
                }
                mAdapter2.notifyDataSetChanged();
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
                art.currcount, art.totalcount, 0, mPrice, art.margen
                , art.uid);

        return mObj;

    }
}