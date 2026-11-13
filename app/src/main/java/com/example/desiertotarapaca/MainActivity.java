package com.example.desiertotarapaca;

import android.net.Uri;
import android.app.AlertDialog;
import androidx.core.content.FileProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.widget.CheckBox;
import android.widget.ScrollView;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DbHelper dbHelper;

    private TextView tvFechaHeader;
    private TextView tvMesAnio, tvRangoSemana, btnSemanaAnterior, btnSemanaSiguiente;
    private TextView[] tvDias = new TextView[7];
    private String[] fechasDiasSemana = new String[7];
    private String[] titulosDiasSemana = new String[7];
    private int indiceDiaSeleccionado = -1;
    private int posHoyEnSemana = -1;
    private Calendar calendarioActual;

    private TextView tvTituloHoyTareas, tvCantidadTareasPendientes;
    private LinearLayout contenedorListaTareas;
    private ScrollView scrollListaTareas;

    private LinearLayout cardRegistrarServicio, cardConsultarRegistros, cardInventario;
    private LinearLayout cardDirectorioClientes, cardMapaGps;
    private Button btnFinalizarDia;

    private DrawerLayout drawerLayout;
    private TextView tvNombreUsuarioDrawer, tvAvatarDrawer, tvCargoUsuarioDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DbHelper(this);

        inicializarVistas();
        configurarAccesosRapidos();
        configurarMenuHamburguesa();

        actualizarFechaHeader();
        calendarioActual = Calendar.getInstance();
        actualizarCalendarioSemanal();

        for (int i = 0; i < 7; i++) {
            final int pos = i;
            if (tvDias[pos] != null) {
                tvDias[pos].setOnClickListener(v -> seleccionarDia(pos));
            }
        }

        btnSemanaAnterior.setOnClickListener(v -> {
            calendarioActual.add(Calendar.WEEK_OF_YEAR, -1);
            actualizarCalendarioSemanal();
        });

        btnSemanaSiguiente.setOnClickListener(v -> {
            calendarioActual.add(Calendar.WEEK_OF_YEAR, 1);
            actualizarCalendarioSemanal();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarFechaHeader();
        cargarDatosUsuarioDrawer();

        if (indiceDiaSeleccionado != -1 && fechasDiasSemana[indiceDiaSeleccionado] != null) {
            cargarTareasPorFecha(fechasDiasSemana[indiceDiaSeleccionado], titulosDiasSemana[indiceDiaSeleccionado]);
        } else {
            cargarTareasPendientesHoy();
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private void inicializarVistas() {
        tvFechaHeader = findViewById(R.id.tvFechaHeader);

        tvMesAnio = findViewById(R.id.tvMesAnio);
        tvRangoSemana = findViewById(R.id.tvRangoSemana);
        btnSemanaAnterior = findViewById(R.id.btnSemanaAnterior);
        btnSemanaSiguiente = findViewById(R.id.btnSemanaSiguiente);

        tvDias[0] = findViewById(R.id.tvDia1);
        tvDias[1] = findViewById(R.id.tvDia2);
        tvDias[2] = findViewById(R.id.tvDia3);
        tvDias[3] = findViewById(R.id.tvDia4);
        tvDias[4] = findViewById(R.id.tvDia5);
        tvDias[5] = findViewById(R.id.tvDia6);
        tvDias[6] = findViewById(R.id.tvDia7);

        tvTituloHoyTareas = findViewById(R.id.tvTituloHoyTareas);
        tvCantidadTareasPendientes = findViewById(R.id.tvCantidadTareasPendientes);
        contenedorListaTareas = findViewById(R.id.contenedorListaTareas);
        scrollListaTareas = findViewById(R.id.scrollListaTareas);

        cardRegistrarServicio = findViewById(R.id.cardRegistrarServicio);
        cardConsultarRegistros = findViewById(R.id.cardConsultarRegistros);
        cardInventario = findViewById(R.id.cardInventario);
        cardDirectorioClientes = findViewById(R.id.cardDirectorioClientes);
        cardMapaGps = findViewById(R.id.cardMapaGps);
        btnFinalizarDia = findViewById(R.id.btnFinalizarDia);
    }

    private void configurarAccesosRapidos() {
        if (cardRegistrarServicio != null) {
            cardRegistrarServicio.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, RegistrarServicioActivity.class)));
        }

        if (cardConsultarRegistros != null) {
            cardConsultarRegistros.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, RegistrarTareaActivity.class)));
        }

        if (cardInventario != null) {
            cardInventario.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, InventarioQuimicosActivity.class)));
        }

        if (cardDirectorioClientes != null) {
            cardDirectorioClientes.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, DirectorioClientesActivity.class)));
        }

        if (cardMapaGps != null) {
            cardMapaGps.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, MapaRutasActivity.class)));
        }

        if (btnFinalizarDia != null) {
            btnFinalizarDia.setOnClickListener(v -> {

                btnFinalizarDia.setEnabled(false);

                Toast.makeText(this,
                        "Generando reporte de cierre...",
                        Toast.LENGTH_SHORT).show();

                new Thread(() -> {
                    try {
                        java.io.File archivo = ExcelExporter.generarExcel(
                                getApplicationContext()
                        );


                        runOnUiThread(() -> {
                            btnFinalizarDia.setEnabled(true);

                            try {
                                Uri uri = FileProvider.getUriForFile(
                                        MainActivity.this,
                                        getPackageName() + ".fileprovider",
                                        archivo
                                );

                                Intent compartir = new Intent(Intent.ACTION_SEND);
                                compartir.setType(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                );
                                compartir.putExtra(Intent.EXTRA_STREAM, uri);
                                compartir.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                compartir.setClipData(
                                        android.content.ClipData.newRawUri("Reporte Excel", uri)
                                );

                                Intent selector = Intent.createChooser(
                                        compartir,
                                        "Compartir reporte Excel"
                                );

                                startActivity(selector);

                            } catch (Exception e) {
                                android.util.Log.e(
                                        "ExcelExporter",
                                        "Error al compartir el archivo",
                                        e
                                );

                                Toast.makeText(this,
                                        "Excel generado, pero no se pudo compartir",
                                        Toast.LENGTH_LONG).show();
                            }
                        });


                    } catch (Exception e) {
                        android.util.Log.e(
                                "ExcelExporter",
                                "Error al generar el reporte",
                                e
                        );


                    }
                }).start();
            });
        }
    }

    private void actualizarFechaHeader() {
        if (tvFechaHeader != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("es", "ES"));
            String fechaActual = sdf.format(Calendar.getInstance().getTime());
            fechaActual = fechaActual.substring(0, 1).toUpperCase() + fechaActual.substring(1);
            tvFechaHeader.setText(fechaActual);
        }
    }

    private void actualizarCalendarioSemanal() {
        Calendar calSemana = (Calendar) calendarioActual.clone();
        calSemana.setFirstDayOfWeek(Calendar.MONDAY);
        calSemana.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        Calendar hoy = Calendar.getInstance();

        SimpleDateFormat formatoMes = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String mesStr = formatoMes.format(calSemana.getTime());
        tvMesAnio.setText(mesStr.substring(0, 1).toUpperCase() + mesStr.substring(1));

        SimpleDateFormat sdfDb = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfTitulo = new SimpleDateFormat("EEEE d", new Locale("es", "ES"));

        int primerDiaMes = calSemana.get(Calendar.DAY_OF_MONTH);
        posHoyEnSemana = -1;

        for (int i = 0; i < 7; i++) {
            int numeroDia = calSemana.get(Calendar.DAY_OF_MONTH);
            tvDias[i].setText(String.valueOf(numeroDia));

            fechasDiasSemana[i] = sdfDb.format(calSemana.getTime());
            titulosDiasSemana[i] = sdfTitulo.format(calSemana.getTime()).toUpperCase();

            boolean esHoy = (calSemana.get(Calendar.YEAR) == hoy.get(Calendar.YEAR)) &&
                    (calSemana.get(Calendar.DAY_OF_YEAR) == hoy.get(Calendar.DAY_OF_YEAR));

            if (esHoy) {
                posHoyEnSemana = i;
            }

            if (i == 6) {
                int ultimoDiaMes = calSemana.get(Calendar.DAY_OF_MONTH);
                String mesAbrevFin = new SimpleDateFormat("MMM", new Locale("es", "ES")).format(calSemana.getTime());
                tvRangoSemana.setText(String.format(Locale.getDefault(), "%d – %d %s.", primerDiaMes, ultimoDiaMes, mesAbrevFin));
            }

            calSemana.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (posHoyEnSemana != -1) {
            seleccionarDia(posHoyEnSemana);
        } else {
            seleccionarDia(0);
        }
    }

    private void seleccionarDia(int index) {
        indiceDiaSeleccionado = index;

        for (int i = 0; i < 7; i++) {
            if (tvDias[i] != null) {
                tvDias[i].setBackground(null);
                tvDias[i].setTextColor(ContextCompat.getColor(this, R.color.text_main));
            }
        }

        if (posHoyEnSemana != -1 && tvDias[posHoyEnSemana] != null) {
            tvDias[posHoyEnSemana].setBackgroundResource(R.drawable.bg_day_selected);
            tvDias[posHoyEnSemana].setTextColor(ContextCompat.getColor(this, R.color.text_light));
        }

        if (tvDias[index] != null) {
            tvDias[index].setBackgroundResource(R.drawable.bg_selected_day_circle);
            tvDias[index].setTextColor(ContextCompat.getColor(this, R.color.text_light));
        }

        cargarTareasPorFecha(fechasDiasSemana[index], titulosDiasSemana[index]);
    }

    private void cargarTareasPorFecha(String fechaIso, String tituloDia) {
        if (contenedorListaTareas == null || dbHelper == null) return;

        contenedorListaTareas.removeAllViews();

        SimpleDateFormat sdfDb =
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String hoyIso = sdfDb.format(Calendar.getInstance().getTime());

        if (tvTituloHoyTareas != null) {
            tvTituloHoyTareas.setText(
                    (fechaIso.equals(hoyIso) ? "HOY · " : "TAREAS · ") + tituloDia
            );
        }

        Cursor cursor = dbHelper.obtenerTareasPorFecha(fechaIso);
        int pendientes = 0;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idxId = cursor.getColumnIndex(DbHelper.COL_TAREA_ID);
                int idxCategoria = cursor.getColumnIndex(DbHelper.COL_TAREA_CATEGORIA);
                int idxTitulo = cursor.getColumnIndex(DbHelper.COL_TAREA_TITULO);
                int idxCliente = cursor.getColumnIndex(DbHelper.COL_TAREA_CLIENTE);
                int idxTipo = cursor.getColumnIndex(DbHelper.COL_TAREA_TIPO);
                int idxHora = cursor.getColumnIndex(DbHelper.COL_TAREA_HORA);
                int idxEstado = cursor.getColumnIndex(DbHelper.COL_TAREA_ESTADO);

                long id = cursor.getLong(idxId);

                String categoria = idxCategoria >= 0
                        ? cursor.getString(idxCategoria) : "SERVICIO_PROGRAMADO";

                String titulo = idxTitulo >= 0
                        ? cursor.getString(idxTitulo) : "";

                String cliente = idxCliente >= 0
                        ? cursor.getString(idxCliente) : "";

                String tipo = idxTipo >= 0
                        ? cursor.getString(idxTipo) : "";

                String hora = idxHora >= 0
                        ? cursor.getString(idxHora) : "";

                String estado = idxEstado >= 0
                        ? cursor.getString(idxEstado) : "pendiente";

                boolean interna = "INTERNA".equalsIgnoreCase(categoria);
                boolean completada = "Completada".equalsIgnoreCase(estado);

                if (!completada) pendientes++;

                String nombre = interna ? titulo : cliente;
                String subtitulo = interna ? "Tarea interna" : tipo;

                if (nombre == null || nombre.trim().isEmpty()) {
                    nombre = interna ? "Tarea interna" : "Servicio programado";
                }

                if (subtitulo == null) subtitulo = "";
                if (hora == null || hora.trim().isEmpty()) {
                    hora = "Sin hora";
                }

                LinearLayout tarjeta = new LinearLayout(this);
                tarjeta.setOrientation(LinearLayout.HORIZONTAL);
                tarjeta.setGravity(android.view.Gravity.CENTER_VERTICAL);
                tarjeta.setBackgroundResource(R.drawable.bg_input_field);
                tarjeta.setPadding(
                        dpToPx(14),
                        dpToPx(12),
                        dpToPx(12),
                        dpToPx(12)
                );

                LinearLayout.LayoutParams tarjetaParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                dpToPx(92)
                        );

                tarjetaParams.setMargins(0, 0, 0, dpToPx(8));
                tarjeta.setLayoutParams(tarjetaParams);

                LinearLayout informacion = new LinearLayout(this);
                informacion.setOrientation(LinearLayout.VERTICAL);
                informacion.setLayoutParams(
                        new LinearLayout.LayoutParams(0, -2, 1f)
                );

                TextView tvNombre = new TextView(this);
                tvNombre.setText(nombre);
                tvNombre.setTextSize(15f);
                tvNombre.setTypeface(null, Typeface.BOLD);
                tvNombre.setTextColor(
                        ContextCompat.getColor(this, R.color.text_main)
                );
                tvNombre.setMaxLines(2);

                TextView tvSubtitulo = new TextView(this);
                tvSubtitulo.setText(subtitulo);
                tvSubtitulo.setTextSize(12f);
                tvSubtitulo.setTextColor(
                        ContextCompat.getColor(this, R.color.text_muted)
                );

                TextView tvHora = new TextView(this);
                tvHora.setText("⏰ " + hora);
                tvHora.setTextSize(12f);
                tvHora.setTextColor(
                        ContextCompat.getColor(this, R.color.text_muted)
                );

                informacion.addView(tvNombre);
                informacion.addView(tvSubtitulo);
                informacion.addView(tvHora);

                CheckBox checkBox = new CheckBox(this);
                checkBox.setChecked(completada);
                checkBox.setContentDescription("Completar " + nombre);

                checkBox.setOnClickListener(v -> {
                    if (completada) {
                        checkBox.setChecked(true);
                        Toast.makeText(
                                this,
                                "La tarea ya está completada",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    if (!interna) {
                        checkBox.setChecked(false);

                        new AlertDialog.Builder(this)
                                .setTitle("Servicio programado")
                                .setMessage("¿Deseas registrar el trabajo realizado para completar esta tarea?")
                                .setPositiveButton("Registrar servicio", (dialog, which) -> {
                                    Intent intent = new Intent(
                                            MainActivity.this,
                                            RegistrarServicioActivity.class
                                    );
                                    intent.putExtra("ID_TAREA_PROGRAMADA", id);
                                    startActivity(intent);
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();

                        return;
                    }
                    boolean exito = dbHelper.marcarTareaCompletada(id);

                    if (exito) {
                        cargarTareasPorFecha(fechaIso, tituloDia);
                    } else {
                        checkBox.setChecked(false);
                        Toast.makeText(
                                this,
                                "No se pudo completar la tarea",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

                tarjeta.addView(informacion);
                tarjeta.addView(checkBox);

                contenedorListaTareas.addView(tarjeta);
            }

            cursor.close();
        }

        if (contenedorListaTareas.getChildCount() == 0) {
            TextView vacio = new TextView(this);
            vacio.setText("No hay tareas programadas para este día.");
            vacio.setTextColor(
                    ContextCompat.getColor(this, R.color.text_muted)
            );
            vacio.setPadding(0, dpToPx(12), 0, dpToPx(12));
            contenedorListaTareas.addView(vacio);
        }

        if (tvCantidadTareasPendientes != null) {
            tvCantidadTareasPendientes.setText(
                    pendientes == 1
                            ? "1 tarea pendiente"
                            : pendientes + " tareas pendientes"
            );
        }

        scrollListaTareas.post(() -> {
            int cantidad = contenedorListaTareas.getChildCount();

            int alturaMaxima = dpToPx(300);

            int alturaContenido = 0;

            for (int i = 0; i < Math.min(cantidad, 3); i++) {
                View tarjeta = contenedorListaTareas.getChildAt(i);

                alturaContenido += tarjeta.getHeight();

                android.view.ViewGroup.LayoutParams lp = tarjeta.getLayoutParams();

                if (lp instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) lp;
                    alturaContenido += params.topMargin + params.bottomMargin;
                }
            }

            android.view.ViewGroup.LayoutParams scrollParams =
                    scrollListaTareas.getLayoutParams();

            scrollParams.height = Math.min(alturaContenido, alturaMaxima);

            if (scrollParams.height <= 0) {
                scrollParams.height = alturaMaxima;
            }

            scrollListaTareas.setLayoutParams(scrollParams);
            scrollListaTareas.scrollTo(0, 0);
        });
    }

    private void cargarTareasPendientesHoy() {
        SimpleDateFormat sdfDb = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfTitulo = new SimpleDateFormat("EEEE d", new Locale("es", "ES"));
        Calendar cal = Calendar.getInstance();
        cargarTareasPorFecha(sdfDb.format(cal.getTime()), sdfTitulo.format(cal.getTime()).toUpperCase());
    }

    private void configurarMenuHamburguesa() {
        drawerLayout = findViewById(R.id.drawerLayout);
        View btnMenuHamburguesa = findViewById(R.id.btnMenuHamburguesa);
        View btnCerrarDrawer = findViewById(R.id.btnCerrarDrawer);

        tvNombreUsuarioDrawer = findViewById(R.id.tvNombreUsuario);
        tvAvatarDrawer = findViewById(R.id.tvAvatarIniciales);
        tvCargoUsuarioDrawer = findViewById(R.id.tvCargoUsuario);

        if (btnMenuHamburguesa != null && drawerLayout != null) {
            btnMenuHamburguesa.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (btnCerrarDrawer != null && drawerLayout != null) {
            btnCerrarDrawer.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        }

        View navItemMiJornada = findViewById(R.id.navItemMiJornada);
        View navItemMiCuenta = findViewById(R.id.navItemMiCuenta);
        View navItemHistoriales = findViewById(R.id.navItemHistoriales);
        View navItemConfiguracion = findViewById(R.id.navItemConfiguracion);
        View navItemDocumentos = findViewById(R.id.navItemDocumentos);
        View navItemSincronizacion = findViewById(R.id.navItemSincronizacion);
        View navItemSoporte = findViewById(R.id.navItemSoporte);

        if (navItemMiJornada != null && drawerLayout != null) {
            navItemMiJornada.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        }

        if (navItemMiCuenta != null && drawerLayout != null) {
            navItemMiCuenta.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(MainActivity.this, MiCuentaActivity.class));
            });
        }

        if (navItemHistoriales != null && drawerLayout != null) {
            navItemHistoriales.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(MainActivity.this, HistorialActivity.class));
            });
        }

        if (navItemConfiguracion != null && drawerLayout != null) {
            navItemConfiguracion.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(MainActivity.this, ConfiguracionActivity.class));
            });
        }

        if (navItemDocumentos != null && drawerLayout != null) {
            navItemDocumentos.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(this, "Fichas técnicas y protocolos", Toast.LENGTH_SHORT).show();
            });
        }

        if (navItemSincronizacion != null && drawerLayout != null) {
            navItemSincronizacion.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(this, "Sincronización: Datos al día", Toast.LENGTH_SHORT).show();
            });
        }

        if (navItemSoporte != null && drawerLayout != null) {
            navItemSoporte.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(this, "Contacto soporte: +56 9 8123 4567", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void cargarDatosUsuarioDrawer() {
        SharedPreferences pref = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
        String correoActivo = pref.getString("correo_usuario", "");

        if (!correoActivo.isEmpty() && dbHelper != null) {
            Cursor cursor = dbHelper.obtenerDatosUsuario(correoActivo);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int idxNombre = cursor.getColumnIndex("nombre");
                    int idxCargo = cursor.getColumnIndex("cargo");

                    String nombre = (idxNombre != -1 && !cursor.isNull(idxNombre)) ? cursor.getString(idxNombre) : "";
                    String cargo = (idxCargo != -1 && !cursor.isNull(idxCargo)) ? cursor.getString(idxCargo) : "";

                    if (tvNombreUsuarioDrawer != null) {
                        tvNombreUsuarioDrawer.setText(nombre);
                    }

                    if (tvAvatarDrawer != null) {
                        tvAvatarDrawer.setText(generarIniciales(nombre));
                    }

                    if (tvCargoUsuarioDrawer != null) {
                        tvCargoUsuarioDrawer.setText(cargo.isEmpty() ? "Sin profesión definida" : cargo);
                    }
                }
                cursor.close();
            }
        }
    }

    private String generarIniciales(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) return "US";
        String[] partes = nombreCompleto.trim().split("\\s+");
        if (partes.length >= 2) {
            return ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
        }
        return ("" + partes[0].charAt(0)).toUpperCase();
    }
}