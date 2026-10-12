package com.example.desiertotarapaca;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class ConfiguracionActivity extends AppCompatActivity {

    private TextView btnVolverConfiguracion;
    private AppCompatButton btnCerrarSesion;

    private TextView tvAvatarConfig, tvNombreConfig, tvCorreoConfig;
    private DbHelper dbHelper;

    private static final String PREF_NAME = "SesionUsuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        btnVolverConfiguracion = findViewById(R.id.btnVolverConfiguracion);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        dbHelper = new DbHelper(this);
        tvAvatarConfig = findViewById(R.id.tvAvatarConfig);
        tvNombreConfig = findViewById(R.id.tvNombreConfig);
        tvCorreoConfig = findViewById(R.id.tvCorreoConfig);

        SharedPreferences pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String correo = pref.getString("correo_usuario", "");

        if (!correo.isEmpty()) {
            String nombre = dbHelper.obtenerNombreUsuario(correo);
            tvNombreConfig.setText(nombre);
            tvCorreoConfig.setText(correo);
            tvAvatarConfig.setText(generarIniciales(nombre));
        }

        btnVolverConfiguracion.setOnClickListener(v -> finish());

        btnCerrarSesion.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Cerrar Sesión")
                    .setMessage("¿Estás seguro de que deseas salir de tu cuenta?")
                    .setPositiveButton("Sí, salir", (dialog, which) -> {
                        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                        sharedPreferences.edit().clear().apply();

                        Toast.makeText(this, "Sesión finalizada", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ConfiguracionActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

    }

    private String generarIniciales(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return "US";
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length >= 2) {
            return ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
        }
        return ("" + partes[0].charAt(0)).toUpperCase();
    }
}