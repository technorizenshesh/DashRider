package main.com.dash.activity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import main.com.dash.R;
import main.com.dash.constant.GPSTracker;
import main.com.dash.constant.MyLanguageSession;
import main.com.dash.draglocation.MyTask;
import main.com.dash.draglocation.WebOperations;

public class SetLocation extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        ResultCallback<LocationSettingsResult> {
    private Integer THRESHOLD = 2;
    private int count = 0;
    private GoogleMap gMap;
    GPSTracker gpsTracker;
    private double longitude = 0.0, latitude = 0.0;
    int initial_flag = 0;
    String address_complete = "";
    private AutoCompleteTextView pickuplocation;
    public static LatLng originlatlong;
    public static String pickuplocation_str = "";
    private TextView done_loc;
    private RelativeLayout exit_app_but;
    public static int LOCSTS = 0, SETSTS = 0;
    private String pickuploc_str = "";
    private ImageView gpslocator,clear_pick_ic;
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 0; // in Milliseconds
    Location location;
    Location location_ar;
    LocationManager locationManager;
    private GoogleApiClient googleApiClient;
    GPSTracker tracker;
    ProgressBar prgressbar;
    boolean click_sts = false;
    private String language = "";
    MyLanguageSession myLanguageSession;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myLanguageSession = new MyLanguageSession(this);
        language = myLanguageSession.getLanguage();
        myLanguageSession.setLangRecreate(myLanguageSession.getLanguage());

