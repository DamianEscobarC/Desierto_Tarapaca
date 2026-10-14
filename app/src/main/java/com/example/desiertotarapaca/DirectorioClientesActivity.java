package com.example.desiertotarapaca;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DirectorioClientesActivity extends AppCompatActivity {

    public static class Cliente {
        public long id;
        public String rut;
        public String nombre;
        public String direccion;
        public String telefono;
        public double latitud;
        public double longitud;

        public Cliente(long id, String rut, String nombre, String direccion, String telefono, double latitud, double longitud) {
            this.id = id;
            this.rut = rut;
            this.nombre = nombre;
            this.direccion = direccion;
            this.telefono = telefono;
            this.latitud = latitud;
            this.longitud = longitud;
        }
    }

    private TextView btnVolverClientes;
    private AppCompatButton btnAbrirDialogoAgregar;
    private EditText etBuscadorCliente;
    private LinearLayout contenedorListaClientes;
    private LinearLayout layoutVacioClientes;

    private DbHelper dbHelper;
    private List<Cliente> listaClientes = new ArrayList<>();

    private TextView tvCoordenadasDialogoActual;
    private final double[] coordsCapturadas = new double[]{0.0, 0.0};

    private final ActivityResultLauncher<Intent> selectorUbicacionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    double lat = result.getData().getDoubleExtra("LATITUD_ELEGIDA", 0.0);
                    double lng = result.getData().getDoubleExtra("LONGITUD_ELEGIDA", 0.0);

                    coordsCapturadas[0] = lat;
                    coordsCapturadas[1] = lng;

                    if (tvCoordenadasDialogoActual != null) {
                        tvCoordenadasDialogoActual.setText(String.format(Locale.getDefault(), "📍 GPS: %.4f, %.4f", lat, lng));
                        tvCoordenadasDialogoActual.setTextColor(Color.parseColor("#2D6A4F"));
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directorio_clientes);

        dbHelper = new DbHelper(this);

        btnVolverClientes = findViewById(R.id.btnVolverClientes);
        btnAbrirDialogoAgregar = findViewById(R.id.btnAbrirDialogoAgregar);
        etBuscadorCliente = findViewById(R.id.etBuscadorCliente);
        contenedorListaClientes = findViewById(R.id.contenedorListaClientes);
        layoutVacioClientes = findViewById(R.id.layoutVacioClientes);

        btnVolverClientes.setOnClickListener(v -> finish());
        btnAbrirDialogoAgregar.setOnClickListener(v -> mostrarDialogoAgregarCliente());

        etBuscadorCliente.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderizarClientes(s.toString().toLowerCase().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarClientesDesdeBD();
        String filtro = etBuscadorCliente.getText() != null ?
                etBuscadorCliente.getText().toString().toLowerCase().trim() : "";
        renderizarClientes(filtro);
    }

    private void cargarClientesDesdeBD() {
        listaClientes.clear();
        Cursor cursor = dbHelper.obtenerClientes(null);

        if (cursor != null && cursor.moveToFirst()) {
            int idxId = cursor.getColumnIndex(DbHelper.COL_CLI_ID);
            int idxNombre = cursor.getColumnIndex(DbHelper.COL_CLI_NOMBRE);
            int idxRut = cursor.getColumnIndex(DbHelper.COL_CLI_RUT);
            int idxFaena = cursor.getColumnIndex(DbHelper.COL_CLI_FAENA);
            int idxTel = cursor.getColumnIndex(DbHelper.COL_CLI_TELEFONO);
            int idxLat = cursor.getColumnIndex(DbHelper.COL_CLI_LATITUD);
            int idxLng = cursor.getColumnIndex(DbHelper.COL_CLI_LONGITUD);

            do {
                long id = (idxId != -1) ? cursor.getLong(idxId) : 0;
                String nombre = (idxNombre != -1) ? cursor.getString(idxNombre) : "";
                String rut = (idxRut != -1) ? cursor.getString(idxRut) : "";
                String faena = (idxFaena != -1) ? cursor.getString(idxFaena) : "";
                String telefono = (idxTel != -1) ? cursor.getString(idxTel) : "";
                double lat = (idxLat != -1) ? cursor.getDouble(idxLat) : 0.0;
                double lng = (idxLng != -1) ? cursor.getDouble(idxLng) : 0.0;

                listaClientes.add(new Cliente(id, rut, nombre, faena, telefono, lat, lng));
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    private void renderizarClientes(String filtro) {
        contenedorListaClientes.removeAllViews();

        int colorMain = Color.parseColor("#1B2E24");
        int colorPrimary = Color.parseColor("#2D5A43");
        int colorMuted = Color.parseColor("#6B7F74");
        int visibles = 0;

        for (Cliente cliente : listaClientes) {
            boolean coincide = filtro.isEmpty() ||
                    cliente.rut.toLowerCase().contains(filtro) ||
                    cliente.nombre.toLowerCase().contains(filtro) ||
                    cliente.direccion.toLowerCase().contains(filtro);

            if (coincide) {
                visibles++;

                LinearLayout layoutCard = new LinearLayout(this);
                layoutCard.setOrientation(LinearLayout.VERTICAL);
                layoutCard.setBackgroundResource(R.drawable.bg_white_card);
                layoutCard.setElevation(4f);
                layoutCard.setPadding(36, 32, 36, 32);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 24);
                layoutCard.setLayoutParams(params);

                TextView tvNombre = new TextView(this);
                tvNombre.setText(cliente.nombre);
                tvNombre.setTextSize(15f);
                tvNombre.setTypeface(null, Typeface.BOLD);
                tvNombre.setTextColor(colorMain);
                layoutCard.addView(tvNombre);

                TextView tvRut = new TextView(this);
                tvRut.setText("RUT: " + cliente.rut);
                tvRut.setTextSize(12f);
                tvRut.setTextColor(colorPrimary);
                tvRut.setPadding(0, 4, 0, 0);
                layoutCard.addView(tvRut);

                TextView tvDir = new TextView(this);
                tvDir.setText("📍 " + cliente.direccion);
                tvDir.setTextSize(12f);
                tvDir.setTextColor(colorMuted);
                tvDir.setPadding(0, 4, 0, 0);
                layoutCard.addView(tvDir);

                if (cliente.latitud != 0.0 || cliente.longitud != 0.0) {
                    TextView tvGps = new TextView(this);
                    tvGps.setText(String.format(Locale.getDefault(), "🗺️ GPS: %.4f, %.4f", cliente.latitud, cliente.longitud));
                    tvGps.setTextSize(11f);
                    tvGps.setTextColor(Color.parseColor("#2D6A4F"));
                    tvGps.setPadding(0, 2, 0, 0);
                    layoutCard.addView(tvGps);
                }

                TextView tvTel = new TextView(this);
                tvTel.setText("📞 " + cliente.telefono);
                tvTel.setTextSize(12f);
                tvTel.setTextColor(colorMuted);
                tvTel.setPadding(0, 2, 0, 0);
                layoutCard.addView(tvTel);

                layoutCard.setOnClickListener(v ->
                        Toast.makeText(this, cliente.nombre + "\nMantén presionado para eliminar", Toast.LENGTH_SHORT).show()
                );

                layoutCard.setOnLongClickListener(v -> {
                    confirmarEliminacionCliente(cliente);
                    return true;
                });

                contenedorListaClientes.addView(layoutCard);
            }
        }

        if (layoutVacioClientes != null) {
            layoutVacioClientes.setVisibility(visibles == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void confirmarEliminacionCliente(Cliente cliente) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Cliente")
                .setMessage("¿Deseas eliminar a \"" + cliente.nombre + "\" del directorio?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    boolean ok = dbHelper.eliminarCliente(cliente.id);
                    if (ok) {
                        Toast.makeText(this, "Cliente eliminado", Toast.LENGTH_SHORT).show();
                        cargarClientesDesdeBD();
                        renderizarClientes(etBuscadorCliente.getText().toString().trim().toLowerCase());
                    } else {
                        Toast.makeText(this, "Error al eliminar cliente", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoAgregarCliente() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_agregar_cliente);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int ancho = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(ancho, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etRut = dialog.findViewById(R.id.etDialogRut);
        EditText etNombre = dialog.findViewById(R.id.etDialogNombre);
        EditText etDireccion = dialog.findViewById(R.id.etDialogDireccion);
        EditText etTelefono = dialog.findViewById(R.id.etDialogTelefono);
        View btnElegirMapa = dialog.findViewById(R.id.btnElegirEnMapaDialogo);
        TextView tvCoords = dialog.findViewById(R.id.tvCoordenadasSeleccionadas);
        Button btnCancelar = dialog.findViewById(R.id.btnDialogCancelar);
        AppCompatButton btnGuardar = dialog.findViewById(R.id.btnDialogGuardar);

        coordsCapturadas[0] = 0.0;
        coordsCapturadas[1] = 0.0;
        tvCoordenadasDialogoActual = tvCoords;

        if (btnElegirMapa != null) {
            btnElegirMapa.setOnClickListener(v -> {
                Intent intent = new Intent(DirectorioClientesActivity.this, SeleccionarUbicacionActivity.class);
                selectorUbicacionLauncher.launch(intent);
            });
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String rut = etRut.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            String direccion = etDireccion.getText().toString().trim();
            String telefono = etTelefono.getText().toString().trim();

            if (rut.isEmpty()) {
                etRut.setError("Ingresa el RUT");
                etRut.requestFocus();
                return;
            }
            if (nombre.isEmpty()) {
                etNombre.setError("Ingresa el nombre");
                etNombre.requestFocus();
                return;
            }
            if (direccion.isEmpty()) {
                etDireccion.setError("Ingresa la dirección o faena");
                etDireccion.requestFocus();
                return;
            }
            if (telefono.isEmpty()) {
                etTelefono.setError("Ingresa el teléfono");
                etTelefono.requestFocus();
                return;
            }

            boolean ok = dbHelper.insertarCliente(
                    nombre,
                    rut,
                    direccion,
                    telefono,
                    coordsCapturadas[0],
                    coordsCapturadas[1]
            );

            if (ok) {
                Toast.makeText(this, "Cliente guardado con éxito", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                cargarClientesDesdeBD();
                renderizarClientes(etBuscadorCliente.getText().toString().trim().toLowerCase());
            } else {
                Toast.makeText(this, "Error al guardar cliente en la base de datos", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}