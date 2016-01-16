package fragment;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anhdatdev.appuser.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import task.Task;

/**
 * Created by dokhacdat on 15/01/16.
 */
public class RouteFragment extends Fragment {

    private MapView mapView;
    private GoogleMap map;
    private ArrayList<LatLng> lstLat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lstLat = new ArrayList<>();
        addDataLatLng();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_route, container, false);
        mapView = (MapView) rootView.findViewById(R.id.map_view_route);
        mapView.onCreate(savedInstanceState);
        map = mapView.getMap();
        MapsInitializer.initialize(getActivity());


        for (LatLng l : lstLat) {
            addMarkerDraw(l);
            try {
                new Task(getActivity(),l, lstLat.get(lstLat.indexOf(l) + 1),map).execute();
            } catch (Exception e) {
            }
        }

        return rootView;
    }

    public void addMarkerDraw(LatLng loc) {
        //mMap.clear();
        map.addMarker(new MarkerOptions().position(loc));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14.0f));
    }

    private void addDataLatLng() {
        lstLat.add(new LatLng(21.049155, 105.785707));
        lstLat.add(new LatLng(21.058800, 105.783052));
        lstLat.add(new LatLng(21.021721, 105.767900));
        lstLat.add(new LatLng(21.037220, 105.785665));
        lstLat.add(new LatLng(21.008198, 105.792929));
        lstLat.add(new LatLng(21.015605, 105.795845));
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}