package com.planetapps.qiblacompass;





import com.planetapps.qiblacompass.data.GlobalData;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SplashScreen extends Activity  implements   android.content.DialogInterface.OnClickListener{
	
	protected int _splashTime = 1500; 
	
	private Thread splashTread;
    private LocationManager locManager;
	private LocationListener locListener = new MyLocationListener();
	public Location currentLocation = null;
	
	private boolean gps_enabled = false;
	private boolean network_enabled = false;  
	private ProgressBar progress;	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    //Remove title bar
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);

	    //Remove notification bar
	    //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    
	    setContentView(R.layout.splash);
	    
	    	    
	    final SplashScreen sPlashScreen = this; 
	    
		progress = (ProgressBar) findViewById(R.id.progressBar1);
		progress.setVisibility(View.GONE);
        
		locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		try {
			Location gps = locManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			Location network = locManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			
			//onGetLocation();
			if (gps != null){
				System.out.println("LastKnown gps loc");
				currentLocation = (gps); 
				launchCompass();
			}
			else if (network != null){
				currentLocation = (network);
				System.out.println("LastKnown network loc");
				launchCompass();
			}
			else
			{
				System.out.println("NO LastKnown loc, call onGetLocation()");
				onGetLocation();
			}
		} catch (Exception ex2) {
			System.out.println("in exception > call onGetLocation()");
			onGetLocation();
		}	    
		GlobalData.setCurrentLocation(currentLocation);
		
		
		
	    // thread for displaying the SplashScreen
//	    splashTread = new Thread() {
//	        @Override
//	        public void run() {
//	            try {	            	
//	            	synchronized(this){
//	            		wait(_splashTime);
//	            	}
//	            	
//	            } catch(InterruptedException e) {} 
//	            finally {
//	                finish();
//	                
//	                Intent i = new Intent();
//	                i.setClass(sPlashScreen, ArabiccompassActivity.class);
//	        		startActivity(i);
//	                
//	        		interrupt();
//	            }
//	        }
//	    };
//	    
//	    splashTread.start();
	}
	public void launchCompass()
	{
        Intent i = new Intent();
        i.setClass(this, QiblacompassActivity.class);
		startActivity(i);
		//SplashScreen.this.finish();
	}
	
	
	public void onGetLocation() {
		//turnGPSOn();
		
		
		progress.setVisibility(View.VISIBLE);
		 String locmsg = getResources().getString(R.string.locmsg);
		Toast toast=Toast.makeText(this, locmsg, 3000);
	     toast.setGravity(Gravity.TOP, 0, 0);
	     toast.show();
		 Toast toast2=Toast.makeText(this, "Getting Current Location.. please wait  \n"+locmsg, 3000);
	     toast2.setGravity(Gravity.BOTTOM, 0, 0);
	     toast2.show();
		// exceptions will be thrown if provider is not permitted.
		try {
			gps_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			System.out.println("gps_enabled ?");
		} catch (Exception ex) {
		}
		try {
			network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			System.out.println("network_enabled ?");
		} catch (Exception ex) {
		}

		// don't start listeners if no provider is enabled
		if (!gps_enabled && !isNetworkAvailable()) {
			System.out.println("no gps,network_enabled >");
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("Attention!  "+getResources().getString(R.string.arwarning));
			builder.setMessage("Sorry, location is not determined. Please enable GPS  \n"+ getResources().getString(R.string.engps));
			builder.setPositiveButton("OK", this);
			builder.setNeutralButton("Cancel", this);
			builder.setIcon(R.drawable.gps);
			builder.create().show();
			progress.setVisibility(View.GONE);
		}

		//if (gps_enabled) 
		{
			System.out.println("gps,requestLocationUpdates >");
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, locListener);
		}
		if (network_enabled || isNetworkAvailable()) 
		{
			locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 100, locListener);
		}
	}
	
	class MyLocationListener implements LocationListener {
		public void onLocationChanged(Location location) {
			if (location != null) {
				// This needs to stop getting the location data and save the battery power.
				locManager.removeUpdates(locListener); 

				String londitude = "Londitude: " + location.getLongitude();
				String latitude = "Latitude: " + location.getLatitude();
				String altitiude = "Altitiude: " + location.getAltitude();
				String accuracy = "Accuracy: " + location.getAccuracy();
				String time = "Time: " + location.getTime();

				System.out.println("londitude >"+londitude);
				System.out.println("Latitude >"+latitude);
				System.out.println("Altitiude >"+altitiude);
				System.out.println("Accuracy >"+accuracy);
				System.out.println("time >"+time);
				//editTextShowLocation.setText(londitude + "\n" + latitude + "\n" + altitiude + "\n" + accuracy + "\n" + time);
		        String str = "\n CurrentLocation: "+
		                "\n Latitude: "+ location.getLatitude() + 
		                "\n Longitude: " + location.getLongitude() + 
		                "\n Accuracy: " + location.getAccuracy() + 
		                "\n CurrentTimeStamp "+ location.getTime();         
		                  Toast.makeText(SplashScreen.this,str,Toast.LENGTH_LONG).show();
		                  //tv.append(str);               
				currentLocation = location;
				GlobalData.setCurrentLocation(currentLocation);
				progress.setVisibility(View.GONE);
				turnGPSOff();
				
				launchCompass();
				//startProcess();
			}
		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}
	}
    
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null;
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
    	
    	try {
			locManager.removeUpdates(locListener);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(provider.contains("gps")){ //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3")); 
            sendBroadcast(poke);
        }
    }
	public void onClick(DialogInterface dialog, int which) {
		if(which == DialogInterface.BUTTON_NEUTRAL){
			//editTextShowLocation.setText("Sorry, location is not determined. To fix this please enable location providers");
		}else if (which == DialogInterface.BUTTON_POSITIVE) {
			startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			progress.setVisibility(View.VISIBLE);
			gps_enabled = true;
		}
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
    	turnGPSOff();
    	super.onDestroy();

    	System.out.println("in onDestroy >");

        
        finish();
        System.runFinalizersOnExit(true);
        System.exit(0);

    }	
	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//	    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//	    	synchronized(splashTread){
//	    		splashTread.notifyAll();
//	    	}
//	    }
//	    return true;
//	}
	
}
