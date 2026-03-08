package com.prepscan.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.prepscan.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MaterialButton btnAdd = findViewById(R.id.btnAddContainer);
        MaterialButton btnScan = findViewById(R.id.btnScanContainer);
        MaterialButton btnInv = findViewById(R.id.btnInventory);

        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddContainerActivity.class)));
        btnScan.setOnClickListener(v -> startActivity(new Intent(this, ScanSelectContainerActivity.class)));
        btnInv.setOnClickListener(v -> startActivity(new Intent(this, InventoryActivity.class)));
    }
}
