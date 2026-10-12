package com.example.desiertotarapaca;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.widget.ImageView;
import android.content.Intent;
public class MainActivity extends AppCompatActivity {

    private LinearLayout cardRegistrarServicio, cardConsultarRegistros, cardInventario;
    private LinearLayout cardDirectorioClientes, cardMapaGps, cardRuta;
    private Button btnFinalizarDia;

    private TextView tvMesAnio, tvRangoSemana, btnSemanaAnterior, btnSemanaSiguiente;
    private TextView[] tvDias = new TextView[7];
    private Calendar calendarioActual;
    private DrawerLayout drawerLayout;
    private ImageView btnMenuHamburguesa, btnCerrarDrawer;
    private LinearLayout navItemMiJornada, navItemMiCuenta, navItemHistoriales, navItemSincronizacion;

    private DbHelper dbHelper;
    private TextView tvNombreUsuarioDrawer, tvAvatarDrawer;
    private TextView tvCargoUsuarioDrawer;
    private TextView tvTituloHoyTareas, tvCantidadTareasPendientes;
    private LinearLayout contenedorListaTareas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DbHelper(this);

        cardRegistrarServicio = findViewById(R.id.cardRegistrarServicio);
        cardConsultarRegistros = findViewById(R.id.cardConsultarRegistros);
        cardInventario = findViewById(R.id.cardInventario);
        cardDirectorioClientes = findViewById(R.id.cardDirectorioClientes);
        cardMapaGps = findViewById(R.id.cardMapaGps);
        cardRuta = findViewById(R.id.cardRuta);
        btnFinalizarDia = findViewById(R.id.btnFinalizarDia);

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

        calendarioActual = Calendar.getInstance();
        actualizarCalendarioSemanal();

        btnSemanaAnterior.setOnClickListener(v -> {
            calendarioActual.add(Calendar.WEEK_OF_YEAR, -1);
            actualizarCalendarioSemanal();
        });

        btnSemanaSiguiente.setOnClickListener(v -> {
            calendarioActual.add(Calendar.WEEK_OF_YEAR, 1);
            actualizarCalendarioSemanal();
        });

