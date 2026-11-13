package com.example.desiertotarapaca;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.content.ContentValues;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrarServicioActivity extends AppCompatActivity {

    private TextView btnVolverRegistro;
    private View btnSeleccionarClienteServicio;
    private TextView tvClienteServicioTexto, tvFaenaServicioInfo, tvCoordenadasGps;
    private AppCompatButton btnAgregarBanosServicio;
    private LinearLayout contenedorChecksBanos;
    private TextView tvAvisoSinBanos;
    private EditText etObservaciones, etPrecio, etMetrosCubicos;
    private AppCompatButton btnCapturarGps, btnGuardarServicio;

    private DbHelper dbHelper;
    private long idServicioAEditar = -1;
    private String numeroOtActual = "";
    private long idTareaProgramada = -1;

    private String clienteSeleccionadoNombre = "";
    private double clienteLatitud = 0.0;
    private double clienteLongitud = 0.0;

    private final List<String> banosSeleccionados = new ArrayList<>();

    private static final String TIPO_LIMPIEZA = "Limpieza de fosa";
    private static final String TIPO_ENVARILLADO = "Envarillado";
    private static final String TIPO_ARRIENDO = "Arriendo de baños";

    private TextView opTipoLimpieza, opTipoEnvarillado, opTipoArriendo;
    private View seccionBanos, seccionMetros;
    private String tipoSeleccionado = "";

    private final ActivityResultLauncher<Intent> selectorMapaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    clienteLatitud = result.getData().getDoubleExtra("LATITUD_ELEGIDA", 0.0);
                    clienteLongitud = result.getData().getDoubleExtra("LONGITUD_ELEGIDA", 0.0);

                    if (clienteLatitud != 0.0 || clienteLongitud != 0.0) {
                        tvCoordenadasGps.setText(String.format(Locale.getDefault(), "%.4f, %.4f", clienteLatitud, clienteLongitud));
                        tvCoordenadasGps.setTextColor(Color.parseColor("#2D6A4F"));
                        Toast.makeText(this, "Coordenadas actualizadas desde el mapa", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_servicio);

        dbHelper = new DbHelper(this);

        btnVolverRegistro = findViewById(R.id.btnVolverRegistro);
        btnSeleccionarClienteServicio = findViewById(R.id.btnSeleccionarClienteServicio);
        tvClienteServicioTexto = findViewById(R.id.tvClienteServicioTexto);
        tvFaenaServicioInfo = findViewById(R.id.tvFaenaServicioInfo);
        btnAgregarBanosServicio = findViewById(R.id.btnAgregarBanosServicio);
        contenedorChecksBanos = findViewById(R.id.contenedorChecksBanos);
        tvAvisoSinBanos = findViewById(R.id.tvAvisoSinBanos);
        tvCoordenadasGps = findViewById(R.id.tvCoordenadasGps);
        btnCapturarGps = findViewById(R.id.btnCapturarGps);
        etObservaciones = findViewById(R.id.etObservaciones);
        btnGuardarServicio = findViewById(R.id.btnGuardarServicio);
        opTipoLimpieza = findViewById(R.id.opTipoLimpieza);
        opTipoEnvarillado = findViewById(R.id.opTipoEnvarillado);
        opTipoArriendo = findViewById(R.id.opTipoArriendo);
        seccionBanos = findViewById(R.id.seccionBanos);
        seccionMetros = findViewById(R.id.seccionMetros);
        etPrecio = findViewById(R.id.etPrecio);
        etMetrosCubicos = findViewById(R.id.etMetrosCubicos);
        configurarFormatoPrecio();

        btnVolverRegistro.setOnClickListener(v -> finish());
        btnSeleccionarClienteServicio.setOnClickListener(v -> mostrarBottomSheetClientes());

        btnAgregarBanosServicio.setOnClickListener(v -> {
            if (clienteSeleccionadoNombre.isEmpty()) {
                tvClienteServicioTexto.setError("Selecciona un cliente");
                Toast.makeText(this, "Debes seleccionar un cliente del directorio", Toast.LENGTH_LONG).show();
                btnSeleccionarClienteServicio.requestFocus();
                return;
            }
            mostrarDialogoSeleccionarBanosDisponibles();
        });

        btnCapturarGps.setOnClickListener(v -> {
            Intent intent = new Intent(this, SeleccionarUbicacionActivity.class);
            selectorMapaLauncher.launch(intent);
        });

        opTipoLimpieza.setOnClickListener(v -> seleccionarTipo(TIPO_LIMPIEZA));
        opTipoEnvarillado.setOnClickListener(v -> seleccionarTipo(TIPO_ENVARILLADO));
        opTipoArriendo.setOnClickListener(v -> seleccionarTipo(TIPO_ARRIENDO));
        seleccionarTipo("");

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("ID_SERVICIO")) {
            idServicioAEditar = intent.getLongExtra("ID_SERVICIO", -1);
            if (idServicioAEditar != -1) {
                cargarDatosServicioParaEditar(idServicioAEditar);
                btnGuardarServicio.setText("Actualizar Registro de Servicio");
            }
        }

        if (intent != null && intent.hasExtra("ID_TAREA_PROGRAMADA")) {
            idTareaProgramada = intent.getLongExtra("ID_TAREA_PROGRAMADA", -1);

            if (idServicioAEditar == -1 && idTareaProgramada != -1) {
                cargarTareaProgramada(idTareaProgramada);
            }
        }

        btnGuardarServicio.setOnClickListener(v -> guardarOActualizar());
    }

    private void cargarTareaProgramada(long idTarea) {
        try (Cursor cursor = dbHelper.getReadableDatabase().query(
                DbHelper.TABLA_TAREAS,
                null,
                DbHelper.COL_TAREA_ID + " = ? AND " +
                        DbHelper.COL_TAREA_CATEGORIA + " = ? AND LOWER(" +
                        DbHelper.COL_TAREA_ESTADO + ") <> ?",
                new String[]{String.valueOf(idTarea), "SERVICIO_PROGRAMADO", "completada"},
                null,
                null,
                null
        )) {
            if (!cursor.moveToFirst()) {
                idTareaProgramada = -1;
                Toast.makeText(this, "La tarea no está disponible", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            int idxCliente = cursor.getColumnIndexOrThrow(DbHelper.COL_TAREA_CLIENTE);
            int idxTipo = cursor.getColumnIndexOrThrow(DbHelper.COL_TAREA_TIPO);
            int idxDetalles = cursor.getColumnIndexOrThrow(DbHelper.COL_TAREA_DETALLES);

            String cliente = cursor.getString(idxCliente);
            String tipo = cursor.getString(idxTipo);
            String detalles = cursor.getString(idxDetalles);

            clienteSeleccionadoNombre = cliente == null ? "" : cliente;

            tvClienteServicioTexto.setText(clienteSeleccionadoNombre);
            tvClienteServicioTexto.setTextColor(
                    ContextCompat.getColor(this, R.color.text_main)
            );

            double[] coordenadas = dbHelper.obtenerCoordenadasCliente(clienteSeleccionadoNombre);

            if (coordenadas != null) {
                clienteLatitud = coordenadas[0];
                clienteLongitud = coordenadas[1];

                if (clienteLatitud != 0.0 || clienteLongitud != 0.0) {
                    tvCoordenadasGps.setText(String.format(
                            Locale.getDefault(),
                            "%.4f, %.4f",
                            clienteLatitud,
                            clienteLongitud
                    ));
                }
            }

            seleccionarTipoDesdeTexto(tipo);
            etObservaciones.setText(detalles == null ? "" : detalles);
        }
    }

    private void mostrarBottomSheetClientes() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_selector_clientes, null);
        dialog.setContentView(dialogView);

        LinearLayout contenedor = dialogView.findViewById(R.id.contenedorListaClientesModal);
        TextView tvTotal = dialogView.findViewById(R.id.tvModalTotalClientes);

        Cursor cursor = dbHelper.obtenerClientes("");
        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) cursor.close();
            if (tvTotal != null) tvTotal.setText("0 clientes");
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No hay clientes registrados en el Directorio.");
            tvVacio.setPadding(0, 40, 0, 40);
            tvVacio.setTextSize(13f);
            tvVacio.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            if (contenedor != null) contenedor.addView(tvVacio);
        } else {
            if (tvTotal != null) {
                tvTotal.setText(cursor.getCount() + (cursor.getCount() == 1 ? " cliente" : " clientes"));
            }

            while (cursor.moveToNext()) {
                int idxNombre = cursor.getColumnIndex(DbHelper.COL_CLI_NOMBRE);
                int idxFaena = cursor.getColumnIndex(DbHelper.COL_CLI_FAENA);
                int idxLat = cursor.getColumnIndex(DbHelper.COL_CLI_LATITUD);
                int idxLng = cursor.getColumnIndex(DbHelper.COL_CLI_LONGITUD);

                String nombre = (idxNombre != -1) ? cursor.getString(idxNombre) : "Cliente";
                String faena = (idxFaena != -1) ? cursor.getString(idxFaena) : "Sin faena";
                double lat = (idxLat != -1) ? cursor.getDouble(idxLat) : 0.0;
                double lng = (idxLng != -1) ? cursor.getDouble(idxLng) : 0.0;

                View itemView = getLayoutInflater().inflate(R.layout.item_cliente_selector, contenedor, false);

                TextView tvAvatar = itemView.findViewById(R.id.tvAvatarClienteItem);
                TextView tvNombre = itemView.findViewById(R.id.tvNombreClienteItem);
                TextView tvFaenaItem = itemView.findViewById(R.id.tvFaenaClienteItem);
                TextView tvBadgeGps = itemView.findViewById(R.id.tvBadgeGpsCliente);

                if (tvNombre != null) tvNombre.setText(nombre);
                if (tvFaenaItem != null) tvFaenaItem.setText(faena);
                if (tvAvatar != null) {
                    tvAvatar.setText(nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : "CL");
                }

                if (tvBadgeGps != null) {
                    if (lat == 0.0 && lng == 0.0) {
                        tvBadgeGps.setText("Sin GPS");
                        tvBadgeGps.setTextColor(Color.GRAY);
                    } else {
                        tvBadgeGps.setText("● Con GPS");
                        tvBadgeGps.setTextColor(Color.parseColor("#2D6A4F"));
                    }
                }

                itemView.setOnClickListener(item -> {
                    clienteSeleccionadoNombre = nombre;
                    clienteLatitud = lat;
                    clienteLongitud = lng;

                    tvClienteServicioTexto.setText(nombre);
                    tvClienteServicioTexto.setTextColor(ContextCompat.getColor(this, R.color.text_main));

                    if (lat != 0.0 && lng != 0.0) {
                        tvFaenaServicioInfo.setText(String.format(Locale.getDefault(), "✅ Faena: %s", faena));
                        tvCoordenadasGps.setText(String.format(Locale.getDefault(), "%.4f, %.4f", lat, lng));
                        tvCoordenadasGps.setTextColor(Color.parseColor("#2D6A4F"));
                    } else {
                        tvFaenaServicioInfo.setText("ℹ️ Faena: " + faena + " · Sin coordenadas fijas");
                        tvCoordenadasGps.setText("GPS no capturado");
                        tvCoordenadasGps.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                    }

                    cargarBanosAsignadosPreviamente(nombre);
                    dialog.dismiss();
                });

                if (contenedor != null) {
                    contenedor.addView(itemView);
                }
            }
            cursor.close();
        }

        dialog.show();
    }

    private void cargarBanosAsignadosPreviamente(String nombreCliente) {
        contenedorChecksBanos.removeAllViews();
        banosSeleccionados.clear();

        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT " + DbHelper.COL_BANO_CODIGO + ", " +
                            DbHelper.COL_BANO_ESTADO + ", " +
                            DbHelper.COL_BANO_LATITUD + ", " +
                            DbHelper.COL_BANO_LONGITUD +
                            " FROM " + DbHelper.TABLA_BANOS +
                            " WHERE " + DbHelper.COL_BANO_CLIENTE + " = ?",
                    new String[]{nombreCliente}
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cursor != null && cursor.moveToFirst()) {
            tvAvisoSinBanos.setVisibility(View.GONE);
            int idxCodigo = cursor.getColumnIndex(DbHelper.COL_BANO_CODIGO);
            int idxEstado = cursor.getColumnIndex(DbHelper.COL_BANO_ESTADO);
            int idxLat = cursor.getColumnIndex(DbHelper.COL_BANO_LATITUD);
            int idxLng = cursor.getColumnIndex(DbHelper.COL_BANO_LONGITUD);

            do {
                String codigo = (idxCodigo != -1) ? cursor.getString(idxCodigo) : "Baño";
                String estado = (idxEstado != -1) ? cursor.getString(idxEstado) : "";
                double latBano = (idxLat != -1) ? cursor.getDouble(idxLat) : 0.0;
                double lngBano = (idxLng != -1) ? cursor.getDouble(idxLng) : 0.0;

                LinearLayout fila = new LinearLayout(this);
                fila.setOrientation(LinearLayout.HORIZONTAL);
                fila.setGravity(android.view.Gravity.CENTER_VERTICAL);
                fila.setPadding(0, 4, 0, 4);

                CheckBox cb = new CheckBox(this);
                cb.setTag(codigo);
                cb.setTextSize(13f);
                cb.setText("🚽 Baño " + codigo + " (" + estado + ")");
                cb.setTextColor(ContextCompat.getColor(this, R.color.text_main));
                LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                cb.setLayoutParams(cbParams);

                cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (!banosSeleccionados.contains(codigo)) {
                            banosSeleccionados.add(codigo);
                        }
                        if (latBano != 0.0 && lngBano != 0.0) {
                            clienteLatitud = latBano;
                            clienteLongitud = lngBano;
                            tvCoordenadasGps.setText(String.format(Locale.getDefault(), "%.4f, %.4f", latBano, lngBano));
                            tvCoordenadasGps.setTextColor(Color.parseColor("#2D6A4F"));
                        }
                    } else {
                        banosSeleccionados.remove(codigo);
                    }
                });

                TextView btnMantencion = new TextView(this);
                btnMantencion.setText("🔧 Mantención");
                btnMantencion.setTextSize(11f);
                btnMantencion.setTextColor(Color.parseColor("#D97706"));
                btnMantencion.setPadding(12, 6, 12, 6);
                btnMantencion.setBackgroundResource(R.drawable.bg_input_field);
                btnMantencion.setOnClickListener(v -> {
                    dbHelper.actualizarEstadoBano(codigo, "En Mantención");
                    Toast.makeText(this, "Baño " + codigo + " enviado a En Mantención", Toast.LENGTH_SHORT).show();
                    cargarBanosAsignadosPreviamente(nombreCliente);
                });

                fila.addView(cb);
                fila.addView(btnMantencion);
                contenedorChecksBanos.addView(fila);
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            tvAvisoSinBanos.setVisibility(View.VISIBLE);
            tvAvisoSinBanos.setText("Este cliente no tiene baños asignados. Presiona '+ Asignar Baños' para agregar unidades disponibles en bodega.");
            if (cursor != null) cursor.close();
        }
    }

    private void mostrarDialogoSeleccionarBanosDisponibles() {
        List<String> codigosBanos = new ArrayList<>();
        List<Double> lats = new ArrayList<>();
        List<Double> lngs = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT " + DbHelper.COL_BANO_CODIGO + ", " +
                            DbHelper.COL_BANO_LATITUD + ", " +
                            DbHelper.COL_BANO_LONGITUD +
                            " FROM " + DbHelper.TABLA_BANOS +
                            " WHERE LOWER(" + DbHelper.COL_BANO_ESTADO + ") = 'disponible'",
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int idxCodigo = cursor.getColumnIndex(DbHelper.COL_BANO_CODIGO);
                int idxLat = cursor.getColumnIndex(DbHelper.COL_BANO_LATITUD);
                int idxLng = cursor.getColumnIndex(DbHelper.COL_BANO_LONGITUD);

                do {
                    String cod = cursor.getString(idxCodigo);
                    double lat = (idxLat != -1) ? cursor.getDouble(idxLat) : 0.0;
                    double lng = (idxLng != -1) ? cursor.getDouble(idxLng) : 0.0;

                    codigosBanos.add(cod);
                    lats.add(lat);
                    lngs.add(lng);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        if (codigosBanos.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Sin Stock Disponible")
                    .setMessage("No hay baños registrados con estado 'Disponible' en la bodega.")
                    .setPositiveButton("Entendido", null)
                    .show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_seleccionar_banos, null);
        LinearLayout contenedor = dialogView.findViewById(R.id.contenedorListaBanosModal);
        ScrollView scrollBanos = dialogView.findViewById(R.id.scrollBanosDisponibles);
        TextView btnCancelar = dialogView.findViewById(R.id.btnCancelarModalBanos);
        AppCompatButton btnConfirmar = dialogView.findViewById(R.id.btnConfirmarModalBanos);

        List<String> seleccionTemporal = new ArrayList<>(banosSeleccionados);

        for (int i = 0; i < codigosBanos.size(); i++) {
            String codigo = codigosBanos.get(i);
            double latBano = lats.get(i);
            double lngBano = lngs.get(i);

            LinearLayout itemCard = new LinearLayout(this);
            itemCard.setOrientation(LinearLayout.HORIZONTAL);
            itemCard.setGravity(android.view.Gravity.CENTER_VERTICAL);
            itemCard.setBackgroundResource(R.drawable.bg_input_field);
            itemCard.setPadding(32, 24, 32, 24);

            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            itemParams.setMargins(0, 0, 0, 16);
            itemCard.setLayoutParams(itemParams);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setChecked(seleccionTemporal.contains(codigo));

            LinearLayout infoLayout = new LinearLayout(this);
            infoLayout.setOrientation(LinearLayout.VERTICAL);
            infoLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            infoLayout.setPadding(20, 0, 0, 0);

            TextView tvNombreBano = new TextView(this);
            tvNombreBano.setText("Baño " + codigo);
            tvNombreBano.setTextColor(ContextCompat.getColor(this, R.color.text_main));
            tvNombreBano.setTextSize(14f);
            tvNombreBano.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvSub = new TextView(this);
            tvSub.setText("En bodega · Químico");
            tvSub.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            tvSub.setTextSize(11f);

            infoLayout.addView(tvNombreBano);
            infoLayout.addView(tvSub);

            TextView badgeDisponible = new TextView(this);
            badgeDisponible.setText("Disponible");
            badgeDisponible.setTextColor(Color.parseColor("#2D6A4F"));
            badgeDisponible.setBackgroundColor(Color.parseColor("#E8F5E9"));
            badgeDisponible.setTextSize(11f);
            badgeDisponible.setTypeface(null, android.graphics.Typeface.BOLD);
            badgeDisponible.setPadding(16, 6, 16, 6);

            itemCard.addView(checkBox);
            itemCard.addView(infoLayout);
            itemCard.addView(badgeDisponible);

            itemCard.setOnClickListener(v -> checkBox.setChecked(!checkBox.isChecked()));

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!seleccionTemporal.contains(codigo)) {
                        seleccionTemporal.add(codigo);
                    }
                    if (latBano != 0.0 && lngBano != 0.0) {
                        clienteLatitud = latBano;
                        clienteLongitud = lngBano;
                        tvCoordenadasGps.setText(String.format(Locale.getDefault(), "%.4f, %.4f", clienteLatitud, clienteLongitud));
                        tvCoordenadasGps.setTextColor(Color.parseColor("#2D6A4F"));
                    }
                } else {
                    seleccionTemporal.remove(codigo);
                }
            });

            contenedor.addView(itemCard);
        }

        if (codigosBanos.size() > 3) {
            contenedor.measure(
                    View.MeasureSpec.makeMeasureSpec(
                            getResources().getDisplayMetrics().widthPixels,
                            View.MeasureSpec.AT_MOST
                    ),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );

            int alturaMaxima = 0;

            for (int i = 0; i < 3; i++) {
                View tarjeta = contenedor.getChildAt(i);

                alturaMaxima += tarjeta.getMeasuredHeight();

                LinearLayout.LayoutParams params =
                        (LinearLayout.LayoutParams) tarjeta.getLayoutParams();

                alturaMaxima += params.topMargin + params.bottomMargin;
            }

            scrollBanos.getLayoutParams().height = alturaMaxima;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            banosSeleccionados.clear();
            banosSeleccionados.addAll(seleccionTemporal);
            refrescarVistaBanosSeleccionados();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void refrescarVistaBanosSeleccionados() {
        contenedorChecksBanos.removeAllViews();
        tvAvisoSinBanos.setTextColor(
                ContextCompat.getColor(this, R.color.text_muted)
        );
        if (banosSeleccionados.isEmpty()) {
            tvAvisoSinBanos.setVisibility(View.VISIBLE);
            tvAvisoSinBanos.setText("No hay baños seleccionados para este servicio.");
        } else {
            tvAvisoSinBanos.setVisibility(View.GONE);
            for (String cod : banosSeleccionados) {
                TextView chip = new TextView(this);
                chip.setText("🚽 Baño " + cod + " · Seleccionado para faena");
                chip.setTextColor(Color.parseColor("#2D6A4F"));
                chip.setTextSize(13f);
                chip.setPadding(8, 6, 8, 6);
                contenedorChecksBanos.addView(chip);
            }
        }
    }

    private void seleccionarTipo(String tipo) {
        boolean cambioDeTipo = !tipo.equals(tipoSeleccionado);
        tipoSeleccionado = tipo;

        estilizarOpcion(opTipoLimpieza, tipo.equals(TIPO_LIMPIEZA));
        estilizarOpcion(opTipoEnvarillado, tipo.equals(TIPO_ENVARILLADO));
        estilizarOpcion(opTipoArriendo, tipo.equals(TIPO_ARRIENDO));

        boolean esArriendo = tipo.equals(TIPO_ARRIENDO);
        seccionBanos.setVisibility(esArriendo ? View.VISIBLE : View.GONE);

        seccionMetros.setVisibility(
                tipo.equals(TIPO_LIMPIEZA) ? View.VISIBLE : View.GONE
        );

        if (!cambioDeTipo) return;

        if (!tipo.equals(TIPO_LIMPIEZA)) {
            etMetrosCubicos.setText("");
        }

        if (esArriendo) {
            if (!clienteSeleccionadoNombre.isEmpty()) {
                cargarBanosAsignadosPreviamente(clienteSeleccionadoNombre);
            }
        } else {
            banosSeleccionados.clear();
            contenedorChecksBanos.removeAllViews();
        }
    }

    private void estilizarOpcion(TextView opcion, boolean seleccionada) {
        opcion.setBackgroundResource(seleccionada ? R.drawable.bg_green_tile : R.drawable.bg_input_field);
        opcion.setTextColor(ContextCompat.getColor(this,
                seleccionada ? R.color.text_light : R.color.text_main));
    }

    private void seleccionarTipoDesdeTexto(String texto) {
        String t = texto == null ? "" : texto.toLowerCase(Locale.ROOT);
        if (t.contains("limpieza") || t.contains("fosa")) {
            seleccionarTipo(TIPO_LIMPIEZA);
        } else if (t.contains("envarill")) {
            seleccionarTipo(TIPO_ENVARILLADO);
        } else if (t.contains("arriendo") || t.contains("baño") || t.contains("bano")) {
            seleccionarTipo(TIPO_ARRIENDO);
        } else {
            seleccionarTipo("");
        }
    }

    private void restaurarBanosDelServicio(List<String> codigos) {
        banosSeleccionados.clear();

        for (int i = 0; i < contenedorChecksBanos.getChildCount(); i++) {
            View fila = contenedorChecksBanos.getChildAt(i);

            if (!(fila instanceof LinearLayout)) continue;

            LinearLayout contenedorFila = (LinearLayout) fila;

            for (int j = 0; j < contenedorFila.getChildCount(); j++) {
                View elemento = contenedorFila.getChildAt(j);

                if (elemento instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) elemento;
                    Object etiqueta = checkBox.getTag();

                    if (etiqueta instanceof String) {
                        String codigo = (String) etiqueta;
                        checkBox.setChecked(codigos.contains(codigo));
                    }
                }
            }
        }

        for (String codigo : codigos) {
            if (!banosSeleccionados.contains(codigo)) {
                banosSeleccionados.add(codigo);

                TextView aviso = new TextView(this);
                aviso.setText("🚽 Baño " + codigo + " · Asignado originalmente");
                aviso.setTextColor(ContextCompat.getColor(this, R.color.text_main));
                aviso.setTextSize(13f);
                contenedorChecksBanos.addView(aviso);
            }
        }
    }

    private void cargarDatosServicioParaEditar(long id) {
        Cursor cursor = dbHelper.obtenerServicioPorId(id);
        if (cursor != null && cursor.moveToFirst()) {
            int idxOt = cursor.getColumnIndex(DbHelper.COL_OT);
            int idxCli = cursor.getColumnIndex(DbHelper.COL_CLIENTE);
            int idxTipo = cursor.getColumnIndex(DbHelper.COL_TIPO);
            int idxDesc = cursor.getColumnIndex(DbHelper.COL_DESCRIPCION);

            if (idxOt != -1) numeroOtActual = cursor.getString(idxOt);
            if (idxCli != -1) {
                clienteSeleccionadoNombre = cursor.getString(idxCli);
                tvClienteServicioTexto.setText(clienteSeleccionadoNombre);
                tvClienteServicioTexto.setTextColor(ContextCompat.getColor(this, R.color.text_main));

                double[] coords = dbHelper.obtenerCoordenadasCliente(clienteSeleccionadoNombre);
                if (coords != null && (coords[0] != 0.0 || coords[1] != 0.0)) {
                    clienteLatitud = coords[0];
                    clienteLongitud = coords[1];
                    tvCoordenadasGps.setText(String.format(Locale.getDefault(), "%.4f, %.4f", clienteLatitud, clienteLongitud));
                    tvCoordenadasGps.setTextColor(Color.parseColor("#2D6A4F"));
                }
                cargarBanosAsignadosPreviamente(clienteSeleccionadoNombre);
            }
            if (idxTipo != -1) {
                seleccionarTipoDesdeTexto(cursor.getString(idxTipo));
            }

            if (idxDesc != -1) {
                String descripcionGuardada = cursor.getString(idxDesc);

                etObservaciones.setText(limpiarDescripcionServicio(descripcionGuardada));

                if (TIPO_ARRIENDO.equals(tipoSeleccionado)) {
                    restaurarBanosDelServicio(
                            obtenerBanosDesdeDescripcion(descripcionGuardada)
                    );
                }
            }

            int idxPrecio = cursor.getColumnIndex(DbHelper.COL_PRECIO);
            int idxMetros = cursor.getColumnIndex(DbHelper.COL_METROS_CUBICOS);

            if (idxPrecio != -1 && cursor.getLong(idxPrecio) > 0) {
                etPrecio.setText(String.valueOf(cursor.getLong(idxPrecio)));
            }

            if (idxMetros != -1 && cursor.getDouble(idxMetros) > 0) {
                double m3 = cursor.getDouble(idxMetros);
                etMetrosCubicos.setText(
                        m3 == Math.floor(m3)
                                ? String.valueOf((long) m3)
                                : String.valueOf(m3)
                );
            }

            cursor.close();
        }
    }

    private String limpiarDescripcionServicio(String descripcion) {
        if (descripcion == null) return "";

        String resultado = descripcion;

        Pattern patronBanos = Pattern.compile("^Baños: .*?\\. Unidades: \\d+\\. ");
        Pattern patronGps = Pattern.compile("^GPS: -?\\d+(?:\\.\\d+)?, -?\\d+(?:\\.\\d+)?\\. ");

        boolean cambio;

        do {
            cambio = false;

            Matcher matcherBanos = patronBanos.matcher(resultado);
            if (matcherBanos.find()) {
                resultado = resultado.substring(matcherBanos.end());
                cambio = true;
            }

            Matcher matcherGps = patronGps.matcher(resultado);
            if (matcherGps.find()) {
                resultado = resultado.substring(matcherGps.end());
                cambio = true;
            }
        } while (cambio);

        return resultado.trim();
    }

    private List<String> obtenerBanosDesdeDescripcion(String descripcion) {
        List<String> codigos = new ArrayList<>();

        if (descripcion == null) return codigos;

        Matcher matcher = Pattern.compile("Baños: (.*?)\\. Unidades: \\d+\\.").matcher(descripcion);

        if (matcher.find()) {
            String[] partes = matcher.group(1).split(",");

            for (String parte : partes) {
                String codigo = parte.trim();

                if (!codigo.isEmpty() && !codigos.contains(codigo)) {
                    codigos.add(codigo);
                }
            }
        }

        return codigos;
    }

    private double leerDecimal(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void configurarFormatoPrecio() {
        etPrecio.addTextChangedListener(new TextWatcher() {
            private boolean actualizando = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (actualizando) return;

                String digitos = s.toString().replaceAll("[^0-9]", "");

                if (digitos.isEmpty()) return;

                try {
                    long valor = Long.parseLong(digitos);

                    NumberFormat formato = NumberFormat.getIntegerInstance(
                            new Locale("es", "CL")
                    );

                    String precioFormateado = formato.format(valor);

                    if (!precioFormateado.equals(s.toString())) {
                        actualizando = true;
                        etPrecio.setText(precioFormateado);
                        etPrecio.setSelection(etPrecio.getText().length());
                        actualizando = false;
                    }
                } catch (NumberFormatException e) {
                    etPrecio.setError("Precio demasiado grande");
                }
            }
        });
    }

    private long leerEntero(String texto) {
        String soloDigitos = texto.replaceAll("[^0-9]", "");
        if (soloDigitos.isEmpty()) return 0;

        try {
            return Long.parseLong(soloDigitos);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean finalizarTareaProgramada(long idServicio) {
        if (idTareaProgramada == -1) {
            return true;
        }

        ContentValues valores = new ContentValues();
        valores.put(DbHelper.COL_TAREA_ESTADO, "Completada");
        valores.put(DbHelper.COL_TAREA_ID_SERVICIO, idServicio);

        int filas = dbHelper.getWritableDatabase().update(
                DbHelper.TABLA_TAREAS,
                valores,
                DbHelper.COL_TAREA_ID + " = ? AND " +
                        DbHelper.COL_TAREA_CATEGORIA + " = ? AND LOWER(" +
                        DbHelper.COL_TAREA_ESTADO + ") <> ?",
                new String[]{
                        String.valueOf(idTareaProgramada),
                        "SERVICIO_PROGRAMADO",
                        "completada"
                }
        );

        if (filas > 0) {
            AvisosTareas.cancelar(this, idTareaProgramada);
            return true;
        }

        return false;
    }

    private void mostrarConfirmacionGuardado(boolean esActualizacion) {
        String titulo = esActualizacion
                ? "Servicio actualizado"
                : "Servicio registrado";

        String mensaje = esActualizacion
                ? "Los cambios del servicio se guardaron correctamente en este dispositivo."
                : "El servicio se registró correctamente y quedó guardado en este dispositivo.";

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setCancelable(false)
                .setPositiveButton("Aceptar", (dialog, which) -> finish())
                .show();
    }

    private void guardarOActualizar() {
        if (clienteSeleccionadoNombre.isEmpty()) {
            Toast.makeText(this, "Debes seleccionar un cliente registrado del Directorio", Toast.LENGTH_LONG).show();
            return;
        }

        if (tipoSeleccionado.isEmpty()) {
            Toast.makeText(this, "Selecciona Limpieza de fosa, Envarillado o Arriendo de baños", Toast.LENGTH_LONG).show();
            opTipoLimpieza.requestFocus();
            return;
        }

        boolean esArriendo = tipoSeleccionado.equals(TIPO_ARRIENDO);
        if (esArriendo && banosSeleccionados.isEmpty()) {
            tvAvisoSinBanos.setVisibility(View.VISIBLE);
            tvAvisoSinBanos.setText("⚠ Debes seleccionar al menos un baño para registrar el arriendo");
            tvAvisoSinBanos.setTextColor(Color.parseColor("#C62828"));

            Toast.makeText(this, "Selecciona al menos un baño disponible", Toast.LENGTH_LONG).show();
            btnAgregarBanosServicio.requestFocus();
            return;
        }

        double metrosCubicos = tipoSeleccionado.equals(TIPO_LIMPIEZA)
                ? leerDecimal(etMetrosCubicos.getText().toString()) : 0;

        if (tipoSeleccionado.equals(TIPO_LIMPIEZA) && metrosCubicos <= 0) {
            etMetrosCubicos.setError("Ingresa una cantidad mayor a cero");
            etMetrosCubicos.requestFocus();

            Toast.makeText(this, "Debes indicar los metros cúbicos de la limpieza", Toast.LENGTH_LONG).show();
            return;
        }

        long precio = leerEntero(etPrecio.getText().toString());

        String precioIngresado = etPrecio.getText().toString().trim();

        if (!precioIngresado.isEmpty() && precio == 0
                && !precioIngresado.replaceAll("[^0-9]", "").matches("0+")) {
            etPrecio.setError("Ingresa un precio válido");
            etPrecio.requestFocus();
            return;
        }

        String tipo = tipoSeleccionado;
        String observaciones = etObservaciones.getText().toString().trim();

        StringBuilder descripcionCompleta = new StringBuilder();

        if (esArriendo) {
            descripcionCompleta.append("Baños: ").append(TextUtils.join(", ", banosSeleccionados)).append(". ");
            descripcionCompleta.append("Unidades: ").append(banosSeleccionados.size()).append(". ");
        }

        if (clienteLatitud != 0.0 || clienteLongitud != 0.0) {
            descripcionCompleta.append(String.format(Locale.US, "GPS: %.4f, %.4f. ", clienteLatitud, clienteLongitud));
        }

        if (!observaciones.isEmpty()) {
            descripcionCompleta.append(observaciones);
        }

        if (idServicioAEditar == -1) {
            String otGenerada = "OT-" + (System.currentTimeMillis() % 10000);
            String fechaActual = new SimpleDateFormat("dd MMM yyyy · HH:mm", new Locale("es", "CL")).format(new Date());

            long resultado = dbHelper.insertarServicio(
                    otGenerada,
                    clienteSeleccionadoNombre,
                    tipo,
                    descripcionCompleta.toString().trim(),
                    fechaActual,
                    false,
                    precio,
                    metrosCubicos
            );

            if (resultado > 0) {
                for (String codBano : banosSeleccionados) {
                    try {
                        dbHelper.getWritableDatabase().execSQL(
                                "UPDATE " + DbHelper.TABLA_BANOS + " SET " +
                                        DbHelper.COL_BANO_CLIENTE + " = ?, " +
                                        DbHelper.COL_BANO_ESTADO + " = 'Arrendado', " +
                                        DbHelper.COL_BANO_LATITUD + " = ?, " +
                                        DbHelper.COL_BANO_LONGITUD + " = ? " +
                                        "WHERE " + DbHelper.COL_BANO_CODIGO + " = ?",
                                new Object[]{clienteSeleccionadoNombre, clienteLatitud, clienteLongitud, codBano}
                        );
                    } catch (Exception ignored) {}
                }

                if (finalizarTareaProgramada(resultado)) {
                    mostrarConfirmacionGuardado(false);
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Servicio guardado")
                            .setMessage("El servicio se guardó, pero no se pudo completar la tarea programada. Revisa el historial antes de volver a registrarlo.")
                            .setPositiveButton("Aceptar", (dialog, which) -> finish())
                            .show();
                }
            } else {
                Toast.makeText(this, "Error al guardar en el dispositivo", Toast.LENGTH_SHORT).show();
            }
        } else {
            int filasAfectadas = dbHelper.actualizarServicio(
                    idServicioAEditar,
                    numeroOtActual.isEmpty() ? "OT-Mod" : numeroOtActual,
                    clienteSeleccionadoNombre,
                    tipo,
                    descripcionCompleta.toString().trim(),
                    false,
                    precio,
                    metrosCubicos
            );

            if (filasAfectadas > 0) {
                mostrarConfirmacionGuardado(true);
            } else {
                Toast.makeText(this, "Error al actualizar registro", Toast.LENGTH_SHORT).show();
            }
        }
    }
}