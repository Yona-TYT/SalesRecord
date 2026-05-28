package com.example.salesrecord.ui.home;

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
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.CurrencyEditText;
import com.example.salesrecord.GetDollar;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.adapters.SelListAdapter;
import com.example.salesrecord.adapters.SaleAdapter;
import com.example.salesrecord.databinding.FragmentHomeBinding;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.DatabaseUtils;
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.Obj;
import com.example.salesrecord.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    // DB
    private DaoSal daoSal;
    private DaoArt daoArt;
    private List<Article> mArtList =  new ArrayList<>();

    private CurrencyEditText mInput1;

    private SearchView searchBar;
    private GridView gridView;
    private TextView viewTotal;

    private SelListAdapter mAdapter1;
    private SaleAdapter mAdapter2;
    private ListView mListView;

    private Button mButt1;
    private Button mButt2;

    private Long currGrid = 0L;

    private List<Obj> objListAll = new ArrayList<>();
    private List<Obj> objListSal = new ArrayList<>();

    private Context contex;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    private Sale crrSale;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        contex = AppContextProvider.getContext();

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mInput1 = binding.inputHome1;
        gridView = binding.gcImg;
        mListView = binding.viewList;
        viewTotal = binding.homeText2;

        searchBar = binding.searchBar;

        mButt1 = binding.buttHome1;
        mButt2 = binding.buttHome2;

        GetDollar mGet = new GetDollar(contex, getActivity(), 0, mInput1);
        try {
            GetDollar.urlRun();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        setViwes();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guardamos el valor numérico actual
//        outState.putDouble("monto", glMonto);
//        outState.putBoolean("isEs", isEsFormat);
        outState.putDoubleArray("doubList", glData.getDoubList());
        outState.putLongArray("logList", glData.getLongList());

    }

    @SuppressLint("SetTextI18n")
    private void setViwes(){

        //Limpiamos las listas
        objListAll.clear();
        objListSal.clear();

        daoSal = StartVar.appDBall.daoSal();
        daoArt = StartVar.appDBall.daoAtr();
        mArtList = daoArt.getUsers();

        // Boton Recargar
        mButt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetDollar mGet = new GetDollar(contex, getActivity(), 0, mInput1);
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


        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 1. AQUÍ MANDAS A HACER LA BUSQUEDA
                // Ejemplo: miAdaptador.filtrar(query);

                // 2. OCULTAR EL TECLADO AUTOMÁTICAMENTE
                searchBar.clearFocus();

                return true; // Retornamos true para indicarle al sistema que ya manejamos el evento
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Este método se ejecuta cada vez que el usuario escribe una letra
                // Puedes dejarlo vacío si solo buscas al presionar Enter
                return false;
            }
        });

        //----------------------------------------------------

        //Para la lista de todos los productos
        for (Article obj : mArtList) {
            objListAll.add(setGalleryArray(obj));
        }

        Basic.msg(mArtList.get(0).descr);

        //Basic.msg(""+mArtList.get(0).totalcount);

        //Para la lista de ventas ----------------------------
        mAdapter2 = new SaleAdapter(contex, objListSal);
        //-----------------------------------------------------

        mAdapter1 = new SelListAdapter(contex, objListAll);
        //------------------------------------------------------

        gridView.setAdapter(mAdapter1);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Obj item = objListAll.get(position);

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
                if (view.getId() != R.id.buttDel1) {

                    if (item.currCount > 0 && item.maxCount > 0 && item.saleCount < item.maxCount) {
                        item.saleCount++;
                        item.currCount--;
                        objListAll.set(position, item);


                        if (mAdapter1 != null) {
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

        // Para el Boton de Precesar PAgos
        mButt2.setOnClickListener(v -> {
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

                        mAdapter1.notifyDataSetChanged();
                        mAdapter2.notifyDataSetChanged();
                    }
                    else {
                        Basic.msg("Aqui no hay aqui no hay !: "+mAdapter1 +" : "+ mAdapter2, true);
                    }

                }
            }
            else {
                Basic.msg("Lista VACIA!");
            }
        });
    }

    private double setTotal(List<Obj> list){
        if (list.isEmpty()){
            viewTotal.setVisibility(View.INVISIBLE);
            mButt2.setEnabled(false);
        }
        else {
            viewTotal.setVisibility(View.VISIBLE);
            mButt2.setEnabled(true);
        }

        double total = 0.0;
        for (Obj obj : list) {
            total = total + ( obj.price * obj.saleCount);
        }
        return total;
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



//    public void nextViewActivity(int pos){
//        Intent mIntent = new Intent(this, ViewActivity.class);
//        Bundle mBundle = new Bundle();
//        //Log.d("PhotoPicker", "11100------------------------: " + dirList.size());
//        mBundle.put("index", pos);
//        mIntent.putExtras(mBundle);
//        //Save gallery petition
//        int firstVisiblePosition = gridView.getFirstVisiblePosition();
//        Basic.msg("Primera posición visible: " + firstVisiblePosition);
//
//        // Opcional: Para más precisión, obtén el offset (píxeles desde el top del primer ítem)
//        if (gridView.getChildCount() > 0) {
//            int offset = gridView.getChildAt(0).getTop();
//            Basic.msg("Offset: " + offset);
//            // Guarda ambos: posición + offset
//            myPrefernce.setGalleryPosition(firstVisiblePosition, offset);
//        }
//        else {
//            myPrefernce.setGalleryPosition(firstVisiblePosition, 0);
//        }
//        startActivity(mIntent);
//    }

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

    private boolean saveSale(){
        // Tu lógica de crrSale...
        if (crrSale == null) {

            long currDate = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                currDate = java.time.Instant.now().toEpochMilli();
            }

            String strId = DatabaseUtils.generateId("salID", daoSal);
            String strClt = "Cliente "+strId.replaceAll("\\D","");
            StringBuilder strArtList = new StringBuilder();
            Double total = 0.0;

            List<Article> artList = new ArrayList<>();

            for (Obj o : objListSal){
                total = total + ( o.price * o.saleCount);
                strArtList.append("|").append(o.strId);

                Article art = daoArt.getUsers(o.id);

                art.totalcount -= o.saleCount;
                art.currcount -= o.saleCount;

                artList.add(art);
            }
            Sale mObj = new Sale(
                    strId, strClt, strArtList.toString(), total, StartVar.mDollar, 0,
                    "", "", "@null", 0, "", currDate
            );

            //Se guarda la venta
            //daoSal.insertUser(mObj);

            //Se actualiza la lista de articulos con valores descontados
            daoArt.updateUser(artList);

            return true;

        } else {
            Basic.msg("Aqui no hay aqui no hay !: "+crrSale , true);
        }
        return false;
    }

}