        setContentView(R.layout.activity_set_location);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (myLanguageSession.getLanguage().equalsIgnoreCase("ar")) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            } else {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }

        }

        tracker = new GPSTracker(this);

        getLatLong();
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
        } else {
            String sts = bundle.getString("setLoc");
            if (sts.equalsIgnoreCase("dropoff")) {
                SETSTS = 3;
            } else if (sts.equalsIgnoreCase("pickup")) {
                SETSTS = 4;
            }
        }
        idinits();
        autocompleteView();
        clickevetn();
        getCurrentLocation();
        //  checkGps();
        try {
            // Loading map
            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //    updateLocationUI();

    }

    @Override
    protected void onResume() {
        super.onResume();
        myLanguageSession.setLangRecreate(myLanguageSession.getLanguage());
        String oldLanguage = language;
        language = myLanguageSession.getLanguage();
        if (!oldLanguage.equals(language)) {
            finish();
            startActivity(getIntent());
        }
    }


    private void getLatLong() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                 return;
        }
        location_ar = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, new MyLocationListener());
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (location_ar != null) {
            //Getting longitude and latitude
            longitude = location_ar.getLongitude();
            latitude = location_ar.getLatitude();
            System.out.println("-------------getCurrentLocation--------------" + latitude + " , " + latitude);
            address_complete = loadAddress(latitude, longitude);
            pickuplocation.setText(address_complete);


        } else {
            System.out.println("----------------geting Location from GPS----------------");

            location_ar = tracker.getLocation();
            if (location_ar == null) {
                latitude = SplashActivity.latitude;
                longitude = SplashActivity.longitude;

                address_complete = loadAddress(latitude, longitude);
                pickuplocation.setText(address_complete);

            } else {
                longitude = location_ar.getLongitude();
                latitude = location_ar.getLatitude();
                Log.e("Lat >>", "GPS " + latitude);
                if (latitude == 0.0) {
                    latitude = SplashActivity.latitude;
                    longitude = SplashActivity.longitude;

                }
                address_complete = loadAddress(latitude, longitude);
                pickuplocation.setText(address_complete);
            }
            System.out.println("-------------getCurrentLocation--------------" + latitude + " , " + longitude);
            //moving the map to location

        }
    }

    private void autocompleteView() {
        pickuplocation.setThreshold(THRESHOLD);
        pickuplocation.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    clear_pick_ic.setVisibility(View.VISIBLE);
                    loadData(pickuplocation.getText().toString());
                }
                else {
                    clear_pick_ic.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadData(String s) {
        try {
            if (count == 0) {
                List<String> l1 = new ArrayList<>();
                if (s == null) {

                } else {
                    l1.add(s);

                    GeoAutoCompleteAdapter ga = new GeoAutoCompleteAdapter(SetLocation.this, l1, "" + latitude, "" + longitude);
                    pickuplocation.setAdapter(ga);

                }

            }
            count++;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void clickevetn() {
        done_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pickuplocation_str.equalsIgnoreCase("")) {
                    Toast.makeText(SetLocation.this, "Please Select Location", Toast.LENGTH_LONG).show();
                } else {
                    finish();
                    LOCSTS = 1;
                }

            }
        });
        clear_pick_ic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickuplocation.setText("");
            }
        });
        exit_app_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        gpslocator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gMap == null) {

                } else {
                    Location loc = gMap.getMyLocation();
                    if (loc != null) {
                        LatLng latLang = new LatLng(loc.getLatitude(), loc
                                .getLongitude());
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLang, 17);
                        gMap.animateCamera(cameraUpdate);
                        address_complete = loadAddress(latitude, longitude);
                        pickuplocation.setText(address_complete);

                    }

                }
            }
        });
    }

    private void idinits() {
        prgressbar = (ProgressBar) findViewById(R.id.prgressbar);
        gpslocator = (ImageView) findViewById(R.id.gpslocator);
        clear_pick_ic = (ImageView) findViewById(R.id.clear_pick_ic);
        pickuplocation = (AutoCompleteTextView) findViewById(R.id.pickuplocation);
        exit_app_but = (RelativeLayout) findViewById(R.id.exit_app_but);
        done_loc = (TextView) findViewById(R.id.done_loc);

    }

    private void initilizeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        if (ActivityCompat.checkSelfPermission(SetLocation.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SetLocation.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        gMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        SetLocation.this, R.raw.stylemap_3));

        gMap.setMyLocationEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(false);

        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).flat(true).anchor(0.5f, 0.5f);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        // gMap.addMarker(marker).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pin_pngs));
        gMap.animateCamera(cameraUpdate);
        pickuplocation_str = loadAddress(latLng.latitude, latLng.longitude);
        originlatlong = new LatLng(latLng.latitude, latLng.longitude);

        gMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                System.out.println("Camera idle worked");
                if (initial_flag != 0) {


                    Log.e("CenterLatLng", ">>>>" + gMap.getCameraPosition().target);

                    LatLng latLng = gMap.getCameraPosition().target;
                    //  getAddress(latLng.latitude,latLng.longitude);
                    if (!click_sts){
                        address_complete = loadAddress(latLng.latitude, latLng.longitude);
                        pickuplocation.setText(address_complete);
                        pickuplocation_str = loadAddress(latLng.latitude, latLng.longitude);
                        originlatlong = new LatLng(latLng.latitude, latLng.longitude);
                    }
                    else {
                        click_sts =false;
                    }


                }
                initial_flag++;

            }
        });


    }

    private void checkGps() {
        gpsTracker = new GPSTracker(this);
        if (gpsTracker.canGetLocation()) {

            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();

            Log.e("FindTruck Latitude", "" + latitude);
            Log.e("FindTruck longitude", "" + longitude);
        } else {
            // if gps off get lat long from network
            //   locationfromnetwork();
            gpsTracker.showSettingsAlert();
        }


    }

    private String loadAddress(double latitude, double longitude) {
        try {
            WebOperations wo = new WebOperations(SetLocation.this.getApplicationContext());
            wo.setUrl("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key="+getResources().getString(R.string.googlekey_other));
            String str = new MyTask(wo, 3).execute().get();
            JSONObject jk = new JSONObject(str);
            JSONArray results = jk.getJSONArray("results");
            JSONObject jk1 = results.getJSONObject(0);
            String add1 = jk1.getString("formatted_address");
            return add1;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    class GeoAutoCompleteAdapter extends BaseAdapter implements Filterable {

        private Activity context;
        private List<String> l2 = new ArrayList<>();
        private LayoutInflater layoutInflater;
        private WebOperations wo;
        private String lat, lon;

        public GeoAutoCompleteAdapter(Activity context, List<String> l2, String lat, String lon) {
            this.context = context;
            this.l2 = l2;
            this.lat = lat;
            this.lon = lon;
            layoutInflater = LayoutInflater.from(context);
            wo = new WebOperations(context);
        }

        @Override
        public int getCount() {

            return l2.size();
        }

        @Override
        public Object getItem(int i) {
            return l2.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            view = layoutInflater.inflate(R.layout.geo_search_result, viewGroup, false);
            TextView geo_search_result_text = (TextView) view.findViewById(R.id.geo_search_result_text);
            try {
                geo_search_result_text.setText(l2.get(i));
                geo_search_result_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        InputMethodManager inputManager = (InputMethodManager)
                                getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);

                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                        pickuplocation_str = l2.get(i);
                        pickuplocation.setText(""+pickuplocation_str);
                        pickuplocation.dismissDropDown();
                        click_sts = true;
                        new GetPickUp().execute();


                    }
                });

            } catch (Exception e) {

            }

            return view;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                       // wo.setUrl("https://maps.googleapis.com/maps/api/place/autocomplete/json?key="+getResources().getString(R.string.google_search)+"&input=" + constraint.toString().trim().replaceAll(" ", "+") + "&location=" + lat + "," + lon + "+&radius=20000&types=geocode&sensor=true");
                        wo.setUrl("https://maps.googleapis.com/maps/api/place/autocomplete/json?key="+ getResources().getString(R.string.googlekey_other) +"&input=" + constraint.toString().trim().replaceAll(" ", "+") + "&location=" + lat + "," + lon + "+&radius=20000&sensor=true");

                        String result = null;
                        try {
                            result = new MyTask(wo, 3).execute().get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        parseJson(result);

                        System.out.println("FilterResults===============================" + result);
                        Log.e("Location===========", "Come" + result);
                        // Assign the data to the FilterResults
                        filterResults.values = l2;
                        filterResults.count = l2.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    System.out.println("publishResults===============================" + results);
                    if (results != null && results.count != 0) {
                        l2 = (List) results.values;
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }

        private void parseJson(String result) {
            try {
                l2 = new ArrayList<>();
                JSONObject jk = new JSONObject(result);
                Log.e("Check this GACA jk", ">>>" + jk);
                JSONArray predictions = jk.getJSONArray("predictions");
                for (int i = 0; i < predictions.length(); i++) {
                    JSONObject js = predictions.getJSONObject(i);
                    l2.add(js.getString("description"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private List<Address> findLocations(Context context, String query_text) {

            List<Address> geo_search_results = new ArrayList<Address>();

            Geocoder geocoder = new Geocoder(context, context.getResources().getConfiguration().locale);
            List<Address> addresses = null;

            try {
                // Getting a maximum of 15 Address that matches the input text
                addresses = geocoder.getFromLocationName(query_text, 15);


            } catch (IOException e) {
                e.printStackTrace();
            }

            return addresses;
        }
    }

    private class GetPickUp extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            try {
                super.onPreExecute();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String address = pickuplocation_str.trim().replaceAll(" ", "+");
            Log.e("event_loc_str >>>", "" + pickuploc_str);
            String postReceiverUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key="+getResources().getString(R.string.googlekey_other);

            try {
                //  String postReceiverUrl = "https://api.ctlf.co.uk/";
                URL url = new URL(postReceiverUrl);
                Map<String, Object> params = new LinkedHashMap<>();


                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                String urlParameters = postData.toString();
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

                writer.write(urlParameters);
                writer.flush();

                String response = "";
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                writer.close();
                reader.close();
                Log.e("JsonShyam", ">>>>>>>>>>>>" + response);


                return response;

            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result == null) {

            } else if (result.equalsIgnoreCase("")) {

            } else {
                JSONObject location = null;
                try {
                    location = new JSONObject(result).getJSONArray("results")
                            .getJSONObject(0).getJSONObject("geometry")
                            .getJSONObject("location");


                    //    pickup_lat_str,pickup_lon_str,drop_lat_str,drop_lon_str,
                    double pickup_lat_str = location.getDouble("lat");
                    double pickup_lon_str = location.getDouble("lng");
                    LatLng latLng = new LatLng(pickup_lat_str, pickup_lon_str);
                    MarkerOptions marker = new MarkerOptions().position(new LatLng(pickup_lat_str, pickup_lon_str)).flat(true).anchor(0.5f, 0.5f);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                    gMap.animateCamera(cameraUpdate);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
    }


    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }




}
//http://mobileappdevelop.co/NAXCAN/webservice/favorite_location?user_id=1&location=2&lat=3&lon=4&type=5

/*
*/