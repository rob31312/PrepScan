package com.prepscan.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;

import com.prepscan.data.PrepScanRepository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class InventoryReportPrintAdapter extends PrintDocumentAdapter {

    private final Context ctx;
    private final String jobName;
    private final List<String> containerIds;

    public InventoryReportPrintAdapter(Context ctx, String jobName, List<String> containerIds) {
        this.ctx = ctx.getApplicationContext();
        this.jobName = jobName;
        this.containerIds = containerIds;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal,
                         LayoutResultCallback callback, android.os.Bundle extras) {

        PrintDocumentInfo info = new PrintDocumentInfo.Builder(jobName)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build();
        callback.onLayoutFinished(info, true);
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                        CancellationSignal cancellationSignal,
                        WriteResultCallback callback) {

        PrepScanRepository repo = new PrepScanRepository(ctx);

        PdfDocument pdf = new PdfDocument();

        final int pageW = 612;
        final int pageH = 792;
        final float margin = 36f;

        Paint title = new Paint();
        title.setTextSize(16f);
        title.setFakeBoldText(true);

        Paint header = new Paint();
        header.setTextSize(13f);
        header.setFakeBoldText(true);

        Paint body = new Paint();
        body.setTextSize(12f);

        final float[] y = new float[]{margin};
        final int[] pageNum = new int[]{1};

        PdfDocument.Page[] page = new PdfDocument.Page[]{null};
        Canvas[] canvas = new Canvas[]{null};

        Runnable newPage = () -> {
            if (page[0] != null) pdf.finishPage(page[0]);

            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(pageW, pageH, pageNum[0]).create();
            page[0] = pdf.startPage(info);
            canvas[0] = page[0].getCanvas();

            y[0] = margin;
            canvas[0].drawText("PrepScan Inventory Report", margin, y[0], title);
            y[0] += 22f;
            canvas[0].drawText("Selected containers and contents", margin, y[0], body);
            y[0] += 26f;

            pageNum[0] += 1;
        };

        newPage.run();

        for (String containerId : containerIds) {
            if (cancellationSignal.isCanceled()) {
                pdf.close();
                callback.onWriteCancelled();
                return;
            }

            if (y[0] > pageH - margin - 80f) {
                newPage.run();
            }

            canvas[0].drawText(containerId, margin, y[0], header);
            y[0] += 18f;

            List<PrepScanRepository.ContainerItemRow> items = repo.listContainerItems(containerId);
            if (items.isEmpty()) {
                canvas[0].drawText("(No items)", margin + 14f, y[0], body);
                y[0] += 16f;
            } else {
                for (PrepScanRepository.ContainerItemRow row : items) {
                    if (y[0] > pageH - margin - 20f) {
                        newPage.run();
                        canvas[0].drawText(containerId + " (cont.)", margin, y[0], header);
                        y[0] += 18f;
                    }
                    String line = "• " + row.displayName + "  x" + row.qty;
                    canvas[0].drawText(line, margin + 14f, y[0], body);
                    y[0] += 16f;
                }
            }

            y[0] += 10f;
        }

        if (page[0] != null) pdf.finishPage(page[0]);

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
