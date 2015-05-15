package com.uninorte.jdmaestre.pf;
import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseTwitterUtils;
import com.gimbal.android.Gimbal;

/**
 * Created by Jose on 16/04/2015.
 */
public class PFApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Gimbal.setApiKey(this, "3b59db3e-d6f4-45fc-8595-9cacc3474c7c");
        Gimbal.registerForPush("665825697637");


        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "1meX4eoaUmA1quVUqVWN8TW0JtGaQLUip4xylIdW", "bwFtDiaj6Hrf3rzFWhgBzDvGRBnPTMYuhWcolUsT");
        //ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseTwitterUtils.initialize("jtXfc6pPQY22Z1DwCfhD4lhiT", "Y0t2Ehtpi2SdR14gu5CFGcd2dxqHNOcgqEuwk3hRhDd955cEAk");
    }

}
