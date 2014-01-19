package com.pocketscience;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
	boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
	if (isFirstRun)
	{
	    //======= Code to run once ========
	    // 1. evaluate
	    Intent display = new Intent("com.pocketscience.DISPLAY");
	    startActivity(display);
	    // 2. check connectivity (wifi, internet, server)
	    new isServerReachable().execute();
	    // 3. send information to server (MAC too?)
	    // 4. wait confirmation from server and then set the pref variable to false
	    SharedPreferences.Editor editor = wmbPreference.edit();
	    editor.putBoolean("FIRSTRUN", false);
	    editor.commit();
	}
	
	if (isMyServiceRunning()) //If the service is already running open the management console
	{
	    finish();
	    Intent service = new Intent("com.pocketscience.SERVICE");
	    startActivity(service);
	}
	else //If the service is not yet started, start it and go on with the Activity
	{
	    startService(new Intent(this, MainService.class));
	}
	setContentView(R.layout.activity_main);
	//Toast.makeText(getApplicationContext(), "Hello!", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
    }

    private class isServerReachable  extends AsyncTask<Void, Void, Boolean>
    {
	WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	Boolean reqResult = false;
	
	protected Boolean doInBackground(Void... params)
	{
	    try
	    {    
		URL reqUrl = new URL("http://pocketscience.bugs3.com");
		HttpGet request = new HttpGet(reqUrl.toString());
		HttpParams httpParameters = new BasicHttpParams();  

		HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
		HttpClient httpClient = new DefaultHttpClient(httpParameters);
		HttpResponse response = httpClient.execute(request);	

		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) 
		{
		    reqResult = true;
		}
		
		if (wifi.isWifiEnabled() && wifiNetwork != null && wifiNetwork.isConnected() && reqResult)
		    return true;
		else
		    return false;
	    }
	    catch (SocketTimeoutException  e)
	    {		
		e.printStackTrace();
		return false;
	    }
	    catch (MalformedURLException e)
	    {		
		e.printStackTrace();
		return false;
	    }
	    catch (ClientProtocolException e)
	    {
		e.printStackTrace();
		return false;
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
		return false;
	    }
	}

	protected void onPostExecute(Boolean result)
	{
	    if (result)
		Toast.makeText(getApplicationContext(), "We have a connection!!!", Toast.LENGTH_LONG).show();
	    else 
		Toast.makeText(getApplicationContext(), "We do NOT have a connection!!!", Toast.LENGTH_LONG).show();
	}	
    }
    
    private boolean isMyServiceRunning() 
    {
	ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) 
	{
	    if (MainService.class.getName().equals(service.service.getClassName())) 
	    {
    		return true;
	    }
	}
	return false;
    }
}