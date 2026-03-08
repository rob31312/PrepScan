package com.prepscan.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;

import com.prepscan.ui.SetupPrinterActivity;

import java.io.FileOutputStream;
import java.io.IOException;

public class LabelCalibrationPrintAdapter extends PrintDocumentAdapter {

    private final Context ctx;

    public LabelCalibrationPrintAdapter(Context ctx) {
        this.ctx = ctx.getApplicationContext();
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal,
                         LayoutResultCallback callback, android.os.Bundle extras) {

        PrintDocumentInfo info = new PrintDocumentInfo.Builder("PrepScan Label Calibration")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build();
        callback.onLayoutFinished(info, true);
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                        CancellationSignal cancellationSignal,
                        WriteResultCallback callback) {

        SharedPreferences sp = ctx.getSharedPreferences(SetupPrinterActivity.PREFS, Context.MODE_PRIVATE);
        float left = sp.getFloat(SetupPrinterActivity.K_LEFT, 90f);
        float top = sp.getFloat(SetupPrinterActivity.K_TOP, 72f);
        float labelW = sp.getFloat(SetupPrinterActivity.K_LABEL_W, 108f);
        float labelH = sp.getFloat(SetupPrinterActivity.K_LABEL_H, 108f);
        float hGap = sp.getFloat(SetupPrinterActivity.K_HGAP, 0f);
        float vGap = sp.getFloat(SetupPrinterActivity.K_VGAP, 0f);

        PdfDocument pdf = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(612, 792, 1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint border = new Paint();
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(1f);

        Paint text = new Paint();
        text.setTextSize(10f);

        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 4; c++) {
                float x = left + c * (labelW + hGap);
                float y = top + r * (labelH + vGap);
                RectF rect = new RectF(x, y, x + labelW, y + labelH);
                canvas.drawRect(rect, border);
                canvas.drawText((r*4 + c + 1) + "", x + 4f, y + 12f, text);
            }
        }

        pdf.finishPage(page);

        try (FileOutputStream out = new FileOutputStream(destination.getFileDescriptor())) {
            pdf.writeTo(out);
        } catch (IOException e) {
            callback.onWriteFailed(e.getMessage());
            pdf.close();
            return;
        }

        pdf.close();
        callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
    }
}
