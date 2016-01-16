package task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;

import com.anhdatdev.appuser.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by dokhacdat on 15/01/16.
 */
public class Task extends AsyncTask<String, Void, String> {

    private Context context;
    private GoogleMap map;
    private LatLng latLngFrom;
    private LatLng latLngTo;

    public Task(Context _context,LatLng latLngFrom, LatLng latLngTo, GoogleMap _map) {
        this.latLngFrom = latLngFrom;
        this.latLngTo = latLngTo;
        this.map = _map;
        this.context=_context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        String jsonString = "";
        try {
            URL url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=" + latLngFrom.latitude + "," + latLngFrom.longitude + "&destination=" + latLngTo.latitude + "," + latLngTo.longitude + "&mode=driving&key=" + context.getResources().getString(R.string.google_maps_server_key));
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
        new TaskDraw(latLngFrom, latLngTo, map).execute(s);

    }

}
