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

import org.json.JSONArray;
import org.json.JSONObject;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
        public boolean completado;

        public PuntoDestino(String titulo, String subtitulo, String origen, double lat, double lng, boolean completado) {
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.origen = origen;
            this.lat = lat;
            this.lng = lng;
            this.completado = completado;
        }
    }

    private static final int CODIGO_PERMISO_GPS = 1001;
    private static final double RENDIMIENTO_KM_POR_LITRO = 5.8;
    private static final double PRECIO_COMBUSTIBLE_CLP = 1603.0;

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

    private TextView tabMapaEnProceso, tabMapaCompletados;
    private boolean mostrandoEnProceso = true;

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

        tabMapaEnProceso = findViewById(R.id.tabMapaEnProceso);
        tabMapaCompletados = findViewById(R.id.tabMapaCompletados);

        if (tabMapaEnProceso != null) {
            tabMapaEnProceso.setOnClickListener(v -> cambiarTabPuntos(true));
        }

        if (tabMapaCompletados != null) {
            tabMapaCompletados.setOnClickListener(v -> cambiarTabPuntos(false));
        }

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

    private void cambiarTabPuntos(boolean enProceso) {
        if (mostrandoEnProceso == enProceso) return;
        mostrandoEnProceso = enProceso;

        if (mostrandoEnProceso) {
            tabMapaEnProceso.setBackgroundResource(R.drawable.bg_selected_day_circle);
            tabMapaEnProceso.setTextColor(Color.WHITE);
            tabMapaCompletados.setBackground(null);
            tabMapaCompletados.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        } else {
            tabMapaCompletados.setBackgroundResource(R.drawable.bg_selected_day_circle);
            tabMapaCompletados.setTextColor(Color.WHITE);
            tabMapaEnProceso.setBackground(null);
            tabMapaEnProceso.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }

        actualizarVistaBottomSheet();
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
                    .setTitle("Mi Posición"));
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
                            .setTitle("Mi Posición"));
                }
            }
        }
    }

    private void cargarPuntosDesdeBaseDeDatos() {
        listaPuntosBD.clear();

        try {
            Cursor cursorBanos = dbHelper.obtenerTodosLosBanos();
            if (cursorBanos != null && cursorBanos.moveToFirst()) {
                int idxCodigo = cursorBanos.getColumnIndex(DbHelper.COL_BANO_CODIGO);
                int idxCliente = cursorBanos.getColumnIndex(DbHelper.COL_BANO_CLIENTE);
                int idxFaena = cursorBanos.getColumnIndex(DbHelper.COL_BANO_FAENA);
                int idxEstado = cursorBanos.getColumnIndex(DbHelper.COL_BANO_ESTADO);
                int idxLat = cursorBanos.getColumnIndex(DbHelper.COL_BANO_LATITUD);
                int idxLng = cursorBanos.getColumnIndex(DbHelper.COL_BANO_LONGITUD);

                do {
                    String codigo = (idxCodigo != -1) ? cursorBanos.getString(idxCodigo) : "Baño";
                    String cliente = (idxCliente != -1) ? cursorBanos.getString(idxCliente) : "Cliente";
                    String faena = (idxFaena != -1) ? cursorBanos.getString(idxFaena) : "";
                    String estado = (idxEstado != -1) ? cursorBanos.getString(idxEstado) : "Disponible";
                    double lat = (idxLat != -1) ? cursorBanos.getDouble(idxLat) : 0.0;
                    double lng = (idxLng != -1) ? cursorBanos.getDouble(idxLng) : 0.0;

                    boolean completado = estado.equalsIgnoreCase("Disponible");

                    if (lat != 0.0 && lng != 0.0) {
                        listaPuntosBD.add(new PuntoDestino(
                                codigo + " · " + cliente,
                                faena.isEmpty() ? ("Estado: " + estado) : (faena + " (" + estado + ")"),
                                "🚽 Baño Químico",
                                lat,
                                lng,
                                completado
                        ));
                    }
                } while (cursorBanos.moveToNext());
                cursorBanos.close();
            }
        } catch (Exception ignored) {}

        try {
            Cursor cursorServicios = dbHelper.obtenerTodosLosServicios();
            if (cursorServicios != null && cursorServicios.moveToFirst()) {
                int idxCli = cursorServicios.getColumnIndex(DbHelper.COL_CLIENTE);
                int idxTipo = cursorServicios.getColumnIndex(DbHelper.COL_TIPO);
                int idxDesc = cursorServicios.getColumnIndex(DbHelper.COL_DESCRIPCION);
                int idxOt = cursorServicios.getColumnIndex(DbHelper.COL_OT);
                int idxFirm = cursorServicios.getColumnIndex(DbHelper.COL_FIRMADO);

                do {
                    String cliente = (idxCli != -1) ? cursorServicios.getString(idxCli) : "Cliente";
                    String tipo = (idxTipo != -1) ? cursorServicios.getString(idxTipo) : "Servicio";
                    String desc = (idxDesc != -1) ? cursorServicios.getString(idxDesc) : "";
                    String ot = (idxOt != -1) ? cursorServicios.getString(idxOt) : "OT";
                    boolean completado = (idxFirm != -1) && (cursorServicios.getInt(idxFirm) == 1);

                    LatLng coords = extraerCoordenadasDeTexto(desc);
                    if (coords == null) {
                        double[] coordsCliente = dbHelper.obtenerCoordenadasCliente(cliente);
                        if (coordsCliente != null && (coordsCliente[0] != 0.0 || coordsCliente[1] != 0.0)) {
                            coords = new LatLng(coordsCliente[0], coordsCliente[1]);
                        }
                    }

                    if (coords != null) {
                        listaPuntosBD.add(new PuntoDestino(
                                cliente,
                                tipo + " · " + ot,
                                "🛠 Servicio Registrado",
                                coords.getLatitude(),
                                coords.getLongitude(),
                                completado
                        ));
                    }
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
                int idxEstado = cursorTareas.getColumnIndex(DbHelper.COL_TAREA_ESTADO);

                do {
                    String cliente = (idxCli != -1) ? cursorTareas.getString(idxCli) : "Cliente";
                    String tipo = (idxTipo != -1) ? cursorTareas.getString(idxTipo) : "Tarea";
                    String fecha = (idxFecha != -1) ? cursorTareas.getString(idxFecha) : "";
                    String hora = (idxHora != -1) ? cursorTareas.getString(idxHora) : "";
                    String estado = (idxEstado != -1) ? cursorTareas.getString(idxEstado) : "pendiente";
                    boolean completada = estado.equalsIgnoreCase("Completada");

                    double[] coordsCliente = dbHelper.obtenerCoordenadasCliente(cliente);
                    if (coordsCliente != null && (coordsCliente[0] != 0.0 || coordsCliente[1] != 0.0)) {
                        listaPuntosBD.add(new PuntoDestino(
                                cliente,
                                tipo + " · " + fecha + " " + hora,
                                "📅 Tarea Agendada",
                                coordsCliente[0],
                                coordsCliente[1],
                                completada
                        ));
                    }
                } while (cursorTareas.moveToNext());
                cursorTareas.close();
            }
        } catch (Exception ignored) {}

        actualizarVistaBottomSheet();
    }

    private void actualizarVistaBottomSheet() {
        contenedorItemsBanos.removeAllViews();

        List<PuntoDestino> filtrados = new ArrayList<>();
        for (PuntoDestino p : listaPuntosBD) {
            if (mostrandoEnProceso && !p.completado) {
                filtrados.add(p);
            } else if (!mostrandoEnProceso && p.completado) {
                filtrados.add(p);
            }
        }

        int total = filtrados.size();
        tvBadgeTotalBanos.setText(total + (total == 1 ? " registro" : " registros"));

        if (total == 0) {
            tvSinRegistrosAviso.setVisibility(View.VISIBLE);
            return;
        }

        tvSinRegistrosAviso.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (PuntoDestino punto : filtrados) {
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
                .setTitle("Mi Posición"));

        mapLibreMap.addMarker(new MarkerOptions()
                .position(destino)
                .setTitle(tituloDestino));

        obtenerRutaPorCarretera(ubicacionActualGps, destino, tituloDestino);
    }

    private void obtenerRutaPorCarretera(LatLng origen, LatLng destino, String tituloDestino) {
        new Thread(() -> {
            boolean exito = false;
            String urlStr = String.format(Locale.US,
                    "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                    origen.getLongitude(), origen.getLatitude(),
                    destino.getLongitude(), destino.getLatitude());

            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile) DesiertoTarapacaApp/1.0");
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        sb.append(linea);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(sb.toString());
                    JSONArray routes = json.optJSONArray("routes");

                    if (routes != null && routes.length() > 0) {
                        JSONObject ruta = routes.getJSONObject(0);

                        double distanciaMetros = ruta.getDouble("distance");
                        double distanciaKmReal = distanciaMetros / 1000.0;

                        JSONArray coordinates = ruta.getJSONObject("geometry").getJSONArray("coordinates");
                        List<LatLng> puntosRuta = new ArrayList<>();
                        for (int i = 0; i < coordinates.length(); i++) {
                            JSONArray coord = coordinates.getJSONArray(i);
                            puntosRuta.add(new LatLng(coord.getDouble(1), coord.getDouble(0)));
                        }

                        exito = true;
                        runOnUiThread(() -> {
                            mapLibreMap.addPolyline(new PolylineOptions()
                                    .addAll(puntosRuta)
                                    .color(Color.parseColor("#2D6A4F"))
                                    .width(7f));

                            mapLibreMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                    new CameraPosition.Builder()
                                            .target(destino)
                                            .zoom(13.5)
                                            .build()
                            ), 1000);

                            double litrosEstimados = distanciaKmReal / RENDIMIENTO_KM_POR_LITRO;
                            double costoTotalClp = litrosEstimados * PRECIO_COMBUSTIBLE_CLP;

                            DecimalFormat dfClp = new DecimalFormat("#,###", new DecimalFormatSymbols(new Locale("es", "CL")));
                            DecimalFormat dfDecimal = new DecimalFormat("0.0", new DecimalFormatSymbols(new Locale("es", "CL")));

                            tvValorDistancia.setText(dfDecimal.format(distanciaKmReal) + " km");
                            tvValorCombustible.setText(dfDecimal.format(litrosEstimados) + " L");
                            tvValorCostoClp.setText("$" + dfClp.format(costoTotalClp));
                            tvRutaDetallePie.setText(tituloDestino + " · Conducción por calles · OSRM");

                            layoutEstadoInicialTrayecto.setVisibility(View.GONE);
                            layoutRutaActivaMetricas.setVisibility(View.VISIBLE);
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!exito) {
                runOnUiThread(() -> trazarRutaDirectaFallback(origen, destino, tituloDestino));
            }
        }).start();
    }

    private void trazarRutaDirectaFallback(LatLng origen, LatLng destino, String tituloDestino) {
        mapLibreMap.addPolyline(new PolylineOptions()
                .add(origen, destino)
                .color(Color.parseColor("#2D6A4F"))
                .width(6f));

        double distanciaKm = calcularDistanciaHaversine(origen.getLatitude(), origen.getLongitude(), destino.getLatitude(), destino.getLongitude());
        double distanciaAproxCarretera = distanciaKm * 1.35;
        double litrosEstimados = distanciaAproxCarretera / RENDIMIENTO_KM_POR_LITRO;
        double costoTotalClp = litrosEstimados * PRECIO_COMBUSTIBLE_CLP;

        DecimalFormat dfClp = new DecimalFormat("#,###", new DecimalFormatSymbols(new Locale("es", "CL")));
        DecimalFormat dfDecimal = new DecimalFormat("0.0", new DecimalFormatSymbols(new Locale("es", "CL")));

        tvValorDistancia.setText(dfDecimal.format(distanciaAproxCarretera) + " km");
        tvValorCombustible.setText(dfDecimal.format(litrosEstimados) + " L");
        tvValorCostoClp.setText("$" + dfClp.format(costoTotalClp));
        tvRutaDetallePie.setText(tituloDestino + " · Estimado");

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

    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        cargarPuntosDesdeBaseDeDatos();
    }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
}