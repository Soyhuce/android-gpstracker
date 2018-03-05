package fr.soyhuce.gpstracker;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import fr.soyhuce.gpstracker.exceptions.MockLocationException;
import fr.soyhuce.gpstracker.exceptions.NoLocationException;
import fr.soyhuce.gpstracker.interfaces.LocationListener;

/**
 * Created by mathieuedet on 05/03/2018.
 */

class LocationHandler extends LocationCallback implements OnCompleteListener<Location> {

    private LocationListener locationListener;


    @Override
    public void onComplete(@NonNull Task<Location> task) {
        if(task.isSuccessful() && task.getResult() != null && !task.getResult().isFromMockProvider()){
            broadcastLocation(task);
        }else{
            broadcastLocationError(task);
        }
    }

    public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        if(locationListener != null){
            locationListener.onGetLocation(locationResult.getLastLocation());
        }
    }

    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability) {
        super.onLocationAvailability(locationAvailability);
        if(locationListener != null){
            locationListener.onLocationAvailabilityChanged(locationAvailability.isLocationAvailable());
        }
    }

    private void broadcastLocation(Task<Location> task){
        if(locationListener != null){
            locationListener.onGetLocation(task.getResult());
        }
    }

    private void broadcastLocationError(Task<Location> task){
        if(task.getResult() != null && task.getResult().isFromMockProvider()){
            locationListener.onLocationError(new MockLocationException());
            return;
        }

        if(task.isSuccessful() && task.getResult() == null && task.getException() == null){
            locationListener.onLocationError(new NoLocationException());
            return;
        }

        if(locationListener != null && !task.isSuccessful() && task.getException() != null){
            locationListener.onLocationError(task.getException());
        }
    }

    void setLocationListener(LocationListener locationListener) {
        this.locationListener = locationListener;
    }
}
