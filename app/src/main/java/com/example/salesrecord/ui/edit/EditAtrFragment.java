package com.example.salesrecord.ui.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.CurrencyEditText;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.adapters.SaleAdapter;
import com.example.salesrecord.databinding.FragmentEditBinding;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.Obj;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class EditAtrFragment extends Fragment {

    private FragmentEditBinding binding;

    // DB
    private DaoArt daoArt;
    private List<Article> mArtList =  new ArrayList<>();

    private SaleAdapter mAdapter2;
    private ListView mListView;
    private List<Obj> objListSal = new ArrayList<>();

    private TextInputEditText mInput1;
    private CurrencyEditText mInput2;
    private TextInputEditText mInput3;
    private TextInputEditText mInput4;

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

        mAdapter2 = new SaleAdapter(contex, objListSal);
        //-----------------------------------------------------
        mListView.setAdapter(mAdapter2);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Obj item = (Obj) parent.getAdapter().getItem(position);

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

                        mInput1.setText(crrArt.nombre);
                        mInput2.setText(Basic.setFormatterEs(precio));
                        mInput3.setText(String.valueOf(crrArt.currcount));
                        mInput4.setText(String.valueOf(crrArt.margen));

                    }
                }

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
                art.currcount, art.totalcount, 0, mPrice, art.uid);

        return mObj;

    }
}