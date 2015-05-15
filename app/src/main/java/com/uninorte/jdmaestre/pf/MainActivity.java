package com.uninorte.jdmaestre.pf;

import java.util.Date;
import java.util.Locale;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.*;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.gimbal.android.BeaconEventListener;
import com.gimbal.android.BeaconManager;
import com.gimbal.android.BeaconSighting;
import com.gimbal.android.Communication;
import com.gimbal.android.CommunicationListener;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Push;
import com.gimbal.android.Visit;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import java.util.Collection;
import java.util.List;
import android.util.Log;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private PlaceEventListener placeEventListener;
    private CommunicationListener communicationListener;
    private BeaconEventListener beaconSightingListener;
    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int aux_login = 0;
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null){
            aux_login = 1;
            navigateToLogin();
        }
        else if(currentUser.getUsername() != null) {
            Log.i(TAG, currentUser.getUsername());
        }else{
            Log.i(TAG, ParseTwitterUtils.getTwitter().getScreenName());
        }

        if (aux_login == 0){
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        }


        Gimbal.registerForPush("6658256976374");

        placeEventListener = new PlaceEventListener() {
            @Override
            public void onVisitStart(Visit visit) {
                // This will be invoked when a place is entered. Example below shows a simple log upon enter
                Log.i("Info:", "Enter: " + visit.getPlace().getName() + ", at: " + new Date(visit.getArrivalTimeInMillis()));
                //Toast.makeText(getApplication(),"Comenzo la visita",Toast.LENGTH_LONG).show();

                ParseQuery<ParseObject> query = ParseQuery.getQuery("Sucursales");
                query.whereEqualTo("Beacon",visit.getPlace().getName());
                Toast.makeText(getApplication(),visit.getPlace().getName() , Toast.LENGTH_SHORT).show();

                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if (e == null){
                            for (int n=0; n<parseObjects.size();n++){
                                ParseObject object = parseObjects.get(n);
                                String promo = object.getString("PP");
                                String nom = object.getString("Nombre");
                                Toast.makeText(getApplication(),promo , Toast.LENGTH_SHORT).show();

                                RemoteViews remoteViews = new RemoteViews(getPackageName(),
                                        R.layout.widget);
                                //TextView Titulo = (TextView)findViewById(R.id.widgetTitle);
                                //Titulo.setText(nom);
                                //TextView Descripcion = (TextView)findViewById(R.id.widgetDescripcion);
                                //Titulo.setText(promo);

                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                                        getApplication()).setSmallIcon(R.drawable.ic_notification_name).setContentTitle(nom)
                                        .setContentText(promo).setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
                                // Creates an explicit intent for an Activity in your app
                                Intent resultIntent = new Intent(getApplication(), MainActivity.class);
                                // The stack builder object will contain an artificial back stack for
                                // the
                                // started Activity.
                                // This ensures that navigating backward from the Activity leads out of
                                // your application to the Home screen.
                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplication());
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

            @Override
            public void onVisitEnd(Visit visit) {
                // This will be invoked when a place is exited. Example below shows a simple log upon exit
                Log.i("Info:", "Exit: " + visit.getPlace().getName() + ", at: " + new Date(visit.getDepartureTimeInMillis()));
                Toast.makeText(getApplication(),"Termino la visita",Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplication(),"Push",Toast.LENGTH_LONG).show();
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


        PlaceManager.getInstance().startMonitoring();
        beaconManager.startListening();
        CommunicationManager.getInstance().startReceivingCommunications();






    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            ParseUser.logOut();
            navigateToLogin();
        }

        if (id == R.id.action_settings) {
            if(PlaceManager.getInstance().isMonitoring() == true){

                Toast.makeText(getApplication(),"Esta monitoreando",Toast.LENGTH_LONG).show();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new MapFragment();
                case 1:
                    return new ListFragment ();
            }

            return new MapFragment();
        }


        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1_main).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2_main).toUpperCase(l);
            }
            return null;
        }
    }



}
