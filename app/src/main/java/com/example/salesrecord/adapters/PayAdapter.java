package com.example.salesrecord.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
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
import java.util.List;

public class PayAdapter extends BaseAdapter implements Filterable, View.OnClickListener{
    //Test------------------------------------------------------------
    private Context mContex;
    private CalendUtls cale = new CalendUtls();
    private Basic mBasic;

    private List<Object[]> textList = new ArrayList<>();
    private List<Object[]> currList = new ArrayList<>(); // Original Values
    private List<String> mCurrencyList= Arrays.asList("$", "Bs");
    private int mCindex = StartVar.mCurrency;

    private ArrayList<Integer> newList = new ArrayList<>();    // Values to be displayed

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    public  PayAdapter(Context mContex, List<Object[]> textList){
        this.mContex = mContex;
        this.textList = textList;
        this.currList = textList;

        mBasic = new Basic(mContex);
    }

    static class ViewHolder {
        LinearLayout layout;
        Button butt;
        TextView text1;
        TextView text2;
        TextView text3;
        TextView text4;
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
            holder.text1 = convertView.findViewById(R.id.pay_text1);
            holder.text2 = convertView.findViewById(R.id.pay_text2);
            holder.text3 = convertView.findViewById(R.id.pay_text3);
            holder.text4 = convertView.findViewById(R.id.pay_text4);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

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
            if(opt > 0){
                txMont ="-";
                monto.setTextColor(
                        ContextCompat.getColor(monto.getContext(), R.color.alert_background)
                );
            }

            txMont += " ("+ Basic.getMask((double) textList.get(idx)[2], 0) + " / "+
                    Basic.getMaskConv((double) textList.get(idx)[2], (double) textList.get(idx)[6], 1)+")";

            monto.setText(txMont);

            holder.text4.setText(txFech+" "+txTime);
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<Integer> FilteredArrList = new ArrayList<Integer>();
                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                //Log.d("PhotoPicker", "Constrain ------------------------: " + constraint);
                if (constraint == null || constraint.length() == 0) {
                    // set the Original result to return
                    for (int i = 0; i < currList.size(); i++) {
                        FilteredArrList.add(i);
                    }
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                else{
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < currList.size(); i++) {
                        String data = (String)currList.get(i)[1];
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrList.add(i);
                            //Log.d("PhotoPicker", "Constrain ------------------------: " + i);
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                // Log.d("PhotoPicker", "11111------------------------: " + FilteredArrList.size());
                return results;
            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //Log.d("PhotoPicker", "2222------------------------: " +constraint);
                newList = (ArrayList<Integer>) results.values;   // has the filtered values
                notifyDataSetChanged();                         // notifies the data with new filtered values
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