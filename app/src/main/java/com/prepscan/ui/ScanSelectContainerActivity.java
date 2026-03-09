package com.prepscan.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.prepscan.R;
import com.prepscan.data.PrepScanRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScanSelectContainerActivity extends BaseActivity {

    private String lastScan = null;
    private long lastScanTs = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_select_container);
        setupTopBar(getString(R.string.title_scan_container));

        PrepScanRepository repo = new PrepScanRepository(this);

        // Live QR scanning inside the preview box
        DecoratedBarcodeView qrView = findViewById(R.id.qrView);
        if (qrView != null) {
            qrView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(Collections.singletonList(BarcodeFormat.QR_CODE)));
            qrView.decodeContinuous(new BarcodeCallback() {
                @Override
                public void barcodeResult(BarcodeResult result) {
                    if (result == null || result.getText() == null) return;
                    String code = result.getText().trim();
                    if (code.isEmpty()) return;

                    long now = System.currentTimeMillis();
                    if (code.equals(lastScan) && (now - lastScanTs) < 1500) return;
                    lastScan = code;
                    lastScanTs = now;

                    openContainer(code);
                }
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

        ContainerPickAdapter adapter = new ContainerPickAdapter(rows, row -> openContainer(row.id));
        recycler.setAdapter(adapter);
    }

    private void openContainer(String containerId) {
        Intent i = new Intent(this, AddContentsActivity.class);
        i.putExtra(AddContentsActivity.EXTRA_CONTAINER_ID, containerId);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DecoratedBarcodeView bv = findViewById(R.id.qrView);
        if (bv != null) bv.resume();
    }

    @Override
    protected void onPause() {
        DecoratedBarcodeView bv = findViewById(R.id.qrView);
        if (bv != null) bv.pause();
        super.onPause();
    }
}
