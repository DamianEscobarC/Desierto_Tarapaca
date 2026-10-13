package com.example.desiertotarapaca;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegistrarServicioActivity extends AppCompatActivity {

    private TextView btnVolverRegistro;
    private Spinner spnClienteServicio;
    private EditText etTipoTrabajo, etUnidadesAtendidas, etObservaciones;
    private TextView tvCoordenadasGps;
    private AppCompatButton btnCapturarGps, btnGuardarServicio;

    private DbHelper dbHelper;
    private long idServicioAEditar = -1;
    private String numeroOtActual = "";
    private String clienteASeleccionarEnEdicion = "";
    private List<String> listaNombresClientes = new ArrayList<>();
    private ArrayAdapter<String> adapterClientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_servicio);

        dbHelper = new DbHelper(this);

        btnVolverRegistro = findViewById(R.id.btnVolverRegistro);
        spnClienteServicio = findViewById(R.id.spnClienteServicio);
        etTipoTrabajo = findViewById(R.id.etTipoTrabajo);
        etUnidadesAtendidas = findViewById(R.id.etUnidadesAtendidas);
        tvCoordenadasGps = findViewById(R.id.tvCoordenadasGps);
        btnCapturarGps = findViewById(R.id.btnCapturarGps);
        etObservaciones = findViewById(R.id.etObservaciones);
        btnGuardarServicio = findViewById(R.id.btnGuardarServicio);

        btnVolverRegistro.setOnClickListener(v -> finish());

        btnCapturarGps.setOnClickListener(v -> {
            tvCoordenadasGps.setText("-20.9482, -68.7423");
            Toast.makeText(this, "Coordenadas fijadas en faena", Toast.LENGTH_SHORT).show();
        });

        configurarSpinnerClientes();

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

    @Override
    protected void onResume() {
        super.onResume();
        recargarClientesEnSpinner();
    }

    private void configurarSpinnerClientes() {
        listaNombresClientes = new ArrayList<>();
        listaNombresClientes.add("Selecciona un cliente...");

        List<String> clientesBD = dbHelper.obtenerNombresClientes();
        if (clientesBD != null && !clientesBD.isEmpty()) {
            listaNombresClientes.addAll(clientesBD);
        }

        adapterClientes = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                listaNombresClientes
        );
        spnClienteServicio.setAdapter(adapterClientes);
    }

    private void recargarClientesEnSpinner() {
        String seleccionPrevia = (spnClienteServicio.getSelectedItem() != null)
                ? spnClienteServicio.getSelectedItem().toString()
                : "";

        listaNombresClientes.clear();
        listaNombresClientes.add("Selecciona un cliente...");

        List<String> clientesBD = dbHelper.obtenerNombresClientes();
        if (clientesBD != null && !clientesBD.isEmpty()) {
            listaNombresClientes.addAll(clientesBD);
        }

        adapterClientes.notifyDataSetChanged();

        String buscar = !clienteASeleccionarEnEdicion.isEmpty() ? clienteASeleccionarEnEdicion : seleccionPrevia;
        if (!buscar.isEmpty()) {
            for (int i = 0; i < listaNombresClientes.size(); i++) {
                if (listaNombresClientes.get(i).equalsIgnoreCase(buscar)) {
                    spnClienteServicio.setSelection(i);
                    break;
                }
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
                clienteASeleccionarEnEdicion = cursor.getString(idxCli);
                for (int i = 0; i < listaNombresClientes.size(); i++) {
                    if (listaNombresClientes.get(i).equalsIgnoreCase(clienteASeleccionarEnEdicion)) {
                        spnClienteServicio.setSelection(i);
                        break;
                    }
                }
            }
            if (idxTipo != -1) etTipoTrabajo.setText(cursor.getString(idxTipo));
            if (idxDesc != -1) etObservaciones.setText(cursor.getString(idxDesc));

            cursor.close();
        }
    }

    private void guardarOActualizar() {
        int posicionSeleccionada = spnClienteServicio.getSelectedItemPosition();

        if (posicionSeleccionada <= 0 || listaNombresClientes.size() <= 1) {
            Toast.makeText(this, "Debes seleccionar un cliente registrado del Directorio", Toast.LENGTH_LONG).show();
            return;
        }

        String cliente = listaNombresClientes.get(posicionSeleccionada);
        String tipo = etTipoTrabajo.getText().toString().trim();
        String unidades = etUnidadesAtendidas.getText().toString().trim();
        String observaciones = etObservaciones.getText().toString().trim();
        String gps = tvCoordenadasGps.getText().toString().trim();

        if (tipo.isEmpty()) {
            etTipoTrabajo.setError("Ingresa el tipo de trabajo");
            etTipoTrabajo.requestFocus();
            return;
        }

        StringBuilder descripcionCompleta = new StringBuilder();
        if (!unidades.isEmpty()) {
            descripcionCompleta.append("Unidades: ").append(unidades).append(". ");
        }
        if (!gps.equals("GPS no capturado")) {
            descripcionCompleta.append("GPS: ").append(gps).append(". ");
        }
        if (!observaciones.isEmpty()) {
            descripcionCompleta.append(observaciones);
        }

        if (idServicioAEditar == -1) {
            String otGenerada = "OT-" + (System.currentTimeMillis() % 10000);
            String fechaActual = new SimpleDateFormat("dd MMM yyyy · HH:mm", new Locale("es", "CL")).format(new Date());

            long resultado = dbHelper.insertarServicio(
                    otGenerada,
                    cliente,
                    tipo,
                    descripcionCompleta.toString().trim(),
                    fechaActual,
                    true
            );

            if (resultado > 0) {
                Toast.makeText(this, "Servicio registrado localmente en SQLite", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error al guardar en el dispositivo", Toast.LENGTH_SHORT).show();
            }
        } else {
            int filasAfectadas = dbHelper.actualizarServicio(
                    idServicioAEditar,
                    numeroOtActual.isEmpty() ? "OT-Mod" : numeroOtActual,
                    cliente,
                    tipo,
                    descripcionCompleta.toString().trim(),
                    true
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