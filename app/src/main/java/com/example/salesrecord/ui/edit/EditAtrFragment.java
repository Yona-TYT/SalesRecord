package com.example.salesrecord.ui.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

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
import com.example.salesrecord.db.Conf;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.db.dao.DaoCfg;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.InputHelper;
import com.example.salesrecord.utls.MathUtls;
import com.example.salesrecord.utls.MoneyUtls;
import com.example.salesrecord.utls.Obj;
import com.example.salesrecord.utls.SharedViewModel;
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

    private SearchView searchBar;
    private String strValidate = "";

    private TextInputEditText mInput1;
    private CurrencyEditText mInput2;
    private CurrencyEditText mInput3;
    private CurrencyEditText mInput4;
    private CurrencyEditText glMarg;

    private TextInputLayout mTil1;
    private TextInputLayout mTil2;

    private SwitchMaterial mSw1;
    private SwitchMaterial mSw2;

    private boolean swCurrency = false;

    private Button editButt;
    private Button acepButt;

    private Article crrArt;
    private Conf mConf;
    private DaoCfg daoCfg;

    private Context contex;

    private SharedViewModel sharedViewModel;
    private boolean isMarg = false;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentEditBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        contex = AppContextProvider.getContext();

        mListView = binding.editViewList;

        searchBar = binding.searchBar;

        mInput1 = binding.etNombre;
        mInput2 = binding.etMonto;
        mInput3 = binding.etReponer;
        mInput4 = binding.etMargen;
        glMarg = binding.editGlmarg;

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

        if(crrArt != null) {

            objListSal.clear();

            mArtList = daoArt.getUsers();

            reloadList();

            if (!objListSal.isEmpty() && currSel1 < objListSal.size()) {

                // Mueve el scroll visualmente hacia el ítem
                mListView.setSelection(currSel1);

                mSw1.setChecked(true);

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
        reloadList();

        daoCfg = StartVar.appDBall.daoCfg();
        mConf = daoCfg.getUsers(StartVar.mConfID);

        if(mConf != null) {
            glMarg.setText(Basic.setFormatterEs(mConf.margen));
        }
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getMargToggle().observe(getViewLifecycleOwner(), visible -> {
            if (visible == null) return;

            // Quiere abrir la calculadora
            if (visible) {
                isMarg = true;
                binding.searchBar.setVisibility(View.GONE);
                binding.topPanel.setVisibility(View.VISIBLE);
                return;
            }

            // Quiere cerrar (visible == false)
            binding.searchBar.setVisibility(View.VISIBLE);
            binding.topPanel.setVisibility(View.GONE);
        });

        // Configuramos el listener para capturar el botón "Listo" / "Enter" del teclado
        glMarg.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Detecta el botón 'Listo' (actionDone) o la presión física de la tecla Enter
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {


                    // 3. Ocultamos el teclado de la pantalla
                    InputMethodManager imm = (InputMethodManager) glMarg.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(glMarg.getWindowToken(), 0);
                    }

                    mConf.margen = glMarg.getNumericValue();
                    daoCfg.insertUser(mConf);

                    //Encola al elemento a sincronizar
                    GlobalData.getInstance(getContext()).getGenericQueue().enqueue(mConf, 3);

                    return true;

                }
                return false; // Pasa el evento al sistema si no es la tecla Enter
            }
        });

        glMarg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    mConf.margen = glMarg.getNumericValue();
                    daoCfg.insertUser(mConf);

                    //Encola al elemento a sincronizar
                    GlobalData.getInstance(getContext()).getGenericQueue().enqueue(mConf, 3);
                }
            }
        });


        // 1. Listener de texto estándar corregido
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Ejecuta la búsqueda (incluso si viene vacía)
                if(query != null) {
                    strValidate = query.trim();
                }
                else {
                    strValidate = "";

                }
                reloadList();
                searchBar.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Si el usuario borra manualmente hasta dejarlo vacío,
                // forzamos la restauración inmediata de la lista completa
                if (newText == null || newText.trim().isEmpty()) {
                    strValidate = "";
                    reloadList();
                }
                return true;
            }
        });

        // 2. SOLUCIÓN AL PROBLEMA: Forzar la acción al pulsar el botón de la lupa (icono de envío)
        int searchButtonId = searchBar.getContext().getResources().getIdentifier("android:id/search_go_btn", null, null);
        View searchButton = searchBar.findViewById(searchButtonId);

        if (searchButton != null) {
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String query = searchBar.getQuery().toString();
                    // Forzamos la carga del catálogo con lo que tenga (cadena vacía o texto)
                    strValidate = query.trim();
                    reloadList();
                    searchBar.clearFocus();
                }
            });
        }
        //----------------------------------------------------

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

                mSw1.setChecked(false);
                swCurrency = false;
                mInput2.setCurrencySymbol("$");

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
                    double clcPrice = MathUtls.addPercentage(price, mInput4.getNumericValue()+mConf.margen);
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
                    double clcPrice = MathUtls.addPercentage(price, mInput4.getNumericValue()+mConf.margen);

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


                String nombre = InputHelper.cleanText(Objects.requireNonNull(mInput1.getText()).toString());

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
                GlobalData.getInstance(getContext()).getGenericQueue().enqueue(crrArt, 3);

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

                reloadList();
            }
        });
    }

    private void reloadList(){
        objListSal.clear();


        //Para la lista de Articulos ----------------------------
        //Para la lista de todos los productos
        for (Article obj : mArtList) {
            if(InputHelper.hasWordMatch(obj.nombre+obj.descr, strValidate) ){
                objListSal.add(setGalleryArray(obj));
            }
        }

        if(mAdapter1 != null) {
            mAdapter1.setSelectedPos(-1);
            mAdapter1.notifyDataSetChanged();
        }
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