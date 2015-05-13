package com.planetapps.qiblacompass;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import com.planetapps.qiblacompass.data.GlobalData;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * This class extends Activity and processes sensor data and location data.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class SensorsActivity extends Activity implements SensorEventListener,
		LocationListener {
	@SuppressWarnings("unused")
	private static final String TAG = "SensorsActivity";
	private static final AtomicBoolean computing = new AtomicBoolean(false);

	private static final int MIN_TIME = 30 * 1000;
	private static final int MIN_DISTANCE = 10;

	private static final float grav[] = new float[3]; // Gravity (a.k.a
														// accelerometer data)
	private static final float mag[] = new float[3]; // Magnetic
	private static final float rotation[] = new float[9]; // Rotation matrix in
															// Android format
	private static final float orientation[] = new float[3]; // azimuth, pitch,
																// roll
	private static float smoothed[] = new float[3];
	private ProgressBar progress;
	private static SensorManager sensorMgr = null;
	private static List<Sensor> sensors = null;
	private static Sensor sensorGrav = null;
	private static Sensor sensorMag = null;
	private static Sensor sensorOrient = null;

	private static LocationManager locationMgr = null;
	private static Location currentLocation = null;
	private static GeomagneticField gmf = null;
	private double kaabaoffset = 0;
	private static double floatBearing = 0;
	private static boolean sensorunreliable = false;
	private boolean gps_enabled = false;
	private boolean network_enabled = false;
	private static View compassView = null;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		progress = (ProgressBar) findViewById(R.id.progressBar1);
		//progress.setVisibility(View.GONE);
		compassView = findViewById(R.id.compass);
	}

