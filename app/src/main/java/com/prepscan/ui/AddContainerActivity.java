package com.prepscan.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.zxing.WriterException;
import com.prepscan.R;
import com.prepscan.data.PrepScanRepository;
import com.prepscan.util.QrUtil;

public class AddContainerActivity extends BaseActivity {

    private String currentContainerId = "FS001";
    private String currentContentLetter = "F";
    private String currentContainerLetter = "S";
    private int currentSeq = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_container);
        setupTopBar(getString(R.string.title_add_container));

        PrepScanRepository repo = new PrepScanRepository(this);

        MaterialAutoCompleteTextView spnContent = findViewById(R.id.spnContentType);
        MaterialAutoCompleteTextView spnContainer = findViewById(R.id.spnContainerType);
        EditText txtGenerated = findViewById(R.id.inpGeneratedName);

        EditText inpRoom = findViewById(R.id.inpRoom);
        EditText inpRack = findViewById(R.id.inpRack);
        EditText inpBay = findViewById(R.id.inpBay);
        EditText inpShelf = findViewById(R.id.inpShelf);

        ImageView imgQr = findViewById(R.id.imgQrPreview);

        String[] contentNames = getResources().getStringArray(R.array.content_type_names);
        String[] contentLetters = getResources().getStringArray(R.array.content_type_letters);
        String[] containerNames = getResources().getStringArray(R.array.container_type_names);
        String[] containerLetters = getResources().getStringArray(R.array.container_type_letters);

        if (spnContent != null) {
            spnContent.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contentNames));
            spnContent.setText(contentNames[0], false);
        }
        if (spnContainer != null) {
            spnContainer.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, containerNames));
            spnContainer.setText(containerNames[0], false);
        }

        java.util.function.BiConsumer<String, String> recalcAndSave = (cName, tName) -> {
            String cLetter = "F";
            String tLetter = "S";
            for (int i = 0; i < contentNames.length; i++) {
                if (contentNames[i].equals(cName)) { cLetter = contentLetters[i]; break; }
            }
            for (int i = 0; i < containerNames.length; i++) {
                if (containerNames[i].equals(tName)) { tLetter = containerLetters[i]; break; }
            }

            String id = repo.nextContainerId(cLetter, tLetter);
            currentContainerId = id;
            currentContentLetter = cLetter;
            currentContainerLetter = tLetter;
            try { currentSeq = Integer.parseInt(id.substring(2)); } catch (Exception ignored) {}

            if (txtGenerated != null) txtGenerated.setText(id);

            if (imgQr != null) {
                try {
                    Bitmap qr = QrUtil.makeQrBitmap(id, 600);
                    imgQr.setImageBitmap(qr);
                } catch (WriterException ignored) {}
            }

            repo.upsertContainer(id, cLetter, tLetter, currentSeq,
                    val(inpRoom), val(inpRack), val(inpBay), val(inpShelf));
        };

        // initial
        recalcAndSave.accept(contentNames[0], containerNames[0]);

        if (spnContent != null) {
            spnContent.setOnItemClickListener((parent, view, position, id) -> {
                String c = (String) parent.getItemAtPosition(position);
                String t = spnContainer != null ? spnContainer.getText().toString() : containerNames[0];
                recalcAndSave.accept(c, t);
            });
        }
        if (spnContainer != null) {
            spnContainer.setOnItemClickListener((parent, view, position, id) -> {
                String t = (String) parent.getItemAtPosition(position);
                String c = spnContent != null ? spnContent.getText().toString() : contentNames[0];
                recalcAndSave.accept(c, t);
            });
        }

        TextWatcher autosave = new SimpleTextWatcher(() -> repo.upsertContainer(
                currentContainerId, currentContentLetter, currentContainerLetter, currentSeq,
                val(inpRoom), val(inpRack), val(inpBay), val(inpShelf)
        ));

        if (inpRoom != null) inpRoom.addTextChangedListener(autosave);
        if (inpRack != null) inpRack.addTextChangedListener(autosave);
        if (inpBay != null) inpBay.addTextChangedListener(autosave);
        if (inpShelf != null) inpShelf.addTextChangedListener(autosave);

        MaterialButton btnAddRemove = findViewById(R.id.btnAddRemoveItems);
        MaterialButton btnPrint = findViewById(R.id.btnPrintQr);

        if (btnAddRemove != null) {
            btnAddRemove.setOnClickListener(v -> {
                Intent i = new Intent(this, AddContentsActivity.class);
                i.putExtra(AddContentsActivity.EXTRA_CONTAINER_ID, currentContainerId);
                startActivity(i);
            });
        }

        if (btnPrint != null) {
            btnPrint.setOnClickListener(v -> {
                Intent i = new Intent(this, PrintLabelActivity.class);
                i.putExtra("container_id", currentContainerId);
                startActivity(i);
            });
        }
    }

    private static String val(EditText e) {
        return e == null ? null : e.getText().toString().trim();
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable onChange;
        SimpleTextWatcher(Runnable onChange) { this.onChange = onChange; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) { if (onChange != null) onChange.run(); }
    }
}
