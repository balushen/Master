package com.pocketscience;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class DisplayInfo extends Activity
{
    TextView info;
    SystemInfo systemInfo = new SystemInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.display_info);

	/*
	 * MemoryInfo mi = new MemoryInfo(); ActivityManager activityManager =
	 * (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	 * activityManager.getMemoryInfo(mi); long availableMegs = mi.availMem /
	 * 1048576L; info.setText(Long.toString(availableMegs));
	 */
	info = (TextView) findViewById(R.id.infoTv);
	systemInfo.getInfo();
	info.setText(systemInfo.toString());
    }

}
/*
 * TextView info; String infoString ="Debug-infos:";
 * 
 * infoString += "\nCPU_ABI (ABI2): " + android.os.Build.CPU_ABI + " ("+
 * android.os.Build.CPU_ABI2 + ")";
 * 
 * infoString += "\nBoard (Brand): " + android.os.Build.BOARD + " ("+
 * android.os.Build.BRAND + ")"; infoString += "\nDevice (Display): " +
 * android.os.Build.DEVICE + " ("+ android.os.Build.DISPLAY + ")"; infoString +=
 * "\nHardware (Host): " + android.os.Build.HARDWARE + " ("+
 * android.os.Build.HOST + ")"; infoString += "\nId (Radio): " +
 * android.os.Build.ID + " ("+ android.os.Build.RADIO + ")"; infoString +=
 * "\nSerial (Tags): " + android.os.Build.SERIAL + " ("+ android.os.Build.TAGS +
 * ")"; infoString += "\nType (Unknown): " + android.os.Build.TYPE + " ("+
 * android.os.Build.UNKNOWN + ")"; infoString += "\nUser (Fingerprint): " +
 * android.os.Build.USER + " ("+ android.os.Build.FINGERPRINT + ")";
 * 
 * Integer drun; drun = Runtime.getRuntime().availableProcessors();
 */
// info.setText(infoString);
// info.setText(ReadCPUinfo());
// info.setText(drun.toString());
// info.setText(String.valueOf(getNumCores()));
// info.setText(String.valueOf(getMaxFreq(0)));

