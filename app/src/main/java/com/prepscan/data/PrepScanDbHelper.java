package com.prepscan.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PrepScanDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "prepscan.db";
    public static final int DB_VERSION = 1;

    public PrepScanDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE containers (" +
                "id TEXT PRIMARY KEY," +
                "content_letter TEXT NOT NULL," +
                "container_letter TEXT NOT NULL," +
                "seq_num INTEGER NOT NULL," +
                "room TEXT, rack TEXT, bay TEXT, shelf TEXT," +
                "created_at INTEGER NOT NULL," +
                "updated_at INTEGER NOT NULL" +
                ");");

        db.execSQL("CREATE INDEX idx_containers_combo ON containers(content_letter, container_letter, seq_num);");

        db.execSQL("CREATE TABLE items (" +
                "barcode TEXT PRIMARY KEY," +
                "name TEXT," +
                "description TEXT," +
                "photo_uri TEXT," +
                "updated_at INTEGER NOT NULL" +
                ");");

        db.execSQL("CREATE TABLE container_items (" +
                "container_id TEXT NOT NULL," +
                "barcode TEXT NOT NULL," +
                "qty INTEGER NOT NULL DEFAULT 0," +
                "PRIMARY KEY(container_id, barcode)," +
                "FOREIGN KEY(container_id) REFERENCES containers(id) ON DELETE CASCADE," +
                "FOREIGN KEY(barcode) REFERENCES items(barcode) ON DELETE CASCADE" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
