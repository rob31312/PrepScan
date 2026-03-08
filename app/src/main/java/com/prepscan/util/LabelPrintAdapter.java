package com.prepscan.util;

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

import java.io.FileOutputStream;
import java.io.IOException;

public class LabelPrintAdapter extends PrintDocumentAdapter {

    private final String jobName;
    private final String containerId;
    private final Bitmap qr;
    private final int labelIndex1Based;

    public LabelPrintAdapter(String jobName, String containerId, Bitmap qr, int labelIndex1Based) {
        this.jobName = jobName;
        this.containerId = containerId;
        this.qr = qr;
        this.labelIndex1Based = labelIndex1Based;
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
        PdfDocument pdf = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(612, 792, 1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        float labelW = 108f, labelH = 108f;
        float left = (612f - 4f * labelW) / 2f;
        float top = (792f - 6f * labelH) / 2f;

        int idx = Math.max(1, Math.min(24, labelIndex1Based)) - 1;
        int row = idx / 4;
        int col = idx % 4;

        float x = left + col * labelW;
        float y = top + row * labelH;

        Paint border = new Paint();
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(1f);

        Paint text = new Paint();
        text.setTextSize(12f);
        text.setFakeBoldText(true);

        RectF rect = new RectF(x, y, x + labelW, y + labelH);
        canvas.drawRect(rect, border);

        float pad = 6f;
        float qrSize = 80f;
        Bitmap scaled = Bitmap.createScaledBitmap(qr, (int) qrSize, (int) qrSize, true);
        canvas.drawBitmap(scaled, x + pad, y + pad, null);
        canvas.drawText(containerId, x + pad, y + pad + qrSize + 16f, text);

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
