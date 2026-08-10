package com.example.salesrecord.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.CurrencyEditText;
import com.example.salesrecord.GetDollar;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.activitys.ReloadActivity;
import com.example.salesrecord.adapters.SaleMainAdapter;
import com.example.salesrecord.adapters.SaleResultAdapter;
import com.example.salesrecord.adapters.SelecAdapter;
import com.example.salesrecord.databinding.FragmentHomeBinding;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.db.Conf;
import com.example.salesrecord.db.DatabaseUtils;
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.db.dao.DaoClt;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.CalendUtls;
import com.example.salesrecord.utls.InputHelper;
import com.example.salesrecord.utls.MathUtls;
import com.example.salesrecord.utls.MoneyUtls;
import com.example.salesrecord.utls.Msg;
import com.example.salesrecord.utls.Obj;
import com.example.salesrecord.R;
import com.example.salesrecord.utls.SharedViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    // DB
    private DaoSal daoSal;
    private DaoArt daoArt;
    private Conf mConf;
    private List<Article> mArtList =  new ArrayList<>();

    private CurrencyEditText mInput1;

    private SearchView searchBar;
    private GridView gridView;
    private TextView viewTotal;

    private SaleMainAdapter mAdapter1;
    private SaleResultAdapter mAdapter2;
    private ListView mListView;

    private Button mButt1;
    private Button mButt2;
    private Button mButt3;
    private Button mButt4;

    private Spinner mSpinn1;
    private int currSel1 = 0;

    private CurrencyEditText mInput2;
    private int currGrid = 0;
    private Obj currObj = null;
    private TextView viewResult;
    private SwitchCompat mSw1;
    private boolean swCurrency = false;
    private boolean isCalc = false;
    private double calcCount = 0;
    private SharedViewModel sharedViewModel;

    private boolean isSrch = false;

    private EditText mInput3;

    // Almacenamiento real (3 cajas independientes)
    private final ArrayList<Obj>[] allSlots = new ArrayList[3];
    private final ArrayList<Obj>[] salSlots = new ArrayList[3];

    // Índice activo: 0, 1 o 2
    private int currSlot = 0;

    // Referencias de trabajo (apuntan al slot activo)
    private ArrayList<Obj> objListAll = new ArrayList<>();
    private ArrayList<Obj> objListSal = new ArrayList<>();

    private Context contex;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    private Sale crrSale;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("curr_slot", currSlot);

        for (int i = 0; i < 3; i++) {
            if (allSlots[i] != null) {
                outState.putParcelableArrayList("all_obj_" + i, allSlots[i]);
            }
            if (salSlots[i] != null) {
                outState.putParcelableArrayList("sal_obj_" + i, salSlots[i]);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        contex = AppContextProvider.getContext();

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicialización de tus componentes de la interfaz
        mInput1 = binding.inputHome1;
        gridView = binding.gcImg;
        mListView = binding.viewList;
        viewTotal = binding.homeText2;
        viewResult = binding.calcResult;
        searchBar = binding.searchBar;
        mButt1 = binding.buttHome1;
        mButt2 = binding.buttHome2;
        mButt3 = binding.buttHome3;
        mButt4 = binding.buttHome4;
        mInput2 = binding.calcInput;
        mInput3 = binding.inputClient;
        mSpinn1 = binding.homeSelect1;
        mSw1 = binding.calcSwBs;

        // =========================================================================
        // RECOLECCIÓN DE DATOS RESPALDADOS (savedInstanceState)
        // =========================================================================

        Msg.init(contex);
        if (savedInstanceState != null) {
            initSlots(); // listas vacías base

            currSlot = savedInstanceState.getInt("curr_slot", 0);
            if (currSlot < 0 || currSlot > 2) currSlot = 0;

            try {
                for (int i = 0; i < 3; i++) {
                    ArrayList<Obj> all = savedInstanceState.getParcelableArrayList("all_obj_" + i);
                    ArrayList<Obj> sal = savedInstanceState.getParcelableArrayList("sal_obj_" + i);
                    allSlots[i] = all != null ? all : new ArrayList<>();
                    salSlots[i] = sal != null ? sal : new ArrayList<>();

                    // Referencias compartidas dentro de cada slot
                    for (int s = 0; s < salSlots[i].size(); s++) {
                        Obj saleObj = salSlots[i].get(s);
                        for (int a = 0; a < allSlots[i].size(); a++) {
                            if (Objects.equals(allSlots[i].get(a).id, saleObj.id)) {
                                salSlots[i].set(s, allSlots[i].get(a));
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("FragmentHome", "Error restaurando slots", e);
                initSlots();
            }

            bindWorkingLists();
            setViwes(true);
        }
        else {
            // SI NO HAY RESPALDO: Es la primera vez que se abre la pantalla, buscamos el dólar en internet
            AppContextProvider.runWithSafeActivity(new AppContextProvider.SafeActivityRunnable() {
                @Override
                public void onActivityReady(Activity activity) {
                    GetDollar mGet = new GetDollar(getActivity(), 0, mInput1);
                    try {
                        GetDollar.urlRun();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            initSlots();
            setViwes(false);
        }

        // homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SuppressLint("SetTextI18n")
    private void setViwes(boolean isSave){

        if (StartVar.appDBall == null) {
            //Satrted variables
            StartVar startVar = new StartVar();
            StartVar.setAllListDB();
        }

        daoSal = StartVar.appDBall.daoSal();
        daoArt = StartVar.appDBall.daoAtr();
        mArtList = daoArt.getUsers();
        mConf = StartVar.appDBall.daoCfg().getUsers(StartVar.mConfID);


        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getCalcToggle().observe(getViewLifecycleOwner(), visible -> {
            if (visible == null) return;

            // Quiere abrir la calculadora
            if (visible) {
                if (currObj == null) {
                    Basic.msg("Debe seleccionar un producto primero!.");
                    // solo cerrar si estaba en true
                    sharedViewModel.setCalcVisible(false);
                    return;
                }

                isCalc = true;
                binding.bottomPanel.setVisibility(View.GONE);
                binding.panelCalc.setVisibility(View.VISIBLE);
                // opcional: rellenar nombre del producto
                // binding.calcProductName.setText(currObj.name);
                return;
            }

            // Quiere cerrar (visible == false)
            isCalc = false;
            binding.panelCalc.setVisibility(View.GONE);
            binding.bottomPanel.setVisibility(View.VISIBLE);
        });

        sharedViewModel.getSrchToggle().observe(getViewLifecycleOwner(), visible -> {
            if (visible == null) return;

            if (visible) {
                isSrch = true;
                binding.topPanel.setVisibility(View.GONE);
                searchBar.setVisibility(View.VISIBLE);

                // 1. Desplegar el SearchView (si estuviera colapsado por un icono)
                searchBar.setIconified(false);

                // 2. Esperar a que la interfaz se dibuje para solicitar el foco y el teclado
                searchBar.post(() -> {
                    searchBar.requestFocus();

                    // 3. Forzar la apertura del teclado virtual
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        // Buscamos el elemento interno del SearchView que recibe el texto
                        int editTextId = searchBar.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
                        View searchEditText = searchBar.findViewById(editTextId);

                        if (searchEditText != null) {
                            searchEditText.requestFocus();
                            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
                        } else {
                            // Respaldo por si no encuentra el ID interno en alguna capa de personalización
                            imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                });
                return;
            }

            // Quiere cerrar (visible == false)
            isSrch = false;
            searchBar.clearFocus(); // Oculta el foco al cerrar
            searchBar.setVisibility(View.GONE);
            binding.topPanel.setVisibility(View.VISIBLE);

            // Opcional: Ocultar el teclado explícitamente al cerrar la barra
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
            }
        });


        // Boton Recargar
        mButt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetDollar mGet = new GetDollar(getActivity(), 0, mInput1);
                try {
                    GetDollar.urlRun();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        mInput1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s != null && !s.toString().isEmpty() && StartVar.mDollar > 0){
                    refreshSaleListUI();
                    if (mAdapter1 != null) {
                        mAdapter1.notifyDataSetChanged();
                    }

                }
            }
        });

        // Para el input de la calculadora
        mInput2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(currObj != null) {
                    if (currObj.maxCount > 0) {
                        mInput2.setError(null);
                        double price = MathUtls.addPercentage(currObj.price, currObj.margen+mConf.margen);
                        double cueePrice = MoneyUtls.getInDollar(mInput2.getNumericValue(), StartVar.mDollar, swCurrency?1:0);

                        double quant = MoneyUtls.getQuantity(price, cueePrice);
                        calcCount = quant;
                        viewResult.setText("Cantidad: " + MoneyUtls.setFormatterEs(quant) + " / " +MoneyUtls.setFormatterEs(currObj.maxCount)+" "+glData.unitList.get(currObj.unit));
                        if(quant <= currObj.maxCount){
                            mButt2.setEnabled(true);
                        }
                        else {
                            mInput2.setError("El Monto es mayor a la cantidad MAXIMA DISPONIBLE!");
                            mButt2.setEnabled(false);
                        }
                    }
                    else {
                        mInput2.setError("Producto AGOTADO!.");
                        mButt2.setEnabled(false);
                    }
                }
                else {
                    mInput2.setError("Debe seleccionar un Producto primero !.");
                    mButt2.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Configuramos el listener para capturar el botón "Listo" / "Enter" del teclado
        mInput2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Detecta el botón 'Listo' (actionDone) o la presión física de la tecla Enter
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {


                    // 3. Ocultamos el teclado de la pantalla
                    InputMethodManager imm = (InputMethodManager) mInput2.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(mInput2.getWindowToken(), 0);
                    }

                    setCalcResult();

                    return true;

                }
                return false; // Pasa el evento al sistema si no es la tecla Enter
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

        // Para el boton de procesar la calculadora
        mButt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCalcResult();
            }
        });

        // Para el boton de limpiar la lista
        mButt3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                objListAll.clear();
                objListSal.clear();

                //Para la lista de todos los productos

                mArtList = daoArt.getUsers();    //Se actualiza la lista de articulos
                loadCatalogIntoSlot(currSlot);
                bindWorkingLists();
                
                if (mAdapter1 != null && mAdapter2 != null) {

                    Double total = setTotal(objListSal);
                    viewTotal.setText("Total: " + Basic.getMaskConv(total, 0) +" / "+Basic.getMaskConv(total, 1));

                    mAdapter1.setSelectedPos(-1);

                    mAdapter1.notifyDataSetChanged();
                    mAdapter2.notifyDataSetChanged();
                }
                else {
                    Basic.msg("Aqui no hay aqui no hay !: "+mAdapter1 +" : "+ mAdapter2, true);
                }

                currObj = null;
                return false;
            }
        });
        mButt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Basic.msg("Mantegan precionado para limpiar la lista.");
            }
        });


