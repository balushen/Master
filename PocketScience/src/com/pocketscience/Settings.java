package com.pocketscience;

import android.app.Activity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Settings extends Activity
{
    private SeekBar seekBar;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.settings);

	seekBar = (SeekBar)findViewById(R.id.seekBar1);
	textView = (TextView)findViewById(R.id.seekbarTV);
	textView.setText(seekBar.getProgress() + "%");
	
	seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
	    int progress = 50;
	  
	    @Override
	    public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
	      progress = progresValue;
	    }

	    @Override
	    public void onStartTrackingTouch(SeekBar seekBar) {
	      // Do something here, 
	      //if you want to do anything at the start of
	      // touching the seekbar
	    }

	    @Override
	    public void onStopTrackingTouch(SeekBar seekBar) {
	      // Display the value in textview
	      textView.setText(progress + "%");
	    }
	});
    }

}
