package com.prepscan.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.prepscan.R;
import com.prepscan.data.PrepScanRepository;

import java.util.ArrayList;

public class AddContentsActivity extends BaseActivity {

    public static final String EXTRA_CONTAINER_ID = "container_id";

    private String containerId = "FS001";

    private PrepScanRepository repo;
    private final java.util.List<PrepScanRepository.ContainerItemRow> rows = new ArrayList<>();
    private ContentsAdapter adapter;

    private ActivityResultLauncher<Intent> editItemLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contents);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_CONTAINER_ID)) {
            String incoming = getIntent().getStringExtra(EXTRA_CONTAINER_ID);
            if (incoming != null && !incoming.trim().isEmpty()) containerId = incoming.trim();
        }

        setupTopBarWithMenu("Edit Contents, " + containerId, R.menu.menu_topbar_contents);

        repo = new PrepScanRepository(this);

        RecyclerView recycler = findViewById(R.id.recyclerContents);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        rows.clear();
        rows.addAll(repo.listContainerItems(containerId));
        adapter = new ContentsAdapter(rows, repo, containerId, () -> {
            // open edit item without barcode not supported
        });
        recycler.setAdapter(adapter);

        editItemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        refreshList();
                    }
                }
        );

        ActivityResultLauncher<ScanOptions> itemLauncher = registerForActivityResult(
                new ScanContract(),
                result -> {
                    if (result.getContents() == null) return;
                    String barcode = result.getContents().trim();

                    PrepScanRepository.Item item = repo.getItem(barcode);
                    if (item == null) {
                        Intent i = new Intent(this, EditItemActivity.class);
                        i.putExtra(EditItemActivity.EXTRA_BARCODE, barcode);
                        i.putExtra(EditItemActivity.EXTRA_CONTAINER_ID, containerId);
                        editItemLauncher.launch(i);
                        return;
                    }

                    // Increment qty for existing item
                    int newQty = 1;
                    for (PrepScanRepository.ContainerItemRow r : repo.listContainerItems(containerId)) {
                        if (r.barcode.equals(barcode)) { newQty = r.qty + 1; break; }
                    }
                    repo.setContainerItemQty(containerId, barcode, newQty);
                    refreshList();
                }
        );

        View cameraFrame = findViewById(R.id.cameraFrame);
        if (cameraFrame != null) {
            cameraFrame.setOnClickListener(v -> {
                ScanOptions opt = new ScanOptions();
                opt.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
                opt.setPrompt("Scan item barcode");
                opt.setBeepEnabled(true);
                opt.setOrientationLocked(false);
                itemLauncher.launch(opt);
            });
        }
    }

    @Override
    protected boolean onContainerOptionsSelected() {
        showContainerOptionsSheet();
        return true;
    }

    private void showContainerOptionsSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_container_options, null, false);

        sheet.findViewById(R.id.btnEditLocation).setOnClickListener(v -> {
            dialog.dismiss();
            showEditLocationDialog();
        });

        sheet.findViewById(R.id.btnPrintQrCode).setOnClickListener(v -> {
            dialog.dismiss();
            Intent i = new Intent(this, PrintLabelActivity.class);
            i.putExtra("container_id", containerId);
            startActivity(i);
        });

        sheet.findViewById(R.id.btnDeleteContainer).setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirm();
        });

        dialog.setContentView(sheet);
        dialog.show();
    }

    private void showEditLocationDialog() {
    View v = LayoutInflater.from(this).inflate(R.layout.dialog_edit_location, null, false);

    com.google.android.material.textfield.TextInputEditText inpRoom = v.findViewById(R.id.inpRoom);
    com.google.android.material.textfield.TextInputEditText inpRack = v.findViewById(R.id.inpRack);
    com.google.android.material.textfield.TextInputEditText inpBay = v.findViewById(R.id.inpBay);
    com.google.android.material.textfield.TextInputEditText inpShelf = v.findViewById(R.id.inpShelf);

    PrepScanRepository.ContainerInfo info = repo.getContainer(containerId);
    if (info != null) {
        if (inpRoom != null) inpRoom.setText(info.room);
        if (inpRack != null) inpRack.setText(info.rack);
        if (inpBay != null) inpBay.setText(info.bay);
        if (inpShelf != null) inpShelf.setText(info.shelf);
    }

    new AlertDialog.Builder(this)
            .setTitle("Edit location, " + containerId)
            .setView(v)
            .setPositiveButton("Done", (d, which) -> {
                String room = inpRoom != null && inpRoom.getText()!=null ? inpRoom.getText().toString().trim() : null;
                String rack = inpRack != null && inpRack.getText()!=null ? inpRack.getText().toString().trim() : null;
                String bay = inpBay != null && inpBay.getText()!=null ? inpBay.getText().toString().trim() : null;
                String shelf = inpShelf != null && inpShelf.getText()!=null ? inpShelf.getText().toString().trim() : null;

                repo.updateContainerLocation(containerId, room, rack, bay, shelf);
                refreshList();
                Toast.makeText(this, "Location updated.", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
}

private void showDeleteConfirm() {
        new AlertDialog.Builder(this)
                .setTitle("Delete container?")
                .setMessage("Delete " + containerId + " and its contents?")
                .setPositiveButton("Delete", (d, which) -> {
                    repo.deleteContainer(containerId);
                    Toast.makeText(this, "Deleted container: " + containerId, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshList() {
        rows.clear();
        rows.addAll(repo.listContainerItems(containerId));
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}
