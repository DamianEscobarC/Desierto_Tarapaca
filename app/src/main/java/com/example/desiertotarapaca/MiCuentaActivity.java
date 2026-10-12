package com.example.desiertotarapaca;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class MiCuentaActivity extends AppCompatActivity {

    private TextView btnVolverCuenta;
    private TextView tvAvatarCuenta, tvNombreCuenta, tvCargoSubtitulo;
    private EditText etCuentaEmail, etCuentaCargo, etCuentaTelefono, etCuentaDireccion;
    private AppCompatButton btnGuardarCuenta;

    private DbHelper dbHelper;
    private String correoUsuarioActivo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_cuenta);

        dbHelper = new DbHelper(this);

        btnVolverCuenta = findViewById(R.id.btnVolverCuenta);
        tvAvatarCuenta = findViewById(R.id.tvAvatarCuenta);
        tvNombreCuenta = findViewById(R.id.tvNombreCuenta);
        tvCargoSubtitulo = findViewById(R.id.tvCargoSubtitulo);

        etCuentaEmail = findViewById(R.id.etCuentaEmail);
        etCuentaCargo = findViewById(R.id.etCuentaCargo);
        etCuentaTelefono = findViewById(R.id.etCuentaTelefono);
        etCuentaDireccion = findViewById(R.id.etCuentaDireccion);
        btnGuardarCuenta = findViewById(R.id.btnGuardarCuenta);

        btnVolverCuenta.setOnClickListener(v -> finish());

        SharedPreferences pref = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
        correoUsuarioActivo = pref.getString("correo_usuario", "");

        if (!correoUsuarioActivo.isEmpty()) {
            cargarPerfil();
        }

        btnGuardarCuenta.setOnClickListener(v -> guardarPerfil());
    }

    private void cargarPerfil() {
        Cursor cursor = dbHelper.obtenerDatosUsuario(correoUsuarioActivo);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idxNombre = cursor.getColumnIndex("nombre");
                int idxCargo = cursor.getColumnIndex("cargo");
                int idxTelefono = cursor.getColumnIndex("telefono");
                int idxDireccion = cursor.getColumnIndex("direccion");

                String nombre = (idxNombre != -1 && !cursor.isNull(idxNombre)) ? cursor.getString(idxNombre) : "Usuario";
                String cargo = (idxCargo != -1 && !cursor.isNull(idxCargo)) ? cursor.getString(idxCargo) : "";
                String telefono = (idxTelefono != -1 && !cursor.isNull(idxTelefono)) ? cursor.getString(idxTelefono) : "";
                String direccion = (idxDireccion != -1 && !cursor.isNull(idxDireccion)) ? cursor.getString(idxDireccion) : "";

                tvNombreCuenta.setText(nombre);
                tvAvatarCuenta.setText(generarIniciales(nombre));

                if (cargo.isEmpty()) {
                    tvCargoSubtitulo.setText("Sin profesión definida");
                } else {
                    tvCargoSubtitulo.setText(cargo);
                }

                etCuentaEmail.setText(correoUsuarioActivo);
                etCuentaCargo.setText(cargo);
                etCuentaTelefono.setText(telefono);
                etCuentaDireccion.setText(direccion);
            }
            cursor.close();
        }
    }

    private void guardarPerfil() {
        String cargo = etCuentaCargo.getText().toString().trim();
        String telefono = etCuentaTelefono.getText().toString().trim();
        String direccion = etCuentaDireccion.getText().toString().trim();

        boolean exito = dbHelper.actualizarPerfilUsuario(correoUsuarioActivo, cargo, telefono, direccion);

        if (exito) {
            if (cargo.isEmpty()) {
                tvCargoSubtitulo.setText("Sin profesión definida");
            } else {
                tvCargoSubtitulo.setText(cargo);
            }
            Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al guardar cambios", Toast.LENGTH_SHORT).show();
        }
    }

    private String generarIniciales(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return "US";
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length >= 2) {
            return ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
        }
        return ("" + partes[0].charAt(0)).toUpperCase();
    }
}