package com.bikeonet.android.dslrbrowser.util;

import android.location.Location;

/**
 * Created by andrasbekesi on 18.05.17.
 */

public class LocationStore {

    private Location lastLocation;
    private static LocationStore instance;

    private LocationStore() {};

    public static LocationStore getInstance() {
        if ( instance == null ) {
            instance = new LocationStore();
        }

        return instance;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }
}
