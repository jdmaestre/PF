package com.uninorte.jdmaestre.pf;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.gimbal.android.BeaconEventListener;
import com.gimbal.android.BeaconManager;
import com.gimbal.android.BeaconSighting;
import com.gimbal.android.Communication;
import com.gimbal.android.CommunicationListener;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Push;
import com.gimbal.android.Visit;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.gimbal.android.Gimbal;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Jose on 16/04/2015.
 */
public class PFApplication extends Application {

    private PlaceEventListener placeEventListener;
    private CommunicationListener communicationListener;
    private BeaconEventListener beaconSightingListener;
    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();

        Gimbal.setApiKey(this, "3b59db3e-d6f4-45fc-8595-9cacc3474c7c");
        Gimbal.registerForPush("665825697637");


        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "1meX4eoaUmA1quVUqVWN8TW0JtGaQLUip4xylIdW", "bwFtDiaj6Hrf3rzFWhgBzDvGRBnPTMYuhWcolUsT");
        //ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseTwitterUtils.initialize("jtXfc6pPQY22Z1DwCfhD4lhiT", "Y0t2Ehtpi2SdR14gu5CFGcd2dxqHNOcgqEuwk3hRhDd955cEAk");

        Gimbal.registerForPush("6658256976374");

        placeEventListener = new PlaceEventListener() {
            @Override
            public void onVisitStart(Visit visit) {
                // This will be invoked when a place is entered. Example below shows a simple log upon enter
                Log.i("Info:", "Enter: " + visit.getPlace().getName() + ", at: " + new Date(visit.getArrivalTimeInMillis()));
               // Toast.makeText(getApplicationContext(), "Comenzo la visita", Toast.LENGTH_LONG).show();

                if(isNetworkAvailable()){
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Sucursales");
                    query.whereEqualTo("Beacon",visit.getPlace().getName());
                 //   Toast.makeText(getApplicationContext(),visit.getPlace().getName() , Toast.LENGTH_SHORT).show();

                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            if (e == null){
                                for (int n=0; n<parseObjects.size();n++){
                                    ParseObject object = parseObjects.get(n);
                                    String promo = object.getString("PP");
                                    String nom = object.getString("Nombre");
                                    Toast.makeText(getApplicationContext(),promo , Toast.LENGTH_SHORT).show();

                                    RemoteViews remoteViews = new RemoteViews(getPackageName(),
                                            R.layout.widget);
                                    //TextView Titulo = (TextView)findViewById(R.id.widgetTitle);
                                    //Titulo.setText(nom);
                                    //TextView Descripcion = (TextView)findViewById(R.id.widgetDescripcion);
                                    //Titulo.setText(promo);

                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                                            getApplicationContext()).setSmallIcon(R.drawable.ic_notification_name).setContentTitle(nom)
                                            .setContentText(promo).setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
                                    // Creates an explicit intent for an Activity in your app
                                    Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                                    // The stack builder object will contain an artificial back stack for
                                    // the
                                    // started Activity.
                                    // This ensures that navigating backward from the Activity leads out of
                                    // your application to the Home screen.
                                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                                    // Adds the back stack for the Intent (but not the Intent itself)
                                    stackBuilder.addParentStack(MainActivity.class);
                                    // Adds the Intent that starts the Activity to the top of the stack
                                    stackBuilder.addNextIntent(resultIntent);
                                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                                            PendingIntent.FLAG_UPDATE_CURRENT);
                                    //remoteViews.setOnClickPendingIntent(R.id.button1, resultPendingIntent);
                                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    // mId allows you to update the notification later on.
                                    mNotificationManager.notify(100, mBuilder.build());

                                }
                            }
                            else{

                            }

                        }

                    });

                }


            }

            @Override
            public void onVisitEnd(Visit visit) {
                // This will be invoked when a place is exited. Example below shows a simple log upon exit
                Log.i("Info:", "Exit: " + visit.getPlace().getName() + ", at: " + new Date(visit.getDepartureTimeInMillis()));
                //Toast.makeText(getApplicationContext(),"Termino la visita",Toast.LENGTH_LONG).show();
            }
        };
        PlaceManager.getInstance().addListener(placeEventListener);

        communicationListener = new CommunicationListener() {
            @Override
            public Collection<Communication> presentNotificationForCommunications(Collection<Communication> communications, Visit visit) {
                for (Communication comm : communications) {
                    Log.i("INFO", "Place Communication: " + visit.getPlace().getName() + ", message: " + comm.getTitle());

                }
                //allow Gimbal to show the notification for all communications
                return communications;
            }



            @Override
            public Collection<Communication> presentNotificationForCommunications(Collection<Communication> communications, Push push) {
                for (Communication comm : communications) {
                    Log.i("INFO", "Received a Push Communication with message: " + comm.getTitle());
                    Toast.makeText(getApplicationContext(),"Push",Toast.LENGTH_LONG).show();
                }
                //allow Gimbal to show the notification for all communications
                return communications;
            }

            @Override
            public void onNotificationClicked(List<Communication> communications) {
                Log.i("INFO", "Notification was clicked on");
            }
        };
        CommunicationManager.getInstance().addListener(communicationListener);

        beaconSightingListener = new BeaconEventListener() {
            @Override
            public void onBeaconSighting(BeaconSighting sighting) {
                Log.i("INFO", sighting.toString());
                //Toast.makeText(getApplication(),sighting.getBeacon().getName(),Toast.LENGTH_LONG).show();

            }
        };
        beaconManager = new BeaconManager();
        beaconManager.addListener(beaconSightingListener);


        if(!PlaceManager.getInstance().isMonitoring()){
            PlaceManager.getInstance().startMonitoring();
            beaconManager.startListening();
            CommunicationManager.getInstance().startReceivingCommunications();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) this.getSystemService(MainActivity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isNetworkAvaible = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isNetworkAvaible = true;

        } else {
            Toast.makeText(this, "No hay red disponible ", Toast.LENGTH_LONG)
                    .show();
        }
        return isNetworkAvaible;
    }

}
