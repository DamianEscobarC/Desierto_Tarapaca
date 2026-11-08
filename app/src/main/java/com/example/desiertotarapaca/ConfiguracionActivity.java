package com.example.desiertotarapaca;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ConfiguracionActivity extends AppCompatActivity {

    private TextView btnVolverConfiguracion;

    private TextView tvAvatarConfig, tvNombreConfig, tvCorreoConfig;
    private DbHelper dbHelper;

    private static final String PREF_NAME = "SesionUsuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        btnVolverConfiguracion = findViewById(R.id.btnVolverConfiguracion);
        btnVolverConfiguracion.setOnClickListener(v -> finish());

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