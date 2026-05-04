package com.example.salesrecord.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.core.content.ContextCompat;

import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.db.dao.DaoClt;

import java.util.ArrayList;
import java.util.List;

public class BoxAdapter extends BaseAdapter implements View.OnClickListener{
    private Context mContex;

    private List<Object[]> textList = new ArrayList<>();

    Spinner mSpin;

    private ArrayList<Integer> newList = new ArrayList<>();    // Values to be displayed

    public BoxAdapter(Context mContex, List<Object[]> textList, Spinner mSpin){
        this.mContex = mContex;
        this.textList = textList;
        this.mSpin = mSpin;

        StartVar.bitList.clear();
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
    public View getView(int pos, View convertView, ViewGroup parent){
        Log.d("PhotoPicker", "Ya hay ? 11111------------------------: "+ textList.size());
        CheckBox check = new CheckBox(mContex);
        check.setChecked((Boolean)textList.get(pos)[1]);
        LinearLayout layout = new LinearLayout(mContex);

        check.setId(R.id.check_acclist);
        check.setTag(textList.get(pos)[0]);

        // Se ajustan los parametros del Texto ----------------------------------
        check.setText((String)textList.get(pos)[2]);
        check.setTypeface(Typeface.DEFAULT_BOLD);
        check.setGravity(Gravity.CENTER);
        check.setWidth(R.dimen.spinner_w1);
        check.setMaxLines(1);
        check.setTextColor(ContextCompat.getColor(check.getContext(), R.color.text_color1));
        check.setBackgroundColor(ContextCompat.getColor(check.getContext(), R.color.text_background2));
        check.setPadding(10,5,10,5);
        check.setOnClickListener(this);
        layout.addView(check);

        //-----------------------------------------------------------------------

        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setVisibility(View.VISIBLE);

        return layout;
    }

    @Override
    public void onClick(View view) {
        int itemId = view.getId();
        if(itemId == R.id.check_acclist) {
            CheckBox check =  (CheckBox)view;
            DaoClt daoClt = StartVar.appDBall.daoClt();
            int cltId = StartVar.cltIndex;
            Cliente mClt = daoClt.getUsers("cltID"+cltId);

            int idx = (int)view.getTag();

            Object[] item = textList.get(idx);
            item[1] = check.isChecked();

            StartVar.bitList = textList;

//            Iterator<Object[]> iterator = StartVar.bitList.iterator();
//            while (iterator.hasNext()) {
//                Object[] mSList = iterator.next();
//                if ((int) mSList[0] == idx) {
//                    iterator.remove();  // Elimina el elemento actual de la lista
//                    break;  // Sale del bucle si solo quieres remover uno (opcional)
//                }
//            }
//
//            List<Object> mList = BitsOper.setBits(check.isChecked(), idx);
//            for (Object mL : mList){
//                int value = (int) mL;
////                for (int value : intList) {
////
////                    Basic.msg("" + String.format("value %x ", value));
////                }
//                //daoClt.updateBits(mClt.cliente, BitsOper.saveBits(intList));
//                if(check.isChecked()) {
//                    Object[] objList = new Object[2];
//                    objList[0] = idx;
//                    objList[1] = value;
//                    StartVar.bitList.add(objList);
//                }
//            }


            // Modifica directamente el booleano
//            String accText = (String) textList.get(idx)[2];
//            textList.set(idx, new Object[]{idx, check.isChecked(), accText});
            // Notifica al adaptador para refrescar vistas
            notifyDataSetChanged();
        }
    }
}
