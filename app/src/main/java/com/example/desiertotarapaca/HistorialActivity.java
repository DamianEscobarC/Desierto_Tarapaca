package com.example.desiertotarapaca;

import android.content.Intent;
import android.database.Cursor;
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
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    public static class RegistroServicio {
        public long id;
        public String numeroOt;
        public String tipoTrabajo;
        public String faenaCliente;
        public String fechaHora;
        public String descripcion;
        public boolean firmado;

        public RegistroServicio(long id, String numeroOt, String tipoTrabajo, String faenaCliente,
                                String fechaHora, String descripcion, boolean firmado) {
            this.id = id;
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

    private DbHelper dbHelper;
    private List<RegistroServicio> listaRegistros = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        dbHelper = new DbHelper(this);

        btnVolverHistorial = findViewById(R.id.btnVolverHistorial);
        tvTotalServiciosMes = findViewById(R.id.tvTotalServiciosMes);
        tvHorasTerreno = findViewById(R.id.tvHorasTerreno);
        tvPorcentajeFirmados = findViewById(R.id.tvPorcentajeFirmados);
        etBuscadorHistorial = findViewById(R.id.etBuscadorHistorial);
        layoutEstadoVacio = findViewById(R.id.layoutEstadoVacio);
        contenedorHistorial = findViewById(R.id.contenedorHistorial);

        btnVolverHistorial.setOnClickListener(v -> finish());

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

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosDesdeBBDD();
        String filtroActual = etBuscadorHistorial.getText() != null ?
                etBuscadorHistorial.getText().toString().toLowerCase().trim() : "";
        renderizarHistorial(filtroActual);
    }

    private void cargarDatosDesdeBBDD() {
        listaRegistros.clear();
        Cursor cursor = dbHelper.obtenerTodosLosServicios();

        if (cursor != null && cursor.moveToFirst()) {
            int idxId = cursor.getColumnIndex(DbHelper.COL_ID);
            int idxOt = cursor.getColumnIndex(DbHelper.COL_OT);
            int idxCli = cursor.getColumnIndex(DbHelper.COL_CLIENTE);
            int idxTipo = cursor.getColumnIndex(DbHelper.COL_TIPO);
            int idxDesc = cursor.getColumnIndex(DbHelper.COL_DESCRIPCION);
            int idxFecha = cursor.getColumnIndex(DbHelper.COL_FECHA);
            int idxFirm = cursor.getColumnIndex(DbHelper.COL_FIRMADO);

            do {
                long id = (idxId != -1) ? cursor.getLong(idxId) : 0;
                String ot = (idxOt != -1) ? cursor.getString(idxOt) : "";
                String cli = (idxCli != -1) ? cursor.getString(idxCli) : "";
                String tipo = (idxTipo != -1) ? cursor.getString(idxTipo) : "";
                String desc = (idxDesc != -1) ? cursor.getString(idxDesc) : "";
                String fecha = (idxFecha != -1) ? cursor.getString(idxFecha) : "";
                boolean firmado = (idxFirm != -1) && (cursor.getInt(idxFirm) == 1);

                listaRegistros.add(new RegistroServicio(id, ot, tipo, cli, fecha, desc, firmado));
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    private void renderizarHistorial(String filtro) {
        contenedorHistorial.removeAllViews();

        int colorMain = Color.parseColor("#1B2E24");
        int colorMuted = Color.parseColor("#6B7F74");
        int serviciosVisibles = 0;
        int firmadosCount = 0;

        for (RegistroServicio item : listaRegistros) {
            if (item.firmado) firmadosCount++;

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
                tvTitulo.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f
                ));
                headerCard.addView(tvTitulo);

                TextView tvBadge = new TextView(this);
                if (item.firmado) {
                    tvBadge.setText("✓ Guardado");
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

                card.setOnClickListener(v -> {
                    Intent intent = new Intent(HistorialActivity.this, RegistrarServicioActivity.class);
                    intent.putExtra("ID_SERVICIO", item.id);
                    startActivity(intent);
                });

                contenedorHistorial.addView(card);
            }
        }

        layoutEstadoVacio.setVisibility(serviciosVisibles == 0 ? View.VISIBLE : View.GONE);

        tvTotalServiciosMes.setText(String.valueOf(listaRegistros.size()));
        if (!listaRegistros.isEmpty()) {
            int porcentaje = (firmadosCount * 100) / listaRegistros.size();
            tvPorcentajeFirmados.setText(porcentaje + "%");
        } else {
            tvPorcentajeFirmados.setText("0%");
        }
    }
}