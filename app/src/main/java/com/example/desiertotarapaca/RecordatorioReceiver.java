package com.example.desiertotarapaca;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class RecordatorioReceiver extends BroadcastReceiver {

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        long id = intent.getLongExtra(AvisosTareas.EXTRA_ID, -1);
        String cliente = intent.getStringExtra(AvisosTareas.EXTRA_CLIENTE);
        String tipo = intent.getStringExtra(AvisosTareas.EXTRA_TIPO);
        String hora = intent.getStringExtra(AvisosTareas.EXTRA_HORA);
        String fecha = intent.getStringExtra(AvisosTareas.EXTRA_FECHA);

        AvisosTareas.crearCanal(context);

        String titulo = "Tarea próxima: " + (tipo != null && !tipo.isEmpty() ? tipo : "sin tipo");
        String texto = (cliente != null ? cliente : "Sin cliente")
                + " · " + formatearFecha(fecha) + " a las " + (hora != null ? hora : "--:--");

        PendingIntent alTocar = null;
        Intent abrirApp = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (abrirApp != null) {
            alTocar = PendingIntent.getActivity(context, (int) id, abrirApp,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AvisosTareas.CANAL_ID)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true);

        if (alTocar != null) builder.setContentIntent(alTocar);

        NotificationManagerCompat.from(context).notify((int) id, builder.build());
    }

    private String formatearFecha(String fechaIso) {
        if (fechaIso == null) return "--/--/----";
        String[] partes = fechaIso.split("-");
        if (partes.length == 3) return partes[2] + "/" + partes[1] + "/" + partes[0];
        return fechaIso;
    }
}
