package com.example.desiertotarapaca;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

public class InventarioQuimicosActivity extends AppCompatActivity {

    private TextView btnVolverInventario;
    private EditText etBuscarQuimico;
    private AppCompatButton btnAgregarProducto;
    private LinearLayout contenedorListaQuimicos;
    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_quimicos);

        dbHelper = new DbHelper(this);

        btnVolverInventario = findViewById(R.id.btnVolverInventario);
        etBuscarQuimico = findViewById(R.id.etBuscarQuimico);
        btnAgregarProducto = findViewById(R.id.btnAgregarProducto);
        contenedorListaQuimicos = findViewById(R.id.contenedorListaQuimicos);

        btnVolverInventario.setOnClickListener(v -> finish());
        btnAgregarProducto.setOnClickListener(v -> mostrarDialogoAgregarProducto());

        etBuscarQuimico.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarListaProductos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        cargarListaProductos("");
    }

    private void cargarListaProductos(String filtro) {
        contenedorListaQuimicos.removeAllViews();
        Cursor cursor = dbHelper.obtenerInventarioQuimicos(filtro);

        if (cursor != null) {
            if (cursor.getCount() == 0) {
                TextView tvVacio = new TextView(this);
                tvVacio.setText("No hay productos químicos en inventario.");
                tvVacio.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                tvVacio.setTextSize(13);
                tvVacio.setPadding(0, 20, 0, 0);
                contenedorListaQuimicos.addView(tvVacio);
            } else {
                while (cursor.moveToNext()) {
                    int idxId = cursor.getColumnIndex(DbHelper.COL_QUIM_ID);
                    int idxNombre = cursor.getColumnIndex(DbHelper.COL_QUIM_NOMBRE);
                    int idxTipo = cursor.getColumnIndex(DbHelper.COL_QUIM_TIPO);
                    int idxStock = cursor.getColumnIndex(DbHelper.COL_QUIM_STOCK);
                    int idxUnidad = cursor.getColumnIndex(DbHelper.COL_QUIM_UNIDAD);

                    long id = idxId != -1 ? cursor.getLong(idxId) : -1;
                    String nombre = idxNombre != -1 ? cursor.getString(idxNombre) : "";
                    String tipo = idxTipo != -1 ? cursor.getString(idxTipo) : "";
                    double stock = idxStock != -1 ? cursor.getDouble(idxStock) : 0.0;
                    String unidad = idxUnidad != -1 ? cursor.getString(idxUnidad) : "";

                    LinearLayout card = new LinearLayout(this);
                    card.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, 12);
                    card.setLayoutParams(params);
                    card.setBackgroundResource(R.drawable.bg_white_card);
                    card.setPadding(18, 16, 18, 16);
                    card.setElevation(2f);
                    card.setClickable(true);
                    card.setFocusable(true);

                    card.setOnClickListener(v -> mostrarDialogoConsumo(id, nombre, stock, tipo, unidad));

                    LinearLayout infoLayout = new LinearLayout(this);
                    infoLayout.setOrientation(LinearLayout.VERTICAL);
                    infoLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                    TextView tvNombre = new TextView(this);
                    tvNombre.setText(nombre);
                    tvNombre.setTextColor(ContextCompat.getColor(this, R.color.text_main));
                    tvNombre.setTextSize(14);
                    tvNombre.setTypeface(null, Typeface.BOLD);

                    TextView tvTipo = new TextView(this);
                    tvTipo.setText(tipo);
                    tvTipo.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                    tvTipo.setTextSize(12);

                    infoLayout.addView(tvNombre);
                    infoLayout.addView(tvTipo);

                    LinearLayout stockLayout = new LinearLayout(this);
                    stockLayout.setOrientation(LinearLayout.VERTICAL);
                    stockLayout.setGravity(android.view.Gravity.END);
                    stockLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));

                    String stockFormateado = (stock % 1 == 0)
                            ? String.valueOf((long) stock)
                            : String.valueOf(stock);

                    TextView tvStock = new TextView(this);
                    tvStock.setText(stockFormateado);
                    tvStock.setTextColor(ContextCompat.getColor(this, R.color.text_main));
                    tvStock.setTextSize(14);
                    tvStock.setTypeface(null, Typeface.BOLD);
                    tvStock.setGravity(android.view.Gravity.END);

                    TextView tvUnidad = new TextView(this);
                    tvUnidad.setText(unidad);
                    tvUnidad.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                    tvUnidad.setTextSize(11);
                    tvUnidad.setGravity(android.view.Gravity.END);

                    stockLayout.addView(tvStock);
                    if (!unidad.isEmpty()) {
                        stockLayout.addView(tvUnidad);
                    }

                    card.addView(infoLayout);
                    card.addView(stockLayout);

                    contenedorListaQuimicos.addView(card);
                }
            }
            cursor.close();
        }
    }

    private void mostrarDialogoAgregarProducto() {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_agregar_producto, null);

        EditText etNombre = dialogView.findViewById(R.id.etNuevoNombre);
        EditText etTipo = dialogView.findViewById(R.id.etNuevoTipo);
        EditText etStock = dialogView.findViewById(R.id.etNuevoStock);
        EditText etUnidad = dialogView.findViewById(R.id.etNuevaUnidad);
        TextView btnCancelar = dialogView.findViewById(R.id.btnCancelarNuevo);
        androidx.appcompat.widget.AppCompatButton btnGuardar = dialogView.findViewById(R.id.btnGuardarNuevo);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String tipo = etTipo.getText().toString().trim();
            String stockStr = etStock.getText().toString().trim();
            String unidad = etUnidad.getText().toString().trim();

            if (nombre.isEmpty() || stockStr.isEmpty() || unidad.isEmpty()) {
                Toast.makeText(this, "Completa el nombre, cantidad y unidad", Toast.LENGTH_SHORT).show();
                return;
            }

            double stock;
            try {
                stock = Double.parseDouble(stockStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Cantidad numérica inválida", Toast.LENGTH_SHORT).show();
                return;
            }

            if (stock <= 0) {
                Toast.makeText(this, "La cantidad inicial debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean exito = dbHelper.insertarProductoQuimico(nombre, tipo, stock, unidad);
            if (exito) {
                Toast.makeText(this, "Producto registrado con éxito", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                cargarListaProductos(etBuscarQuimico.getText().toString());
            } else {
                Toast.makeText(this, "Error al guardar el producto", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void mostrarDialogoConsumo(long id, String nombre, double stockActual, String tipo, String unidad) {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_consumo, null);

        TextView tvTitulo = dialogView.findViewById(R.id.tvTituloConsumo);
        TextView tvStock = dialogView.findViewById(R.id.tvStockActual);
        TextView tvUnidad = dialogView.findViewById(R.id.tvUnidadQuimico);
        TextView tvTipo = dialogView.findViewById(R.id.tvTipoQuimico);
        TextView tvLabelConsumo = dialogView.findViewById(R.id.tvLabelConsumo);
        EditText etCantidad = dialogView.findViewById(R.id.etCantidadDescontar);
        TextView btnCancelar = dialogView.findViewById(R.id.btnCancelarConsumo);
        androidx.appcompat.widget.AppCompatButton btnAceptar = dialogView.findViewById(R.id.btnAceptarConsumo);

        String stockFormateado = (stockActual % 1 == 0)
                ? String.valueOf((long) stockActual)
                : String.valueOf(stockActual);

        tvTitulo.setText("Consumo: " + nombre);
        tvStock.setText(stockFormateado);
        tvUnidad.setText(unidad.isEmpty() ? "No especificada" : unidad);
        tvTipo.setText(tipo.isEmpty() ? "No especificado" : tipo);

        tvLabelConsumo.setText("CANTIDAD A DESCONTAR");
        etCantidad.setHint("Ej. 1");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAceptar.setOnClickListener(v -> {
            String cantStr = etCantidad.getText().toString().trim();
            if (cantStr.isEmpty()) {
                Toast.makeText(this, "Ingresa una cantidad válida", Toast.LENGTH_SHORT).show();
                return;
            }

            double cantidadUsada;
            try {
                cantidadUsada = Double.parseDouble(cantStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Cantidad no válida", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cantidadUsada <= 0) {
                Toast.makeText(this, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean exito = dbHelper.consumirStockQuimico(id, cantidadUsada);
            if (exito) {
                if (cantidadUsada >= stockActual) {
                    Toast.makeText(this, "Producto " + nombre + " agotado y retirado", Toast.LENGTH_SHORT).show();
                } else {
                    double restante = stockActual - cantidadUsada;
                    String restanteStr = (restante % 1 == 0) ? String.valueOf((long) restante) : String.valueOf(restante);
                    Toast.makeText(this, "Stock restante: " + restanteStr + (unidad.isEmpty() ? "" : " " + unidad), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                cargarListaProductos(etBuscarQuimico.getText().toString());
            } else {
                Toast.makeText(this, "Error al procesar el descuento", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}