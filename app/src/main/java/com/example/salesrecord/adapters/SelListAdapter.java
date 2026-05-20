package com.example.salesrecord.adapters;

import static android.widget.GridLayout.spec;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.salesrecord.R;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.Obj;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelListAdapter extends BaseAdapter  {
    //Test------------------------------------------------------------
    private Context mContex;
    private List<Obj> objList = new ArrayList<>();

    private static class ViewHolder {
        LinearLayout layout1;
        ImageView imageView;
        TextView viewcount;
        TextView view1;
        TextView view2;
        TextView view3;
        Button mButt;
    }

    public SelListAdapter(Context mContex, List<Obj> objList){
        this.mContex = mContex;
        this.objList = objList;
    }

    @Override
    public int getCount(){
        return objList.size();
    }

    @Override
    public Object getItem(int pos){
        return objList.get(pos);
    }

    @Override
    public long getItemId(int i) {
        if (objList == null || objList.isEmpty()){
            return 0;
        }
        return objList.get(i).id;
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContex).inflate(R.layout.item_gallery, parent, false);

            holder = new ViewHolder();
            holder.layout1 = convertView.findViewById(R.id.list1Layout);
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.viewcount = convertView.findViewById(R.id.textCount);
            holder.view1 = convertView.findViewById(R.id.tex1);
            holder.view2 = convertView.findViewById(R.id.tex2);
            holder.view3 = convertView.findViewById(R.id.tex3);
            holder.mButt = convertView.findViewById(R.id.buttDel1);   // Asegúrate que el ID sea correcto

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Obj item = objList.get(pos);

        // Layout
        if (item.currCount <= 0 || item.maxCount <= 0){
            holder.layout1.setBackgroundColor(
                    ContextCompat.getColor(holder.layout1.getContext(), R.color.alert_background)
            );
        }
        else {
            holder.layout1.setBackgroundColor(
                    ContextCompat.getColor(holder.layout1.getContext(), R.color.text_background2)
            );
        }

        // Imagen
        if (!item.img.isEmpty() && !item.img.equals("null")) {
            File file = new File(item.img);
            holder.imageView.setImageURI(file.exists() ? Uri.fromFile(file) : Uri.parse(item.img));
        } else {
            holder.imageView.setImageResource(R.drawable.image_icon);
        }

        // ==================== CONTADOR Y BOTÓN ====================
        float count = item.saleCount;

        holder.viewcount.setText(String.valueOf(count));

        //  Boton X
        TextView button = holder.mButt;

        if (count > 0) {
            holder.viewcount.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
            button.setClickable(false);      // Evita que bloquee el click del item
            button.setFocusable(false);

            button.setOnClickListener(v -> {
                // 1. Obtener la posición real (el tag debe ser actualizado en onBind)
                Object tag = v.getTag();
                if (tag == null) return;
                int idx = (int) tag;

                // 2. Usar SIEMPRE idx para obtener el objeto
                Obj mitem = objList.get(idx);
                mitem.click = 0;

                // 3. Validar límites ANTES de modificar nada
                if (mitem.currCount >= mitem.maxCount) {
                    return;
                }

                // 4. Aplicar cambios lógicos
                if (mitem.saleCount > 0) {
                    mitem.saleCount--;
                }
                mitem.currCount++;

                // 5. Notificar  el cambio
                notifyDataSetChanged();

                // 🔥 ESTO ACTIVA EL onItemClick DEL LISTVIEW
                // parent es el ViewGroup que recibe el Adapter en el constructor o getView
                if (parent instanceof GridView) {
                    ((GridView) parent).performItemClick(v, idx, idx);
                }

            });

            button.setOnLongClickListener(v -> {
                Object tag = v.getTag();

                if (tag == null) return false;
                int idx = (int) tag;
                Obj mitem = objList.get(idx);
                mitem.saleCount = 0;
                mitem.currCount = mitem.maxCount;
                mitem.click = 1;
                objList.set(idx, mitem);
                //------------------------------------

                notifyDataSetChanged();

                // 🔥 ESTO ACTIVA EL onItemClick DEL LISTVIEW
                // parent es el ViewGroup que recibe el Adapter en el constructor o getView
                if (parent instanceof GridView) {
                    ((GridView) parent).performItemClick(v, idx, idx);
                }

                return true;
            });

        } else {
            holder.viewcount.setVisibility(View.INVISIBLE);

            button.setVisibility(View.INVISIBLE);
            button.setVisibility(View.INVISIBLE);
        }

        // Textos
        holder.view1.setText(item.name);
        holder.view2.setText("Disponible: " + item.currCount + "/" + item.maxCount);
        holder.view3.setText("Precio: " + Basic.getMaskConv(item.price, 0) +"/" + Basic.getMaskConv(item.price, 1));

        holder.view1.setTextColor(
                ContextCompat.getColor(holder.view1.getContext(), R.color.alert_text)
        );
        holder.view2.setTextColor(
                ContextCompat.getColor(holder.view2.getContext(), R.color.alert_text)
        );
        holder.view3.setTextColor(
                ContextCompat.getColor(holder.view3.getContext(), R.color.alert_text)
        );

        // Tags (para identificar posición)
        holder.imageView.setTag(pos);
        holder.view1.setTag(pos);
        holder.view2.setTag(pos);
        button.setTag(pos);

        return convertView;
    }
}