// 1. Listener de texto estándar corregido
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Ejecuta la búsqueda (incluso si viene vacía)
                loadCatalogIntoSlot(currSlot, query != null ? query.trim() : "");
                searchBar.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Si el usuario borra manualmente hasta dejarlo vacío,
                // forzamos la restauración inmediata de la lista completa
                if (newText == null || newText.trim().isEmpty()) {
                    loadCatalogIntoSlot(currSlot, "");
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
                    loadCatalogIntoSlot(currSlot, query.trim());
                    searchBar.clearFocus();
                }
            });
        }
        //----------------------------------------------------

        //Para la lista de todos los productos
        if(!isSave) {
            for (int i = 0; i < 3; i++) {
                loadCatalogIntoSlot(i);  // cada caja: listas propias e independientes
            }
            bindWorkingLists();
        }


        //Basic.msg(""+mArtList.get(0).totalcount);

        //Para la lista de ventas ----------------------------
        mAdapter2 = new SaleResultAdapter(contex, objListSal, true);
        //-----------------------------------------------------

        mAdapter1 = new SaleMainAdapter(contex, objListAll);
        //------------------------------------------------------

        gridView.setAdapter(mAdapter1);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Obj item = objListAll.get(position);

                //Para las calculadora
                binding.calcProductName.setText(item.name);
                currGrid = position;
                currObj = item;
                mInput2.setText("");
                if(item.maxCount > 0) {
                    mInput2.setError(null);
                }
                else {
                    mInput2.setError("Producto AGOTADO!.");
                }

                if (mAdapter1 != null) {
                    mAdapter1.setSelectedPos(position);
                }

                //Termina aqui si la calculadora esta activa
                if (isCalc){
                    updateSaleList(item);
                    return;
                }

                //--------------------------------------------

                // ==================== LONG CLICK (Reset) ====================
                if (item.click == 1 ) {
                    if (view.getId() == R.id.buttDel1){
                        item.currCount = item.maxCount; // Restaurar stock completo
                        //item.saleCount = 0;

                        objListAll.set(position, item);

                        // Limpiar de la lista de ventas
                        objListSal.removeIf(obj -> Objects.equals(obj.id, item.id));

                        refreshSaleListUI();
                        if (mAdapter1 != null) mAdapter1.notifyDataSetChanged();
                        return;
                    }
                    item.click = 0;
                    return;
                }

                // ==================== CLICK NORMAL (fuera del botón X) ====================
                if (view.getId() != R.id.buttDel1 && view.getId() != R.id.inputCount ) {

                    if (item.currCount > 0 && item.maxCount > 0 && item.saleCount < item.maxCount) {
                        if((item.saleCount +1) > item.maxCount) {
                            double minus = item.maxCount - item.saleCount;

                            item.saleCount += minus;
                            item.currCount -= minus;
//                            Basic.msg("Remanente no alcanza "+minus);
//                            return;
                        }
                        else {
                            item.saleCount++;
                            item.currCount--;
                        }

                        objListAll.set(position, item);


                        if (mAdapter1 != null) {
                            //Basic.msg("currCount "+item.currCount+" > 0 && maxCount "+ item.maxCount +" > 0 && saleCount "+ item.saleCount +" < maxCount "+ item.maxCount, true);

                            mAdapter1.notifyDataSetChanged();
                        }
                    } else {
                        Basic.msg("Producto AGOTADO!");
                        return;
                    }
                }
                // ==================== ACTUALIZACIÓN SIEMPRE ====================
                updateSaleList(item);
            }
        });

        mListView.setAdapter(mAdapter2);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view.getId() == R.id.sale_button) {
                    Obj item = (Obj) parent.getAdapter().getItem(position);

                    item.click = 0;

                    boolean b = false;
                    for (int i = 0; i < objListAll.size(); i++) {
                        Obj obj = objListAll.get(i);
                        if(obj.id == item.id){
                            objListAll.set(i, item);
                            b = true;
                            break;
                        }
                    }
                    //Si es necesario se actualiza
                    if (b){
                        objListSal.removeIf(obj -> obj.saleCount == 0);

                        if (mAdapter1 != null) {

                            Double total = setTotal(objListSal);
                            viewTotal.setText("Total: " + Basic.getMaskConv(total, 0) +" / "+Basic.getMaskConv(total, 1));

                            mAdapter1.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

        mSpinn1.setAdapter(new SelecAdapter(contex, glData.saleType));
        mSpinn1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currSel1 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Para el Boton de Precesar PAgos
        mButt4.setOnClickListener(v -> {
            if(!objListSal.isEmpty()){
                //Procesa la venta y guarda el registro
                if(saveSale()){
                    //Si sale bien se limpian los valores
                    objListAll.clear();
                    objListSal.clear();

                    //Para la lista de todos los productos

                    mArtList = daoArt.getUsers();    //Se actualiza la lista de articulos

                    for (Article obj : mArtList) {
                        objListAll.add(setGalleryArray(obj));
                    }

                    if (mAdapter1 != null && mAdapter2 != null) {

                        Double total = setTotal(objListSal);
                        viewTotal.setText("Total: " + Basic.getMaskConv(total, 0) +" / "+Basic.getMaskConv(total, 1));

                        mAdapter1.setSelectedPos(-1);

                        mAdapter1.notifyDataSetChanged();
                        mAdapter2.notifyDataSetChanged();
                    }
                    else {
                        Basic.msg("Aqui no hay aqui no hay !: "+mAdapter1 +" : "+ mAdapter2, true);
                    }

                    currObj = null;

                    // sync envia una actualizacion por red
                    Bundle mBundle = new Bundle();
                    mBundle.putBoolean("sync", true);
                    Intent mIntent = new Intent(contex, ReloadActivity.class);
                    mIntent.putExtras(mBundle);
                    //Esto inicia las actividad Reload

                    startActivity(mIntent);
                    //-------------------------------------
                }
            }
            else {
                Basic.msg("Lista VACIA!");
            }
        });

        if(isSave) {
            refreshAllUI();
        }
    }

    private void setCalcResult(){
        if (currObj != null && calcCount > 0 && calcCount <= currObj.maxCount){
           // Basic.msg("Aqui hay NARIZ ! \uD83D\uDC43\uD83D\uDC3D\uD83E\uDD25\uD83E\uDD78 ");

            currObj.saleCount = calcCount;
            currObj.currCount =  currObj.maxCount - calcCount;

            objListAll.set(currGrid, currObj);

            calcCount = 0;
            viewResult.setText("Cantidad:"); ;
            mButt2.setEnabled(false);

            if (mAdapter1 != null) {
                //Basic.msg("currCount "+item.currCount+" > 0 && maxCount "+ item.maxCount +" > 0 && saleCount "+ item.saleCount +" < maxCount "+ item.maxCount, true);

                mAdapter1.setSelectedPos(-1);
                mAdapter1.notifyDataSetChanged();
            }
            updateSaleList(currObj);

            binding.bottomPanel.setVisibility(View.VISIBLE);
            binding.panelCalc.setVisibility(View.GONE);

            sharedViewModel.setCalcVisible(false);
            isCalc = false;
            currObj = null;

        }
    }

    // Actualiza la lista de ventas (objListSal)
    private void updateSaleList(Obj item) {

        if (item.saleCount <= 0) {
            objListSal.removeIf(obj -> Objects.equals(obj.id, item.id));
        } else {
            // Agregar si no existe
            boolean exists = objListSal.stream().anyMatch(obj -> Objects.equals(obj.id, item.id));
            if (!exists) {
                objListSal.add(item);
            }
        }

        refreshSaleListUI();
    }

    // Refresca UI de la lista de ventas
    @SuppressLint("SetTextI18n")
    private void refreshSaleListUI() {
        objListSal.removeIf(obj -> obj.saleCount <= 0);
        if (mAdapter2 != null) {
            Double total = setTotal(objListSal);
            viewTotal.setText("Total: " + Basic.getMaskConv(total, 0) +" / "+Basic.getMaskConv(total, 1));
            mAdapter2.notifyDataSetChanged();
        }
    }

    @SuppressLint("SetTextI18n")
    private void refreshAllUI() {
        if (mAdapter1 != null && mAdapter2 != null) {
            Double total = setTotal(objListSal);
            viewTotal.setText("Total: " + Basic.getMaskConv(total, 0) +" / "+Basic.getMaskConv(total, 1));
            mAdapter1.notifyDataSetChanged();
            mAdapter2.notifyDataSetChanged();
        }
    }

    private double setTotal(List<Obj> list){
        if (list.isEmpty()){
            viewTotal.setVisibility(View.INVISIBLE);
            mInput3.setVisibility(View.GONE);
            mButt3.setVisibility(View.GONE);
            mButt4.setEnabled(false);
            mSpinn1.setEnabled(false);

        }
        else {
            viewTotal.setVisibility(View.VISIBLE);
            mInput3.setVisibility(View.VISIBLE);
            mButt3.setVisibility(View.VISIBLE);
            mButt4.setEnabled(true);
            mSpinn1.setEnabled(true);
        }

        double total = 0.0;
        for (Obj obj : list) {
            double price = MathUtls.addPercentage(obj.price, obj.margen+mConf.margen);
            total = total + ( price * obj.saleCount);
        }
        return total;
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
                art.staus, art.currcount, art.totalcount, 0, mPrice, art.margen+mConf.margen, art.uid
        );

        return mObj;
    }

    private boolean saveSale(){
        // Tu lógica de crrSale...
        if (crrSale == null) {

            long currDate = 0;
            long currTime = 0;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                currDate = java.time.Instant.now().toEpochMilli();
                currTime = System.currentTimeMillis();
            }

            String strClt = "";
            int cltNr = 1;
            for (Sale sal : daoSal.getUsers()) {
                if (CalendUtls.isSameDay(sal.fecha, currDate)) {
                    cltNr++;
                }
            }
            String strNr = " nr"+cltNr + (" (" + CalendUtls.getShortDate(currDate) + ")");
            strClt = "Cliente" + strNr ;

            String strRawName = mInput3.getText().toString();
            if(!strRawName.isEmpty()) {

                strClt = strRawName+strNr;
            }

            Cliente mCl = null;
            DaoClt daoClt = StartVar.appDBall.daoClt();
            if(!strRawName.isEmpty()) {
                String idUser = InputHelper.sanitizeText(strRawName);
                boolean b = true;
                for (Cliente cl : daoClt.getUsers()){
                    if(cl.iduser.equals(idUser)){
                        b = false;
                        mCl = cl;
                        break;
                    }
                }
                if(b){
                String cltId = DatabaseUtils.generateId("cltID", daoClt);
                mCl = new Cliente(cltId, strRawName, idUser, "", 0, currDate, (float) 0, currDate, 0, "");
                }
            }

            String strId = DatabaseUtils.generateId("salID", daoSal);
            StringBuilder strArtList = new StringBuilder();
            StringBuilder strCountList = new StringBuilder();
            StringBuilder strPriceList = new StringBuilder();
            StringBuilder strMargList = new StringBuilder();

            double total = 0.0;

            List<Article> artList = new ArrayList<>();

            for (Obj o : objListSal){
                double price = MathUtls.addPercentage(o.price, o.margen+mConf.margen);
                total = total + ( price * o.saleCount);
                strArtList.append("|").append(o.strId);
                strCountList.append("|").append(o.saleCount);
                strPriceList.append("|").append(o.price);
                strMargList.append("|").append(o.margen);

                Article art = daoArt.getUsers(o.id);

                art.totalcount -= o.saleCount;
                art.currcount -= o.saleCount;

                artList.add(art);
            }

            if (mCl != null){
                strClt = mCl.cliente;
                daoClt.insertUser(mCl);
            }

            Sale mObj = new Sale(
                    strId, strClt, strArtList.toString(), strCountList.toString(), strPriceList.toString(), strMargList.toString(),
                    total, StartVar.mDollar, currSel1, "", currTime, "@null", 0, "", currDate
            );

            //Se restauran los elementos
            mInput3.setText("");
            mInput3.setVisibility(View.GONE);
            mButt4.setEnabled(false);
            mSpinn1.setEnabled(false);

            //Se guarda la venta
            daoSal.insertUser(mObj);

            //Se actualiza la lista de articulos con valores descontados
            daoArt.updateUser(artList);

            //Se actualiza la lista de fechas si esta no existe
           CalendUtls.getAndCreateDate(contex, 0);

//            Fecha objB = CalendUtls.getAndCreateDate(contex,0);
//            List<Object> mList = Arrays.asList(mF, objB);
//            GlobalData.getInstance(getContext()).getGenericQueue().enqueueList(mList, 3);

            return true;

        } else {
            Basic.msg("Aqui no hay aqui no hay !: "+crrSale , true);
        }
        return false;
    }


    private void initSlots() {
        for (int i = 0; i < 3; i++) {
            allSlots[i] = new ArrayList<>();
            salSlots[i] = new ArrayList<>();
        }
        bindWorkingLists(); // objListAll / objListSal → slot 0
    }

    /** Hace que objListAll y objListSal apunten al slot actual */
    private void bindWorkingLists() {
        objListAll = allSlots[currSlot];
        objListSal = salSlots[currSlot];
    }

    private void selectSlot(int slot) {
        if (slot < 0 || slot > 2) return;
        if (slot == currSlot) return;

        currSlot = slot;
        bindWorkingLists(); // ahora lees/escribes otro par

        // Re-sincronizar adapters con las listas nuevas
        if (mAdapter1 != null) {
            // Si el adapter guarda la lista internamente, recréalo o pásale la nueva:
            mAdapter1 = new SaleMainAdapter(requireContext(), objListAll);
            gridView.setAdapter(mAdapter1);
        }
        if (mAdapter2 != null) {
            mAdapter2 = new SaleResultAdapter(requireContext(), objListSal, true);
            mListView.setAdapter(mAdapter2);
        }

        refreshAllUI();
    }

    private void loadCatalogIntoSlot(int slot) {
        loadCatalogIntoSlot( slot, "");
    }

    private void loadCatalogIntoSlot(int slot, String str) {
        str = InputHelper.cleanText(str);
        if (slot < 0 || slot > 2) return;
        if (allSlots[slot] == null) allSlots[slot] = new ArrayList<>();
        if (salSlots[slot] == null) salSlots[slot] = new ArrayList<>();

        allSlots[slot].clear();
        salSlots[slot].clear();

        if (mArtList == null) return;

        for (Article obj : mArtList) {
            if (obj.staus > 0 && InputHelper.hasWordMatch(obj.nombre+obj.descr, str)) {
                allSlots[slot].add(setGalleryArray(obj));
            }
        }
    }

}