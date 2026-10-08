package com.example.desiertotarapaca;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class MiCuentaActivity extends AppCompatActivity {

    private TextView btnVolverCuenta;
    private EditText etCuentaEmail, etCuentaTelefono, etCuentaDireccion;
    private AppCompatButton btnGuardarCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_cuenta);

        btnVolverCuenta = findViewById(R.id.btnVolverCuenta);
        etCuentaEmail = findViewById(R.id.etCuentaEmail);
        etCuentaTelefono = findViewById(R.id.etCuentaTelefono);
        etCuentaDireccion = findViewById(R.id.etCuentaDireccion);
        btnGuardarCuenta = findViewById(R.id.btnGuardarCuenta);

        btnVolverCuenta.setOnClickListener(v -> finish());

        btnGuardarCuenta.setOnClickListener(v -> {
            String correo = etCuentaEmail.getText().toString().trim();
            String telefono = etCuentaTelefono.getText().toString().trim();
            String direccion = etCuentaDireccion.getText().toString().trim();

            if (correo.isEmpty() || !correo.contains("@")) {
                etCuentaEmail.setError("Ingresa un correo electrónico válido");
                etCuentaEmail.requestFocus();
                return;
            }

            if (telefono.isEmpty()) {
                etCuentaTelefono.setError("Ingresa tu número de teléfono");
                etCuentaTelefono.requestFocus();
                return;
            }

            if (direccion.isEmpty()) {
                etCuentaDireccion.setError("Ingresa tu dirección");
                etCuentaDireccion.requestFocus();
                return;
            }

            Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}