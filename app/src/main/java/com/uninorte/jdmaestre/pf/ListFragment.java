package com.uninorte.jdmaestre.pf;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gimbal.android.BeaconManager;
import com.gimbal.android.PlaceManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose on 17/04/2015.
 */
public class ListFragment extends Fragment {

    boolean sw = false;
    LocationListener locationListener;
    private BeaconManager beaconManager;


    ArrayList<DireccionSucursal> sucursales = new ArrayList<DireccionSucursal>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        LocationManager locationManager1 = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                LocationManager locationManager1 = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                String locationProvider = LocationManager.GPS_PROVIDER;
                final Location lastKnownLocation = locationManager1.getLastKnownLocation(locationProvider);



                if(lastKnownLocation != null && isNetworkAvailable()){

                    ParseGeoPoint userLocation = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Sucursales");
                    query.whereNear("Ubicacion", userLocation);
                    query.setLimit(10);


                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            if(e == null){

                                for(int n=0;n<parseObjects.size();n++){
                                    ParseObject object = parseObjects.get(n);
                                    String Nombre = object.getString("Nombre");
                                    String Direccion = object.getString("Direccion");
                                    String id = object.getString("idEmpresa");
                                    sucursales.add(new DireccionSucursal(Nombre, Direccion, id));

                                    ListView list = (ListView)getView().findViewById(R.id.sucursalesListView);

                                    final DireccionSucursalAdapter adapter = new DireccionSucursalAdapter(getActivity(), sucursales);
                                    list.setAdapter(adapter);

                                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            Intent intent = new Intent(getActivity(), LocalActivity.class);
                                            intent.putExtra("id", sucursales.get(position).id);
                                            startActivity(intent);

                                        }
                                    });

                                }
                                LocationManager locationManager1 = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                                locationManager1.removeUpdates(locationListener);


                            }else{

                            }
                        }

                    });

                }

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {

            }

            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        String locationProvider = LocationManager.NETWORK_PROVIDER;


        locationManager1.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5 , locationListener);



    }

    public void onResume(){
        super.onResume();



    }

    @Override
    public void onPause() {
        super.onPause();

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);

    }

    class DireccionSucursal{

        private String direccion;
        private String title;
        private String id;

        public DireccionSucursal (String title, String direccion,String id){
            this.title = title;
            this.direccion = direccion;
            this.id = id;
        }

        public void setTitle(String title){this.title = title;}
        public void setDireccion(String direccion){this.direccion = direccion;}
        public void setId(String id){this.id = id;}

        public String getTitle(){return title;}
        public String getDireccion() {return direccion;}
        public String getId(){return id;}

    }

    class DireccionSucursalAdapter extends BaseAdapter {

        private ArrayList<DireccionSucursal> listadoSucursales;
        private LayoutInflater inflater;

        public DireccionSucursalAdapter(Context context, ArrayList<DireccionSucursal> sucursales){

            this.inflater = LayoutInflater.from(context);
            this.listadoSucursales = sucursales;

        }


        @Override
        public int getCount() {
            return listadoSucursales.size();
        }

        @Override
        public Object getItem(int i) {
            return listadoSucursales.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            ContenedorView contenedor ;

            if (view == null){

                view = inflater.inflate(R.layout.list_fragment_item, null);

                contenedor = new ContenedorView();
                contenedor.tite = (TextView) view.findViewById(R.id.sucursalesTitle);
                contenedor.direcccion= (TextView) view.findViewById(R.id.sucursalesDireccion);

                view.setTag(contenedor);

            }else {
                contenedor = (ContenedorView) view.getTag();
            }

            DireccionSucursal sucursales = (DireccionSucursal) getItem(i);
            contenedor.tite.setText(sucursales.getTitle());
            contenedor.direcccion.setText(sucursales.getDireccion());


            return view;



        }

        class ContenedorView{
            TextView tite;
            TextView direcccion;
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(MainActivity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isNetworkAvaible = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isNetworkAvaible = true;

        } else {
            Toast.makeText(getActivity(), "No hay red disponible ", Toast.LENGTH_LONG)
                    .show();
        }
        return isNetworkAvaible;
    }
}




