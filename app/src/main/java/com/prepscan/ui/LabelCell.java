package com.prepscan.ui;

public class LabelCell {
    public final int index1Based;   // 1..24
    public boolean selected;

    public LabelCell(int index1Based) {
        this.index1Based = index1Based;
        this.selected = false;
    }
}
