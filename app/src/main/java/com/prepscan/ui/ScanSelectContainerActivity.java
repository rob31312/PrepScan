package com.prepscan.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.prepscan.R;
import com.prepscan.data.PrepScanRepository;

import java.util.ArrayList;
import java.util.List;

public class ScanSelectContainerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_select_container);
        setupTopBar(getString(R.string.title_scan_container));

        PrepScanRepository repo = new PrepScanRepository(this);

        ActivityResultLauncher<ScanOptions> qrLauncher = registerForActivityResult(
                new ScanContract(),
                result -> {
                    if (result.getContents() == null) return;
                    String scanned = result.getContents().trim();
                    Intent i = new Intent(this, AddContentsActivity.class);
                    i.putExtra(AddContentsActivity.EXTRA_CONTAINER_ID, scanned);
                    startActivity(i);
                }
        );

        View cameraFrame = findViewById(R.id.cameraFrame);
        if (cameraFrame != null) {
            cameraFrame.setOnClickListener(v -> {
                ScanOptions opt = new ScanOptions();
                opt.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
                opt.setPrompt("Scan container QR");
                opt.setBeepEnabled(true);
                opt.setOrientationLocked(false);
                qrLauncher.launch(opt);
            });
        }

        RecyclerView recycler = findViewById(R.id.recyclerContainers);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        List<ContainerPickRow> rows = new ArrayList<>();
        for (PrepScanRepository.ContainerRow cr : repo.listContainers()) {
            rows.add(new ContainerPickRow(cr.id, (cr.location == null || cr.location.isEmpty()) ? "(no location)" : cr.location));
        }
        if (rows.isEmpty()) {
            rows.add(new ContainerPickRow("FS001", "Example only (no containers yet)"));
        }

        ContainerPickAdapter adapter = new ContainerPickAdapter(rows, row -> {
            Intent i = new Intent(this, AddContentsActivity.class);
            i.putExtra(AddContentsActivity.EXTRA_CONTAINER_ID, row.id);
            startActivity(i);
        });

        recycler.setAdapter(adapter);
    }
}
