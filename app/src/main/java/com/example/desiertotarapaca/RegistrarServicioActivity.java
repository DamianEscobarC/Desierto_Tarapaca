package com.example.desiertotarapaca;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class RegistrarServicioActivity extends AppCompatActivity {

    private TextView btnVolverRegistro;
    private View btnSeleccionarClienteServicio;
    private TextView tvClienteServicioTexto, tvFaenaServicioInfo, tvCoordenadasGps;
    private AppCompatButton btnAgregarBanosServicio;
    private LinearLayout contenedorChecksBanos;
    private TextView tvAvisoSinBanos;
    private EditText etTipoTrabajo, etObservaciones;
    private AppCompatButton btnCapturarGps, btnGuardarServicio;

    private DbHelper dbHelper;
    private long idServicioAEditar = -1;
    private String numeroOtActual = "";

    private String clienteSeleccionadoNombre = "";
    private double clienteLatitud = 0.0;
    private double clienteLongitud = 0.0;

    private final List<String> banosSeleccionados = new ArrayList<>();

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
        etTipoTrabajo = findViewById(R.id.etTipoTrabajo);
        tvCoordenadasGps = findViewById(R.id.tvCoordenadasGps);
        btnCapturarGps = findViewById(R.id.btnCapturarGps);
        etObservaciones = findViewById(R.id.etObservaciones);
        btnGuardarServicio = findViewById(R.id.btnGuardarServicio);

        btnVolverRegistro.setOnClickListener(v -> finish());
        btnSeleccionarClienteServicio.setOnClickListener(v -> mostrarBottomSheetClientes());

        btnAgregarBanosServicio.setOnClickListener(v -> {
            if (clienteSeleccionadoNombre.isEmpty()) {
                Toast.makeText(this, "Primero selecciona un cliente del directorio", Toast.LENGTH_SHORT).show();
                return;
            }
            mostrarDialogoSeleccionarBanosDisponibles();
        });

        btnCapturarGps.setOnClickListener(v -> {
            Intent intent = new Intent(this, SeleccionarUbicacionActivity.class);
            selectorMapaLauncher.launch(intent);
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("ID_SERVICIO")) {
            idServicioAEditar = intent.getLongExtra("ID_SERVICIO", -1);
            if (idServicioAEditar != -1) {
                cargarDatosServicioParaEditar(idServicioAEditar);
                btnGuardarServicio.setText("Actualizar Registro de Servicio");
            }
        }

        btnGuardarServicio.setOnClickListener(v -> guardarOActualizar());
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
            if (idxTipo != -1) etTipoTrabajo.setText(cursor.getString(idxTipo));
            if (idxDesc != -1) etObservaciones.setText(cursor.getString(idxDesc));

            cursor.close();
        }
    }

    private void guardarOActualizar() {
        if (clienteSeleccionadoNombre.isEmpty()) {
            Toast.makeText(this, "Debes seleccionar un cliente registrado del Directorio", Toast.LENGTH_LONG).show();
            return;
        }

        if (banosSeleccionados.isEmpty()) {
            Toast.makeText(this, "Debes asignar al menos un baño disponible para este servicio", Toast.LENGTH_LONG).show();
            return;
        }

        String tipo = etTipoTrabajo.getText().toString().trim();
        String observaciones = etObservaciones.getText().toString().trim();

        if (tipo.isEmpty()) {
            etTipoTrabajo.setError("Ingresa el tipo de trabajo");
            etTipoTrabajo.requestFocus();
            return;
        }

        StringBuilder descripcionCompleta = new StringBuilder();

        descripcionCompleta.append("Baños: ").append(TextUtils.join(", ", banosSeleccionados)).append(". ");
        descripcionCompleta.append("Unidades: ").append(banosSeleccionados.size()).append(". ");

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
                    false
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

                Toast.makeText(this, "Servicio registrado localmente en SQLite", Toast.LENGTH_SHORT).show();
                finish();
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
                    false
            );

            if (filasAfectadas > 0) {
                Toast.makeText(this, "Registro actualizado correctamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error al actualizar registro", Toast.LENGTH_SHORT).show();
            }
        }
    }
}