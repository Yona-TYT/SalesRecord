package com.example.salesrecord.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.R;
import com.example.salesrecord.ThemeHelper;

import java.util.ArrayList;
import java.util.List;

public class SelecAdapter extends BaseAdapter {
    //Test------------------------------------------------------------
    private Context mContex;

    private List<String> textList = new ArrayList<>();

    private ArrayList<Integer> newList = new ArrayList<>();    // Values to be displayed

    public  SelecAdapter(Context mContex, List<String> textList){
        this.mContex = mContex;
        this.textList = textList;
    }

    static class ViewHolder {
        TextView textView;
    }

    @Override
    public int getCount(){
        return textList.size();
    }

    @Override
    public Object getItem(int pos){
        return textList.get(pos);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // 1. OBTENEMOS EL ID DEL TEMA DE LA ACTIVIDAD (Tu estrategia)
            int mStyle = ThemeHelper.getManifestThemeId(AppContextProvider.getCurrentActivity());


            // 2. ENVOLVEMOS EL CONTEXTO CON EL TEMA DINÁMICO
            ContextThemeWrapper themedContext = new ContextThemeWrapper(mContex, mStyle);

            // 3. INFLAMOS EL XML USANDO EL CONTEXTO WRAPPED
            // De esta manera, el XML hereda TODOS los estilos y estados del Tema oficial
            convertView = LayoutInflater.from(themedContext).inflate(R.layout.item_selec, parent, false);

            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.text_item_spinner);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (pos < textList.size()) {
            holder.textView.setText(textList.get(pos));
        } else {
            holder.textView.setText("Error");
        }

        return convertView;
    }


    // 2. NUEVO: SEPARA LOS ELEMENTOS SÓLO CUANDO LA LISTA SE DESPLIEGA
    @Override
    public View getDropDownView(int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            // Inflamos el XML con los paddings grandes de separación
            convertView = LayoutInflater.from(mContex).inflate(R.layout.item_selec_dropdown, parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.text_item_spinner_drop);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (pos < textList.size()) holder.textView.setText(textList.get(pos));
        return convertView;
    }


}
