package com.prepscan.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LabelPrintAdapterMulti extends PrintDocumentAdapter {

    private final Context ctx;
    private final String jobName;
    private final String containerId;
    private final Bitmap qr;
    private final Set<Integer> labelIndices1Based;

    public LabelPrintAdapterMulti(Context ctx, String jobName, String containerId, Bitmap qr, List<Integer> labelIndices1Based) {
        this.ctx = ctx.getApplicationContext();
        this.jobName = jobName;
        this.containerId = containerId;
        this.qr = qr;
        this.labelIndices1Based = new HashSet<>();
        if (labelIndices1Based != null) this.labelIndices1Based.addAll(labelIndices1Based);
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal,
                         LayoutResultCallback callback, android.os.Bundle extras) {

        PrintDocumentInfo info = new PrintDocumentInfo.Builder(jobName)
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
        text.setTextSize(12f);
        text.setFakeBoldText(true);

        float pad = 6f;
        float qrSize = Math.min(80f, Math.min(labelW, labelH) - 2f * pad);
        Bitmap scaledQr = Bitmap.createScaledBitmap(qr, (int) qrSize, (int) qrSize, true);

        for (Integer idx1 : labelIndices1Based) {
            if (idx1 == null) continue;
            int idx = Math.max(1, Math.min(24, idx1)) - 1;
            int row = idx / 4;
            int col = idx % 4;

            float x = left + col * (labelW + hGap);
            float y = top + row * (labelH + vGap);

            RectF rect = new RectF(x, y, x + labelW, y + labelH);
            canvas.drawRect(rect, border);

            canvas.drawBitmap(scaledQr, x + pad, y + pad, null);
            canvas.drawText(containerId, x + pad, y + pad + qrSize + 16f, text);
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
