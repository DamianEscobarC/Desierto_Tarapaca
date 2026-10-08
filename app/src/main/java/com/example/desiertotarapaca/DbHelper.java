package com.example.desiertotarapaca;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DesiertoTarapaca.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLA_SERVICIOS = "servicios";
    public static final String COL_ID = "id";
    public static final String COL_OT = "numero_ot";
    public static final String COL_CLIENTE = "cliente";
    public static final String COL_TIPO = "tipo_trabajo";
    public static final String COL_DESCRIPCION = "descripcion";
    public static final String COL_FECHA = "fecha_hora";
    public static final String COL_FIRMADO = "firmado";
    public static final String COL_SINCRONIZADO = "sincronizado";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String crearTabla = "CREATE TABLE " + TABLA_SERVICIOS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_OT + " TEXT, " +
                COL_CLIENTE + " TEXT, " +
                COL_TIPO + " TEXT, " +
                COL_DESCRIPCION + " TEXT, " +
                COL_FECHA + " TEXT, " +
                COL_FIRMADO + " INTEGER, " +
                COL_SINCRONIZADO + " INTEGER DEFAULT 0)";
        db.execSQL(crearTabla);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_SERVICIOS);
        onCreate(db);
    }

    public long insertarServicio(String ot, String cliente, String tipo, String desc, String fecha, boolean firmado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_OT, ot);
        cv.put(COL_CLIENTE, cliente);
        cv.put(COL_TIPO, tipo);
        cv.put(COL_DESCRIPCION, desc);
        cv.put(COL_FECHA, fecha);
        cv.put(COL_FIRMADO, firmado ? 1 : 0);
        cv.put(COL_SINCRONIZADO, 0); // 0 = pendiente de subir
        return db.insert(TABLA_SERVICIOS, null, cv);
    }

    public int actualizarServicio(long id, String ot, String cliente, String tipo, String desc, boolean firmado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_OT, ot);
        cv.put(COL_CLIENTE, cliente);
        cv.put(COL_TIPO, tipo);
        cv.put(COL_DESCRIPCION, desc);
        cv.put(COL_FIRMADO, firmado ? 1 : 0);
        cv.put(COL_SINCRONIZADO, 0); // Vuelve a 0 para que se resincronice la modificación
        return db.update(TABLA_SERVICIOS, cv, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Cursor obtenerTodosLosServicios() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLA_SERVICIOS + " ORDER BY " + COL_ID + " DESC", null);
    }

    public Cursor obtenerServicioPorId(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLA_SERVICIOS + " WHERE " + COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Cursor obtenerPendientesSincronizacion() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLA_SERVICIOS + " WHERE " + COL_SINCRONIZADO + " = 0", null);
    }
}