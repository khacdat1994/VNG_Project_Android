package fragment;

import android.Manifest;
import android.app.AlertDialog;

import android.app.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.anhdatdev.appuser.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import gps.LocationProvider;
import task.Task;
import task.TaskGetAddress;

/**
 * Created by dokhacdat on 15/01/16.
 */
public class HomeFragment extends Fragment implements LocationProvider.LocationCallback {

    MapView mapView;
    GoogleMap map;
    private TextView txtFrom, txtTo;
    private Button btnOrder;
    private LocationProvider mLocationProvider;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE_FROM = 1;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE_TO = 2;


    HashMap<String, Marker> hashMarker = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mapView = (MapView) rootView.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        MapsInitializer.initialize(getActivity());
//        map.getUiSettings().setZoomControlsEnabled(true);
//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return null;
//        }
//        map.setMyLocationEnabled(true);


        txtFrom = (TextView) rootView.findViewById(R.id.txtFrom);
        txtTo = (TextView) rootView.findViewById(R.id.txtTo);
        btnOrder = (Button) rootView.findViewById(R.id.btnOrder);

        mLocationProvider = new LocationProvider(getActivity(), this);
        mLocationProvider.connect();

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


        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });


        return rootView;
    }

    private void sentIntent(int _request) {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(getActivity());
            startActivityForResult(intent, _request);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }


//    private void addMarker(Place _place, String _str) {
//        LatLng loc = _place.getLatLng();
//        //mMap.clear();
//        map.addMarker(new MarkerOptions().position(loc).title(_str).snippet((String) _place.getAddress()));
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14.0f));
//    }

    //


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_FROM) {

                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                txtFrom.setText(place.getAddress());
                if (hashMarker.get("MarkerFrom") != null) {
                    hashMarker.get("MarkerFrom").remove();
                }

                Marker mk = addMarkers(place.getLatLng(), "Điểm đi", place.getAddress().toString());
                hashMarker.put("MarkerFrom", mk);

                map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        hashMarker.get("MarkerFrom").remove();
                        Marker marker = addMarkers(latLng, "Điểm đi", "Đánh dấu bằng tay");
                        hashMarker.put("MarkerFrom", marker);
                        try {
                            txtFrom.setText(new TaskGetAddress(getActivity()).execute(latLng).get());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                        }
                    }
                });

            }
            if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_TO) {

                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                txtTo.setText(place.getAddress());
                if (hashMarker.get("MarkerTo") != null) {
                    hashMarker.get("MarkerTo").remove();
                }
                Marker mk = addMarkers(place.getLatLng(), "Điểm đến", place.getAddress() + "");
                hashMarker.put("MarkerTo", mk);
                // new Task(getActivity(), hashMap.get("from"), hashMap.get("to"), map).execute();

                map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        hashMarker.get("MarkerTo").remove();
                        Marker marker = addMarkers(latLng, "Điểm đến", "Đánh dấu bằng tay");
                        hashMarker.put("MarkerTo", marker);
                        try {
                            txtTo.setText(new TaskGetAddress(getActivity()).execute(latLng).get());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                        }
                    }
                });
            }
        } catch (Exception e) {
        }
    }

    // XU LY THOI GIAN
    private Calendar calendar;
    private DateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    public void showDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.dialog_select, null);
        final EditText edtTime = (EditText) dialoglayout.findViewById(R.id.edtTime);
        final EditText edtSL = (EditText) dialoglayout.findViewById(R.id.edtSL);
        final EditText edtLoaiXe = (EditText) dialoglayout.findViewById(R.id.edtLoaiXe);

        final String TIME_PATTERN = "HH:mm";


        calendar = Calendar.getInstance();
        dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        timeFormat = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());


        edtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {

                        calendar.set(year, monthOfYear, dayOfMonth);
                        updateTime(edtTime);

                        TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                updateTime(edtTime);
                            }
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show(getFragmentManager(), "timePicker");
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show(getFragmentManager(), "datePicker");
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialoglayout);
        builder.setTitle("Thông tin đặt xe");
        builder.setNegativeButton("Xác nhận đặt", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), "Thông tin đặt xe: \n" + "Điểm đi: " + hashMarker.get("MarkerFrom").getPosition().latitude + ", " + hashMarker.get("MarkerFrom").getPosition().longitude + "\n" + "Điểm đến:" + hashMarker.get("MarkerTo").getPosition().latitude + ", " + hashMarker.get("MarkerTo").getPosition().longitude + "\n" + "Ngày giờ:  " + calendar.getTimeInMillis() + "\n" + "Số lượng: " + edtSL.getText() + "\n" + "Loại xe:", Toast.LENGTH_LONG).show();
            }
        });
        builder.setPositiveButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();


    }

    private void updateTime(EditText _edt) {
        _edt.setText(dateFormat.format(calendar.getTime()) + ", " + timeFormat.format(calendar.getTime()));
    }


    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationProvider.disconnect();
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


    @Override
    public void handleNewLocation(Location location) {
        //map.clear();
        try {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            String add = new TaskGetAddress(getActivity()).execute(loc).get().toString();
            txtFrom.setText(add);
            Marker mk = addMarkers(loc, "Vị trí của bạn", add);
            hashMarker.put("MarkerFrom", mk);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14.0f));
//

    }

    private Marker addMarkers(LatLng _lat, String _title, String _snippet) {
        Marker mk = null;
        MarkerOptions option = new MarkerOptions();
        option.title(_title);
        option.snippet(_snippet);
        option.position(_lat);
        // option.icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker));
        mk = map.addMarker(option);
        mk.showInfoWindow();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(_lat, 14.0f));
        return mk;
    }


}
