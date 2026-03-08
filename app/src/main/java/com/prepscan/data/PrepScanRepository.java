package com.prepscan.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PrepScanRepository {

    private final PrepScanDbHelper helper;

    public PrepScanRepository(Context ctx) {
        helper = new PrepScanDbHelper(ctx.getApplicationContext());
    }

    private SQLiteDatabase db() {
        return helper.getWritableDatabase();
    }

    public String nextContainerId(String contentLetter, String containerLetter) {
        int next = 1;
        try (Cursor c = db().rawQuery(
                "SELECT MAX(seq_num) FROM containers WHERE content_letter=? AND container_letter=?",
                new String[]{contentLetter, containerLetter})) {
            if (c.moveToFirst() && !c.isNull(0)) next = c.getInt(0) + 1;
        }
        return contentLetter + containerLetter + String.format("%03d", next);
    }

    public void upsertContainer(String id, String contentLetter, String containerLetter, int seqNum,
                                @Nullable String room, @Nullable String rack, @Nullable String bay, @Nullable String shelf) {
        long now = System.currentTimeMillis();
        ContentValues v = new ContentValues();
        v.put("id", id);
        v.put("content_letter", contentLetter);
        v.put("container_letter", containerLetter);
        v.put("seq_num", seqNum);
        v.put("room", room);
        v.put("rack", rack);
        v.put("bay", bay);
        v.put("shelf", shelf);
        v.put("updated_at", now);

        ContentValues ins = new ContentValues(v);
        ins.put("created_at", now);

        SQLiteDatabase db = db();
        db.beginTransaction();
        try {
            int updated = db.update("containers", v, "id=?", new String[]{id});
            if (updated == 0) db.insertOrThrow("containers", null, ins);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteContainer(String id) {
        db().delete("containers", "id=?", new String[]{id});
    }

    public static class ContainerRow {
        public final String id;
        public final String location;
        public ContainerRow(String id, String location) { this.id = id; this.location = location; }
    }

    public List<ContainerRow> listContainers() {
        List<ContainerRow> out = new ArrayList<>();
        try (Cursor c = db().rawQuery("SELECT id, room, rack, bay, shelf FROM containers ORDER BY updated_at DESC", null)) {
            while (c.moveToNext()) {
                String id = c.getString(0);
                String loc = joinLoc(c.getString(1), c.getString(2), c.getString(3), c.getString(4));
                out.add(new ContainerRow(id, loc));
            }
        }
        return out;
    }

    private static String joinLoc(String room, String rack, String bay, String shelf) {
        StringBuilder sb = new StringBuilder();
        if (room != null && !room.isEmpty()) sb.append(room);
        if (rack != null && !rack.isEmpty()) { if (sb.length()>0) sb.append(", "); sb.append("Rack ").append(rack); }
        if (bay != null && !bay.isEmpty()) { if (sb.length()>0) sb.append(", "); sb.append("Bay ").append(bay); }
        if (shelf != null && !shelf.isEmpty()) { if (sb.length()>0) sb.append(", "); sb.append("Shelf ").append(shelf); }
        return sb.toString();
    }

    public static class Item {
        public final String barcode, name, description, photoUri;
        public Item(String barcode, String name, String description, String photoUri) {
            this.barcode = barcode; this.name = name; this.description = description; this.photoUri = photoUri;
        }
    }

    public void upsertItem(String barcode, @Nullable String name, @Nullable String description, @Nullable String photoUri) {
        long now = System.currentTimeMillis();
        ContentValues v = new ContentValues();
        v.put("barcode", barcode);
        v.put("name", name);
        v.put("description", description);
        v.put("photo_uri", photoUri);
        v.put("updated_at", now);

        SQLiteDatabase db = db();
        db.beginTransaction();
        try {
            int updated = db.update("items", v, "barcode=?", new String[]{barcode});
            if (updated == 0) db.insertOrThrow("items", null, v);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Nullable
    public Item getItem(String barcode) {
        try (Cursor c = db().rawQuery(
                "SELECT barcode, name, description, photo_uri FROM items WHERE barcode=?",
                new String[]{barcode})) {
            if (c.moveToFirst()) return new Item(c.getString(0), c.getString(1), c.getString(2), c.getString(3));
        }
        return null;
    }

    public static class ContainerItemRow {
        public final String barcode, displayName;
        public int qty;
        public ContainerItemRow(String barcode, String displayName, int qty) {
            this.barcode = barcode; this.displayName = displayName; this.qty = qty;
        }
    }

    public List<ContainerItemRow> listContainerItems(String containerId) {
        List<ContainerItemRow> out = new ArrayList<>();
        String sql =
                "SELECT ci.barcode, IFNULL(i.name, ci.barcode) as display, ci.qty " +
                "FROM container_items ci LEFT JOIN items i ON i.barcode=ci.barcode " +
                "WHERE ci.container_id=? ORDER BY display COLLATE NOCASE";
        try (Cursor c = db().rawQuery(sql, new String[]{containerId})) {
            while (c.moveToNext()) out.add(new ContainerItemRow(c.getString(0), c.getString(1), c.getInt(2)));
        }
        return out;
    }

    public void setContainerItemQty(String containerId, String barcode, int qty) {
        ContentValues v = new ContentValues();
        v.put("container_id", containerId);
        v.put("barcode", barcode);
        v.put("qty", qty);

        SQLiteDatabase db = db();
        db.beginTransaction();
        try {
            int updated = db.update("container_items", v, "container_id=? AND barcode=?", new String[]{containerId, barcode});
            if (updated == 0) db.insertOrThrow("container_items", null, v);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
public static class ContainerInfo {
    public final String id;
    public final String contentLetter;
    public final String containerLetter;
    public final int seqNum;
    public final String room;
    public final String rack;
    public final String bay;
    public final String shelf;

    public ContainerInfo(String id, String contentLetter, String containerLetter, int seqNum,
                         String room, String rack, String bay, String shelf) {
        this.id = id;
        this.contentLetter = contentLetter;
        this.containerLetter = containerLetter;
        this.seqNum = seqNum;
        this.room = room;
        this.rack = rack;
        this.bay = bay;
        this.shelf = shelf;
    }
}

@Nullable
public ContainerInfo getContainer(String id) {
    String sql = "SELECT id, content_letter, container_letter, seq_num, room, rack, bay, shelf FROM containers WHERE id=?";
    try (Cursor c = db().rawQuery(sql, new String[]{id})) {
        if (c.moveToFirst()) {
            return new ContainerInfo(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getInt(3),
                    c.getString(4),
                    c.getString(5),
                    c.getString(6),
                    c.getString(7)
            );
        }
    }
    return null;
}

public void updateContainerLocation(String id, @Nullable String room, @Nullable String rack,
                                    @Nullable String bay, @Nullable String shelf) {
    long now = System.currentTimeMillis();
    ContentValues v = new ContentValues();
    v.put("room", room);
    v.put("rack", rack);
    v.put("bay", bay);
    v.put("shelf", shelf);
    v.put("updated_at", now);
    db().update("containers", v, "id=?", new String[]{id});
}

}
