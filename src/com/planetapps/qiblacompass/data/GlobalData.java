package com.planetapps.qiblacompass.data;

import android.location.Location;


/**
 * Abstract class which should be used to set global data.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public abstract class GlobalData {
	private static final Object lock = new Object();
    private static float bearing = 0;
    private static int dialCode = 1;
    private static String locality = "";
    private static double kaabadistance = 0;
    private static Location currentLocation = null;
    
    /**
     * Set the bearing.
     * @param bearing int representing the current bearing.
     */
    public static void setBearing(float bearing) {
    	synchronized (lock) {
    		GlobalData.bearing = bearing;
    	}
    }
    
    /**
     * Get the bearing.
     * @return int representing the bearing.
     */
    public static float getBearing() {
    	synchronized (lock) {
    		return bearing;
    	}
    }
    
    public static int getDialCode() {
		return dialCode;
	}

	public static void setDialCode(int dialCode) {
		GlobalData.dialCode = dialCode;
	}

	/**
	 * @return the locality
	 */
	public static String getLocality() {
		return locality;
	}

	/**
	 * @param locality the locality to set
	 */
	public static void setLocality(String locality) {
		GlobalData.locality = locality;
	}

	/**
	 * @return the kaabadistance
	 */
	public static double getKaabadistance() {
		return kaabadistance;
	}

	/**
	 * @param kaabadistance the kaabadistance to set
	 */
	public static void setKaabadistance(double kaabadistance) {
		GlobalData.kaabadistance = kaabadistance;
	}

	/**
	 * @return the currentLocation
	 */
	public static Location getCurrentLocation() {
		return currentLocation;
	}

	/**
	 * @param currentLocation the currentLocation to set
	 */
	public static void setCurrentLocation(Location currentLocation) {
		GlobalData.currentLocation = currentLocation;
	}    
}
