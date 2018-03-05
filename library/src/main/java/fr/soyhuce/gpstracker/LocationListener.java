package fr.soyhuce.gpstracker;

import android.location.Location;

/**
 * Created by mathieuedet on 02/03/2018.
 */
public interface LocationListener {
    void onGetLocation(Location location);
    void onLocationError(Exception ex);
    void onLocationAvailabilityChanged(boolean isAvailable);
}
