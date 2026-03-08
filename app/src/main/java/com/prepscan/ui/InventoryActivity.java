package com.prepscan.ui;

import android.os.Bundle;
import android.print.PrintManager;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.prepscan.R;
import com.prepscan.data.PrepScanRepository;
import com.prepscan.util.InventoryReportPrintAdapter;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        setupTopBar(getString(R.string.title_inventory));

        RecyclerView recycler = findViewById(R.id.recyclerContainers);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        PrepScanRepository repo = new PrepScanRepository(this);

        List<String> list = new ArrayList<>();
        for (PrepScanRepository.ContainerRow cr : repo.listContainers()) {
            String line = (cr.location == null || cr.location.isEmpty()) ? cr.id : (cr.id + "  (" + cr.location + ")");
            list.add(line);
        }
        if (list.isEmpty()) list.add("No containers yet");

        ContainerAdapter adapter = new ContainerAdapter(list);
        recycler.setAdapter(adapter);

        MaterialButton btnPrint = findViewById(R.id.btnPrintContainers);
        btnPrint.setOnClickListener(v -> {
            int count = adapter.getSelectedCount();
            if (count <= 0) {
                Toast.makeText(this, "Select at least one container.", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> selectedIds = new ArrayList<>();
            for (Integer pos : adapter.getSelectedPositions()) {
                if (pos == null || pos < 0 || pos >= list.size()) continue;
                String line = list.get(pos);
                String id = line.split("\\s+")[0];
                if (id.equalsIgnoreCase("No")) continue;
                selectedIds.add(id);
            }

            if (selectedIds.isEmpty()) {
                Toast.makeText(this, "No valid containers selected.", Toast.LENGTH_SHORT).show();
                return;
            }

            PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
            if (pm != null) {
                pm.print("PrepScan Inventory",
                        new InventoryReportPrintAdapter(this, "PrepScan Inventory", selectedIds),
                        null);
            }
        });
    }
}
