package com.example.desiertotarapaca;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout cardRegistrarServicio, cardConsultarRegistros, cardInventario;
    private LinearLayout cardDirectorioClientes, cardMapaGps, cardRuta;
    private Button btnFinalizarDia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardRegistrarServicio = findViewById(R.id.cardRegistrarServicio);
        cardConsultarRegistros = findViewById(R.id.cardConsultarRegistros);
        cardInventario = findViewById(R.id.cardInventario);
        cardDirectorioClientes = findViewById(R.id.cardDirectorioClientes);
        cardMapaGps = findViewById(R.id.cardMapaGps);
        cardRuta = findViewById(R.id.cardRuta);
        btnFinalizarDia = findViewById(R.id.btnFinalizarDia);

        cardRegistrarServicio.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo formulario de servicio...", Toast.LENGTH_SHORT).show();
        });

        btnFinalizarDia.setOnClickListener(v -> {
            Toast.makeText(this, "Generando reporte de cierre PDF/Excel...", Toast.LENGTH_LONG).show();
        });
    }
}