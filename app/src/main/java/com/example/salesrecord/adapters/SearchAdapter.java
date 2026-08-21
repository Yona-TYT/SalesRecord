package com.example.salesrecord.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends BaseAdapter implements Filterable {

    private final Context mContext;
    private final List<String> originalList;      // Lista original
    private List<Integer> filteredIndexes;        // Índices filtrados

    public SearchAdapter(Context context, List<String> textList) {
        this.mContext = context;
        this.originalList = textList != null ? textList : new ArrayList<>();
        this.filteredIndexes = new ArrayList<>();

        // Al inicio mostramos todos
        for (int i = 0; i < originalList.size(); i++) {
            filteredIndexes.add(i);
        }
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
            LinearLayout layout = new LinearLayout(mContext);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(10, 8, 10, 8);

            TextView text = new TextView(mContext);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setGravity(Gravity.CENTER);
            text.setTextSize(18);
            text.setPadding(10, 5, 10, 5);

            layout.addView(text);

            holder = new ViewHolder();
            holder.textView = text;
            layout.setTag(holder);

            convertView = layout;
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int originalIndex = filteredIndexes.get(position);
        String item = originalList.get(originalIndex);

        holder.textView.setText(item);

        return convertView;
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
            }
        };
    }
}