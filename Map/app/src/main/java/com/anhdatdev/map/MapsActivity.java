package com.anhdatdev.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import gps.LocationProvider;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView txtFrom, txtTo;
    private LocationProvider mLocationProvider;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE_FROM = 1;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE_TO = 2;
    private ArrayList<LatLng> lstLat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        txtFrom = (TextView) findViewById(R.id.txtFrom);
        txtTo = (TextView) findViewById(R.id.txtTo);
        lstLat = new ArrayList<>();

        txtFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sentIntent(PLACE_AUTOCOMPLETE_REQUEST_CODE_FROM);
            }
        });

        txtTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sentIntent(PLACE_AUTOCOMPLETE_REQUEST_CODE_TO);
            }
        });
        addDataLatLng();
    }


    private void addDataLatLng() {
        lstLat.add(new LatLng(21.049155, 105.785707));
        lstLat.add(new LatLng(21.058800, 105.783052));
        lstLat.add(new LatLng(21.021721, 105.767900));
        lstLat.add(new LatLng(21.037220, 105.785665));
        lstLat.add(new LatLng(21.008198, 105.792929));
        lstLat.add(new LatLng(21.015605, 105.795845));
    }

    HashMap<String, LatLng> hashMap = new HashMap<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LatLng latLngFrom = null;
        LatLng latLngTo = null;
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_FROM) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                //Log.i(TAG, "Place: " + place.getName());
                txtFrom.setText(place.getAddress());
                addMarker(place, "Điểm đi");
                latLngFrom = place.getLatLng();
                hashMap.put("from", latLngFrom);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                //Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_TO) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                //Log.i(TAG, "Place: " + place.getName());
                txtTo.setText(place.getAddress());
                addMarker(place, "Điểm đến");
                latLngTo = place.getLatLng();
                hashMap.put("to", latLngTo);

                new Task(hashMap.get("from"), hashMap.get("to")).execute();


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                //Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }


        }
    }

    private void addMarkerDraw(LatLng loc) {

        //mMap.clear();
        mMap.addMarker(new MarkerOptions().position(loc));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14.0f));
    }

    private void addMarker(Place _place, String _str) {
        LatLng loc = _place.getLatLng();
        //mMap.clear();
        mMap.addMarker(new MarkerOptions().position(loc).title(_str).snippet((String) _place.getAddress()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14.0f));
    }

    private void sentIntent(int _request) {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, _request);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setPadding(0, 400, 0, 0);

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Location currentLoc = mMap.getMyLocation();
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude())).title("Điểm đi"));

                for (LatLng l : lstLat) {
                    addMarkerDraw(l);
                    try {
                        //addMarkerDraw(l);
                        new Task(l, lstLat.get(lstLat.indexOf(l) + 1)).execute();
                    } catch (Exception e) {
                    }
                }

                return false;
            }
        });


    }

    private ArrayList<LatLng> decodePoly(String encoded) {

        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private void drawMap(LatLng _latFrom, LatLng _latTo, String _polyline_overview) {

        ArrayList<LatLng> lst = new ArrayList<>();
        lst.add(_latFrom);
        //
        ArrayList<LatLng> poly1 = decodePoly(_polyline_overview);

        for (LatLng l : poly1) {
            lst.add(l);
        }

        lst.add(_latTo);


        PolylineOptions rectLine = new PolylineOptions().width(10).color(
                Color.RED);

        //for (int i = 0; i < directionPoint.size(); i++) {
        for (LatLng lat : lst) {
            rectLine.add(lat);
        }

        //}
        Polyline polylin = mMap.addPolyline(rectLine);
    }


    private class Task extends AsyncTask<String, Void, String> {

        private LatLng latLngFrom;
        private LatLng latLngTo;

        public Task(LatLng latLngFrom, LatLng latLngTo) {
            this.latLngFrom = latLngFrom;
            this.latLngTo = latLngTo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            String jsonString = "";
            try {
                URL url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=" + latLngFrom.latitude + "," + latLngFrom.longitude + "&destination=" + latLngTo.latitude + "," + latLngTo.longitude + "&mode=driving&key=" + getResources().getString(R.string.maps_server_key));
                URLConnection connection = url.openConnection();
                connection.connect();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                jsonString = bufferedReader.readLine();

                while (bufferedReader.read() != -1) {
                    jsonString += bufferedReader.readLine();
                }

            } catch (Exception e) {

            }

            String str = "";
            try {
                JSONObject jsonObject = new JSONObject(jsonString + "}");
                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                JSONObject obj = jsonArray.getJSONObject(0);
                JSONObject obj1 = obj.getJSONObject("overview_polyline");
                str = obj1.optString("points");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Toast.makeText(getApplication(),s,Toast.LENGTH_LONG).show();
            new TaskDraw(latLngFrom, latLngTo).execute(s);

        }
    }

    private class TaskDraw extends AsyncTask<String, PolylineOptions, Void> {
        private LatLng latLngFrom;
        private LatLng latLngTo;
        private PolylineOptions rectLine = new PolylineOptions().width(15).color(
                Color.BLUE);

        public TaskDraw(LatLng latLngFrom, LatLng latLngTo) {
            this.latLngFrom = latLngFrom;
            this.latLngTo = latLngTo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            ArrayList<LatLng> lst = new ArrayList<>();
            lst.add(latLngFrom);
            //
            ArrayList<LatLng> poly = decodePoly(params[0]);
            for (LatLng l : poly) {
                lst.add(l);
            }
            //
            lst.add(latLngTo);

            for (LatLng lat : lst) {
                rectLine.add(lat);
                publishProgress(rectLine);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(PolylineOptions... values) {
            super.onProgressUpdate(values);
            Polyline polylin = mMap.addPolyline(values[0]);
        }

    }


}
