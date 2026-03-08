package com.prepscan.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prepscan.R;

import java.util.List;

public class SimpleNumberAdapter extends RecyclerView.Adapter<SimpleNumberAdapter.VH> {
    private final List<Integer> nums;
    public SimpleNumberAdapter(List<Integer> nums) { this.nums = nums; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_label_cell, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.txt.setText(String.valueOf(nums.get(position)));
    }

    @Override
    public int getItemCount() { return nums.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txt;
        VH(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.txtLabelIndex);
        }
    }
}
