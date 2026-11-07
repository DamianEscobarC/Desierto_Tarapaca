package com.example.desiertotarapaca;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;

public class SeleccionarUbicacionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private MapLibreMap mapLibreMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapLibre.getInstance(this);
        setContentView(R.layout.activity_seleccionar_ubicacion);

        mapView = findViewById(R.id.mapViewSelector);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        findViewById(R.id.btnVolverSelector).setOnClickListener(v -> finish());

        findViewById(R.id.btnConfirmarPuntoMapa).setOnClickListener(v -> {
            if (mapLibreMap != null) {
                LatLng puntoSeleccionado = mapLibreMap.getCameraPosition().target;

                Intent resultado = new Intent();
                resultado.putExtra("LATITUD_ELEGIDA", puntoSeleccionado.getLatitude());
                resultado.putExtra("LONGITUD_ELEGIDA", puntoSeleccionado.getLongitude());
                setResult(RESULT_OK, resultado);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap map) {
        this.mapLibreMap = map;

        map.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(-20.2208, -70.1390))
                .zoom(13.0)
                .build());

        map.setStyle(new Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty"));
    }

    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}