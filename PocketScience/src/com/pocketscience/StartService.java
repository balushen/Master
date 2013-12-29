package com.pocketscience;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class StartService extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.service);
	//startService(new Intent(this, MainService.class));

	//Intent intent = new Intent(Intent.ACTION_MAIN);
	//intent.addCategory(Intent.CATEGORY_HOME);
	//startActivity(intent);
	//finish();
    }

    // start the service
    public void onClickStartService(View V)
    {
	startService(new Intent(this, MainService.class));
    }

    // Stop the started service
    public void onClickStopService(View V)
    {
	// Service will only stop if it is already running.
	stopService(new Intent(this, MainService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	// Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.activity_main, menu);
	return true;
    }

}
