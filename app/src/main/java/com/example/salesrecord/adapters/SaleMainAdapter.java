package com.example.salesrecord.adapters;

import static android.widget.GridLayout.spec;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.CurrencyEditText;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.R;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.MathUtls;
import com.example.salesrecord.utls.MoneyUtls;
import com.example.salesrecord.utls.Msg;
import com.example.salesrecord.utls.Obj;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaleMainAdapter extends BaseAdapter  {
    //Test------------------------------------------------------------
    private Context mContex;
    private List<Obj> objList = new ArrayList<>();
    private int currPos = -1;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    private static class ViewHolder {
        LinearLayout layout1;
        ImageView imageView;
        TextView viewcount;
        CurrencyEditText inputcount;
        TextView view1;
        TextView view2;
        TextView view3;
        Button mButt;
        TextWatcher activeTextWatcher;

    }

    public SaleMainAdapter(Context mContex, List<Obj> objList){
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

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContex).inflate(R.layout.item_main_sale, parent, false);

            holder = new ViewHolder();
            holder.layout1 = convertView.findViewById(R.id.list1Layout);
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.viewcount = convertView.findViewById(R.id.viewCount);
            holder.inputcount = convertView.findViewById(R.id.inputCount);

            holder.view1 = convertView.findViewById(R.id.tex1);
            holder.view2 = convertView.findViewById(R.id.tex2);
            holder.view3 = convertView.findViewById(R.id.tex3);
            holder.mButt = convertView.findViewById(R.id.buttDel1);   // Asegúrate que el ID sea correcto
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Obj item = objList.get(pos);

//        // SOLUCIÓN AL RECICLAJE: Modificamos el Layout interno, NO el convertView entero
//        if (item.visible == 0) {
//            // Reducimos el alto y ancho a 0 para que no deje un hueco vacío en el Grid
//            ViewGroup.LayoutParams params = holder.layout1.getLayoutParams();
//            params.height = 0;
//            params.width = 0;
//            holder.layout1.setLayoutParams(params);
//            holder.layout1.setVisibility(View.GONE);
//        } else {
            // Restauramos sus dimensiones originales para que vuelva a ser visible al reciclarse
            ViewGroup.LayoutParams params = holder.layout1.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT; // O pon el alto fijo en PX de tu diseño (ej: Basic.dpToPx(80))
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            holder.layout1.setLayoutParams(params);
            holder.layout1.setVisibility(View.VISIBLE);


            double mAlert = item.unit == 0 ? GlobalData.glUdsAlert : GlobalData.glMtrAlert;
            // Layout
            if (item.currCount <= 0 || item.maxCount <= 0) {
                holder.layout1.setBackgroundColor(
                        ContextCompat.getColor(holder.layout1.getContext(), R.color.alert_background)
                );
            } else if (item.currCount <= mAlert || item.maxCount <= mAlert) {
                holder.layout1.setBackgroundColor(
                        ContextCompat.getColor(holder.layout1.getContext(), R.color.warning_background)
                );
            } else if (pos == currPos) {

                holder.layout1.setBackgroundColor(
                        ContextCompat.getColor(holder.layout1.getContext(), R.color.selected_background)
                );
            } else {
                holder.layout1.setBackgroundColor(
                        ContextCompat.getColor(holder.layout1.getContext(), R.color.normal_background)
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
            Double count = item.saleCount;

            TextView viewCount = holder.viewcount;

            CurrencyEditText inputCount = holder.inputcount;
            viewCount.setText(MoneyUtls.formatDecimal(count));

            Object mTag = inputCount.getTag();

            if (mTag != null && (int) mTag == pos) {
                inputCount.setText(Basic.getValueFormatter(String.valueOf(count)));
            }

            //  Boton X
            TextView button = holder.mButt;

            if (count > 0) {
                if (inputCount.getVisibility() == View.INVISIBLE) {
                    viewCount.setVisibility(View.VISIBLE);
                }
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
                        if (mitem.saleCount < 1) {
                            mitem.saleCount = 0;
                        } else {
                            mitem.saleCount--;
                        }
                    }
                    mitem.currCount++;
                    if (mitem.currCount > mitem.maxCount) {
                        mitem.currCount = mitem.maxCount;
                    }

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

                // --- COMIENZO DE LA SECCIÓN DE LISTENERS (DENTRO DE getView) ---

                // 1. CLICK EN EL VIEWCOUNT PARA CONMUTAR AL INPUT
                viewCount.setOnClickListener(v -> {
                    if (inputCount.getVisibility() == View.INVISIBLE) {
                        inputCount.setVisibility(View.VISIBLE);
                        viewCount.setVisibility(View.INVISIBLE);

                        // 1. Obtener la posición real (el tag debe ser actualizado en onBind)
                        Object tag = v.getTag();
                        if (tag == null) return;
                        int idx = (int) tag;

                        Obj mitem = objList.get(idx);


                        // Se usa la referencia del ítem actual para rellenar el input antes de mostrarlo
                        if (mitem.saleCount > 0) {
                            inputCount.setText(Basic.getValueFormatter(mitem.saleCount));
                        } else {
                            inputCount.setText("");
                        }

                        inputCount.post(() -> {
                            inputCount.requestFocus();
                            InputMethodManager imm = (InputMethodManager) inputCount.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                imm.showSoftInput(inputCount, InputMethodManager.SHOW_IMPLICIT);
                            }
                        });
                    } else {
                        // Si por alguna razón ya estaba visible, procesamos el valor actual antes de ocultar
                        Object tag = inputCount.getTag();
                        if (tag instanceof Integer) {
                            commitQuantity(inputCount, viewCount, (int) tag);
                        }
                    }
                });

                // 2. DETECTAR EL BOTÓN "LISTO" / "ENTER" DEL TECLADO
                holder.inputcount.setOnEditorActionListener((v, actionId, event) -> {
                    boolean isDone = actionId == EditorInfo.IME_ACTION_DONE;
                    boolean isEnter = event != null
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN;

                    if (!isDone && !isEnter) return false;

                    Object tag = holder.inputcount.getTag();
                    if (!(tag instanceof Integer)) return false;

                    commitQuantity(holder.inputcount, holder.viewcount, (int) tag);
                    return true;
                });

                // 3. DETECTAR LA PÉRDIDA DE FOCO (SCROLL O CLICK FUERA)
                holder.inputcount.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) return;

                    Object tag = holder.inputcount.getTag();
                    if (!(tag instanceof Integer)) return;

                    commitQuantity(holder.inputcount, holder.viewcount, (int) tag);
                });

            // --- FIN DE LA SECCIÓN DE LISTENERS ---


            }
            else {
                viewCount.setVisibility(View.INVISIBLE);

                button.setVisibility(View.INVISIBLE);
                button.setVisibility(View.INVISIBLE);
            }

            // Textos
            String metrTag = item.unit == 0 ? "" : "Por "+glData.unitList.get(item.unit);
            String desc = (item.desc.isEmpty() ? " ("+metrTag+")" : " (" + item.desc + " "+ metrTag +")");
            if(metrTag.isEmpty() && item.desc.isEmpty()){
                desc = "";
            }
            holder.view1.setText(item.name + desc);
            holder.view2.setText("Disponible: " + MoneyUtls.formatDecimal(item.currCount) + "/" + MoneyUtls.formatDecimal(item.maxCount));

            double clcPrice = MathUtls.addPercentage(BigDecimal.valueOf(item.price), BigDecimal.valueOf(item.margen)).doubleValue();

            holder.view3.setText("Precio: " + Basic.getMaskConv(clcPrice, 0) + "/" + Basic.getMaskConv(clcPrice, 1));

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
            inputCount.setTag(pos);


        //}
        return convertView;

    }

    public void updateList(List<Obj> newList) {
        this.objList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public Obj getObjAt(int position) {
        if (position < 0 || position >= objList.size()) return null;
        return objList.get(position);
    }

    public interface OnQuantityChangedListener {
        void onQuantityChanged(Obj item);
    }

    private OnQuantityChangedListener quantityListener;

    public void setOnQuantityChangedListener(OnQuantityChangedListener listener) {
        this.quantityListener = listener;
    }

    private void commitQuantity(CurrencyEditText inputCount, TextView viewCount, int idx) {
        if (idx < 0 || idx >= objList.size()) return;
        if (inputCount.getVisibility() != View.VISIBLE) return;

        Obj mitem = objList.get(idx);
        double value = inputCount.getNumericValue();

        if (value < 0) value = 0;

        if (value <= mitem.maxCount) {
            mitem.saleCount = value;
            mitem.currCount = mitem.maxCount - value;
            // misma referencia que en objListAll → ya queda sincronizado el catálogo
        } else {
            inputCount.setText(Basic.getValueFormatter(mitem.saleCount));
            Msg.m("Valor EXCEDIDO!");
            return;
        }

        inputCount.setVisibility(View.INVISIBLE);
        viewCount.setVisibility(View.VISIBLE);
        viewCount.setText(MoneyUtls.formatDecimal(mitem.saleCount));

        InputMethodManager imm = (InputMethodManager) inputCount.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(inputCount.getWindowToken(), 0);
        }

        // Avisa al Fragment para objListSal + totales
        if (quantityListener != null) {
            quantityListener.onQuantityChanged(mitem);
        }

        notifyDataSetChanged();
    }



}
