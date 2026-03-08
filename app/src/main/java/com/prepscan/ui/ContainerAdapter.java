package com.prepscan.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prepscan.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContainerAdapter extends RecyclerView.Adapter<ContainerAdapter.VH> {

    private final List<String> rows;
    private final Set<Integer> selectedPositions = new HashSet<>();

    public ContainerAdapter(List<String> rows) {
        this.rows = rows;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_container_checkbox, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String text = rows.get(position);
        holder.chk.setOnCheckedChangeListener(null);
        holder.chk.setText(text);
        holder.chk.setChecked(selectedPositions.contains(position));

        holder.chk.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedPositions.add(holder.getBindingAdapterPosition());
            else selectedPositions.remove(holder.getBindingAdapterPosition());
        });

        holder.itemView.setOnClickListener(v -> holder.chk.toggle());
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    public int getSelectedCount() {
        return selectedPositions.size();
    }

    public Set<Integer> getSelectedPositions() {
        return new HashSet<>(selectedPositions);
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox chk;
        VH(@NonNull View itemView) {
            super(itemView);
            chk = itemView.findViewById(R.id.chkContainer);
        }
    }
}
