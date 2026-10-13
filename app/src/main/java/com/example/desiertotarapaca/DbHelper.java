package com.example.desiertotarapaca;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DesiertoTarapaca.db";
    private static final int DATABASE_VERSION = 6;

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

    public static final String TABLA_TAREAS = "tareas";
    public static final String COL_TAREA_ID = "id";
    public static final String COL_TAREA_CLIENTE = "cliente";
    public static final String COL_TAREA_TIPO = "tipo";
    public static final String COL_TAREA_HORA = "hora";
    public static final String COL_TAREA_FECHA = "fecha";
    public static final String COL_TAREA_ESTADO = "estado";

    public static final String TABLA_INVENTARIO = "inventario_quimicos";
    public static final String COL_QUIM_ID = "id";
    public static final String COL_QUIM_NOMBRE = "nombre";
    public static final String COL_QUIM_TIPO = "tipo";
    public static final String COL_QUIM_STOCK = "stock";
    public static final String COL_QUIM_UNIDAD = "unidad";

    public static final String TABLA_CLIENTES = "clientes";
    public static final String COL_CLI_ID = "id";
    public static final String COL_CLI_NOMBRE = "nombre";
    public static final String COL_CLI_RUT = "rut";
    public static final String COL_CLI_FAENA = "faena";
    public static final String COL_CLI_TELEFONO = "telefono";

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

        String crearTablaTareas = "CREATE TABLE " + TABLA_TAREAS + " (" +
                COL_TAREA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TAREA_CLIENTE + " TEXT, " +
                COL_TAREA_TIPO + " TEXT, " +
                COL_TAREA_HORA + " TEXT, " +
                COL_TAREA_FECHA + " TEXT, " +
                COL_TAREA_ESTADO + " TEXT DEFAULT 'pendiente')";
        db.execSQL(crearTablaTareas);

        String crearTablaInventario = "CREATE TABLE " + TABLA_INVENTARIO + " (" +
                COL_QUIM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_QUIM_NOMBRE + " TEXT, " +
                COL_QUIM_TIPO + " TEXT, " +
                COL_QUIM_STOCK + " REAL, " +
                COL_QUIM_UNIDAD + " TEXT)";
        db.execSQL(crearTablaInventario);

        String crearTablaClientes = "CREATE TABLE " + TABLA_CLIENTES + " (" +
                COL_CLI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CLI_NOMBRE + " TEXT, " +
                COL_CLI_RUT + " TEXT, " +
                COL_CLI_FAENA + " TEXT, " +
                COL_CLI_TELEFONO + " TEXT)";
        db.execSQL(crearTablaClientes);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_SERVICIOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_USUARIOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_TAREAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_INVENTARIO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_CLIENTES);
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

    public boolean eliminarServicio(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int filas = db.delete(TABLA_SERVICIOS, COL_ID + "=?", new String[]{String.valueOf(id)});
        return filas > 0;
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

    public boolean insertarTarea(String cliente, String tipo, String hora, String fecha) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TAREA_CLIENTE, cliente);
        cv.put(COL_TAREA_TIPO, tipo);
        cv.put(COL_TAREA_HORA, hora);
        cv.put(COL_TAREA_FECHA, fecha);
        cv.put(COL_TAREA_ESTADO, "pendiente");
        long resultado = db.insert(TABLA_TAREAS, null, cv);
        return resultado != -1;
    }
    public Cursor obtenerTareasPorFecha(String fecha) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLA_TAREAS + " WHERE " + COL_TAREA_FECHA + " = ? ORDER BY " + COL_TAREA_HORA + " ASC", new String[]{fecha});
    }

    public Cursor obtenerTodasLasTareas() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLA_TAREAS + " ORDER BY " + COL_TAREA_FECHA + " DESC, " + COL_TAREA_HORA + " ASC", null);
    }

    public Cursor obtenerTareasFiltradas(String filtro) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (filtro == null || filtro.trim().isEmpty()) {
            return obtenerTodasLasTareas();
        }
        String pattern = "%" + filtro.trim() + "%";
        return db.rawQuery("SELECT * FROM " + TABLA_TAREAS +
                        " WHERE " + COL_TAREA_CLIENTE + " LIKE ? OR " + COL_TAREA_TIPO + " LIKE ? OR " + COL_TAREA_FECHA + " LIKE ? " +
                        " ORDER BY " + COL_TAREA_FECHA + " DESC, " + COL_TAREA_HORA + " ASC",
                new String[]{pattern, pattern, pattern});
    }

    public boolean actualizarTarea(long id, String cliente, String tipo, String hora, String fecha, String estado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TAREA_CLIENTE, cliente);
        cv.put(COL_TAREA_TIPO, tipo);
        cv.put(COL_TAREA_HORA, hora);
        cv.put(COL_TAREA_FECHA, fecha);
        cv.put(COL_TAREA_ESTADO, estado);
        int filas = db.update(TABLA_TAREAS, cv, COL_TAREA_ID + " = ?", new String[]{String.valueOf(id)});
        return filas > 0;
    }

    public boolean eliminarTarea(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int filas = db.delete(TABLA_TAREAS, COL_TAREA_ID + " = ?", new String[]{String.valueOf(id)});
        return filas > 0;
    }

    //PRODUCTOS QUIMICOS
    public boolean insertarProductoQuimico(String nombre, String tipo, double stock, String unidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_QUIM_NOMBRE, nombre);
        cv.put(COL_QUIM_TIPO, tipo);
        cv.put(COL_QUIM_STOCK, stock);
        cv.put(COL_QUIM_UNIDAD, unidad);
        long res = db.insert(TABLA_INVENTARIO, null, cv);
        return res != -1;
    }

    public Cursor obtenerInventarioQuimicos(String filtro) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (filtro == null || filtro.trim().isEmpty()) {
            return db.rawQuery("SELECT * FROM " + TABLA_INVENTARIO + " ORDER BY " + COL_QUIM_NOMBRE + " ASC", null);
        } else {
            return db.rawQuery("SELECT * FROM " + TABLA_INVENTARIO + " WHERE " + COL_QUIM_NOMBRE + " LIKE ? ORDER BY " + COL_QUIM_NOMBRE + " ASC",
                    new String[]{"%" + filtro.trim() + "%"});
        }
    }
    public boolean actualizarProductoQuimico(long id, String nombre, String tipo, double stock, String unidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_QUIM_NOMBRE, nombre);
        cv.put(COL_QUIM_TIPO, tipo);
        cv.put(COL_QUIM_STOCK, stock);
        cv.put(COL_QUIM_UNIDAD, unidad);
        int filas = db.update(TABLA_INVENTARIO, cv, COL_QUIM_ID + " = ?", new String[]{String.valueOf(id)});
        return filas > 0;
    }

    public boolean consumirStockQuimico(long id, double cantidadUsada) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + COL_QUIM_STOCK + " FROM " + TABLA_INVENTARIO + " WHERE " + COL_QUIM_ID + " = ?", new String[]{String.valueOf(id)});
        if (cursor != null && cursor.moveToFirst()) {
            double stockActual = cursor.getDouble(0);
            cursor.close();

            double nuevoStock = stockActual - cantidadUsada;

            if (nuevoStock <= 0) {
                int eliminados = db.delete(TABLA_INVENTARIO, COL_QUIM_ID + " = ?", new String[]{String.valueOf(id)});
                return eliminados > 0;
            } else {
                ContentValues cv = new ContentValues();
                cv.put(COL_QUIM_STOCK, nuevoStock);
                int filas = db.update(TABLA_INVENTARIO, cv, COL_QUIM_ID + " = ?", new String[]{String.valueOf(id)});
                return filas > 0;
            }
        }

        if (cursor != null) cursor.close();
        return false;
    }
        //Para el Direcotrio
    public boolean insertarCliente(String nombre, String rut, String faena, String telefono) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CLI_NOMBRE, nombre.trim());
        cv.put(COL_CLI_RUT, rut.trim());
        cv.put(COL_CLI_FAENA, faena.trim());
        cv.put(COL_CLI_TELEFONO, telefono.trim());
        long res = db.insert(TABLA_CLIENTES, null, cv);
        return res != -1;
    }

    public Cursor obtenerClientes(String filtro) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (filtro == null || filtro.trim().isEmpty()) {
            return db.rawQuery("SELECT * FROM " + TABLA_CLIENTES + " ORDER BY " + COL_CLI_NOMBRE + " ASC", null);
        }
        String pattern = "%" + filtro.trim() + "%";
        return db.rawQuery("SELECT * FROM " + TABLA_CLIENTES +
                        " WHERE " + COL_CLI_NOMBRE + " LIKE ? OR " + COL_CLI_RUT + " LIKE ? OR " + COL_CLI_FAENA + " LIKE ? " +
                        " ORDER BY " + COL_CLI_NOMBRE + " ASC",
                new String[]{pattern, pattern, pattern});
    }

    public List<String> obtenerNombresClientes() {
        List<String> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_CLI_NOMBRE + " FROM " + TABLA_CLIENTES + " ORDER BY " + COL_CLI_NOMBRE + " ASC", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                lista.add(cursor.getString(0));
            }
            cursor.close();
        }
        return lista;
    }

    public boolean eliminarCliente(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int filas = db.delete(TABLA_CLIENTES, COL_CLI_ID + "=?", new String[]{String.valueOf(id)});
        return filas > 0;
    }
}