package com.example.desiertotarapaca;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    public static class RegistroServicio {
        public String numeroOt;
        public String tipoTrabajo;
        public String faenaCliente;
        public String fechaHora;
        public String descripcion;
        public boolean firmado;

        public RegistroServicio(String numeroOt, String tipoTrabajo, String faenaCliente,
                                String fechaHora, String descripcion, boolean firmado) {
            this.numeroOt = numeroOt;
            this.tipoTrabajo = tipoTrabajo;
            this.faenaCliente = faenaCliente;
            this.fechaHora = fechaHora;
            this.descripcion = descripcion;
            this.firmado = firmado;
        }
    }

    private TextView btnVolverHistorial;
    private TextView tvTotalServiciosMes, tvHorasTerreno, tvPorcentajeFirmados;
    private EditText etBuscadorHistorial;
    private LinearLayout layoutEstadoVacio;
    private LinearLayout contenedorHistorial;

    private List<RegistroServicio> listaRegistros = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        btnVolverHistorial = findViewById(R.id.btnVolverHistorial);
        tvTotalServiciosMes = findViewById(R.id.tvTotalServiciosMes);
        tvHorasTerreno = findViewById(R.id.tvHorasTerreno);
        tvPorcentajeFirmados = findViewById(R.id.tvPorcentajeFirmados);
        etBuscadorHistorial = findViewById(R.id.etBuscadorHistorial);
        layoutEstadoVacio = findViewById(R.id.layoutEstadoVacio);
        contenedorHistorial = findViewById(R.id.contenedorHistorial);

        btnVolverHistorial.setOnClickListener(v -> finish());

        renderizarHistorial("");

        etBuscadorHistorial.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderizarHistorial(s.toString().toLowerCase().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void renderizarHistorial(String filtro) {
        contenedorHistorial.removeAllViews();

        int colorMain = Color.parseColor("#1B2E24");
        int colorMuted = Color.parseColor("#6B7F74");
        int colorGreenPrimary = Color.parseColor("#2D5A43");

        int serviciosVisibles = 0;

        for (RegistroServicio item : listaRegistros) {
            boolean coincide = filtro.isEmpty() ||
                    item.numeroOt.toLowerCase().contains(filtro) ||
                    item.tipoTrabajo.toLowerCase().contains(filtro) ||
                    item.faenaCliente.toLowerCase().contains(filtro);

            if (coincide) {
                serviciosVisibles++;

                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackgroundResource(R.drawable.bg_white_card);
                card.setElevation(4f);
                card.setPadding(36, 28, 36, 28);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 24);
                card.setLayoutParams(params);

                LinearLayout headerCard = new LinearLayout(this);
                headerCard.setOrientation(LinearLayout.HORIZONTAL);
                headerCard.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                TextView tvTitulo = new TextView(this);
                tvTitulo.setText(item.numeroOt + " · " + item.tipoTrabajo);
                tvTitulo.setTextSize(14f);
                tvTitulo.setTypeface(null, Typeface.BOLD);
                tvTitulo.setTextColor(colorMain);
                LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f
                );
                tvTitulo.setLayoutParams(titleParams);
                headerCard.addView(tvTitulo);

                TextView tvBadge = new TextView(this);
                if (item.firmado) {
                    tvBadge.setText("✓ Firmado");
                    tvBadge.setTextColor(Color.parseColor("#137333"));
                    tvBadge.setBackgroundColor(Color.parseColor("#E6F4EA"));
                } else {
                    tvBadge.setText("⏳ Pendiente");
                    tvBadge.setTextColor(Color.parseColor("#B06000"));
                    tvBadge.setBackgroundColor(Color.parseColor("#FEF7E0"));
                }
                tvBadge.setTextSize(11f);
                tvBadge.setTypeface(null, Typeface.BOLD);
                tvBadge.setPadding(16, 6, 16, 6);
                headerCard.addView(tvBadge);

                card.addView(headerCard);

                TextView tvFaena = new TextView(this);
                tvFaena.setText("🏢 " + item.faenaCliente);
                tvFaena.setTextSize(12f);
                tvFaena.setTextColor(colorMuted);
                tvFaena.setPadding(0, 8, 0, 0);
                card.addView(tvFaena);

                TextView tvFecha = new TextView(this);
                tvFecha.setText("📅 " + item.fechaHora);
                tvFecha.setTextSize(11f);
                tvFecha.setTextColor(colorMuted);
                tvFecha.setPadding(0, 4, 0, 0);
                card.addView(tvFecha);

                if (item.descripcion != null && !item.descripcion.isEmpty()) {
                    TextView tvDesc = new TextView(this);
                    tvDesc.setText(item.descripcion);
                    tvDesc.setTextSize(12f);
                    tvDesc.setTextColor(colorMain);
                    tvDesc.setPadding(0, 6, 0, 0);
                    card.addView(tvDesc);
                }

                card.setOnClickListener(v ->
                        Toast.makeText(this, "Detalle de " + item.numeroOt, Toast.LENGTH_SHORT).show()
                );

                contenedorHistorial.addView(card);
            }
        }

        if (serviciosVisibles == 0) {
            layoutEstadoVacio.setVisibility(View.VISIBLE);
        } else {
            layoutEstadoVacio.setVisibility(View.GONE);
        }

        tvTotalServiciosMes.setText(String.valueOf(listaRegistros.size()));
    }
}