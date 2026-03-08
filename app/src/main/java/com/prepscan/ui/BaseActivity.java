package com.prepscan.ui;

import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.prepscan.R;

public abstract class BaseActivity extends AppCompatActivity {

    protected void setupTopBar(String title) {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar == null) return;

        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(v -> finish());

        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_topbar);
        toolbar.setOnMenuItemClickListener(this::onTopBarMenuItem);
    }

    protected void setupTopBarWithMenu(String title, int menuRes) {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar == null) return;

        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(v -> finish());

        toolbar.getMenu().clear();
        toolbar.inflateMenu(menuRes);
        toolbar.setOnMenuItemClickListener(this::onTopBarMenuItem);
    }

    protected boolean onContainerOptionsSelected() {
        return false;
    }

    private boolean onTopBarMenuItem(MenuItem item) {
        if (item.getItemId() == R.id.action_home) {
            Intent i = new Intent(this, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            return true;
        }

        if (item.getItemId() == R.id.action_container_options) {
            return onContainerOptionsSelected();
        }

        return false;
    }
}
