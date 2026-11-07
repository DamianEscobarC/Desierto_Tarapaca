package com.example.desiertotarapaca;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombreRegister, etEmailRegister, etPasswordRegister;
    private AppCompatButton btnRegister;
    private TextView tvVolverALogin;
    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DbHelper(this);

        etNombreRegister = findViewById(R.id.etNombreRegister);
        etEmailRegister = findViewById(R.id.etEmailRegister);
        etPasswordRegister = findViewById(R.id.etPasswordRegister);
        btnRegister = findViewById(R.id.btnRegister);
        tvVolverALogin = findViewById(R.id.tvVolverALogin);

        tvVolverALogin.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre = etNombreRegister.getText().toString().trim();
        String correo = etEmailRegister.getText().toString().trim();
        String password = etPasswordRegister.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombreRegister.setError("Ingresa tu nombre y apellido");
            etNombreRegister.requestFocus();
            return;
        }

        if (correo.isEmpty()) {
            etEmailRegister.setError("Ingresa un correo electrónico");
            etEmailRegister.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etEmailRegister.setError("Ingresa un correo electrónico válido");
            etEmailRegister.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPasswordRegister.setError("La contraseña debe tener al menos 6 caracteres");
            etPasswordRegister.requestFocus();
            return;
        }

        if (dbHelper.existeCorreo(correo)) {
            etEmailRegister.setError("Este correo ya se encuentra registrado");
            etEmailRegister.requestFocus();
            Toast.makeText(this, "El correo ya está en uso", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean registrado = dbHelper.registrarUsuario(nombre, correo, password);

        if (registrado) {
            Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al crear la cuenta. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
        }
    }
}