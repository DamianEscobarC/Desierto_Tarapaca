package com.example.desiertotarapaca;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DesiertoTarapaca.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLA_SERVICIOS = "servicios";
    public static final String COL_ID = "id";
    public static final String COL_OT = "numero_ot";
    public static final String COL_CLIENTE = "cliente";
    public static final String COL_TIPO = "tipo_trabajo";
    public static final String COL_DESCRIPCION = "descripcion";
    public static final String COL_FECHA = "fecha_hora";
    public static final String COL_FIRMADO = "firmado";
    public static final String COL_SINCRONIZADO = "sincronizado";

    public static final String TABLA_USUARIOS = "usuarios";
    public static final String COL_USER_ID = "id_usuario";
    public static final String COL_USER_NOMBRE = "nombre";
    public static final String COL_USER_CORREO = "correo";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_CARGO = "cargo";
    public static final String COL_USER_TELEFONO = "telefono";
    public static final String COL_USER_DIRECCION = "direccion";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String crearTablaServicios = "CREATE TABLE " + TABLA_SERVICIOS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_OT + " TEXT, " +
                COL_CLIENTE + " TEXT, " +
                COL_TIPO + " TEXT, " +
                COL_DESCRIPCION + " TEXT, " +
                COL_FECHA + " TEXT, " +
                COL_FIRMADO + " INTEGER, " +
                COL_SINCRONIZADO + " INTEGER DEFAULT 0)";
        db.execSQL(crearTablaServicios);

        String crearTablaUsuarios = "CREATE TABLE " + TABLA_USUARIOS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NOMBRE + " TEXT, " +
                COL_USER_CORREO + " TEXT UNIQUE, " +
                COL_USER_PASSWORD + " TEXT, " +
                COL_USER_CARGO + " TEXT, " +
                COL_USER_TELEFONO + " TEXT, " +
                COL_USER_DIRECCION + " TEXT)";
        db.execSQL(crearTablaUsuarios);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_SERVICIOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_USUARIOS);
        onCreate(db);
    }



    public boolean existeCorreo(String correo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLA_USUARIOS + " WHERE " + COL_USER_CORREO + " = ?", new String[]{correo.toLowerCase().trim()});
        boolean existe = (cursor.getCount() > 0);
        cursor.close();
        return existe;
    }


    public boolean registrarUsuario(String nombre, String correo, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_NOMBRE, nombre.trim());
        cv.put(COL_USER_CORREO, correo.toLowerCase().trim());
        cv.put(COL_USER_PASSWORD, password);
        long resultado = db.insert(TABLA_USUARIOS, null, cv);
        return resultado != -1;
    }


    public boolean validarLogin(String correo, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLA_USUARIOS + " WHERE " + COL_USER_CORREO + " = ? AND " + COL_USER_PASSWORD + " = ?",
                new String[]{correo.toLowerCase().trim(), password});
        boolean valido = (cursor.getCount() > 0);
        cursor.close();
        return valido;
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
        cv.put(COL_SINCRONIZADO, 0);
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
        cv.put(COL_SINCRONIZADO, 0);
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
    public String obtenerNombreUsuario(String correo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_USER_NOMBRE + " FROM " + TABLA_USUARIOS + " WHERE " + COL_USER_CORREO + " = ?", new String[]{correo.toLowerCase().trim()});
        String nombre = "Usuario";
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int colIdx = cursor.getColumnIndex(COL_USER_NOMBRE);
                if (colIdx != -1) {
                    nombre = cursor.getString(colIdx);
                }
            }
            cursor.close();
        }
        return nombre;
    }
    public Cursor obtenerDatosUsuario(String correo) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLA_USUARIOS + " WHERE " + COL_USER_CORREO + " = ?", new String[]{correo.toLowerCase().trim()});
    }

    public boolean actualizarPerfilUsuario(String correo, String cargo, String telefono, String direccion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_CARGO, cargo);
        cv.put(COL_USER_TELEFONO, telefono);
        cv.put(COL_USER_DIRECCION, direccion);

        int filas = db.update(TABLA_USUARIOS, cv, COL_USER_CORREO + " = ?", new String[]{correo.toLowerCase().trim()});
        return filas > 0;
    }
}