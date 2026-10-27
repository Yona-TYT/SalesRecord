package com.example.salesrecord.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.activitys.PayDetailsActivity;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.ThemeHelper;
import com.example.salesrecord.utls.CalendUtls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PayAdapter extends BaseAdapter implements Filterable, View.OnClickListener{
    private Context mContex;
    private List<Object[]> textList = new ArrayList<>();
    private List<Object[]> currList = new ArrayList<>(); // Original Values

    private ArrayList<Integer> newList = new ArrayList<>();    // Values to be displayed

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    private boolean checkBox = false;
    private HashMap<String, Boolean> checkStates = new HashMap<>();

    public interface OnCheckClickListener {
        void onCheckClick(int position, boolean isChecked);
    }
    private OnCheckClickListener customCheckListener;

    // Método público para asignar el listener desde la Activity
    public void setOnCheckClickListener(OnCheckClickListener listener) {
        this.customCheckListener = listener;
    }

    public  PayAdapter(Context mContex, List<Object[]> textList, boolean checkBox){
        this(mContex, textList);
        this.checkBox = checkBox;
    }

    public  PayAdapter(Context mContex, List<Object[]> textList){
        this.mContex = mContex;
        this.textList = textList;
        this.currList = textList;
    }

    static class ViewHolder {
        LinearLayout layout;
        Button butt;
        CheckBox check;
        TextView text1;
        TextView text2;
        TextView text3;
        TextView text4;
        int defaultTextColor;
    }

    @Override
    public int getCount(){
        return newList.size();
    }

    @Override
    public Object getItem(int pos){
        return newList;
    }

    @Override
    public long getItemId(int i) {  return newList.get(i);  }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // 1. Obtenemos el tema de la actividad para que el botón lo herede de forma nativa
            int mStyle = ThemeHelper.getManifestThemeId(AppContextProvider.getCurrentActivity());
            ContextThemeWrapper themedContext = new ContextThemeWrapper(mContex, mStyle);

            // 2. Inflamos el nuevo archivo XML usando ese contexto con estilo
            convertView = LayoutInflater.from(themedContext).inflate(R.layout.item_pay, parent, false);

            holder = new ViewHolder();
            holder.layout = convertView.findViewById(R.id.pay_item_layout);
            holder.butt = convertView.findViewById(R.id.butt_paylist);
            holder.check = convertView.findViewById(R.id.cb_paylist);
            holder.text1 = convertView.findViewById(R.id.pay_text1);
            holder.text2 = convertView.findViewById(R.id.pay_text2);
            holder.text3 = convertView.findViewById(R.id.pay_text3);
            holder.text4 = convertView.findViewById(R.id.pay_text4);
            holder.defaultTextColor = holder.text3.getCurrentTextColor();

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Alternar visibilidad de componentes según el modo seleccionado
        if(checkBox){
            holder.butt.setVisibility(View.GONE);
            holder.check.setVisibility(View.VISIBLE);
        } else {
            holder.butt.setVisibility(View.VISIBLE);
            holder.check.setVisibility(View.GONE);
        }
        // 1. Obtener la Key Única (String)
        String itemKey = "";
        if (pos < newList.size()) {
            int idx = newList.get(pos);
            itemKey = (String) textList.get(idx)[0];
        }

        // 2. CRÍTICO: Apagar el listener anterior antes de forzar el valor visual
        // Esto evita que el reciclaje de celdas marque/desmarque elementos locamente al hacer scroll
        holder.check.setOnCheckedChangeListener(null);

        // 3. Forzar el estado visual correcto recuperado del HashMap
        boolean isItemChecked = checkStates.containsKey(itemKey) ? checkStates.get(itemKey) : false;
        holder.check.setChecked(isItemChecked);

        // 4. Asignar el nuevo Listener usando el evento correcto de los CheckBox
        final String finalKey = itemKey;
        holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Guardamos el estado usando el String como clave de inmediato
                checkStates.put(finalKey, isChecked);

                // Disparamos el evento hacia la Activity
                if (customCheckListener != null) {
                    customCheckListener.onCheckClick(pos, isChecked);
                }
            }
        });

        if (textList != null && pos < newList.size()) {
            int idx = newList.get(pos);

            // Configuración dinámica del Botón
            holder.butt.setTag((String) textList.get(idx)[0]);
            holder.butt.setOnClickListener(this); // Asigna el listener de la clase

            // Construcción y asignación de Textos
            Integer opt = (Integer) textList.get(idx)[4];
            String txStatus = (String) textList.get(idx)[1];

            String txFech = (String) textList.get(idx)[3];
            String txTime = (String) textList.get(idx)[5];
            String txName = (String) textList.get(idx)[7];
            holder.text1.setText(txName);
            holder.text2.setText(txStatus);

            TextView monto = holder.text3;
            String txMont ="+";
            if (opt == 3){
                txMont ="-";
                monto.setTextColor(
                        ContextCompat.getColor(monto.getContext(), R.color.retire_background)
                );
            }
            else if(opt > 0){
                txMont ="-";
                monto.setTextColor(
                        ContextCompat.getColor(monto.getContext(), R.color.alert_background)
                );
            }
            else {
                monto.setTextColor(holder.defaultTextColor);
            }

            txMont += " ("+ Basic.getMask((double) textList.get(idx)[2], 0) + " / "+
                    Basic.getMaskConv((double) textList.get(idx)[2], null, 1)+")";

            monto.setText(txMont);

            holder.text4.setText(txFech+" "+txTime);
        }

        return convertView;
    }

    // Obtiene la lista completa de checks
    public HashMap<String, Boolean> checkedList() {
        return checkStates;
    }

    // Saber si un elemento está marcado pasando su ID único
    public boolean isItemChecked(String itemKey) {
        return checkStates.containsKey(itemKey) ? checkStates.get(itemKey) : false;
    }

    // Limpiar todas las selecciones de golpe cuando se apague el Switch
    public void clearSelections() {
        checkStates.clear();
    }

    // Permite marcar los checkboxes pasando un nuevo mapa de estados desde la Activity
    public void setCheckedList(HashMap<String, Boolean> newCheckStates) {
        if (newCheckStates != null) {
            // Opción A: Reemplazar el mapa por completo
            this.checkStates = new HashMap<>(newCheckStates);

            notifyDataSetChanged();
        }
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<Integer> FilteredArrList = new ArrayList<Integer>();

                if (constraint == null || constraint.length() == 0) {
                    for (int i = 0; i < currList.size(); i++) {
                        FilteredArrList.add(i);
                    }
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < currList.size(); i++) {
                        String data = (String)currList.get(i)[1];
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrList.add(i);
                        }
                    }
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                // 1. Asignamos la nueva lista filtrada
                newList = (ArrayList<Integer>) results.values;

                // 2. SOLUCIÓN AL ERROR DE COMPILACIÓN: Accedemos a la variable de la clase externa PayAdapter
                HashMap<String, Boolean> savedStates = GlobalData.getInstance(AppContextProvider.getContext()).getChecksState();
                if (savedStates != null) {
                    // Usamos "PayAdapter.this" para especificar que la variable le pertenece al Adapter
                    PayAdapter.this.checkStates = new HashMap<>(savedStates);
                }

                // 3. Notificamos los cambios a la pantalla de forma segura en el hilo principal
                notifyDataSetChanged();
            }
        };
        return filter;
    }



    @Override
    public void onClick(View view) {
        int itemId = view.getId();

        if(itemId == R.id.butt_paylist) {
            glData.setCurrSalId( (String) view.getTag());

            Application application = (Application) mContex.getApplicationContext();
            Intent mIntent = new Intent(mContex, PayDetailsActivity.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            application.startActivity(mIntent);
        }
    }
}