package com.example.desiertotarapaca;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;
import java.util.Locale;

public class RegistrarTareaActivity extends AppCompatActivity {

    private TextView btnVolverTarea;
    private View btnSeleccionarClienteModal;
    private TextView tvClienteSeleccionadoTexto, tvUbicacionClienteInfo;
    private EditText etTareaTipo, etTareaFecha, etTareaHora;
    private AppCompatButton btnGuardarTarea;
    private DbHelper dbHelper;

    private String clienteSeleccionadoNombre = "";
    private double clienteLatitud = 0.0;
    private double clienteLongitud = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_tarea);

        dbHelper = new DbHelper(this);

        btnVolverTarea = findViewById(R.id.btnVolverTarea);
        btnSeleccionarClienteModal = findViewById(R.id.btnSeleccionarClienteModal);
        tvClienteSeleccionadoTexto = findViewById(R.id.tvClienteSeleccionadoTexto);
        tvUbicacionClienteInfo = findViewById(R.id.tvUbicacionClienteInfo);
        etTareaTipo = findViewById(R.id.etTareaTipo);
        etTareaFecha = findViewById(R.id.etTareaFecha);
        etTareaHora = findViewById(R.id.etTareaHora);
        btnGuardarTarea = findViewById(R.id.btnGuardarTarea);

        btnVolverTarea.setOnClickListener(v -> finish());
        etTareaFecha.setOnClickListener(v -> mostrarDatePicker());
        etTareaHora.setOnClickListener(v -> mostrarTimePicker());
        btnGuardarTarea.setOnClickListener(v -> guardarTarea());

        btnSeleccionarClienteModal.setOnClickListener(v -> mostrarBottomSheetClientes());
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
            tvTotal.setText("0 clientes");
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No hay clientes registrados en el Directorio.");
            tvVacio.setPadding(0, 40, 0, 40);
            tvVacio.setTextSize(13);
            tvVacio.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            contenedor.addView(tvVacio);
        } else {
            tvTotal.setText(cursor.getCount() + (cursor.getCount() == 1 ? " cliente" : " clientes"));

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

                tvNombre.setText(nombre);
                tvFaenaItem.setText(faena);
                tvAvatar.setText(nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : "CL");

                if (lat == 0.0 && lng == 0.0) {
                    tvBadgeGps.setText("Sin GPS");
                    tvBadgeGps.setTextColor(Color.GRAY);
                } else {
                    tvBadgeGps.setText("● Con GPS");
                    tvBadgeGps.setTextColor(Color.parseColor("#2D6A4F"));
                }

                itemView.setOnClickListener(item -> {
                    clienteSeleccionadoNombre = nombre;
                    clienteLatitud = lat;
                    clienteLongitud = lng;

                    tvClienteSeleccionadoTexto.setText(nombre);
                    tvClienteSeleccionadoTexto.setTextColor(ContextCompat.getColor(this, R.color.text_main));

                    if (lat != 0.0 && lng != 0.0) {
                        tvUbicacionClienteInfo.setText(String.format(Locale.getDefault(),
                                "✅ Faena: %s · GPS (%.4f, %.4f)", faena, lat, lng));
                    } else {
                        tvUbicacionClienteInfo.setText("ℹ️ Faena: " + faena + " · Sin coordenadas asignadas");
                    }

                    dialog.dismiss();
                });

                contenedor.addView(itemView);
            }
            cursor.close();
        }

        dialog.show();
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            etTareaFecha.setText(fechaSeleccionada);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void mostrarTimePicker() {
        Calendar cal = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String horaSeleccionada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            etTareaHora.setText(horaSeleccionada);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void guardarTarea() {
        if (clienteSeleccionadoNombre.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona un cliente de la lista", Toast.LENGTH_SHORT).show();
            return;
        }

        String tipo = etTareaTipo.getText().toString().trim();
        String fecha = etTareaFecha.getText().toString().trim();
        String hora = etTareaHora.getText().toString().trim();

        if (tipo.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean exito = dbHelper.insertarTarea(clienteSeleccionadoNombre, tipo, hora, fecha);

        if (exito) {
            Toast.makeText(this, "Tarea registrada con éxito", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al guardar la tarea", Toast.LENGTH_SHORT).show();
        }
    }
}