//	private class mylocationlistener implements LocationListener {
//		
//		public void onLocationChanged(Location location) {
//			Date today = new Date();
//			Timestamp currentTimeStamp = new Timestamp(today.getTime());
//			if (location != null) {
//				Log.d("LOCATION CHANGED", location.getLatitude() + "");
//				Log.d("LOCATION CHANGED", location.getLongitude() + "");
//				String str = "\n CurrentLocation: " + "\n Latitude: "
//						+ location.getLatitude() + "\n Longitude: "
//						+ location.getLongitude() + "\n Accuracy: "
//						+ location.getAccuracy() + "\n CurrentTimeStamp "
//						+ currentTimeStamp;
//				Toast.makeText(SensorsActivity.this, str, Toast.LENGTH_LONG)
//						.show();
//				// tv.append(str);
//				//progress.setVisibility(View.GONE);
//				turnGPSOff();
//				if (compassView!=null) {
//					System.out.println("postInvalidate ....");
//					compassView.postInvalidate();
//					compassView.refreshDrawableState();
//				}else System.out.println("null compassView");
//			}
//		}
//
//		public void onProviderDisabled(String provider) {
//			// Toast.makeText(SensorsActivity.this,"Error Provider Disabled",Toast.LENGTH_LONG).show();
//		}
//
//		public void onProviderEnabled(String provider) {
//			Toast.makeText(SensorsActivity.this, "Provider Enabled",
//					Toast.LENGTH_LONG).show();
//		}
//
//		public void onStatusChanged(String provider, int status, Bundle extras) {
//			// Toast.makeText(SensorsActivity.this,"Status Changed",Toast.LENGTH_LONG).show();
//		}
//	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart() {
		super.onStart();

		try {
			
			sensorunreliable = false;
			
			sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

			sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if (sensors.size() > 0)
				sensorGrav = sensors.get(0);

			sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
			if (sensors.size() > 0)
				sensorMag = sensors.get(0);

			sensors = sensorMgr.getSensorList(Sensor.TYPE_ORIENTATION);
			if (sensors.size() > 0)
				sensorOrient = sensors.get(0);

			sensorMgr.registerListener(this, sensorGrav,
					SensorManager.SENSOR_DELAY_NORMAL);
			sensorMgr.registerListener(this, sensorMag,
					SensorManager.SENSOR_DELAY_NORMAL);
			sensorMgr.registerListener(this, sensorOrient,
					SensorManager.SENSOR_DELAY_NORMAL);

			locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//			LocationListener ll = new mylocationlistener();
			locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					MIN_TIME, MIN_DISTANCE, this);
			locationMgr.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE,
					this);

			try {
				/* defaulting to our place */
				Location hardFix = new Location("ATL");
				hardFix.setLatitude(0);
				hardFix.setLongitude(0);
				hardFix.setAltitude(1);
				
				if(GlobalData.getCurrentLocation() == null){
					System.out.println("GlobalData.getCurrentLocation() == null");
					try {
						Location gps = locationMgr
								.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						Location network = locationMgr
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	
						if (gps != null) {
							System.out.println("gps");
							currentLocation = (gps);
						} else if (network != null) {
							currentLocation = (network);
							System.out.println("network");
						} else {
							System.out.println("hardFix");
							currentLocation = (hardFix);
						}
					} catch (Exception ex2) {
						currentLocation = (hardFix);
					}
				}else
				{
					currentLocation = GlobalData.getCurrentLocation();
					System.out.println("GlobalData notnull currentLocation>"+currentLocation.getLatitude());
				}
				//currentLocation = (hardFix); //remove
				if (currentLocation == (hardFix)) {
					// System.out.println("currentLocation == (hardFix)");
					// currentLocation = (hardFix);
					// AlertDialog.Builder alt_bld = new
					// AlertDialog.Builder(this);
					// alt_bld.setMessage("Current Location Un-Available - Turn-ON GPS !!")
					// .setCancelable(false)
					// .setPositiveButton("OK",
					// new DialogInterface.OnClickListener() {
					// public void onClick(
					// DialogInterface dialog, int id) {
					// // Action for 'Yes' Button
					//
					// onStart();
					// }
					// })
					// .setNegativeButton("Exit",
					// new DialogInterface.OnClickListener() {
					// public void onClick(
					// DialogInterface dialog, int id) {
					// // Action for 'Yes' Button
					//
					// finish();
					// }
					// });
					// AlertDialog alert = alt_bld.create();
					// // Title for AlertDialog
					// alert.setTitle("GPS");
					// // Icon for AlertDialog
					// alert.setIcon(R.drawable.gps);
					// alert.show();

					//onGetLocation();

				}
				GlobalData.setCurrentLocation(currentLocation);

				// try {
				// System.out.println("locality .....");
				// Geocoder gcd = new Geocoder(this, Locale.getDefault());
				// List<Address> addresses =
				// gcd.getFromLocation(currentLocation.getLatitude(),
				// currentLocation.getLongitude(), 1);
				// if (addresses.size() > 0)
				// {
				// System.out.println(addresses.get(0).getLocality());
				// GlobalData.setLocality(addresses.get(0).getLocality());
				// }
				// } catch (Exception e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }

				// currentLocation=(hardFix); //remove later
				onLocationChanged(currentLocation);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			kaabaoffset = kaababearing();
			System.out.println("kaabaoffset >" + kaabaoffset);

		} catch (Exception ex1) {
			try {
				if (sensorMgr != null) {
					sensorMgr.unregisterListener(this, sensorGrav);
					sensorMgr.unregisterListener(this, sensorMag);
					sensorMgr = null;
				}
				if (locationMgr != null) {
					locationMgr.removeUpdates(this);
					locationMgr = null;
				}
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		}
	}

	private void turnGPSOn(){
	    String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

	    if(!provider.contains("gps")){ //if gps is disabled
	        final Intent poke = new Intent();
	        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider"); 
	        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
	        poke.setData(Uri.parse("3")); 
	        sendBroadcast(poke);
	    }
	}
    private void turnGPSOff(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(provider.contains("gps")){ //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3")); 
            sendBroadcast(poke);
        }
    }
    
