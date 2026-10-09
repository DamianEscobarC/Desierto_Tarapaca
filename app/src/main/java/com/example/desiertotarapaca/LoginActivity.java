package com.example.desiertotarapaca;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmailLogin, etPasswordLogin;
    private CheckBox cbRecordarme;
    private AppCompatButton btnLogin;
    private TextView tvIrARegistro;
    private DbHelper dbHelper;
    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "SesionUsuario";
    private static final String KEY_RECORDAR = "recordar_sesion";
    private static final String KEY_CORREO = "correo_usuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        if (sharedPreferences.getBoolean(KEY_RECORDAR, false)) {
            irAlMain();
            return;
        }

        setContentView(R.layout.activity_login);

        dbHelper = new DbHelper(this);

        etEmailLogin = findViewById(R.id.etEmailLogin);
        etPasswordLogin = findViewById(R.id.etPasswordLogin);
        cbRecordarme = findViewById(R.id.cbRecordarme);
        btnLogin = findViewById(R.id.btnLogin);
        tvIrARegistro = findViewById(R.id.tvIrARegistro);

        tvIrARegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> autenticarUsuario());
    }

    private void autenticarUsuario() {
        String correo = etEmailLogin.getText().toString().trim();
        String password = etPasswordLogin.getText().toString().trim();

        if (correo.isEmpty()) {
            etEmailLogin.setError("Ingresa tu correo");
            etEmailLogin.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPasswordLogin.setError("Ingresa tu contraseña");
            etPasswordLogin.requestFocus();
            return;
        }

        boolean credencialesValidas = dbHelper.validarLogin(correo, password);

        if (credencialesValidas) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (cbRecordarme.isChecked()) {
                editor.putBoolean(KEY_RECORDAR, true);
                editor.putString(KEY_CORREO, correo);
            } else {
                editor.clear();
            }
            editor.apply();

            Toast.makeText(this, "Bienvenido a terreno", Toast.LENGTH_SHORT).show();
            irAlMain();
        } else {
            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }

    private void irAlMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}