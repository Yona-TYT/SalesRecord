package com.example.salesrecord.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.R;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.MathUtls;
import com.example.salesrecord.utls.Obj;

import java.util.ArrayList;
import java.util.List;

public class SummaryAdapter extends BaseAdapter  {
    //Test------------------------------------------------------------
    private Context mContex;
    private List<Obj> objList = new ArrayList<>();

    private int currPos = -1;
    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());


    private static class ViewHolder {
        LinearLayout layout1;
        TextView view1;
        TextView view2;
    }

    public  SummaryAdapter(Context mContex, List<Obj> objList){
        this.mContex = mContex;
        this.objList = objList;
    }

    public void setSelectedPos(int pos) {
        this.currPos = pos;
        notifyDataSetChanged();
    }

    public int getSelectedPos() {
        return currPos;
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
            convertView = LayoutInflater.from(mContex).inflate(R.layout.item_summary, parent, false);

            holder = new ViewHolder();
            holder.layout1 = convertView.findViewById(R.id.summary_Layout);
            holder.view1 = convertView.findViewById(R.id.summary_tex1);
            holder.view2 = convertView.findViewById(R.id.summary_tex2);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Obj item = objList.get(pos);   // Cambia TuItem por tu clase

        // Layout
        if (pos == currPos) {

            holder.layout1.setBackgroundColor(
                    ContextCompat.getColor(holder.layout1.getContext(), R.color.selected_background)
            );
        } else {
            // Si no está seleccionado, mantiene tus colores lógicos originales
            if (item.status == 0){
                holder.layout1.setBackgroundColor(
                        ContextCompat.getColor(holder.layout1.getContext(), R.color.retire_background)
                );
            }
            else if (item.currCount <= 0 || item.maxCount <= 0){
                holder.layout1.setBackgroundColor(
                        ContextCompat.getColor(holder.layout1.getContext(), R.color.alert_background)
                );
            }
            else {
                holder.layout1.setBackgroundColor(
                        ContextCompat.getColor(holder.layout1.getContext(), R.color.normal_background)
                );
            }
        }


        // Textos
        String off = item.status == 0 ? "(RETIRADO) ":"";

        holder.view1.setText(off+item.name);
        double clcPrice = MathUtls.addPercentage(item.price, item.margen);

        Double total = (clcPrice*item.saleCount);
        holder.view2.setText("uds. " + Basic.formatDecimal(item.maxCount) +
                "  -  precio: " + Basic.getMaskConv(item.price, 0) +
                " / "+  Basic.getMaskConv(item.price, 1) +
                "  -  (" + Basic.formatDecimal(item.margen)+" %)");

        holder.view1.setTextColor(
                ContextCompat.getColor(holder.view1.getContext(), R.color.alert_text)
        );
        holder.view2.setTextColor(
                ContextCompat.getColor(holder.view2.getContext(), R.color.alert_text)
        );

        holder.view1.setTag(pos);
        holder.view2.setTag(pos);

        return convertView;
    }
}