//	public void onGetLocation() {
//		turnGPSOn();
//		progress.setVisibility(View.VISIBLE);
//		Toast toast = Toast
//				.makeText(this, "Getting Current Location....", 2000);
//		toast.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
//		toast.show();
//		// exceptions will be thrown if provider is not permitted.
//		try {
//			gps_enabled = locationMgr
//					.isProviderEnabled(LocationManager.GPS_PROVIDER);
//			System.out.println("gps_enabled >");
//		} catch (Exception ex) {
//		}
//		try {
//			network_enabled = locationMgr
//					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//			System.out.println("network_enabled >");
//		} catch (Exception ex) {
//		}
//
//		// don't start listeners if no provider is enabled
//		if (!gps_enabled && !network_enabled) {
//			AlertDialog.Builder builder = new Builder(this);
//			builder.setTitle("Attention!");
//			builder.setMessage("Sorry, location is not determined. Please enable location providers");
//			// builder.setPositiveButton("OK", this);
//			// builder.setNeutralButton("Cancel", this);
//			builder.create().show();
//			progress.setVisibility(View.GONE);
//		}
//
//		// if (gps_enabled) {
//		// locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
//		// locListener);
//		// }
//		// if (network_enabled) {
//		// locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
//		// 0, 0, locListener);
//		// }
//	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onStop() {
		super.onStop();

		try {
			try {
				sensorMgr.unregisterListener(this, sensorGrav);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				sensorMgr.unregisterListener(this, sensorMag);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			sensorMgr = null;

			try {
				locationMgr.removeUpdates(this);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			locationMgr = null;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			try {
				sensorMgr.unregisterListener(this, sensorGrav);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				sensorMgr.unregisterListener(this, sensorMag);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			sensorMgr = null;

			try {
				locationMgr.removeUpdates(this);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			locationMgr = null;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// if(sersorrunning){
		// sensorMgr.unregisterListener(this);
		// }
	}

	/**
	 * {@inheritDoc}
	 */
	public void onSensorChanged(SensorEvent event) {
		if (!computing.compareAndSet(false, true))
			return;

		if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
				&& sensorunreliable == false) {
			// Toast.makeText(SensorsActivity.this,
			// "Magnetic Sensor Unreliable, Please Re-Calibrate",Toast.LENGTH_SHORT).show();
			//
			// AlertDialog alertDialog = new
			// AlertDialog.Builder(SensorsActivity.this).create();
			// alertDialog.setTitle("Sensor Unreliable...");
			// alertDialog.setMessage("Magnetic Sensor Unreliable, Please Re-Calibrate");
			// alertDialog.setButton("OK", new DialogInterface.OnClickListener()
			// {
			// public void onClick(DialogInterface dialog, int which) {
			//
			// // //here you can add functions
			// //
			// } });
			// alertDialog.show();
			//sensorunreliable = true;
		}

		// if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
		// smoothed = LowPassFilter.filter(event.values, grav);
		// grav[0] = smoothed[0];
		// grav[1] = smoothed[1];
		// grav[2] = smoothed[2];
		// } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
		// smoothed = LowPassFilter.filter(event.values, mag);
		// mag[0] = smoothed[0];
		// mag[1] = smoothed[1];
		// mag[2] = smoothed[2];
		// }

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			grav[0] = event.values[0];
			grav[1] = event.values[1];
			grav[2] = event.values[2];
		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

			mag[0] = event.values[0];
			mag[1] = event.values[1];
			mag[2] = event.values[2];
		}

		// Get rotation matrix given the gravity and geomagnetic matrices
		SensorManager.getRotationMatrix(rotation, null, grav, mag);
		// SensorManager.remapCoordinateSystem (rotation, int X, int
		// Y,rotation);
		// getRotationMatrixFromVector
		SensorManager.getOrientation(rotation, orientation);
		floatBearing = orientation[0];

		// Convert from radians to degrees
		floatBearing = Math.toDegrees(floatBearing); // degrees east of true
														// north (180 to -180)

		// Compensate for the difference between true north and magnetic north
		if (gmf != null)
			floatBearing += gmf.getDeclination();

		// adjust to 0-360
		floatBearing -= kaabaoffset; // change later

		if (floatBearing < 0)
			floatBearing += 360;

		// System.out.println("floatBearing >"+floatBearing);
		GlobalData.setBearing((int) floatBearing);

		computing.set(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		System.out.println("sensor.getType() >>"+sensor.getType());
		System.out.println("in accuracy >>"+accuracy);
		if (sensorunreliable == false && sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			System.out.println("sensorunreliable is false");
			System.out.println("accuracy >>"+accuracy);
			System.out.println("SensorManager.SENSOR_STATUS_UNRELIABLE >>"+SensorManager.SENSOR_STATUS_UNRELIABLE);
			
		}else{
			System.out.println("sensorunreliable == true");
		}
		if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
				&& accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
				&& sensorunreliable == false) {
			// Log.w(TAG,"Compass data unreliable");
			//Toast.makeText(SensorsActivity.this,"Magnetic Sensor Not Accurate, Please Re-Calibrate   " +getResources().getString(R.string.unrelsensor),	Toast.LENGTH_SHORT).show();

//			AlertDialog alertDialog = new AlertDialog.Builder(
//					SensorsActivity.this).create();
//			alertDialog.setTitle("Sensor Unreliable.. \n" + getResources().getString(R.string.unrelsenstitle));
//			alertDialog.setIcon(R.drawable.recalib);
//			alertDialog.setMessage("Magnetic Sensor Unreliable, Please Re-Calibrate...    "+getResources().getString(R.string.unrelsensor));
//			//alertDialog.setMessage(R.string.unrelsensor);
//			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//
//					// //here you can add functions
//					//
//					finish();
//				}
//			});
//			alertDialog.show();
			sensorunreliable = true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void onLocationChanged(Location location) {
		if (location == null)
			throw new NullPointerException();
		currentLocation = (location);
		gmf = new GeomagneticField((float) currentLocation.getLatitude(),
				(float) currentLocation.getLongitude(),
				(float) currentLocation.getAltitude(),
				System.currentTimeMillis());
	}

	/**
	 * {@inheritDoc}
	 */
	public void onProviderDisabled(String provider) {
		// Ignore
	}

	/**
	 * {@inheritDoc}
	 */
	public void onProviderEnabled(String provider) {
		// Ignore
	}

	/**
	 * {@inheritDoc}
	 */
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Ignore
	}

	/*
	 * ==========================================================================
	 * ======================
	 */

	private static int EARTH_RADIUS_KM = 6371;

	public static double MILLION = 1000000;

	/**
	 * 
	 * Computes the bearing in degrees between two points on Earth.
	 * 
	 * 
	 * 
	 * @param lat1
	 *            Latitude of the first point
	 * 
	 * @param lon1
	 *            Longitude of the first point
	 * 
	 * @param lat2
	 *            Latitude of the second point
	 * 
	 * @param lon2
	 *            Longitude of the second point
	 * 
	 * @return Bearing between the two points in degrees. A value of 0 means due
	 * 
	 *         north.
	 */

	public static double kaababearing() {
		double lat1 = currentLocation.getLatitude();
		double lon1 = currentLocation.getLongitude();

		Location kaaba = new Location("ATL");
		kaaba.setLatitude(21.422534);
		kaaba.setLongitude(39.826205);
		kaaba.setAltitude(1);

		double lat2 = 21.422534;
		double lon2 = 39.826205;
		System.out.println("currentLocation >" + lat1 + " long> " + lon1);
		System.out.println("Distance >" + currentLocation.distanceTo(kaaba));
		float kaabadistance = Math
				.round(currentLocation.distanceTo(kaaba) / 1000);
		GlobalData.setKaabadistance(kaabadistance);
		double lat1Rad = Math.toRadians(lat1);

		double lat2Rad = Math.toRadians(lat2);

		double deltaLonRad = Math.toRadians(lon2 - lon1);

		double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);

		double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad)
				* Math.cos(lat2Rad)

				* Math.cos(deltaLonRad);

		return radToBearing(Math.atan2(y, x));

	}

	/**
	 * 
	 * Computes the bearing in degrees between two points on Earth.
	 * 
	 * 
	 * 
	 * @param p1
	 *            First point
	 * 
	 * @param p2
	 *            Second point
	 * 
	 * @return Bearing between the two points in degrees. A value of 0 means due
	 * 
	 *         north.
	 */

	// public static double bearing(GeoPoint p1, GeoPoint p2) {
	//
	// double lat1 = p1.getLatitudeE6() / MILLION;
	//
	// double lon1 = p1.getLongitudeE6() / MILLION;
	//
	// double lat2 = p2.getLatitudeE6() / MILLION;
	//
	// double lon2 = p2.getLongitudeE6() / MILLION;
	//
	//
	//
	// return bearing(lat1, lon1, lat2, lon2);
	//
	// }

	/**
	 * 
	 * Converts an angle in radians to degrees
	 */

	public static double radToBearing(double rad) {

		return (Math.toDegrees(rad) + 360) % 360;

	}

}
