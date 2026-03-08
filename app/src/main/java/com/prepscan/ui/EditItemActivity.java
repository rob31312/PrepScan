package com.prepscan.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.prepscan.R;
import com.prepscan.data.PrepScanRepository;

import java.io.File;

public class EditItemActivity extends BaseActivity {

    public static final String EXTRA_BARCODE = "barcode";
    public static final String EXTRA_CONTAINER_ID = "container_id";

    private String barcode;
    private String containerId;

    private Uri photoUri = null;
    private int qty = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        setupTopBar(getString(R.string.title_edit_item));

        PrepScanRepository repo = new PrepScanRepository(this);

        barcode = getIntent() != null ? getIntent().getStringExtra(EXTRA_BARCODE) : null;
        containerId = getIntent() != null ? getIntent().getStringExtra(EXTRA_CONTAINER_ID) : null;

        TextView txtBarcode = findViewById(R.id.txtBarcode);
        ImageView imgItem = findViewById(R.id.imgItem);
        MaterialButton btnTakePhoto = findViewById(R.id.btnTakePhoto);

        EditText inpName = findViewById(R.id.inpName);
        EditText inpDesc = findViewById(R.id.inpDesc);

        TextView txtQty = findViewById(R.id.txtQty);
        MaterialButton btnMinus = findViewById(R.id.btnQtyMinus);
        MaterialButton btnPlus = findViewById(R.id.btnQtyPlus);

        MaterialButton btnSave = findViewById(R.id.btnDone);

        if (barcode == null || barcode.trim().isEmpty()) {
            Toast.makeText(this, "No barcode provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtBarcode.setText("Barcode: " + barcode);

        // If item already exists, prefill fields
        PrepScanRepository.Item existing = repo.getItem(barcode);
        if (existing != null) {
            if (existing.name != null) inpName.setText(existing.name);
            if (existing.description != null) inpDesc.setText(existing.description);
            if (existing.photoUri != null) {
                try {
                    photoUri = Uri.parse(existing.photoUri);
                    imgItem.setImageURI(photoUri);
                } catch (Exception ignored) {}
            }
        }

        ActivityResultLauncher<Uri> takePicture = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && photoUri != null) {
                        imgItem.setImageURI(photoUri);
                    }
                }
        );

        btnTakePhoto.setOnClickListener(v -> {
            try {
                File f = new File(getCacheDir(), "item_" + barcode + "_" + System.currentTimeMillis() + ".jpg");
                photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", f);
                takePicture.launch(photoUri);
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open camera.", Toast.LENGTH_SHORT).show();
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (qty > 1) qty--;
            txtQty.setText(String.valueOf(qty));
        });

        btnPlus.setOnClickListener(v -> {
            qty++;
            txtQty.setText(String.valueOf(qty));
        });

        btnSave.setOnClickListener(v -> {
            String name = inpName.getText() != null ? inpName.getText().toString().trim() : null;
            String desc = inpDesc.getText() != null ? inpDesc.getText().toString().trim() : null;
            String photo = photoUri != null ? photoUri.toString() : null;

            repo.upsertItem(barcode, name, desc, photo);

            if (containerId != null && !containerId.trim().isEmpty()) {
                repo.setContainerItemQty(containerId, barcode, qty);
            }

            setResult(RESULT_OK);
            finish();
        });
    }
}
