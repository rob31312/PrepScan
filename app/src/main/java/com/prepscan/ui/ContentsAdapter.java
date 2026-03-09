package com.prepscan.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.prepscan.R;
import com.prepscan.data.PrepScanRepository;

import java.util.List;

public class ContentsAdapter extends RecyclerView.Adapter<ContentsAdapter.VH> {

    public interface OnUnknownBarcodeFlow {
        void openEditItem(String barcode);
    }

    private final List<PrepScanRepository.ContainerItemRow> rows;
    private final PrepScanRepository repo;
    private final String containerId;
    private final OnUnknownBarcodeFlow flow;

    public ContentsAdapter(List<PrepScanRepository.ContainerItemRow> rows, PrepScanRepository repo, String containerId, OnUnknownBarcodeFlow flow) {
        this.rows = rows;
        this.repo = repo;
        this.containerId = containerId;
        this.flow = flow;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_content_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        PrepScanRepository.ContainerItemRow row = rows.get(position);
        holder.txtName.setText(row.displayName);
        holder.txtQty.setText(String.valueOf(row.qty));

        holder.btnMinus.setOnClickListener(v -> {
            if (row.qty > 0) row.qty--;
            if (repo != null) repo.setContainerItemQty(containerId, row.barcode, row.qty);
            holder.txtQty.setText(String.valueOf(row.qty));
        });

        holder.btnPlus.setOnClickListener(v -> {
            row.qty++;
            if (repo != null) repo.setContainerItemQty(containerId, row.barcode, row.qty);
            

        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(vv -> {
                int p = holder.getBindingAdapterPosition();
                if (p == RecyclerView.NO_POSITION) return;
                if (repo != null) repo.deleteContainerItem(containerId, row.barcode);
                rows.remove(p);
                notifyItemRemoved(p);
            });
        }
holder.txtQty.setText(String.valueOf(row.qty));
        });

        holder.txtName.setOnClickListener(v -> flow.openEditItem(row.barcode));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView txtQty;
        MaterialButton btnMinus;
        MaterialButton btnPlus;
        MaterialButton btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtItemName);
            txtQty = itemView.findViewById(R.id.txtQty);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
