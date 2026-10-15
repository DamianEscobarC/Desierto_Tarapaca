package com.example.desiertotarapaca;

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
            btnFinalizarDia.setOnClickListener(v ->
                    Toast.makeText(this, "Generando reporte de cierre PDF...", Toast.LENGTH_LONG).show());
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

        SimpleDateFormat sdfDb = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String hoyIso = sdfDb.format(Calendar.getInstance().getTime());

        if (tvTituloHoyTareas != null) {
            if (fechaIso.equals(hoyIso)) {
                tvTituloHoyTareas.setText("HOY · " + tituloDia);
            } else {
                tvTituloHoyTareas.setText("TAREAS · " + tituloDia);
            }
        }

        Cursor cursor = dbHelper.obtenerTareasPorFecha(fechaIso);
        int tareasPendientes = 0;

        if (cursor != null) {
            if (cursor.getCount() == 0) {
                TextView tvVacio = new TextView(this);
                tvVacio.setText("No hay tareas programadas para este día.");
                tvVacio.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                tvVacio.setTextSize(13);
                tvVacio.setPadding(0, 10, 0, 10);
                contenedorListaTareas.addView(tvVacio);
            } else {
                while (cursor.moveToNext()) {
                    int idxCliente = cursor.getColumnIndex("cliente");
                    int idxTipo = cursor.getColumnIndex("tipo");
                    int idxHora = cursor.getColumnIndex("hora");
                    int idxEstado = cursor.getColumnIndex("estado");

                    String cliente = idxCliente != -1 ? cursor.getString(idxCliente) : "";
                    String tipo = idxTipo != -1 ? cursor.getString(idxTipo) : "";
                    String hora = idxHora != -1 ? cursor.getString(idxHora) : "";
                    String estado = idxEstado != -1 ? cursor.getString(idxEstado) : "pendiente";

                    boolean completada = estado.equalsIgnoreCase("Completada");
                    if (!completada) {
                        tareasPendientes++;
                    }

                    LinearLayout fila = new LinearLayout(this);
                    fila.setOrientation(LinearLayout.HORIZONTAL);
                    fila.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    fila.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    fila.setPadding(0, dpToPx(5), 0, dpToPx(5));

                    TextView tvHoraItem = new TextView(this);
                    tvHoraItem.setText(hora);
                    tvHoraItem.setTextColor(completada ? ContextCompat.getColor(this, R.color.text_muted) : ContextCompat.getColor(this, R.color.text_main));
                    tvHoraItem.setTextSize(13);
                    tvHoraItem.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(52), LinearLayout.LayoutParams.WRAP_CONTENT));

                    TextView tvClienteItem = new TextView(this);
                    tvClienteItem.setText(cliente);
                    tvClienteItem.setTextColor(completada ? ContextCompat.getColor(this, R.color.text_muted) : ContextCompat.getColor(this, R.color.text_main));
                    tvClienteItem.setTextSize(13);
                    if (completada) {
                        tvClienteItem.setPaintFlags(tvClienteItem.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    tvClienteItem.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                    TextView tvTipoItem = new TextView(this);
                    tvTipoItem.setText(tipo);
                    tvTipoItem.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                    tvTipoItem.setTextSize(12);

                    fila.addView(tvHoraItem);
                    fila.addView(tvClienteItem);
                    fila.addView(tvTipoItem);

                    if (completada) {
                        TextView tvBadge = new TextView(this);
                        tvBadge.setText("✓ Hecho");
                        tvBadge.setTextColor(Color.parseColor("#137333"));
                        tvBadge.setBackgroundColor(Color.parseColor("#E6F4EA"));
                        tvBadge.setTextSize(10f);
                        tvBadge.setTypeface(null, Typeface.BOLD);
                        tvBadge.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));

                        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        badgeParams.setMarginStart(dpToPx(6));
                        tvBadge.setLayoutParams(badgeParams);

                        fila.addView(tvBadge);
                    }

                    contenedorListaTareas.addView(fila);
                }
            }
            cursor.close();
        }

        if (tvCantidadTareasPendientes != null) {
            tvCantidadTareasPendientes.setText(tareasPendientes + (tareasPendientes == 1 ? " tarea pendiente" : " tareas pendientes"));
        }
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