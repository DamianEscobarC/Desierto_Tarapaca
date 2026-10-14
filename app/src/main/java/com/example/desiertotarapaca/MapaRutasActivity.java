package com.example.desiertotarapaca;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.maplibre.android.MapLibre;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.annotations.PolylineOptions;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapaRutasActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static class PuntoDestino {
        public String titulo;
        public String subtitulo;
        public String origen;
        public double lat;
        public double lng;

        public PuntoDestino(String titulo, String subtitulo, String origen, double lat, double lng) {
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.origen = origen;
            this.lat = lat;
            this.lng = lng;
        }
    }

    private static final int CODIGO_PERMISO_GPS = 1001;
    private static final double RENDIMIENTO_KM_POR_LITRO = 6.5;
    private static final double PRECIO_DIESEL_CLP = 1050.0;

    private LatLng ubicacionActualGps = new LatLng(-20.2208, -70.1390);

    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private boolean mapaListo = false;

    private View btnVolverMapa, btnCentrarGps, btnBrujulaNorte, btnZoomIn, btnZoomOut;
    private View btnAbrirMenuBanos;
    private LinearLayout layoutEstadoInicialTrayecto, layoutRutaActivaMetricas;
    private TextView tvValorDistancia, tvValorCombustible, tvValorCostoClp, tvRutaDetallePie;
    private View bottomSheetBanos;
    private BottomSheetBehavior<View> sheetBehavior;
    private LinearLayout contenedorItemsBanos;
    private TextView tvBadgeTotalBanos, tvSinRegistrosAviso;

    private DbHelper dbHelper;
    private final List<PuntoDestino> listaPuntosBD = new ArrayList<>();

    private LatLng destinoExterno = null;
    private String nombreDestinoExterno = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapLibre.getInstance(this);
        setContentView(R.layout.activity_mapa_rutas);

        dbHelper = new DbHelper(this);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        btnVolverMapa = findViewById(R.id.btnVolverMapa);
        btnCentrarGps = findViewById(R.id.btnCentrarGps);
        btnBrujulaNorte = findViewById(R.id.btnBrujulaNorte);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        btnAbrirMenuBanos = findViewById(R.id.btnAbrirMenuBanos);

        layoutEstadoInicialTrayecto = findViewById(R.id.layoutEstadoInicialTrayecto);
        layoutRutaActivaMetricas = findViewById(R.id.layoutRutaActivaMetricas);
        tvValorDistancia = findViewById(R.id.tvValorDistancia);
        tvValorCombustible = findViewById(R.id.tvValorCombustible);
        tvValorCostoClp = findViewById(R.id.tvValorCostoClp);
        tvRutaDetallePie = findViewById(R.id.tvRutaDetallePie);

        bottomSheetBanos = findViewById(R.id.bottomSheetBanos);
        sheetBehavior = BottomSheetBehavior.from(bottomSheetBanos);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        contenedorItemsBanos = findViewById(R.id.contenedorItemsBanos);
        tvBadgeTotalBanos = findViewById(R.id.tvBadgeTotalBanos);
        tvSinRegistrosAviso = findViewById(R.id.tvSinRegistrosAviso);

        btnVolverMapa.setOnClickListener(v -> finish());

        btnCentrarGps.setOnClickListener(v -> obtenerUbicacionActualReal());

        btnBrujulaNorte.setOnClickListener(v -> {
            if (mapLibreMap != null) {
                CameraPosition posActual = mapLibreMap.getCameraPosition();
                mapLibreMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(posActual.target)
                                .zoom(posActual.zoom)
                                .bearing(0)
                                .tilt(0)
                                .build()
                ), 800);
            }
        });

        if (btnZoomIn != null) {
            btnZoomIn.setOnClickListener(v -> {
                if (mapLibreMap != null) {
                    mapLibreMap.animateCamera(CameraUpdateFactory.zoomIn(), 250);
                }
            });
        }

        if (btnZoomOut != null) {
            btnZoomOut.setOnClickListener(v -> {
                if (mapLibreMap != null) {
                    mapLibreMap.animateCamera(CameraUpdateFactory.zoomOut(), 250);
                }
            });
        }

        btnAbrirMenuBanos.setOnClickListener(v -> sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("DESTINO_LAT") && intent.hasExtra("DESTINO_LNG")) {
            double lat = intent.getDoubleExtra("DESTINO_LAT", 0);
            double lng = intent.getDoubleExtra("DESTINO_LNG", 0);
            nombreDestinoExterno = intent.getStringExtra("DESTINO_NOMBRE");
            if (nombreDestinoExterno == null || nombreDestinoExterno.isEmpty()) {
                nombreDestinoExterno = "Destino Seleccionado";
            }
            destinoExterno = new LatLng(lat, lng);
        }

        cargarPuntosDesdeBaseDeDatos();
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap map) {
        this.mapLibreMap = map;
        this.mapaListo = true;

        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setScrollGesturesEnabled(true);
        map.getUiSettings().setDoubleTapGesturesEnabled(true);
        map.getUiSettings().setQuickZoomGesturesEnabled(true);

        String urlEstiloValido = "https://tiles.openfreemap.org/styles/liberty";

        map.setStyle(new Style.Builder().fromUri(urlEstiloValido), style -> {
            verificarPermisoYObtenerGps();

            if (destinoExterno != null) {
                enfocarDestinoYCalcularRuta(destinoExterno, nombreDestinoExterno, "Navegación Externa");
            }
        });
    }

    private void verificarPermisoYObtenerGps() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    CODIGO_PERMISO_GPS
            );
        } else {
            obtenerUbicacionActualReal();
        }
    }

    private void obtenerUbicacionActualReal() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager == null) return;

            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Location mejorUbicacion = null;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGpsEnabled) {
                    mejorUbicacion = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (mejorUbicacion == null && isNetworkEnabled) {
                    mejorUbicacion = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

                LocationListener listener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        actualizarPosicionUsuarioEnMapa(new LatLng(location.getLatitude(), location.getLongitude()));
                        locationManager.removeUpdates(this);
                    }
                    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override public void onProviderEnabled(@NonNull String provider) {}
                    @Override public void onProviderDisabled(@NonNull String provider) {}
                };

                if (isGpsEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, listener);
                } else if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, listener);
                }
            }

            if (mejorUbicacion != null) {
                actualizarPosicionUsuarioEnMapa(new LatLng(mejorUbicacion.getLatitude(), mejorUbicacion.getLongitude()));
            }
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo obtener el GPS actual", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarPosicionUsuarioEnMapa(LatLng nuevaPosicion) {
        this.ubicacionActualGps = nuevaPosicion;

        if (mapLibreMap != null) {
            mapLibreMap.clear();

            mapLibreMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(ubicacionActualGps)
                            .zoom(14.0)
                            .build()
            ), 1000);

            mapLibreMap.addMarker(new MarkerOptions()
                    .position(ubicacionActualGps)
                    .setTitle("Mi Posición Actual"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISO_GPS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActualReal();
            } else {
                Toast.makeText(this, "Permiso GPS denegado. Usando ubicación base.", Toast.LENGTH_SHORT).show();
                if (mapLibreMap != null) {
                    mapLibreMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(ubicacionActualGps)
                                    .zoom(12.0)
                                    .build()
                    ));
                    mapLibreMap.addMarker(new MarkerOptions()
                            .position(ubicacionActualGps)
                            .setTitle("Mi Posición Actual"));
                }
            }
        }
    }

    private void cargarPuntosDesdeBaseDeDatos() {
        listaPuntosBD.clear();
        contenedorItemsBanos.removeAllViews();

        try {
            Cursor cursorServicios = dbHelper.obtenerTodosLosServicios();
            if (cursorServicios != null && cursorServicios.moveToFirst()) {
                int idxCli = cursorServicios.getColumnIndex(DbHelper.COL_CLIENTE);
                int idxTipo = cursorServicios.getColumnIndex(DbHelper.COL_TIPO);
                int idxDesc = cursorServicios.getColumnIndex(DbHelper.COL_DESCRIPCION);
                int idxOt = cursorServicios.getColumnIndex(DbHelper.COL_OT);

                do {
                    String cliente = (idxCli != -1) ? cursorServicios.getString(idxCli) : "Cliente";
                    String tipo = (idxTipo != -1) ? cursorServicios.getString(idxTipo) : "Servicio";
                    String desc = (idxDesc != -1) ? cursorServicios.getString(idxDesc) : "";
                    String ot = (idxOt != -1) ? cursorServicios.getString(idxOt) : "OT";

                    LatLng coords = extraerCoordenadasDeTexto(desc);
                    if (coords == null) {
                        coords = new LatLng(-20.9705, -68.6750);
                    }

                    listaPuntosBD.add(new PuntoDestino(
                            cliente,
                            tipo + " · " + ot,
                            "🛠 Servicio Registrado",
                            coords.getLatitude(),
                            coords.getLongitude()
                    ));
                } while (cursorServicios.moveToNext());
                cursorServicios.close();
            }
        } catch (Exception ignored) {}

        try {
            Cursor cursorTareas = dbHelper.obtenerTodasLasTareas();
            if (cursorTareas != null && cursorTareas.moveToFirst()) {
                int idxCli = cursorTareas.getColumnIndex(DbHelper.COL_TAREA_CLIENTE);
                int idxTipo = cursorTareas.getColumnIndex(DbHelper.COL_TAREA_TIPO);
                int idxFecha = cursorTareas.getColumnIndex(DbHelper.COL_TAREA_FECHA);
                int idxHora = cursorTareas.getColumnIndex(DbHelper.COL_TAREA_HORA);

                do {
                    String cliente = (idxCli != -1) ? cursorTareas.getString(idxCli) : "Cliente";
                    String tipo = (idxTipo != -1) ? cursorTareas.getString(idxTipo) : "Tarea";
                    String fecha = (idxFecha != -1) ? cursorTareas.getString(idxFecha) : "";
                    String hora = (idxHora != -1) ? cursorTareas.getString(idxHora) : "";

                    LatLng coords = new LatLng(-20.2740, -70.1085);

                    listaPuntosBD.add(new PuntoDestino(
                            cliente,
                            tipo + " · " + fecha + " " + hora,
                            "📅 Tarea Agendada",
                            coords.getLatitude(),
                            coords.getLongitude()
                    ));
                } while (cursorTareas.moveToNext());
                cursorTareas.close();
            }
        } catch (Exception ignored) {}

        actualizarVistaBottomSheet();
    }

    private void actualizarVistaBottomSheet() {
        int total = listaPuntosBD.size();
        tvBadgeTotalBanos.setText(total + (total == 1 ? " registro" : " registros"));

        if (total == 0) {
            tvSinRegistrosAviso.setVisibility(View.VISIBLE);
            return;
        }

        tvSinRegistrosAviso.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (PuntoDestino punto : listaPuntosBD) {
            View itemView = inflater.inflate(R.layout.item_bano_arrendado, contenedorItemsBanos, false);

            TextView tvTitulo = itemView.findViewById(R.id.tvTituloItemDestino);
            TextView tvSubtitulo = itemView.findViewById(R.id.tvSubtituloItemDestino);
            TextView tvBadge = itemView.findViewById(R.id.tvBadgeOrigenDestino);

            tvTitulo.setText(punto.titulo);
            tvSubtitulo.setText(punto.subtitulo);
            tvBadge.setText(punto.origen);

            itemView.setOnClickListener(v -> {
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                LatLng destino = new LatLng(punto.lat, punto.lng);
                enfocarDestinoYCalcularRuta(destino, punto.titulo, punto.subtitulo);
            });

            contenedorItemsBanos.addView(itemView);
        }
    }

    private void enfocarDestinoYCalcularRuta(LatLng destino, String tituloDestino, String detalleDestino) {
        if (!mapaListo || mapLibreMap == null) return;

        mapLibreMap.clear();

        mapLibreMap.addMarker(new MarkerOptions()
                .position(ubicacionActualGps)
                .setTitle("Mi Posición Actual"));

        mapLibreMap.addMarker(new MarkerOptions()
                .position(destino)
                .setTitle(tituloDestino));

        mapLibreMap.addPolyline(new PolylineOptions()
                .add(ubicacionActualGps, destino)
                .color(Color.parseColor("#2D6A4F"))
                .width(6f));

        mapLibreMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(destino)
                        .zoom(13.5)
                        .build()
        ), 1200);

        double distanciaKm = calcularDistanciaHaversine(
                ubicacionActualGps.getLatitude(), ubicacionActualGps.getLongitude(),
                destino.getLatitude(), destino.getLongitude()
        );

        double litrosEstimados = distanciaKm / RENDIMIENTO_KM_POR_LITRO;
        double costoTotalClp = litrosEstimados * PRECIO_DIESEL_CLP;

        DecimalFormat dfClp = new DecimalFormat("#,###", new DecimalFormatSymbols(new Locale("es", "CL")));
        DecimalFormat dfDecimal = new DecimalFormat("0.0", new DecimalFormatSymbols(new Locale("es", "CL")));

        tvValorDistancia.setText(dfDecimal.format(distanciaKm) + " km");
        tvValorCombustible.setText(dfDecimal.format(litrosEstimados) + " L");
        tvValorCostoClp.setText("$" + dfClp.format(costoTotalClp));
        tvRutaDetallePie.setText(tituloDestino + " · Solo ida · Valores estimados");

        layoutEstadoInicialTrayecto.setVisibility(View.GONE);
        layoutRutaActivaMetricas.setVisibility(View.VISIBLE);
    }

    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private LatLng extraerCoordenadasDeTexto(String texto) {
        if (texto == null || texto.isEmpty()) return null;
        try {
            Pattern pattern = Pattern.compile("(-?\\d{1,2}\\.\\d+)[,\\s]+(-?\\d{1,3}\\.\\d+)");
            Matcher matcher = pattern.matcher(texto);
            if (matcher.find()) {
                double lat = Double.parseDouble(matcher.group(1));
                double lng = Double.parseDouble(matcher.group(2));
                return new LatLng(lat, lng);
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    protected void onStart() { super.onStart(); mapView.onStart(); }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        cargarPuntosDesdeBaseDeDatos();
    }

    @Override
    protected void onPause() { super.onPause(); mapView.onPause(); }

    @Override
    protected void onStop() { super.onStop(); mapView.onStop(); }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }

    @Override
    protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
}