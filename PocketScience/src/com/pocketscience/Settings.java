package com.pocketscience;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Settings extends Activity
{
    private Integer mLoad, mMinBat;
    private Boolean m3G, mCharge, mAuto, mNotify;
    private SeekBar seekBar;
    private TextView tvBatteryLevel;
    private CheckBox cb3G, cbCharge, cbAutostart, cbNotify;
    private RadioGroup rgSystemLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.settings);

	SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
	final SharedPreferences.Editor editor = wmbPreference.edit();
	
	seekBar = (SeekBar)findViewById(R.id.batteryBar);
	tvBatteryLevel = (TextView)findViewById(R.id.tvBatteryLevel);
	cb3G = (CheckBox)findViewById(R.id.cb3G);
	cbCharge = (CheckBox)findViewById(R.id.cbCharge);
	cbAutostart = (CheckBox)findViewById(R.id.cbAutostart);
	cbNotify = (CheckBox)findViewById(R.id.cbNotify);
	rgSystemLoad = (RadioGroup) findViewById(R.id.rgSystemLoad);  

	getSettings(wmbPreference);
	
	applySettings();
	
	rgSystemLoad.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
	{	   
	    @Override
	    public void onCheckedChanged(RadioGroup group, int checkedId)
	    {
		editor.putInt("LOAD", checkedId);
		editor.commit();		
	    }
	});
	
	cb3G.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
	{	    
	    @Override
	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	    {
		editor.putBoolean("3G", isChecked);	
		editor.commit();	
	    }
	});	
	
	cbCharge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
	{	    
	    @Override
	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	    {
		editor.putBoolean("CHARGE", isChecked);	
		editor.commit();	
	    }
	});	
	
	cbAutostart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
	{	    
	    @Override
	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	    {
		editor.putBoolean("AUTO", isChecked);	
		editor.commit();			    
	    }
	});
	
	cbNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
	{	    
	    @Override
	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	    {
		editor.putBoolean("NOTIFY", isChecked);	
		editor.commit();			    
	    }
	});
	
	seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
	    int progress = 50;
	  
	    @Override
	    public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
	      progress = progresValue;
	    }

	    @Override
	    public void onStartTrackingTouch(SeekBar seekBar) {
	    }

	    @Override
	    public void onStopTrackingTouch(SeekBar seekBar) {
	        // Display the value in textview
		tvBatteryLevel.setText(progress + "%");
		
		editor.putInt("MIN_BAT", progress);
		editor.commit();
	    }
	});
    }

    private void applySettings()
    {
	rgSystemLoad.check(mLoad);
	cb3G.setChecked(m3G);
	cbCharge.setChecked(mCharge);
	cbAutostart.setChecked(mAuto);
	cbNotify.setChecked(mNotify);
	seekBar.setProgress(mMinBat);
	tvBatteryLevel.setText(mMinBat + "%");	
    }

    private void getSettings(SharedPreferences wmbPreference)
    {
	mLoad = wmbPreference.getInt("LOAD", 1);
	mMinBat = wmbPreference.getInt("MIN_BAT", 50);
	m3G = wmbPreference.getBoolean("3G", false);
	mCharge = wmbPreference.getBoolean("CHARGE", true);
	mAuto = wmbPreference.getBoolean("AUTO", false);
	mNotify = wmbPreference.getBoolean("NOTIFY", false);
    }
}
