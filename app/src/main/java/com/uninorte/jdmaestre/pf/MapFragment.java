package com.uninorte.jdmaestre.pf;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by Jose on 17/04/2015.
 */
public class MapFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    protected String TAG = MainActivity.class.getSimpleName();
    protected static Context mContext;
    protected MarkerOptions myPosition;
    LatLng a;

    LocationListener locationListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();


        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
       // googleMap.getUiSettings().isZoomControlsEnabled();
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                SharedPreferences locationP = getActivity().getSharedPreferences("location", 0);
                Double lat = Double.longBitsToDouble(locationP.getLong("latitud",0));
                Double lon = Double.longBitsToDouble(locationP.getLong("longitud",0));

                if(lat != null && lon != null){
                    a = new LatLng(lat,lon );

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(a)    // Sets the center of the map to Mountain View
                            .zoom(15)                   // Sets the zoom
                                    //.bearing(90)                // Sets the orientation of the camera to east
                            .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),2000,null);

                    googleMap.setMyLocationEnabled(true);

                }

            }
        });







        ParseQuery<ParseObject> query = ParseQuery.getQuery("Sucursales");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null){
                    for (int n=0;n<parseObjects.size();n++) {
                        ParseObject object = parseObjects.get(n);
                        String nom = object.getString("Nombre");
                        String dir = object.getString("Direccion");
                        ParseGeoPoint point = object.getParseGeoPoint("Ubicacion");
                        LatLng latLng = new LatLng(point.getLatitude(),point.getLongitude());
                        googleMap.addMarker(new MarkerOptions().position(latLng).title(nom).snippet(dir));

                    }
                }else{

                }
            }
        });


       googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
           @Override
           public void onInfoWindowClick(Marker marker) {

               ParseQuery<ParseObject> query = ParseQuery.getQuery("Sucursales");
               query.whereEqualTo("Direccion",marker.getSnippet());
               query.whereEqualTo("Nombre",marker.getTitle());
               query.findInBackground(new FindCallback<ParseObject>() {
                   @Override
                   public void done(List<ParseObject> parseObjects, ParseException e) {
                       if (e == null){
                           for (int n=0;n<parseObjects.size();n++) {
                               ParseObject object = parseObjects.get(n);
                               String id = object.getString("idEmpresa");
                               Intent intent = new Intent(getActivity(), LocalActivity.class);
                               intent.putExtra("id", id);
                               startActivity(intent);

                           }
                       }else{

                       }
                   }
               });
           }
       });



        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                a = new LatLng(location.getLatitude(),location.getLongitude());

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(a)    // Sets the center of the map to Mountain View
                        .zoom(15)                   // Sets the zoom
                                //.bearing(90)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),2000,null);
                googleMap.setMyLocationEnabled(true);

                SharedPreferences locationP = getActivity().getSharedPreferences("location",0);
                SharedPreferences.Editor editor = locationP.edit();
                editor.putLong("latitud",Double.doubleToLongBits(location.getLatitude()) );
                editor.putLong("longitud", Double.doubleToLongBits(location.getLongitude()));
                editor.commit();


            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {

            }

            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        String locationProvider = LocationManager.NETWORK_PROVIDER;


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 30 , locationListener);





    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();


        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



}
