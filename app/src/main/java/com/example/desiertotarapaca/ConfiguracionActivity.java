package com.example.desiertotarapaca;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.materialswitch.MaterialSwitch;

public class ConfiguracionActivity extends AppCompatActivity {

    public static final String PREF_CONFIG = "ConfigApp";
    public static final String KEY_SEGUNDO_PLANO = "segundo_plano";
    public static final String KEY_ANTICIPACION_MIN = "anticipacion_minutos";
    public static final int ANTICIPACION_POR_DEFECTO_MIN = 60;

    private SharedPreferences prefs;
    private MaterialSwitch swSegundoPlano;
    private RadioGroup rgAnticipacion;
    private TextView tvAvisoAnticipacion;

    private boolean actualizandoUi = false;

    private ActivityResultLauncher<String> lanzadorPermisoNotificaciones;
    private ActivityResultLauncher<Intent> lanzadorAlarmasExactas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        prefs = getSharedPreferences(PREF_CONFIG, Context.MODE_PRIVATE);

        TextView btnVolver = findViewById(R.id.btnVolverConfiguracion);
        swSegundoPlano = findViewById(R.id.swSegundoPlano);
        rgAnticipacion = findViewById(R.id.rgAnticipacion);
        tvAvisoAnticipacion = findViewById(R.id.tvAvisoAnticipacion);

        btnVolver.setOnClickListener(v -> finish());

        aplicarColoresSwitch();
        registrarLanzadoresDePermisos();
        cargarEstadoGuardado();

        swSegundoPlano.setOnCheckedChangeListener((boton, activado) -> {
            if (actualizandoUi) return;
            if (activado) {
                iniciarActivacion();
            } else {
                desactivarSegundoPlano();
            }
        });

        rgAnticipacion.setOnCheckedChangeListener((grupo, idSeleccionado) -> {
            if (actualizandoUi) return;
            RadioButton rb = findViewById(idSeleccionado);
            if (rb != null && rb.getTag() != null) {
                int minutos = Integer.parseInt(rb.getTag().toString());
                prefs.edit().putInt(KEY_ANTICIPACION_MIN, minutos).apply();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefs.getBoolean(KEY_SEGUNDO_PLANO, false) && !tienePermisosCompletos()) {
            desactivarSegundoPlano();
        }
    }

    private void cargarEstadoGuardado() {
        boolean activo = prefs.getBoolean(KEY_SEGUNDO_PLANO, false);
        mostrarSwitch(activo);
        habilitarAnticipacion(activo);

        int minutos = prefs.getInt(KEY_ANTICIPACION_MIN, ANTICIPACION_POR_DEFECTO_MIN);
        actualizandoUi = true;
        for (int i = 0; i < rgAnticipacion.getChildCount(); i++) {
            View hijo = rgAnticipacion.getChildAt(i);
            if (hijo.getTag() != null && String.valueOf(minutos).equals(hijo.getTag().toString())) {
                rgAnticipacion.check(hijo.getId());
                break;
            }
        }
        actualizandoUi = false;
    }

    private void mostrarSwitch(boolean activo) {
        actualizandoUi = true;
        swSegundoPlano.setChecked(activo);
        actualizandoUi = false;
    }

    private void habilitarAnticipacion(boolean habilitado) {
        for (int i = 0; i < rgAnticipacion.getChildCount(); i++) {
            rgAnticipacion.getChildAt(i).setEnabled(habilitado);
        }
        rgAnticipacion.setAlpha(habilitado ? 1f : 0.4f);
        tvAvisoAnticipacion.setVisibility(habilitado ? View.GONE : View.VISIBLE);
    }

    private void confirmarActivacion() {
        prefs.edit().putBoolean(KEY_SEGUNDO_PLANO, true).apply();
        mostrarSwitch(true);
        habilitarAnticipacion(true);
    }

    private void desactivarSegundoPlano() {
        prefs.edit().putBoolean(KEY_SEGUNDO_PLANO, false).apply();
        mostrarSwitch(false);
        habilitarAnticipacion(false);
    }

    private void aplicarColoresSwitch() {
        int[][] estados = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int verde = ContextCompat.getColor(this, R.color.green_tile);
        int blanco = ContextCompat.getColor(this, R.color.text_light);
        int grisTrack = ContextCompat.getColor(this, R.color.border_light);
        int grisThumb = ContextCompat.getColor(this, R.color.text_muted);

        swSegundoPlano.setThumbTintList(new android.content.res.ColorStateList(
                estados, new int[]{blanco, grisThumb}));
        swSegundoPlano.setTrackTintList(new android.content.res.ColorStateList(
                estados, new int[]{verde, grisTrack}));
    }

    private void registrarLanzadoresDePermisos() {
        lanzadorPermisoNotificaciones = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                concedido -> {
                    if (concedido) {
                        verificarAlarmasExactasYActivar();
                    } else {
                        desactivarSegundoPlano();
                        manejarPermisoNotificacionesDenegado();
                    }
                });

        lanzadorAlarmasExactas = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                resultado -> {
                    if (puedeProgramarAlarmasExactas()) {
                        confirmarActivacion();
                    } else {
                        desactivarSegundoPlano();
                        Toast.makeText(this,
                                "No se activó: falta el permiso de alarmas y recordatorios",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void iniciarActivacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            lanzadorPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            verificarAlarmasExactasYActivar();
        }
    }

    private void verificarAlarmasExactasYActivar() {
        if (puedeProgramarAlarmasExactas()) {
            confirmarActivacion();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Alarmas y recordatorios")
                .setMessage("Para avisarte a la hora justa, Android necesita que actives el permiso "
                        + "“Alarmas y recordatorios” para esta app. En la siguiente pantalla "
                        + "activa la opción y vuelve atrás.")
                .setPositiveButton("Continuar", (dialog, which) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:" + getPackageName()));
                        lanzadorAlarmasExactas.launch(intent);
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> desactivarSegundoPlano())
                .setOnCancelListener(dialog -> desactivarSegundoPlano())
                .show();
    }

    private void manejarPermisoNotificacionesDenegado() {
        boolean negadoParaSiempre = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);

        if (negadoParaSiempre) {
            new AlertDialog.Builder(this)
                    .setTitle("Permiso de notificaciones")
                    .setMessage("Sin este permiso no podemos enviarte avisos de tareas. "
                            + "Puedes activarlo en los ajustes de la app.")
                    .setPositiveButton("Abrir ajustes", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getPackageName(), null));
                        startActivity(intent);
                    })
                    .setNegativeButton("Ahora no", null)
                    .show();
        } else {
            Toast.makeText(this,
                    "Necesitamos el permiso de notificaciones para avisarte de tus tareas",
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean puedeProgramarAlarmasExactas() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        return alarmManager != null && alarmManager.canScheduleExactAlarms();
    }

    private boolean tienePermisosCompletos() {
        boolean notificaciones = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
        return notificaciones && puedeProgramarAlarmasExactas();
    }
}