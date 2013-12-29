package com.pocketscience;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MainService extends android.app.Service 
{
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
	public void onStart(Intent intent, int startId) 
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
}