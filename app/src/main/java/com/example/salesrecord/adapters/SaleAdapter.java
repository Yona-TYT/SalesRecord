package com.example.salesrecord.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.R;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.Obj;

import java.util.ArrayList;
import java.util.List;

public class SaleAdapter extends BaseAdapter  {
    //Test------------------------------------------------------------
    private Context mContex;
    private List<Obj> objList = new ArrayList<>();

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());


    private static class ViewHolder {
        TextView view1;
        TextView view2;
        Button mButt;
    }

    public  SaleAdapter(Context mContex, List<Obj> objList){
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

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int pos, View convertView, ViewGroup parent){

        ViewHolder holder;

        if (convertView == null) {
            // Inflamos el layout XML
            convertView = LayoutInflater.from(mContex).inflate(R.layout.item_sales, parent, false);

            holder = new ViewHolder();
            holder.view1 = convertView.findViewById(R.id.sale_tex1);
            holder.view2 = convertView.findViewById(R.id.sale_tex2);
            holder.mButt = convertView.findViewById(R.id.sale_button);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Obj item = objList.get(pos);   // Cambia TuItem por tu clase

        // Contador y Boton X
        TextView button = holder.mButt;

        float count = item.saleCount;
        if (count > 0) {

            button.setVisibility(View.VISIBLE);
            button.setClickable(false);      // Evita que bloquee el click del item
            button.setFocusable(false);

            button.setOnClickListener(v -> {
                Object tag = v.getTag();
                if (tag == null) return;

                int idx = (int) tag;

                Obj mitem = objList.get(idx);
                float num = mitem.saleCount;

                mitem.saleCount = ( num > 0 ? num-1 : 0 ); //Decrementa si es el Button X

                //Actualiza datos en el Objeto
                mitem.currCount++;
                mitem.click = 0;
                if(mitem.currCount > mitem.maxCount){
                    return;
                }
                objList.set(idx, mitem);
                //------------------------------------

                notifyDataSetChanged();

                // 🔥 ESTO ACTIVA EL onItemClick DEL LISTVIEW
                // parent es el ViewGroup que recibe el Adapter en el constructor o getView
                if (parent instanceof ListView) {
                    ((ListView) parent).performItemClick(v, idx, idx);
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
                if (parent instanceof ListView) {
                    ((ListView) parent).performItemClick(v, idx, idx);
                }

                return true;
            });


        } else {
            button.setVisibility(View.INVISIBLE);
        }

        // Textos
        holder.view1.setText(item.name);
        Double total = (item.price*item.saleCount);
        holder.view2.setText("Cantidad: " +item.saleCount+" "+glData.unitList.get(item.unit)+ " ("+ Basic.getMaskConv(total, 0) +" / "+Basic.getMaskConv(total, 1)+")");


        holder.view1.setTextColor(
                ContextCompat.getColor(holder.view1.getContext(), R.color.alert_text)
        );
        holder.view2.setTextColor(
                ContextCompat.getColor(holder.view2.getContext(), R.color.alert_text)
        );


        holder.view1.setTag(pos);
        holder.view2.setTag(pos);
        holder.mButt.setTag(pos);

        return convertView;
    }
}
