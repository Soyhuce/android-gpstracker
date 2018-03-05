#### gpstracker
#### Version `0.1.0`
---

#### Description
Cette librairie permet de faciliter la récupérer de position GPS en s'appuyant sur FusedLocationProviderClient.

### Installation

* Ouvrez le projet dans lequel vous souhaitez utiliser la librairie,
* Ouvrez le fichier build.gradle de votre projet (pas celui de l'application),
* Ajoutez-y (si ce n'est pas déjà fait) le code suivant :

```ruby
allprojects {
    repositories {
        jcenter()
        maven{ url "https://jitpack.io" }
    }
}
```

* Ouvrez ensuite le build.gradle de votre application
* Ajoutez la dépendance suivante :
```ruby
implementation 'com.github.soyhuce:gpstracker:0.1.0'
```



### Utilisation

Une fois ajoutée au projet, pour utiliser cette librarie, vous devrez initialisé le gpstracker avec un context. Pour faciliter son utilisation,
vous pouvez l'initialiser avec le context de l'application.

Exemple d'initialisation :

```java
public class MyApplication extends Application {

    @Override
    public onCreate() {
        super.onCreate();
        GPSTracker.getInstance().setContext(this);
    }
    
    [...]
}
```

OU 


```java
public class MyFirstActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GPSTracker.getInstance().setContext(this);
    }
    
    [...]
}
```

### Récupération de la dernière position connue

- Assurez-vous d'avoir bien initialisé le context comme décrit ci-dessus
- Faites hériter votre classe de LocationListener
- Appelez la méthode getLastLocationAsync(Activity, LocationListener)

```java

public class MyActivity extends AppCompatActivity implements LocationListener {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        [...]
        GPSTracker.getInstance().getLastLocationAsync(this, this);
    }
    [...]
    
    @Override
    public void onGetLocation(Location location) {
        // Handle retrieved location
    }

    @Override
    public void onLocationError(Exception ex) {
        // Handle exception
    }


    @Override
    public void onLocationAvailabilityChanged(boolean isAvailable) {
        // On location availability changed => when gps become enabled/disabled
    }
}
```



### Récupération de la position et de chaque maj en fonction de paramètres

- Assurez-vous d'avoir bien initialisé le context comme décrit ci-dessus
- Faites hériter votre classe de LocationListener

- Appelez la méthode requestLocationUpdate(Activity, LocationListener)

**Ou** pour customiser la précision de la géolocalisation :
 
- Appelez la méthode  : 
```java

void requestLocationUpdate(Activity activity, LocationListener locationListener, long intervalMillis, long fastestIntervalMillis, long maxWaitTimeMillis, long smallestDisplacementMeters, int priority);
```

- **activity** (*android.app.Activity*): Activity à partir de laquelle la géolocalisation est effectuée (implémenter ``` onRequestPermissionsResult``` au sein de l'activity)
- **locationListener** (*GPSTracker.LocationListener*) : Classe implémentant LocationListener pour récupérer chaque maj de position ou une erreur en cas d'erreur
- **intervalMillis** (*long*) : Interval de mise à jour (possible mais non conseillé de mettre 0 car les mises à jour de position sont amené à être de plus en plus rapide)
- **fastestIntervalMillis** (*long*) : Interval le plus rapide de mise à jour (possible mais non conseillé de mettre 0 car les mises à jour de position sont amené à être de plus en plus rapide). Est inférieure à **intervalMillis** et peut permettre de récupérer une position avant la fin de la valeur de **intervalMillis** si une autre application demande la localisation et l'obtient
- **maxWaitTimeMillis** (*long*) : Temps maximum en millisecondes à attendre (Attention ! Valeur inexacte selon la doc officielle Android)
- **smallestDisplacementMeters** (*long*) : Déplacement minimum entre deux maj de position en mètres
- **priority** (*int*) : Priorité de la requête de géolocalisation (**LocationRequest.PRIORITY_HIGH_ACCURACY** ou **LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY** ou **LocationRequest.PRIORITY_LOW_POWER**)

```java
public class MyActivity extends AppCompatActivity implements LocationListener {
    
    
    @Override
    protected void onStart(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        [...]
        GPSTracker.getInstance().requestLocationUpdate(this, this);
        
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        [...]
        GPSTracker.getInstance().stopLocationUpdate();
    }
    
    [...]
    
    @Override
    public void onGetLocation(Location location) {
        // Handle retrieved location
        [...]
    }

    @Override
    public void onLocationError(Exception ex) {
        // Handle exception
    }


    @Override
    public void onLocationAvailabilityChanged(boolean isAvailable) {
        // On location availability changed => when gps become enabled/disabled
    }
}
```