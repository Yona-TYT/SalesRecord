package com.example.salesrecord.ui.addAtr;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.activitys.ReloadActivity;
import com.example.salesrecord.databinding.FragmentDashboardBinding;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.DatabaseUtils;
import com.example.salesrecord.db.dao.DaoArt;

import java.util.ArrayList;
import java.util.List;

public class AddAtrFragment extends Fragment {

    private FragmentDashboardBinding binding;

    // DB
    private DaoArt daoArt;

    private List<EditText> mInpList =  new ArrayList<>();

    private Button mBtn1;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setViwes();

        //dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    private void setViwes(){
        mInpList.add(binding.etNombre);
        mInpList.add(binding.etDescr);

        mInpList.add(binding.etPrecund);
        mInpList.add(binding.etPreccj);
        mInpList.add(binding.etPrecpq);
        mInpList.add(binding.etMargen);

        mInpList.add(binding.etTotalcount);
        mInpList.add(binding.etIsopen);
        mInpList.add(binding.etArtipo);
        mInpList.add(binding.etMetrica);
        mInpList.add(binding.etCaduca);

        mBtn1 = binding.btnAceptar;

        daoArt = StartVar.appDBall.daoAtr();

        mBtn1.setOnClickListener(new View.OnClickListener() {
            List<String> mTxList =  new ArrayList<>();

            @Override
            public void onClick(View v) {
                boolean isOk = true;
                for (EditText obj : mInpList){
                    String t = obj.getText().toString();
                    boolean b = validateField(obj);
                    if(isOk) {
                        isOk = b;
                    }
                    mTxList.add(t.isEmpty() ? "0":t);
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
                objA = new Article(atrId, mTxList.get(0), mTxList.get(1),"@null", "",
                        Double.parseDouble(mTxList.get(2)), Double.parseDouble(mTxList.get(3)),
                        Double.parseDouble(mTxList.get(4)), Double.parseDouble(mTxList.get(5)),

                        Integer.parseInt(mTxList.get(6)), Integer.parseInt(mTxList.get(6)),
                        Integer.parseInt(mTxList.get(7)), Integer.parseInt(mTxList.get(8)),
                        Integer.parseInt(mTxList.get(9)), Integer.parseInt(mTxList.get(10)),

                        0, currDate, currDate
                );
                daoArt.insert(objA);
                //Esto inicia las actividad Reload
                Intent mIntent = new Intent(AppContextProvider.getContext(), ReloadActivity.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mIntent);
            }
        });

    }

    public boolean validateField(EditText input) {
        if (input.getText().toString().trim().isEmpty()) {
            // Si el input tiene un tag, lo usa
            Object tag = input.getTag();
            if(tag != null) {
                String msg = tag.toString();
                input.setError(msg);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}