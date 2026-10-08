package com.example.desiertotarapaca;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrarServicioActivity extends AppCompatActivity {

    private TextView btnVolverRegistro, tvCoordenadasGps;
    private EditText etClienteServicio, etTipoTrabajo, etUnidadesAtendidas, etObservaciones;
    private Button btnCapturarGps, btnGuardarServicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_servicio);

        btnVolverRegistro = findViewById(R.id.btnVolverRegistro);
        tvCoordenadasGps = findViewById(R.id.tvCoordenadasGps);
        etClienteServicio = findViewById(R.id.etClienteServicio);
        etTipoTrabajo = findViewById(R.id.etTipoTrabajo);
        etUnidadesAtendidas = findViewById(R.id.etUnidadesAtendidas);
        etObservaciones = findViewById(R.id.etObservaciones);
        btnCapturarGps = findViewById(R.id.btnCapturarGps);
        btnGuardarServicio = findViewById(R.id.btnGuardarServicio);

        btnVolverRegistro.setOnClickListener(v -> finish());

        btnCapturarGps.setOnClickListener(v -> {
            tvCoordenadasGps.setText("-20.2141° S, -70.1524° W (Punto fijado)");
            tvCoordenadasGps.setTextColor(android.graphics.Color.parseColor("#1B2E24"));
            Toast.makeText(this, "Coordenadas capturadas", Toast.LENGTH_SHORT).show();
        });

        btnGuardarServicio.setOnClickListener(v -> validarYGuardar());
    }

    private void validarYGuardar() {
        String cliente = etClienteServicio.getText().toString().trim();
        String trabajo = etTipoTrabajo.getText().toString().trim();
        String unidades = etUnidadesAtendidas.getText().toString().trim();
        String observaciones = etObservaciones.getText().toString().trim();

        if (cliente.isEmpty()) {
            etClienteServicio.setError("Ingresa el cliente o faena");
            etClienteServicio.requestFocus();
            return;
        }

        if (trabajo.isEmpty()) {
            etTipoTrabajo.setError("Ingresa el tipo de trabajo");
            etTipoTrabajo.requestFocus();
            return;
        }

        if (unidades.isEmpty()) {
            etUnidadesAtendidas.setError("Ingresa la cantidad de unidades");
            etUnidadesAtendidas.requestFocus();
            return;
        }

        Toast.makeText(this, "Servicio registrado: " + cliente, Toast.LENGTH_SHORT).show();
        finish();
    }
}