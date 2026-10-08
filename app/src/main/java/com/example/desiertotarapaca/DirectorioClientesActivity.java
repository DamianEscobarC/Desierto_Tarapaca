package com.example.desiertotarapaca;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.util.ArrayList;
import java.util.List;

public class DirectorioClientesActivity extends AppCompatActivity {

    public static class Cliente {
        public String rut;
        public String nombre;
        public String direccion;
        public String telefono;

        public Cliente(String rut, String nombre, String direccion, String telefono) {
            this.rut = rut;
            this.nombre = nombre;
            this.direccion = direccion;
            this.telefono = telefono;
        }
    }

    private TextView btnVolverClientes;
    private AppCompatButton btnAbrirDialogoAgregar;
    private EditText etBuscadorCliente;
    private LinearLayout contenedorListaClientes;

    private List<Cliente> listaClientes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directorio_clientes);

        btnVolverClientes = findViewById(R.id.btnVolverClientes);
        btnAbrirDialogoAgregar = findViewById(R.id.btnAbrirDialogoAgregar);
        etBuscadorCliente = findViewById(R.id.etBuscadorCliente);
        contenedorListaClientes = findViewById(R.id.contenedorListaClientes);

        btnVolverClientes.setOnClickListener(v -> finish());
        btnAbrirDialogoAgregar.setOnClickListener(v -> mostrarDialogoAgregarCliente());

        // Clientes base COMO PRUEBAS PARA PROBAR EL CODEEEeeeEEEeee
        listaClientes.add(new Cliente("76.432.111-K", "Compañía Minera Doña Inés de Collahuasi", "Faena Cordillera · Campamento 3", "+56 9 8123 4567"));
        listaClientes.add(new Cliente("96.876.543-2", "Teck Quebrada Blanca (QB2)", "Puerto Patache · Área Concentradora", "+56 9 7654 3210"));
        listaClientes.add(new Cliente("77.990.222-1", "Constructora Alto Hospicio SpA", "Av. Las Parcelas s/n · Obra Habitacional", "+56 9 5551 2345"));

        renderizarClientes("");

        etBuscadorCliente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderizarClientes(s.toString().toLowerCase().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void renderizarClientes(String filtro) {
        contenedorListaClientes.removeAllViews();

        int colorMain = Color.parseColor("#1B2E24");
        int colorPrimary = Color.parseColor("#2D5A43");
        int colorMuted = Color.parseColor("#6B7F74");

        for (Cliente cliente : listaClientes) {
            boolean coincide = filtro.isEmpty() ||
                    cliente.rut.toLowerCase().contains(filtro) ||
                    cliente.nombre.toLowerCase().contains(filtro) ||
                    cliente.direccion.toLowerCase().contains(filtro);

            if (coincide) {
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

                TextView tvTel = new TextView(this);
                tvTel.setText("📞 " + cliente.telefono);
                tvTel.setTextSize(12f);
                tvTel.setTextColor(colorMuted);
                tvTel.setPadding(0, 2, 0, 0);
                layoutCard.addView(tvTel);

                layoutCard.setOnClickListener(v ->
                        Toast.makeText(this, "Cliente: " + cliente.nombre, Toast.LENGTH_SHORT).show()
                );

                contenedorListaClientes.addView(layoutCard);
            }
        }
    }

    private void mostrarDialogoAgregarCliente() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_agregar_cliente);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        EditText etRut = dialog.findViewById(R.id.etDialogRut);
        EditText etNombre = dialog.findViewById(R.id.etDialogNombre);
        EditText etDireccion = dialog.findViewById(R.id.etDialogDireccion);
        EditText etTelefono = dialog.findViewById(R.id.etDialogTelefono);
        Button btnCancelar = dialog.findViewById(R.id.btnDialogCancelar);
        AppCompatButton btnGuardar = dialog.findViewById(R.id.btnDialogGuardar);

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

            listaClientes.add(0, new Cliente(rut, nombre, direccion, telefono));
            renderizarClientes(etBuscadorCliente.getText().toString().trim().toLowerCase());

            Toast.makeText(this, "Cliente añadido con éxito", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}