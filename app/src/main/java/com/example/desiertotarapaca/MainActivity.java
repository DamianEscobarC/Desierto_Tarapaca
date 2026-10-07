package com.example.desiertotarapaca;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        cardRegistrarServicio.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo formulario de servicio...", Toast.LENGTH_SHORT).show();
        });

        btnFinalizarDia.setOnClickListener(v -> {
            Toast.makeText(this, "Generando reporte de cierre PDF...", Toast.LENGTH_LONG).show();
        });

        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenuHamburguesa = findViewById(R.id.btnMenuHamburguesa);
        btnCerrarDrawer = findViewById(R.id.btnCerrarDrawer);

        btnMenuHamburguesa.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        btnCerrarDrawer.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        navItemMiJornada = findViewById(R.id.navItemMiJornada);
        navItemMiJornada.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        navItemMiCuenta = findViewById(R.id.navItemMiCuenta);
        navItemMiCuenta.setOnClickListener(v -> {
            Toast.makeText(this, "Perfil de operador", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        navItemSincronizacion = findViewById(R.id.navItemSincronizacion);
        navItemSincronizacion.setOnClickListener(v -> {
            Toast.makeText(this, "Comprobando sincronización offline...", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        });
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
}