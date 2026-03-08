package com.prepscan.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prepscan.R;

import java.util.List;

public class ContainerPickAdapter extends RecyclerView.Adapter<ContainerPickAdapter.VH> {

    public interface OnPickListener {
        void onPick(ContainerPickRow row);
    }

    private final List<ContainerPickRow> rows;
    private final OnPickListener listener;

    public ContainerPickAdapter(List<ContainerPickRow> rows, OnPickListener listener) {
        this.rows = rows;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_container_select, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ContainerPickRow row = rows.get(position);
        holder.txtId.setText(row.id);
        holder.txtLoc.setText(row.location);
        holder.itemView.setOnClickListener(v -> listener.onPick(row));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtId;
        TextView txtLoc;

        VH(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.txtContainerId);
            txtLoc = itemView.findViewById(R.id.txtContainerLoc);
        }
    }
}
