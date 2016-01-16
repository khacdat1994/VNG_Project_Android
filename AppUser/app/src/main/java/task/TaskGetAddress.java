package task;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by dokhacdat on 16/01/16.
 */
public class TaskGetAddress extends AsyncTask<LatLng, Void, String> {

    private Context context;

    public TaskGetAddress(Context _con) {
        context = _con;
    }

    private String getAddress(LatLng _lat) {
        Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);
        String str = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(_lat.latitude,
                    _lat.longitude, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();
                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress
                            .append(returnedAddress.getAddressLine(i)).append(
                            ", ");
                }
                str = strReturnedAddress.substring(0, strReturnedAddress.lastIndexOf(", ")).toString();
            } else {
                str = _lat.latitude + ", " + _lat.longitude;
            }
        } catch (IOException e) {
            e.printStackTrace();
            str = _lat.latitude + ", " + _lat.longitude;
        }

        return str;
    }

    @Override
    protected String doInBackground(LatLng... params) {
        String str = getAddress(params[0]);
        return str;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
