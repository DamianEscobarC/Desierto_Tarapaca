package com.example.desiertotarapaca;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class ConfiguracionActivity extends AppCompatActivity {

    private TextView btnVolverConfiguracion;
    private AppCompatButton btnCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        btnVolverConfiguracion = findViewById(R.id.btnVolverConfiguracion);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        btnVolverConfiguracion.setOnClickListener(v -> finish());

        btnCerrarSesion.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Cerrar Sesión")
                    .setMessage("¿Estás seguro de que deseas salir de tu cuenta?")
                    .setPositiveButton("Sí, salir", (dialog, which) -> {
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
}