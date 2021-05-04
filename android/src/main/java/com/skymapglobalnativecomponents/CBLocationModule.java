package com.skymapglobalnativecomponents;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.facebook.react.bridge.Promise;

public class CBLocationModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    private Location location;
    private LocationListener locationListener;
    private final LocationManager locationManager;
    private final PackageManager packageManager;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 200 * 10 * 1; // 2 secondsprivate static final String E_PICKER_CANCELLED = "E_PICKER_CANCELLED";
    private static final String E_NO_PROVIDER_ENABLE = "E_NO_PROVIDER_ENABLE";
    private static final String E_NO_PERMISSION_GRANDED = "E_NO_PERMISSION_GRANDED";
    private static final String E_NETWORK_TIMEOUT = "E_NETWORK_TIMEOUT";


    public CBLocationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        locationManager = (LocationManager) this.reactContext.getSystemService(Context.LOCATION_SERVICE);
        packageManager = this.reactContext.getPackageManager();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.reactContext);
    }

    @Override
    public String getName() {
        return "CBLocation";
    }

    public WritableMap getFormatLocation(Location loc) {
        WritableMap params = Arguments.createMap();
        WritableMap coords = Arguments.createMap();
        coords.putDouble("longitude", loc.getLongitude());
        coords.putDouble("latitude", loc.getLatitude());
        params.putMap("coords", coords);
        params.putDouble("speed", loc.getSpeed());
        params.putDouble("altitude", loc.getAltitude());
        params.putDouble("accuracy", loc.getAccuracy());
        params.putDouble("course", loc.getBearing());
        return params;
    }

    @ReactMethod
    public void getCurrentLocation(Promise promise) {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                location = loc;
                if (location != null) {
                    WritableMap params = Arguments.createMap();
                    params.putDouble("longitude", location.getLongitude());
                    params.putDouble("latitude", location.getLatitude());
                    params.putDouble("speed", location.getSpeed());
                    params.putDouble("altitude", location.getAltitude());
                    params.putDouble("accuracy", location.getAccuracy());
                    params.putDouble("course", location.getBearing());

                    sendEvent(reactContext, "CBLocationChange", params);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (checkPermission()) {
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                promise.reject(E_NO_PROVIDER_ENABLE, "2");
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (location != null) {
                    Log.i("NETWORK_PROVIDER", location.toString());
                    WritableMap params = getFormatLocation(location);
                    promise.resolve(params);
                } else {
                    fusedLocationProviderClient.getLastLocation()
                            .addOnSuccessListener(getCurrentActivity(), new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location mLoc) {
                                    if (mLoc != null) {
                                        WritableMap params = getFormatLocation(mLoc);
                                        promise.resolve(params);
                                    } else {
                                        if (checkPermission()) {
                                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                            if (location != null) {
                                                WritableMap params = getFormatLocation(location);
                                                promise.resolve(params);
                                            } else {
                                                promise.reject(E_NETWORK_TIMEOUT, "3");
                                            }
                                        } else {
                                            promise.reject(E_NO_PERMISSION_GRANDED, "1");
                                        }

                                    }
                                }
                            })
                            .addOnFailureListener(getCurrentActivity(), new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if (checkPermission()) {
                                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                        if (location != null) {
                                            WritableMap params = getFormatLocation(location);
                                            promise.resolve(params);
                                        } else {
                                            promise.reject(E_NETWORK_TIMEOUT, "3");
                                        }
                                    } else {
                                        promise.reject(E_NO_PERMISSION_GRANDED, "1");
                                    }
                                }
                            });
                }
            }

        } else {
            promise.reject(E_NO_PERMISSION_GRANDED, "1");
        }
    }

    @ReactMethod
    private void openLocationSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            if (intent.resolveActivity(packageManager) != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.reactContext.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {

        }

    }


    @ReactMethod
    private void openAppPermissions() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.reactContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            if (intent.resolveActivity(packageManager) != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.reactContext.startActivity(intent);
            }
        }
    }

    @ReactMethod
    public void startUpdatingLocation() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                location = loc;
                if (location != null) {
                    WritableMap params = Arguments.createMap();
                    params.putDouble("longitude", location.getLongitude());
                    params.putDouble("latitude", location.getLatitude());
                    params.putDouble("speed", location.getSpeed());
                    params.putDouble("altitude", location.getAltitude());
                    params.putDouble("accuracy", location.getAccuracy());
                    params.putDouble("course", location.getBearing());

                    sendEvent(reactContext, "CBLocationChange", params);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (checkPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, locationListener);
        }
    }

    @ReactMethod
    public void stopUpdatingLocation() {
        try {
            locationManager.removeUpdates(locationListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        if (reactContext.hasActiveCatalystInstance()) {
            reactContext
                    .getJSModule(RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        }
    }
}
