package com.example.desiertotarapaca;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.util.Calendar;
import java.util.Locale;

public class RegistrarTareaActivity extends AppCompatActivity {

    private TextView btnVolverTarea;
    private EditText etTareaCliente, etTareaTipo, etTareaFecha, etTareaHora;
    private AppCompatButton btnGuardarTarea;
    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_tarea);

        dbHelper = new DbHelper(this);

        btnVolverTarea = findViewById(R.id.btnVolverTarea);
        etTareaCliente = findViewById(R.id.etTareaCliente);
        etTareaTipo = findViewById(R.id.etTareaTipo);
        etTareaFecha = findViewById(R.id.etTareaFecha);
        etTareaHora = findViewById(R.id.etTareaHora);
        btnGuardarTarea = findViewById(R.id.btnGuardarTarea);

        btnVolverTarea.setOnClickListener(v -> finish());

        etTareaFecha.setOnClickListener(v -> mostrarDatePicker());

        etTareaHora.setOnClickListener(v -> mostrarTimePicker());

        btnGuardarTarea.setOnClickListener(v -> guardarTarea());
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
        String cliente = etTareaCliente.getText().toString().trim();
        String tipo = etTareaTipo.getText().toString().trim();
        String fecha = etTareaFecha.getText().toString().trim();
        String hora = etTareaHora.getText().toString().trim();

        if (cliente.isEmpty() || tipo.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean exito = dbHelper.insertarTarea(cliente, tipo, hora, fecha);

        if (exito) {
            Toast.makeText(this, "Tarea registrada con éxito", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al guardar la tarea", Toast.LENGTH_SHORT).show();
        }
    }
}