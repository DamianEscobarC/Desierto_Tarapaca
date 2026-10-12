package com.example.desiertotarapaca;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

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

    public static class RegistroTarea {
        public long id;
        public String cliente;
        public String tipo;
        public String hora;
        public String fecha;
        public String estado;

        public RegistroTarea(long id, String cliente, String tipo, String hora, String fecha, String estado) {
            this.id = id;
            this.cliente = cliente;
            this.tipo = tipo;
            this.hora = hora;
            this.fecha = fecha;
            this.estado = estado;
        }
    }

    private TextView btnVolverHistorial;
    private TextView tvTotalServiciosMes, tvHorasTerreno, tvPorcentajeFirmados;
    private EditText etBuscadorHistorial;
    private LinearLayout layoutEstadoVacio;
    private TextView tvTituloVacio, tvMensajeVacio;
    private LinearLayout contenedorHistorial;

    private AppCompatButton btnTabServicios, btnTabTareas;
    private boolean mostrandoServicios = true;

    private DbHelper dbHelper;
    private List<RegistroServicio> listaServicios = new ArrayList<>();
    private List<RegistroTarea> listaTareas = new ArrayList<>();

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
        tvTituloVacio = findViewById(R.id.tvTituloVacio);
        tvMensajeVacio = findViewById(R.id.tvMensajeVacio);
        contenedorHistorial = findViewById(R.id.contenedorHistorial);

        btnTabServicios = findViewById(R.id.btnTabServicios);
        btnTabTareas = findViewById(R.id.btnTabTareas);

        btnVolverHistorial.setOnClickListener(v -> finish());

        btnTabServicios.setOnClickListener(v -> cambiarTab(true));
        btnTabTareas.setOnClickListener(v -> cambiarTab(false));

        etBuscadorHistorial.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderizarActual(s.toString().toLowerCase().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        recargarDatos();
    }

    private void cambiarTab(boolean esServicios) {
        if (mostrandoServicios == esServicios) return;
        mostrandoServicios = esServicios;

        if (mostrandoServicios) {
            btnTabServicios.setBackgroundResource(R.drawable.bg_selected_day_circle);
            btnTabServicios.setTextColor(Color.WHITE);
            btnTabTareas.setBackgroundResource(R.drawable.bg_input_field);
            btnTabTareas.setTextColor(ContextCompat.getColor(this, R.color.text_main));
            etBuscadorHistorial.setHint("🔍 Buscar por faena, OT o servicio...");
        } else {
            btnTabTareas.setBackgroundResource(R.drawable.bg_selected_day_circle);
            btnTabTareas.setTextColor(Color.WHITE);
            btnTabServicios.setBackgroundResource(R.drawable.bg_input_field);
            btnTabServicios.setTextColor(ContextCompat.getColor(this, R.color.text_main));
            etBuscadorHistorial.setHint("🔍 Buscar por cliente o tipo de tarea...");
        }

        renderizarActual(obtenerTextoFiltro());
    }

    private String obtenerTextoFiltro() {
        return etBuscadorHistorial.getText() != null ?
                etBuscadorHistorial.getText().toString().toLowerCase().trim() : "";
    }

    private void recargarDatos() {
        cargarServiciosDesdeBD();
        cargarTareasDesdeBD();
        renderizarActual(obtenerTextoFiltro());
    }

    private void renderizarActual(String filtro) {
        if (mostrandoServicios) {
            renderizarServicios(filtro);
        } else {
            renderizarTareas(filtro);
        }
    }

    private void cargarServiciosDesdeBD() {
        listaServicios.clear();
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

                listaServicios.add(new RegistroServicio(id, ot, tipo, cli, fecha, desc, firmado));
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    private void cargarTareasDesdeBD() {
        listaTareas.clear();
        Cursor cursor = dbHelper.obtenerTodasLasTareas();

        if (cursor != null && cursor.moveToFirst()) {
            int idxId = cursor.getColumnIndex(DbHelper.COL_TAREA_ID);
            int idxCli = cursor.getColumnIndex(DbHelper.COL_TAREA_CLIENTE);
            int idxTipo = cursor.getColumnIndex(DbHelper.COL_TAREA_TIPO);
            int idxHora = cursor.getColumnIndex(DbHelper.COL_TAREA_HORA);
            int idxFecha = cursor.getColumnIndex(DbHelper.COL_TAREA_FECHA);
            int idxEstado = cursor.getColumnIndex(DbHelper.COL_TAREA_ESTADO);

            do {
                long id = (idxId != -1) ? cursor.getLong(idxId) : 0;
                String cli = (idxCli != -1) ? cursor.getString(idxCli) : "";
                String tipo = (idxTipo != -1) ? cursor.getString(idxTipo) : "";
                String hora = (idxHora != -1) ? cursor.getString(idxHora) : "";
                String fecha = (idxFecha != -1) ? cursor.getString(idxFecha) : "";
                String estado = (idxEstado != -1) ? cursor.getString(idxEstado) : "pendiente";

                listaTareas.add(new RegistroTarea(id, cli, tipo, hora, fecha, estado));
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    private void renderizarServicios(String filtro) {
        contenedorHistorial.removeAllViews();

        int colorMain = Color.parseColor("#1B2E24");
        int colorMuted = Color.parseColor("#6B7F74");
        int visibles = 0;
        int firmadosCount = 0;

        for (RegistroServicio item : listaServicios) {
            if (item.firmado) firmadosCount++;

            boolean coincide = filtro.isEmpty() ||
                    item.numeroOt.toLowerCase().contains(filtro) ||
                    item.tipoTrabajo.toLowerCase().contains(filtro) ||
                    item.faenaCliente.toLowerCase().contains(filtro);

            if (coincide) {
                visibles++;

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

        if (visibles == 0) {
            layoutEstadoVacio.setVisibility(View.VISIBLE);
            if (tvTituloVacio != null) tvTituloVacio.setText("Sin servicios registrados");
            if (tvMensajeVacio != null) tvMensajeVacio.setText("Los servicios completados aparecerán aquí.");
        } else {
            layoutEstadoVacio.setVisibility(View.GONE);
        }

        tvTotalServiciosMes.setText(String.valueOf(listaServicios.size()));
        if (!listaServicios.isEmpty()) {
            int porcentaje = (firmadosCount * 100) / listaServicios.size();
            tvPorcentajeFirmados.setText(porcentaje + "%");
        } else {
            tvPorcentajeFirmados.setText("0%");
        }
    }

    private void renderizarTareas(String filtro) {
        contenedorHistorial.removeAllViews();

        int colorMain = Color.parseColor("#1B2E24");
        int colorMuted = Color.parseColor("#6B7F74");
        int visibles = 0;

        for (RegistroTarea item : listaTareas) {
            boolean coincide = filtro.isEmpty() ||
                    item.cliente.toLowerCase().contains(filtro) ||
                    item.tipo.toLowerCase().contains(filtro) ||
                    item.fecha.toLowerCase().contains(filtro);

            if (coincide) {
                visibles++;

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
                tvTitulo.setText(item.cliente);
                tvTitulo.setTextSize(14f);
                tvTitulo.setTypeface(null, Typeface.BOLD);
                tvTitulo.setTextColor(colorMain);
                tvTitulo.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f
                ));
                headerCard.addView(tvTitulo);

                TextView tvHora = new TextView(this);
                tvHora.setText(item.hora);
                tvHora.setTextSize(12f);
                tvHora.setTypeface(null, Typeface.BOLD);
                tvHora.setTextColor(Color.parseColor("#2D6A4F"));
                headerCard.addView(tvHora);

                card.addView(headerCard);

                TextView tvTipo = new TextView(this);
                tvTipo.setText("🛠 " + item.tipo);
                tvTipo.setTextSize(12f);
                tvTipo.setTextColor(colorMuted);
                tvTipo.setPadding(0, 8, 0, 0);
                card.addView(tvTipo);

                TextView tvFecha = new TextView(this);
                tvFecha.setText("📅 " + item.fecha);
                tvFecha.setTextSize(11f);
                tvFecha.setTextColor(colorMuted);
                tvFecha.setPadding(0, 4, 0, 0);
                card.addView(tvFecha);

                card.setOnClickListener(v -> mostrarDialogoEditarTarea(item));

                contenedorHistorial.addView(card);
            }
        }

        if (visibles == 0) {
            layoutEstadoVacio.setVisibility(View.VISIBLE);
            if (tvTituloVacio != null) tvTituloVacio.setText("Sin tareas registradas");
            if (tvMensajeVacio != null) tvMensajeVacio.setText("Las tareas agendadas aparecerán aquí.");
        } else {
            layoutEstadoVacio.setVisibility(View.GONE);
        }
    }

    private void mostrarDialogoEditarTarea(RegistroTarea tarea) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_tarea, null);

        EditText etCliente = dialogView.findViewById(R.id.etEditClienteTarea);
        EditText etTipo = dialogView.findViewById(R.id.etEditTipoTarea);
        EditText etHora = dialogView.findViewById(R.id.etEditHoraTarea);
        EditText etFecha = dialogView.findViewById(R.id.etEditFechaTarea);
        TextView btnEliminar = dialogView.findViewById(R.id.btnEliminarTarea);
        TextView btnCancelar = dialogView.findViewById(R.id.btnCancelarEditTarea);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarEditTarea);

        etCliente.setText(tarea.cliente);
        etTipo.setText(tarea.tipo);
        etHora.setText(tarea.hora);
        etFecha.setText(tarea.fecha);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnEliminar.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Estás seguro de eliminar esta tarea?")
                    .setPositiveButton("Eliminar", (d, which) -> {
                        boolean ok = dbHelper.eliminarTarea(tarea.id);
                        if (ok) {
                            Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            recargarDatos();
                        } else {
                            Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        btnGuardar.setOnClickListener(v -> {
            String nuevoCliente = etCliente.getText().toString().trim();
            String nuevoTipo = etTipo.getText().toString().trim();
            String nuevaHora = etHora.getText().toString().trim();
            String nuevaFecha = etFecha.getText().toString().trim();

            if (nuevoCliente.isEmpty() || nuevoTipo.isEmpty() || nuevaHora.isEmpty() || nuevaFecha.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean ok = dbHelper.actualizarTarea(tarea.id, nuevoCliente, nuevoTipo, nuevaHora, nuevaFecha, tarea.estado);
            if (ok) {
                Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                recargarDatos();
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}