        if (cardRegistrarServicio != null) {
            cardRegistrarServicio.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RegistrarServicioActivity.class);
                startActivity(intent);
            });
        }
        if (cardDirectorioClientes != null) {
            cardDirectorioClientes.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, DirectorioClientesActivity.class);
                startActivity(intent);
            });
        }

        btnFinalizarDia.setOnClickListener(v -> {
            Toast.makeText(this, "Generando reporte de cierre PDF...", Toast.LENGTH_LONG).show();
        });


        configurarMenuHamburguesa();

        tvTituloHoyTareas = findViewById(R.id.tvTituloHoyTareas);
        tvCantidadTareasPendientes = findViewById(R.id.tvCantidadTareasPendientes);
        contenedorListaTareas = findViewById(R.id.contenedorListaTareas);

        if (cardConsultarRegistros != null) {
            cardConsultarRegistros.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RegistrarTareaActivity.class);
                startActivity(intent);
            });
        }

        cardInventario = findViewById(R.id.cardInventario);
        if (cardInventario != null) {
            cardInventario.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, InventarioQuimicosActivity.class);
                startActivity(intent);
            });
        }


    }
    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosUsuarioDrawer();
        cargarTareasPendientesHoy();
    }


    private void actualizarCalendarioSemanal() {
        Calendar calSemana = (Calendar) calendarioActual.clone();
        calSemana.setFirstDayOfWeek(Calendar.MONDAY);
        calSemana.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        Calendar hoy = Calendar.getInstance();

        SimpleDateFormat formatoMes = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String mesStr = formatoMes.format(calSemana.getTime());
        tvMesAnio.setText(mesStr.substring(0, 1).toUpperCase() + mesStr.substring(1));

        int primerDiaMes = calSemana.get(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < 7; i++) {
            int numeroDia = calSemana.get(Calendar.DAY_OF_MONTH);
            tvDias[i].setText(String.valueOf(numeroDia));

            boolean esHoy = (calSemana.get(Calendar.YEAR) == hoy.get(Calendar.YEAR)) &&
                    (calSemana.get(Calendar.DAY_OF_YEAR) == hoy.get(Calendar.DAY_OF_YEAR));

            if (esHoy) {
                tvDias[i].setBackgroundResource(R.drawable.bg_day_selected);
                tvDias[i].setTextColor(ContextCompat.getColor(this, R.color.text_light));
            } else {
                tvDias[i].setBackground(null);
                tvDias[i].setTextColor(ContextCompat.getColor(this, R.color.text_main));
            }

            if (i == 6) {
                int ultimoDiaMes = calSemana.get(Calendar.DAY_OF_MONTH);
                String mesAbrevFin = new SimpleDateFormat("MMM", new Locale("es", "ES")).format(calSemana.getTime());
                tvRangoSemana.setText(String.format(Locale.getDefault(), "%d – %d %s.", primerDiaMes, ultimoDiaMes, mesAbrevFin));
            }

            calSemana.add(Calendar.DAY_OF_MONTH, 1);
        }
    }
    private void configurarMenuHamburguesa() {
        drawerLayout = findViewById(R.id.drawerLayout);
        android.view.View btnMenuHamburguesa = findViewById(R.id.btnMenuHamburguesa);
        android.view.View btnCerrarDrawer = findViewById(R.id.btnCerrarDrawer);

        tvNombreUsuarioDrawer = findViewById(R.id.tvNombreUsuario);
        tvAvatarDrawer = findViewById(R.id.tvAvatarIniciales);
        tvCargoUsuarioDrawer = findViewById(R.id.tvCargoUsuario);

        if (btnMenuHamburguesa != null && drawerLayout != null) {
            btnMenuHamburguesa.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (btnCerrarDrawer != null && drawerLayout != null) {
            btnCerrarDrawer.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        }

        android.view.View navItemMiJornada = findViewById(R.id.navItemMiJornada);
        android.view.View navItemMiCuenta = findViewById(R.id.navItemMiCuenta);
        android.view.View navItemHistoriales = findViewById(R.id.navItemHistoriales);
        android.view.View navItemConfiguracion = findViewById(R.id.navItemConfiguracion);
        android.view.View navItemDocumentos = findViewById(R.id.navItemDocumentos);
        android.view.View navItemSincronizacion = findViewById(R.id.navItemSincronizacion);
        android.view.View navItemSoporte = findViewById(R.id.navItemSoporte);

        if (navItemMiJornada != null && drawerLayout != null) {
            navItemMiJornada.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        }

        if (navItemMiCuenta != null && drawerLayout != null) {
            navItemMiCuenta.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                android.content.Intent intent = new android.content.Intent(MainActivity.this, MiCuentaActivity.class);
                startActivity(intent);
            });
        }

        if (navItemHistoriales != null && drawerLayout != null) {
            navItemHistoriales.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(MainActivity.this, HistorialActivity.class);
                startActivity(intent);
            });
        }

        if (navItemConfiguracion != null && drawerLayout != null) {
            navItemConfiguracion.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                android.content.Intent intent = new android.content.Intent(MainActivity.this, ConfiguracionActivity.class);
                startActivity(intent);
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
            android.database.Cursor cursor = dbHelper.obtenerDatosUsuario(correoActivo);
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
    private void cargarTareasPendientesHoy() {
        if (contenedorListaTareas == null || dbHelper == null) return;

        contenedorListaTareas.removeAllViews();

        SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfTitulo = new SimpleDateFormat("EEEE d", new Locale("es", "ES"));

        Calendar cal = Calendar.getInstance();
        String fechaHoy = sdfFecha.format(cal.getTime());
        String tituloDia = "HOY · " + sdfTitulo.format(cal.getTime()).toUpperCase();

        if (tvTituloHoyTareas != null) {
            tvTituloHoyTareas.setText(tituloDia);
        }

        android.database.Cursor cursor = dbHelper.obtenerTareasPorFecha(fechaHoy);
        int totalTareas = 0;

        if (cursor != null) {
            totalTareas = cursor.getCount();

            if (totalTareas == 0) {
                TextView tvVacio = new TextView(this);
                tvVacio.setText("No hay tareas programadas para hoy.");
                tvVacio.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                tvVacio.setTextSize(13);
                tvVacio.setPadding(0, 10, 0, 10);
                contenedorListaTareas.addView(tvVacio);
            } else {
                while (cursor.moveToNext()) {
                    int idxCliente = cursor.getColumnIndex("cliente");
                    int idxTipo = cursor.getColumnIndex("tipo");
                    int idxHora = cursor.getColumnIndex("hora");

                    String cliente = idxCliente != -1 ? cursor.getString(idxCliente) : "";
                    String tipo = idxTipo != -1 ? cursor.getString(idxTipo) : "";
                    String hora = idxHora != -1 ? cursor.getString(idxHora) : "";

                    LinearLayout fila = new LinearLayout(this);
                    fila.setOrientation(LinearLayout.HORIZONTAL);
                    fila.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    fila.setPadding(0, 8, 0, 8);

                    TextView tvHoraItem = new TextView(this);
                    tvHoraItem.setText(hora);
                    tvHoraItem.setTextColor(ContextCompat.getColor(this, R.color.text_main));
                    tvHoraItem.setTextSize(13);
                    tvHoraItem.setLayoutParams(new LinearLayout.LayoutParams(130, LinearLayout.LayoutParams.WRAP_CONTENT));

                    TextView tvClienteItem = new TextView(this);
                    tvClienteItem.setText(cliente);
                    tvClienteItem.setTextColor(ContextCompat.getColor(this, R.color.text_main));
                    tvClienteItem.setTextSize(13);
                    tvClienteItem.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                    TextView tvTipoItem = new TextView(this);
                    tvTipoItem.setText(tipo);
                    tvTipoItem.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                    tvTipoItem.setTextSize(13);

                    fila.addView(tvHoraItem);
                    fila.addView(tvClienteItem);
                    fila.addView(tvTipoItem);

                    contenedorListaTareas.addView(fila);
                }
            }
            cursor.close();
        }

        if (tvCantidadTareasPendientes != null) {
            tvCantidadTareasPendientes.setText(totalTareas + (totalTareas == 1 ? " tarea pendiente" : " tareas pendientes"));
        }
    }
}
