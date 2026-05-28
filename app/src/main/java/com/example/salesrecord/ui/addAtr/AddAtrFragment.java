package com.example.salesrecord.ui.addAtr;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.CurrencyEditText;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.activitys.ReloadActivity;
import com.example.salesrecord.adapters.SelecAdapter;
import com.example.salesrecord.databinding.FragmentAddBinding;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.DatabaseUtils;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.utls.Basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddAtrFragment extends Fragment {

    private FragmentAddBinding binding;

    // DB
    private DaoArt daoArt;

    private List<EditText> mInpList =  new ArrayList<>();

    CurrencyEditText mInput1;

    private List<String> spinL1 = Arrays.asList("Unidad", "Paquete", "Caja", "No Empacables");
    private Spinner mSpin1;
    private int currSel1 = 0;

    private List<String> spinL2 = new ArrayList<>();
    private Spinner mSpin2;
    private int currSel2 = 0;

    private Button mBtn1;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentAddBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        spinL2 = glData.getUnitList();

        setViwes();

        //dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    private void setViwes(){
        mInpList.add(binding.etNombre);
        mInpList.add(binding.etDescr);

        //mInpList.add(binding.etPrecio);

        mInput1 = binding.etPrecio;

        mInpList.add(binding.etMargen);

        mInpList.add(binding.etTotalcount);
        mInpList.add(binding.etIsopen);
        mInpList.add(binding.etCaduca);

        mSpin1 = binding.select1;
        mSpin2 = binding.select2;

        mBtn1 = binding.btnAceptar;

        daoArt = StartVar.appDBall.daoAtr();


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

        //Para el selector de tipo de producto
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
                    boolean b = validateField(obj);
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



                objA = new Article(atrId, mTxList.get(0), mTxList.get(1),"@null", "",
                        (currSel1 == 0 ? (mInput1.getNumericValue()) : 0.0),
                        (currSel1 == 1 ? (mInput1.getNumericValue()) : 0.0),
                        (currSel1 == 2 ? (mInput1.getNumericValue()) : 0.0),

                        Double.parseDouble(mTxList.get(2)),
                        Float.parseFloat(mTxList.get(3)), Float.parseFloat(mTxList.get(3)),
                        Integer.parseInt(mTxList.get(4)), currSel1,
                        currSel2, Integer.parseInt(mTxList.get(5)),

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
            // Si el input tiene un tag, lo usa
            Object tag = input.getTag();
            if(tag != null) {
                String s = input.getText().toString().trim();
                if (s.isEmpty()) {

                    String msg = tag.toString();
                    input.setError(msg);
                    return false;
                }
                else if (getInputType(input) == 0 && Double.parseDouble(s.replaceAll("\\D","")) <= 0){
                    String msg = tag.toString();
                    input.setError(msg);
                    return false;
                }
            }

        return true;
    }

    public int getInputType(EditText input) {
        int type = input.getInputType();
        int inputClass = type & InputType.TYPE_MASK_CLASS;

        if (inputClass == InputType.TYPE_CLASS_NUMBER) {
            return 0;
        }

        if (inputClass == InputType.TYPE_CLASS_PHONE) {
            return 1;
        }

        if (inputClass == InputType.TYPE_CLASS_TEXT) {

            return 2;
        }
        return 3;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}