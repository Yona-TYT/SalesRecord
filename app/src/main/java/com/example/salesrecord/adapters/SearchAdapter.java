package com.example.salesrecord.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.R;
import com.example.salesrecord.ThemeHelper;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends BaseAdapter implements Filterable {

    private final Context mContext;
    private final List<String> originalList;
    private List<Integer> filteredIndexes;
    private ListView listView;          // para controlar visibilidad

    public SearchAdapter(Context context, List<String> textList) {
        this.mContext = context;
        this.originalList = textList != null ? textList : new ArrayList<>();
        this.filteredIndexes = new ArrayList<>();

        // Al inicio mostramos todos
        for (int i = 0; i < originalList.size(); i++) {
            filteredIndexes.add(i);
        }
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }

    @Override
    public int getCount() {
        return filteredIndexes.size();
    }

    @Override
    public Object getItem(int position) {
        int originalIndex = filteredIndexes.get(position);
        return originalList.get(originalIndex);
    }

    @Override
    public long getItemId(int position) {
        return filteredIndexes.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // 1. Obtenemos el tema dinámico de la actividad activa
            int mStyle = ThemeHelper.getManifestThemeId(AppContextProvider.getCurrentActivity());
            ContextThemeWrapper themedContext = new ContextThemeWrapper(mContext, mStyle);

            // 2. Inflamos el diseño XML usando el contexto estilizado
            convertView = LayoutInflater.from(themedContext).inflate(R.layout.item_search, parent, false);

            // 3. Mapeamos los elementos al ViewHolder estático
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.inner_text_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 4. Vinculación de tus datos originales
        int originalIndex = filteredIndexes.get(position);
        String item = originalList.get(originalIndex);
        holder.textView.setText(item);

        return convertView;
    }



    private int dpToPx(int dp) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private static class ViewHolder {
        TextView textView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Integer> filtered = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    // Sin texto → mostrar todos
                    for (int i = 0; i < originalList.size(); i++) {
                        filtered.add(i);
                    }
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (int i = 0; i < originalList.size(); i++) {
                        String data = originalList.get(i);
                        if (data != null && data.toLowerCase().contains(filterPattern)) {
                            filtered.add(i);
                        }
                    }
                }

                results.values = filtered;
                results.count = filtered.size();
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredIndexes = (List<Integer>) results.values;
                notifyDataSetChanged();

                // Control de visibilidad
                if (listView != null) {
                    if (results.count > 0 && constraint != null && constraint.length() > 0) {
                        listView.setVisibility(View.VISIBLE);
                    } else {
                        listView.setVisibility(View.GONE);
                    }
                }
            }
        };
    }
}