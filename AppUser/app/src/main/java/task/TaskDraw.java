package task;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import fragment.HomeFragment;

/**
 * Created by dokhacdat on 15/01/16.
 */
public class TaskDraw extends AsyncTask<String, PolylineOptions, Void> {

    private GoogleMap map;
    private LatLng latLngFrom;
    private LatLng latLngTo;
    private PolylineOptions rectLine = new PolylineOptions().width(15).color(
            Color.BLUE);

    public TaskDraw(LatLng latLngFrom, LatLng latLngTo, GoogleMap _map) {
        this.latLngFrom = latLngFrom;
        this.latLngTo = latLngTo;
        this.map = _map;
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
        Polyline polylin = map.addPolyline(values[0]);
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
}
