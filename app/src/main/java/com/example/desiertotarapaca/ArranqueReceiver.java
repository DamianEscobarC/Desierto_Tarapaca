package com.example.desiertotarapaca;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ArranqueReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String accion = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(accion)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(accion)) {
            AvisosTareas.reprogramarTodas(context);
        }
    }
}