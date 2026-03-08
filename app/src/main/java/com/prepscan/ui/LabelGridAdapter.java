package com.prepscan.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prepscan.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LabelGridAdapter extends RecyclerView.Adapter<LabelGridAdapter.VH> {

    private final List<LabelCell> cells;
    private final Set<Integer> selectedIndex1Based = new HashSet<>();

    public LabelGridAdapter(List<LabelCell> cells) {
        this.cells = cells;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_label_cell, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        LabelCell cell = cells.get(position);
        holder.txt.setText(String.valueOf(cell.index1Based));
        holder.itemView.setSelected(cell.selected);

        holder.itemView.setOnClickListener(v -> {
            cell.selected = !cell.selected;
            holder.itemView.setSelected(cell.selected);

            if (cell.selected) selectedIndex1Based.add(cell.index1Based);
            else selectedIndex1Based.remove(cell.index1Based);

            notifyItemChanged(holder.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    public List<Integer> getSelectedIndices() {
        return new ArrayList<>(selectedIndex1Based);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txt;
        VH(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.txtLabelIndex);
        }
    }
}
