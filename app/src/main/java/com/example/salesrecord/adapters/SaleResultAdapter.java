package com.example.salesrecord.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.R;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.MathUtls;
import com.example.salesrecord.utls.MoneyUtls;
import com.example.salesrecord.utls.Obj;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class SaleResultAdapter extends BaseAdapter  {
    //Test------------------------------------------------------------
    private Context mContex;
    private List<Obj> objList = new ArrayList<>();
    private boolean isButt = false;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    private static class ViewHolder {
        TextView view1;
        TextView view2;
        Button mButt;
    }

    public SaleResultAdapter(Context mContex, List<Obj> objList, boolean isButt){
        this.mContex = mContex;
        this.objList = objList;
        this.isButt = isButt;
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
            convertView = LayoutInflater.from(mContex).inflate(R.layout.item_result_sale, parent, false);

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

        double count = item.saleCount;
        if(isButt) {
            if (count > 0) {

                button.setVisibility(View.VISIBLE);
                button.setClickable(false);      // Evita que bloquee el click del item
                button.setFocusable(false);

                button.setOnClickListener(v -> {
                    Object tag = v.getTag();
                    if (!(tag instanceof Integer)) return;
                    int idx = (int) tag;
                    if (idx < 0 || idx >= objList.size()) return;

                    Obj mitem = objList.get(idx);
                    if (mitem.saleCount <= 0) return;

                    mitem.saleCount = Math.max(0, mitem.saleCount - 1);
                    mitem.click = 0;
                    // NO toques currCount

                    if (quantityListener != null) {
                        quantityListener.onQuantityChanged(mitem);
                    }
                });

                button.setOnLongClickListener(v -> {
                    Object tag = v.getTag();
                    if (!(tag instanceof Integer)) return false;
                    int idx = (int) tag;
                    if (idx < 0 || idx >= objList.size()) return false;

                    Obj mitem = objList.get(idx);
                    mitem.saleCount = 0;
                    mitem.click = 1;

                    if (quantityListener != null) {
                        quantityListener.onQuantityChanged(mitem);
                    }
                    return true;
                });


            } else {
                button.setVisibility(View.INVISIBLE);
            }
        }
        else {
            button.setVisibility(View.INVISIBLE);
        }

        // Textos
        String desc = item.desc.isEmpty() ? "":" ("+item.desc+")";
        holder.view1.setText(item.name+desc);
        BigDecimal clcPrice = MathUtls.addPercentage(BigDecimal.valueOf(item.price), BigDecimal.valueOf(item.margen));

        BigDecimal total = clcPrice.multiply(BigDecimal.valueOf(item.saleCount));
        holder.view2.setText("Cantidad: " + MoneyUtls.formatDecimal(item.saleCount)+" "+glData.unitList.get(item.unit)+ " ("+ MoneyUtls.getMask(total.doubleValue(), 0, true) +" / "+MoneyUtls.getMaskConv(total.doubleValue(), 1)+")");


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

    private SaleMainAdapter.OnQuantityChangedListener quantityListener;

    public interface OnQuantityChangedListener {
        void onQuantityChanged(Obj item);
    }

    public void setOnQuantityChangedListener(SaleMainAdapter.OnQuantityChangedListener listener) {
        this.quantityListener = listener;
    }
}
