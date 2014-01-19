package com.pocketscience;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MainService extends android.app.Service
{
    public static final String SERVER = "http://pocketscience.bugs3.com/";
    private static final String TAG = "MainService";

    @Override
    public IBinder onBind(Intent arg0)
    {
	return null;
    }

    @Override
    public void onCreate()
    {
	Toast.makeText(this, "Congrats! MainService Created", Toast.LENGTH_LONG).show();
	Log.d(TAG, "onCreate");
    }

    @Override
    public void onStart(Intent intent, int startId) // THIS IS DEPRECATED, CHANGE IT TO onStartCommand()
    {
	Toast.makeText(this, "MainService Started", Toast.LENGTH_LONG).show();
	Log.d(TAG, "onStart");
	// Note: You can start a new thread and use it for long background
	// processing from here.
    }

    @Override
    public void onDestroy()
    {
	Toast.makeText(this, "MainService Stopped", Toast.LENGTH_LONG).show();
	Log.d(TAG, "onDestroy");
    }

    public boolean wifiEnabled()
    {
	WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	if (wifi.isWifiEnabled())
	    return true;
	else
	    return false;
    }

    public boolean wifiConnected()
    {
	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

	if (wifiNetwork != null && wifiNetwork.isConnected())
	    return true;
	else
	    return false;
    }

    public boolean mobileConnected()
    {
	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

	if (mobileNetwork != null && mobileNetwork.isConnected())
	    return true;
	else
	    return false;
    }

    private class isServerReachable extends AsyncTask<Void, Void, Boolean>
    {
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

		return reqResult;
	    }
	    catch (SocketTimeoutException e)
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
		// continue with the next state
		Toast.makeText(getApplicationContext(), "We have a connection!!!", Toast.LENGTH_LONG).show();
	    else
		// go back, wait and check again
		Toast.makeText(getApplicationContext(), "We do NOT have a connection!!!", Toast.LENGTH_LONG).show();
	}
    }

    private class downloadFile extends AsyncTask<String, Void, Boolean>
    {
	protected Boolean doInBackground(String... params)
	{
	    try
	    {
		//URL url = new URL("http://pocketscience.bugs3.com/drun.jar");
		URL url = new URL(SERVER + params[0]);
		HttpURLConnection c = (HttpURLConnection) url.openConnection();
		c.setRequestMethod("GET");
		c.setDoOutput(true);
		c.connect();

		//FileOutputStream fos = openFileOutput("drun.jar", MODE_PRIVATE);
		FileOutputStream fos = openFileOutput(params[0], MODE_PRIVATE);
		InputStream is = c.getInputStream();

		byte[] buffer = new byte[1024];
		int len1 = 0;
		while ((len1 = is.read(buffer)) != -1)
		{
		    fos.write(buffer, 0, len1);
		}
		fos.close();
		is.close();

		return true;
	    }
	    catch (IOException e)
	    {
		return false;
	    }
	}

	protected void onPostExecute(Boolean result)
	{
	    if (result)
		// go to next state
		Toast.makeText(getApplicationContext(), "Download successful!", Toast.LENGTH_LONG).show();
	    else
		// WHAT DO WE DO HERE??
		Toast.makeText(getApplicationContext(), "Update error!", Toast.LENGTH_LONG).show();

	}
    }

}