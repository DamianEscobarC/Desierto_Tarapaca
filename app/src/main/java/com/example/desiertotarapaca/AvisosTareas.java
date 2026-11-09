package com.example.desiertotarapaca;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class AvisosTareas {

    public static final String CANAL_ID = "avisos_tareas";

    public static final String EXTRA_ID = "tarea_id";
    public static final String EXTRA_CLIENTE = "tarea_cliente";
    public static final String EXTRA_TIPO = "tarea_tipo";
    public static final String EXTRA_HORA = "tarea_hora";
    public static final String EXTRA_FECHA = "tarea_fecha";

    private AvisosTareas() {
    }
    public static void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;

        NotificationChannel canal = new NotificationChannel(
                CANAL_ID, "Avisos de tareas", NotificationManager.IMPORTANCE_HIGH);
        canal.setDescription("Recordatorios de las tareas programadas");
        manager.createNotificationChannel(canal);
    }
    public static void programar(Context context, long idTarea, String cliente,
                                 String tipo, String hora, String fecha) {
        Context app = context.getApplicationContext();

        cancelar(app, idTarea);

        if (!avisosActivados(app)) return;

        long momentoTarea = convertirAMilisegundos(fecha, hora);
        if (momentoTarea < 0) return;

        long momentoAviso = momentoTarea - anticipacionMinutos(app) * 60_000L;
        if (momentoAviso <= System.currentTimeMillis()) return;

        AlarmManager alarmManager = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        crearCanal(app);

        Intent intent = new Intent(app, RecordatorioReceiver.class);
        intent.putExtra(EXTRA_ID, idTarea);
        intent.putExtra(EXTRA_CLIENTE, cliente);
        intent.putExtra(EXTRA_TIPO, tipo);
        intent.putExtra(EXTRA_HORA, hora);
        intent.putExtra(EXTRA_FECHA, fecha);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                app, (int) idTarea, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        try {
            boolean puedeExacta = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                    || alarmManager.canScheduleExactAlarms();
            if (puedeExacta) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, momentoAviso, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, momentoAviso, pendingIntent);
            }
        } catch (SecurityException e) {
        }
    }

    public static void cancelar(Context context, long idTarea) {
        Context app = context.getApplicationContext();
        AlarmManager alarmManager = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(app, RecordatorioReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                app, (int) idTarea, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static void reprogramarTodas(Context context) {
        Context app = context.getApplicationContext();
        DbHelper db = new DbHelper(app);
        Cursor c = db.obtenerTareasPendientesDesde(fechaHoyIso());
        try {
            if (c == null) return;
            int iId = c.getColumnIndex(DbHelper.COL_TAREA_ID);
            int iCliente = c.getColumnIndex(DbHelper.COL_TAREA_CLIENTE);
            int iTipo = c.getColumnIndex(DbHelper.COL_TAREA_TIPO);
            int iHora = c.getColumnIndex(DbHelper.COL_TAREA_HORA);
            int iFecha = c.getColumnIndex(DbHelper.COL_TAREA_FECHA);
            while (c.moveToNext()) {
                programar(app, c.getLong(iId), c.getString(iCliente), c.getString(iTipo),
                        c.getString(iHora), c.getString(iFecha));
            }
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }

    public static void cancelarTodas(Context context) {
        Context app = context.getApplicationContext();
        DbHelper db = new DbHelper(app);
        Cursor c = db.obtenerTareasPendientesDesde(fechaHoyIso());
        try {
            if (c == null) return;
            int iId = c.getColumnIndex(DbHelper.COL_TAREA_ID);
            while (c.moveToNext()) {
                cancelar(app, c.getLong(iId));
            }
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(ConfiguracionActivity.PREF_CONFIG, Context.MODE_PRIVATE);
    }

    private static boolean avisosActivados(Context context) {
        return prefs(context).getBoolean(ConfiguracionActivity.KEY_SEGUNDO_PLANO, false);
    }

    private static int anticipacionMinutos(Context context) {
        return prefs(context).getInt(ConfiguracionActivity.KEY_ANTICIPACION_MIN,
                ConfiguracionActivity.ANTICIPACION_POR_DEFECTO_MIN);
    }

    private static long convertirAMilisegundos(String fecha, String hora) {
        if (fecha == null || hora == null) return -1;
        try {
            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            formato.setLenient(false);
            Date date = formato.parse(fecha.trim() + " " + hora.trim());
            return date != null ? date.getTime() : -1;
        } catch (ParseException e) {
            return -1;
        }
    }

    private static String fechaHoyIso() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }
}