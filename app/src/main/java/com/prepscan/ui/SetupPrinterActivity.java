package com.prepscan.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.print.PrintManager;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.prepscan.R;
import com.prepscan.util.LabelCalibrationPrintAdapter;

public class SetupPrinterActivity extends BaseActivity {

    public static final String PREFS = "prepscan_print_prefs";
    public static final String K_LEFT = "left";
    public static final String K_TOP = "top";
    public static final String K_LABEL_W = "label_w";
    public static final String K_LABEL_H = "label_h";
    public static final String K_HGAP = "hgap";
    public static final String K_VGAP = "vgap";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_setup);
        setupTopBar(getString(R.string.title_printer_setup));

        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);

        TextInputEditText inpLeft = findViewById(R.id.inpLeft);
        TextInputEditText inpTop = findViewById(R.id.inpTop);
        TextInputEditText inpW = findViewById(R.id.inpLabelW);
        TextInputEditText inpH = findViewById(R.id.inpLabelH);
        TextInputEditText inpHGap = findViewById(R.id.inpHGap);
        TextInputEditText inpVGap = findViewById(R.id.inpVGap);

        // Defaults: centered 6x4 grid of 1.5in labels (108pt). No gaps.
        inpLeft.setText(String.valueOf(sp.getFloat(K_LEFT, 90f)));
        inpTop.setText(String.valueOf(sp.getFloat(K_TOP, 72f)));
        inpW.setText(String.valueOf(sp.getFloat(K_LABEL_W, 108f)));
        inpH.setText(String.valueOf(sp.getFloat(K_LABEL_H, 108f)));
        inpHGap.setText(String.valueOf(sp.getFloat(K_HGAP, 0f)));
        inpVGap.setText(String.valueOf(sp.getFloat(K_VGAP, 0f)));

        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            sp.edit()
                    .putFloat(K_LEFT, parse(inpLeft, 90f))
                    .putFloat(K_TOP, parse(inpTop, 72f))
                    .putFloat(K_LABEL_W, parse(inpW, 108f))
                    .putFloat(K_LABEL_H, parse(inpH, 108f))
                    .putFloat(K_HGAP, parse(inpHGap, 0f))
                    .putFloat(K_VGAP, parse(inpVGap, 0f))
                    .apply();

            Toast.makeText(this, "Saved.", Toast.LENGTH_SHORT).show();
        });

        MaterialButton btnTest = findViewById(R.id.btnTestPrint);
        btnTest.setOnClickListener(v -> {
            PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
            if (pm != null) {
                pm.print("PrepScan Label Calibration", new LabelCalibrationPrintAdapter(this), null);
            }
        });
    }

    private static float parse(TextInputEditText e, float def) {
        try {
            if (e.getText() == null) return def;
            String s = e.getText().toString().trim();
            if (s.isEmpty()) return def;
            return Float.parseFloat(s);
        } catch (Exception ex) {
            return def;
        }
    }
}
