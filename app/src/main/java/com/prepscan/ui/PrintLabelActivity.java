package com.prepscan.ui;

import android.os.Bundle;
import android.print.PrintManager;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.zxing.WriterException;
import com.prepscan.R;
import com.prepscan.util.LabelPrintAdapterMulti;
import com.prepscan.util.QrUtil;

import java.util.ArrayList;
import java.util.List;

public class PrintLabelActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_label);
        setupTopBar(getString(R.string.title_print_label));

        String containerId = getIntent() != null ? getIntent().getStringExtra("container_id") : "FS001";

        RecyclerView recycler = findViewById(R.id.recyclerLabels);
        recycler.setLayoutManager(new GridLayoutManager(this, 4));

        List<LabelCell> cells = new ArrayList<>();
        for (int i = 1; i <= 24; i++) cells.add(new LabelCell(i));

        LabelGridAdapter adapter = new LabelGridAdapter(cells);
        recycler.setAdapter(adapter);

        MaterialButton btnSetup = findViewById(R.id.btnSetupPrinter);
        if (btnSetup != null) {
            btnSetup.setOnClickListener(v -> startActivity(new android.content.Intent(this, SetupPrinterActivity.class)));
        }

        MaterialButton btnPrint = findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(v -> {
            List<Integer> selected = adapter.getSelectedIndices();
            if (selected == null || selected.isEmpty()) {
                Toast.makeText(this, "Select one or more label positions.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                android.graphics.Bitmap qr = QrUtil.makeQrBitmap(containerId, 600);
                PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                if (pm != null) {
                    pm.print("PrepScan QR Labels",
                            new LabelPrintAdapterMulti(this, "PrepScan QR Labels", containerId, qr, selected),
                            null);
                }
            } catch (WriterException e) {
                Toast.makeText(this, "QR generation failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
