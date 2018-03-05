package fr.soyhuce.gpstracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


/**
 * Created by mathieuedet on 02/03/2018.
 */

public class GPSTracker extends LocationCallback implements OnCompleteListener<Location> {

    private static final String TAG = GPSTracker.class.getName();
    private static final GPSTracker INSTANCE = new GPSTracker();

    public static final int REQUEST_CODE_LOCATION_PERMISSION = 100;

    public static final long DEFAULT_INTERVAL_LOCATION_REQUEST = 1000; //1 sec par défaut
    public static final long DEFAULT_FASTEST_INTERVAL_LOCATION_REQUEST = DEFAULT_INTERVAL_LOCATION_REQUEST / 6; //ms Par défaut le fastestInterval est 6x la fréquence de l'interval
    public static final long DEFAULT_MAX_WAIT_TIME_LOCATION_REQUEST = DEFAULT_INTERVAL_LOCATION_REQUEST * 5; //ms
    public static final long DEFAULT_SMALLEST_DISPLACEMENT_LOCATION_REQUEST = 0; //metres
    public static final int DEFAULT_PRIORITY_LOCATION_REQUEST = LocationRequest.PRIORITY_HIGH_ACCURACY;

    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationListener locationListener;

    private GPSTracker() { /* Private constructor */ }

    public static GPSTracker getInstance() {
        return INSTANCE;
    }


    /***
     * Permet de lancer une demande de maj regulière en fonction des paramètres spécifiés <br />
     * Plus d'informations sur les paramètres <a href="https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest.html">ici</a>
     * @param locationListener Classe implémentant LocationListener pour récupérer chaque maj de position ou une erreur en cas d'erreur
     * @param intervalMillis Interval de mise à jour (possible mais non conseillé de mettre 0 car les mises à jour de position sont amené à être de plus en plus rapide)
     * @param fastestIntervalMillis Interval le plus rapide de mise à jour (possible mais non conseillé de mettre 0 car les mises à jour de position sont amené à être de plus en plus rapide)
     *                              Est inférieure à {@code intervalMillis} et peut permettre de récupérer une position avant la fin de la valeur de {@code intervalMillis} si une autre app demande la localisation et l'obtient
     * @param maxWaitTimeMillis Temps maximum en millisecondes à attendre (Attention ! Valeur inexacte selon la doc officielle Android)
     * @param smallestDisplacementMeters Déplacement minimum entre deux maj de position en mètres
     * @param priority Priorité de la requete de géolocalisation (LocationRequest.PRIORITY_HIGH_ACCURACY ou LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY ou LocationRequest.PRIORITY_LOW_POWER)
     */
    @SuppressLint("MissingPermission")
    public void requestLocationUpdate(Activity activity, LocationListener locationListener,
                                      long intervalMillis,
                                      long fastestIntervalMillis,
                                      long maxWaitTimeMillis,
                                      long smallestDisplacementMeters,
                                      int priority){

        if(priority != LocationRequest.PRIORITY_HIGH_ACCURACY && priority != LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY && priority != LocationRequest.PRIORITY_LOW_POWER){
            Log.e(TAG, "Priority parameter should be equals to LocationRequest.PRIORITY_HIGH_ACCURACY || LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY || LocationRequest.PRIORITY_LOW_POWER" +
                    "https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest.html#setPriority(int)" +
                    "Default value (" + DEFAULT_PRIORITY_LOCATION_REQUEST + ") will be used");
            priority = DEFAULT_PRIORITY_LOCATION_REQUEST;
        }

        if(!canCheckLocation(activity)){
            return;
        }


        LocationRequest currentLocationRequest = new LocationRequest();
        currentLocationRequest.setInterval(intervalMillis)
                .setFastestInterval(fastestIntervalMillis)
                .setMaxWaitTime(maxWaitTimeMillis)
                .setSmallestDisplacement(smallestDisplacementMeters)
                .setPriority(priority);

        this.locationListener = locationListener;

        this.fusedLocationProviderClient.requestLocationUpdates(currentLocationRequest, this, null);
    }

    public void stopLocationUpdate(){
        this.fusedLocationProviderClient.removeLocationUpdates(this);
    }

    /***
     * Permet de lancer une demande de maj regulière en fonction des paramètres spécifiés <br />
     * @param locationListener Classe implémentant LocationListener pour récupérer chaque maj de position ou une erreur en cas d'erreur
     */
    public void requestLocationUpdate(Activity activity, LocationListener locationListener){
        requestLocationUpdate(activity, locationListener, DEFAULT_INTERVAL_LOCATION_REQUEST, DEFAULT_FASTEST_INTERVAL_LOCATION_REQUEST, DEFAULT_MAX_WAIT_TIME_LOCATION_REQUEST, DEFAULT_SMALLEST_DISPLACEMENT_LOCATION_REQUEST, DEFAULT_PRIORITY_LOCATION_REQUEST);
    }

    private boolean requestLocationPermissionIfNeeded(Activity activity){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_CODE_LOCATION_PERMISSION);
            return false;
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    public void getLastLocationAsync(Activity activity, LocationListener locationListener){

        if(!canCheckLocation(activity)){
            return;
        }

        this.locationListener = locationListener;
        this.fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this);
    }

    private boolean canCheckLocation(Activity activity){
        // Check si la permission est accordée, et demande si jamais elle ne l'est pas
        if (!requestLocationPermissionIfNeeded(activity)){
            return false;
        }

        //Check si la localisation est activé sur le téléphone
        if(!isLocationAccessEnabled(context)){
            //Redirection vers les paramètres du téléphone pour activer le GPS (dialog gérée par FusedLocationProviderClient)
            return false;
        }
        return true;
    }


    private boolean isLocationAccessEnabled(Context context){

        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return !(manager == null || !manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }


    // GET LAST LOCATION UPDATE

    @Override
    public void onComplete(@NonNull Task<Location> task) {
        if(task.isSuccessful() && task.getResult() != null && !task.getResult().isFromMockProvider()){
            broadcastLocation(task);
        }else{
            broadcastLocationError(task);
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



    // LOCATION CALLBACK

    @Override
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

    public void setContext(Context context) {
        this.context = context;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }



    public class MockLocationException extends Exception{
        @Override
        public String getMessage() {
            return "Mock location not permits for the application.";
        }
    }

    public class NoLocationException extends Exception{
        @Override
        public String getMessage() {
            return "No location detected";
        }
    }
}
