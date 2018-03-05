package fr.soyhuce.gpstracker;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import fr.soyhuce.gpstracker.interfaces.LocationListener;

/**
 * Created by mathieuedet on 05/03/2018.
 */

public class GPSTrackerActivityTest extends AppCompatActivity implements LocationListener {

    private static final String TAG = GPSTrackerActivityTest.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.setContentView(R.layout.activity_test);

        GPSTracker.getInstance().setContext(getApplicationContext());

        // Ask runtime permission if needed and start location updates
        GPSTracker.getInstance().requestLocationUpdate(this, this);
    }


    private void displayLocation(@Nullable Location location){
        if(location != null){
            Toast.makeText(this, String.valueOf(location.getLongitude()) + ";" + String.valueOf(location.getLatitude()), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GPSTracker.REQUEST_CODE_LOCATION_PERMISSION && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            GPSTracker.getInstance().requestLocationUpdate(this, this);
        }else if(requestCode == GPSTracker.REQUEST_CODE_LOCATION_PERMISSION && grantResults[0] == PermissionChecker.PERMISSION_DENIED
                && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(this, "Denied, ask again with a button click...", Toast.LENGTH_SHORT).show();
        }else if(requestCode == GPSTracker.REQUEST_CODE_LOCATION_PERMISSION && grantResults[0] == PermissionChecker.PERMISSION_DENIED
                && !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(this, "Denied and don't ask again, go to app parameters", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onGetLocation(Location location) {
        displayLocation(location);
    }

    @Override
    public void onLocationError(Exception ex) {
        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onLocationAvailabilityChanged(boolean isAvailable) {
        // On location availibity changed => when gps become enabled/disabled
    }
}
