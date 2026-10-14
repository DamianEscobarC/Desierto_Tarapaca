package com.example.desiertotarapaca;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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

    private TextView btnVolverInventario, tabQuimicos, tabBanos, tvSubtituloInstruccion;
    private EditText etBuscarQuimico;
    private AppCompatButton btnAgregarProducto;
    private LinearLayout contenedorListaQuimicos;
    private DbHelper dbHelper;

    private boolean pestañaQuimicosActiva = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_quimicos);

        dbHelper = new DbHelper(this);

        btnVolverInventario = findViewById(R.id.btnVolverInventario);
        tabQuimicos = findViewById(R.id.tabQuimicos);
        tabBanos = findViewById(R.id.tabBanos);
        tvSubtituloInstruccion = findViewById(R.id.tvSubtituloInstruccion);
        etBuscarQuimico = findViewById(R.id.etBuscarQuimico);
        btnAgregarProducto = findViewById(R.id.btnAgregarProducto);
        contenedorListaQuimicos = findViewById(R.id.contenedorListaQuimicos);

        btnVolverInventario.setOnClickListener(v -> finish());

        tabQuimicos.setOnClickListener(v -> activarPestañaQuimicos());
        tabBanos.setOnClickListener(v -> activarPestañaBanos());

        btnAgregarProducto.setOnClickListener(v -> {
            if (pestañaQuimicosActiva) {
                mostrarDialogoAgregarProducto();
            } else {
                registrarNuevoBanoAutomatico();
            }
        });

        etBuscarQuimico.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (pestañaQuimicosActiva) {
                    cargarListaProductos(s.toString());
                } else {
                    cargarListaBanosInventario(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        activarPestañaQuimicos();
    }

    private void activarPestañaQuimicos() {
        pestañaQuimicosActiva = true;
        tabQuimicos.setBackgroundResource(R.drawable.bg_green_tile);
        tabQuimicos.setTextColor(ContextCompat.getColor(this, R.color.text_light));

        tabBanos.setBackground(null);
        tabBanos.setTextColor(ContextCompat.getColor(this, R.color.text_muted));

        btnAgregarProducto.setText("+ AGREGAR PRODUCTO");
        etBuscarQuimico.setHint("Buscar producto químico...");
        tvSubtituloInstruccion.setText("Toca un producto para descontar su uso en terreno");

        cargarListaProductos(etBuscarQuimico.getText().toString());
    }

    private void activarPestañaBanos() {
        pestañaQuimicosActiva = false;
        tabBanos.setBackgroundResource(R.drawable.bg_green_tile);
        tabBanos.setTextColor(ContextCompat.getColor(this, R.color.text_light));

        tabQuimicos.setBackground(null);
        tabQuimicos.setTextColor(ContextCompat.getColor(this, R.color.text_muted));

        btnAgregarProducto.setText("+ REGISTRAR BAÑO");
        etBuscarQuimico.setHint("Buscar código de baño o estado...");
        tvSubtituloInstruccion.setText("Toca un baño para actualizar su estado de operación");

        cargarListaBanosInventario(etBuscarQuimico.getText().toString());
    }

    private void registrarNuevoBanoAutomatico() {
        String nuevoCodigo = dbHelper.obtenerSiguienteCodigoBano();
        long res = dbHelper.insertarBano(nuevoCodigo, "Químico", "Disponible");

        if (res > 0) {
            Toast.makeText(this, "Unidad " + nuevoCodigo + " registrada como Disponible", Toast.LENGTH_SHORT).show();
            cargarListaBanosInventario(etBuscarQuimico.getText().toString());
        } else {
            Toast.makeText(this, "Error al registrar la unidad en la base de datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarListaBanosInventario(String filtro) {
        contenedorListaQuimicos.removeAllViews();
        Cursor cursor;

        if (filtro == null || filtro.trim().isEmpty()) {
            cursor = dbHelper.obtenerTodosLosBanosInventario();
        } else {
            String pattern = "%" + filtro.trim() + "%";
            cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT * FROM " + DbHelper.TABLA_BANOS +
                            " WHERE " + DbHelper.COL_BANO_CODIGO + " LIKE ? OR " +
                            DbHelper.COL_BANO_ESTADO + " LIKE ? OR " +
                            DbHelper.COL_BANO_CLIENTE + " LIKE ? ORDER BY " + DbHelper.COL_BANO_CODIGO + " ASC",
                    new String[]{pattern, pattern, pattern}
            );
        }

        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) cursor.close();
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No hay baños químicos registrados en el inventario.");
            tvVacio.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            tvVacio.setTextSize(13);
            tvVacio.setPadding(0, 20, 0, 0);
            contenedorListaQuimicos.addView(tvVacio);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        int idxCod = cursor.getColumnIndex(DbHelper.COL_BANO_CODIGO);
        int idxCli = cursor.getColumnIndex(DbHelper.COL_BANO_CLIENTE);
        int idxTipo = cursor.getColumnIndex(DbHelper.COL_BANO_TIPO);
        int idxEst = cursor.getColumnIndex(DbHelper.COL_BANO_ESTADO);

        while (cursor.moveToNext()) {
            String codigo = (idxCod != -1) ? cursor.getString(idxCod) : "BP";
            String cliente = (idxCli != -1) ? cursor.getString(idxCli) : "En Bodega";
            String tipo = (idxTipo != -1) ? cursor.getString(idxTipo) : "Químico";
            String estado = (idxEst != -1) ? cursor.getString(idxEst) : "Disponible";

            View card = inflater.inflate(R.layout.item_bano_inventario, contenedorListaQuimicos, false);
            TextView tvCodigo = card.findViewById(R.id.tvCodigoBanoItem);
            TextView tvUbicacion = card.findViewById(R.id.tvUbicacionBanoItem);
            TextView tvBadge = card.findViewById(R.id.tvEstadoBanoBadge);

            tvCodigo.setText("Baño " + codigo);
            tvUbicacion.setText(cliente.equalsIgnoreCase("En Bodega") ? "📦 En Bodega · " + tipo : "📍 " + cliente + " · " + tipo);
            tvBadge.setText(estado);

            if (estado.equalsIgnoreCase("Disponible")) {
                tvBadge.setTextColor(Color.parseColor("#2D6A4F"));
                tvBadge.setBackgroundColor(Color.parseColor("#E8F5E9"));
            } else if (estado.equalsIgnoreCase("En Mantención")) {
                tvBadge.setTextColor(Color.parseColor("#D97706"));
                tvBadge.setBackgroundColor(Color.parseColor("#FEF3C7"));
            } else {
                tvBadge.setTextColor(Color.parseColor("#DC2626"));
                tvBadge.setBackgroundColor(Color.parseColor("#FEE2E2"));
            }

            card.setOnClickListener(v -> mostrarDialogoCambiarEstadoBano(codigo, estado));
            contenedorListaQuimicos.addView(card);
        }
        cursor.close();
    }

    private void mostrarDialogoCambiarEstadoBano(String codigoBano, String estadoActual) {
        String[] estados = new String[]{"Disponible", "No disponible", "En Mantención"};
        int preseleccion = 0;
        for (int i = 0; i < estados.length; i++) {
            if (estados[i].equalsIgnoreCase(estadoActual)) {
                preseleccion = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Actualizar Estado · Baño " + codigoBano)
                .setSingleChoiceItems(estados, preseleccion, (dialog, which) -> {
                    String nuevoEstado = estados[which];
                    dbHelper.actualizarEstadoBano(codigoBano, nuevoEstado);
                    Toast.makeText(this, "Baño " + codigoBano + " actualizado a " + nuevoEstado, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    cargarListaBanosInventario(etBuscarQuimico.getText().toString());
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_agregar_producto, null);

        EditText etNombre = dialogView.findViewById(R.id.etNuevoNombre);
        EditText etTipo = dialogView.findViewById(R.id.etNuevoTipo);
        EditText etStock = dialogView.findViewById(R.id.etNuevoStock);
        EditText etUnidad = dialogView.findViewById(R.id.etNuevaUnidad);
        TextView btnCancelar = dialogView.findViewById(R.id.btnCancelarNuevo);
        AppCompatButton btnGuardar = dialogView.findViewById(R.id.btnGuardarNuevo);

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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_consumo, null);

        TextView tvTitulo = dialogView.findViewById(R.id.tvTituloConsumo);
        TextView tvStock = dialogView.findViewById(R.id.tvStockActual);
        TextView tvUnidad = dialogView.findViewById(R.id.tvUnidadQuimico);
        TextView tvTipo = dialogView.findViewById(R.id.tvTipoQuimico);
        TextView tvLabelConsumo = dialogView.findViewById(R.id.tvLabelConsumo);
        EditText etCantidad = dialogView.findViewById(R.id.etCantidadDescontar);
        TextView btnCancelar = dialogView.findViewById(R.id.btnCancelarConsumo);
        AppCompatButton btnAceptar = dialogView.findViewById(R.id.btnAceptarConsumo);

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