package com.example.desiertotarapaca;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText etNombreRegister, etEmailRegister, etPasswordRegister;
    private Button btnRegister;
    private TextView tvVolverALogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNombreRegister = findViewById(R.id.etNombreRegister);
        etEmailRegister = findViewById(R.id.etEmailRegister);
        etPasswordRegister = findViewById(R.id.etPasswordRegister);
        btnRegister = findViewById(R.id.btnRegister);
        tvVolverALogin = findViewById(R.id.tvVolverALogin);

        btnRegister.setOnClickListener(v -> {
            String nombre = etNombreRegister.getText().toString().trim();
            String email = etEmailRegister.getText().toString().trim();
            String password = etPasswordRegister.getText().toString().trim();

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "Cuenta creada para: " + nombre, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        tvVolverALogin.setOnClickListener(v -> {
            finish();
        });
    }
}