package com.pocketscience;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Date;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import dalvik.system.DexClassLoader;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class MainService extends android.app.IntentService
{
    public MainService()
    {
	super("PocketScienceService");
    }

    public static final String SERVER = "http://pocketscience.bugs3.com/";
    private static final String TAG = "MainService";

    private long enqueue;
    private DownloadManager dm;
    private Boolean connected, downloaded = false;
    private Boolean shouldLoad = true;
    private String downloadURL = "process.jar";
    
    private Class<?> implementation = null;
    private String className = "com.pocketscience.PSImplem1";
    private String dexPath = "";
    private File optimizedDexOutputPath;
    private DexClassLoader cl;
    private Interf object;

//    @Override
//    public IBinder onBind(Intent arg0)
//    {
//	return null;
//    }
//
//    @Override
//    public void onCreate()
//    {
//	Toast.makeText(this, "Congrats! MainService Created", Toast.LENGTH_LONG).show();
//	Log.d(TAG, "onCreate");
//    }

    private Notification buildForegroundNotification(String filename) {
	    NotificationCompat.Builder b=new NotificationCompat.Builder(this);

	    b.setOngoing(true);

	    b.setContentTitle("PocketScience")
	     .setContentText(filename)
	     .setSmallIcon(R.drawable.ic_launcher)
	     .setTicker("Ticker");

	    return(b.build());
	  }

    @Override
    protected void onHandleIntent(Intent intent)
    {
	//Get CPU lock, so it doesn't fall asleep when the screen is off
	PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
	WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
	wakeLock.acquire();
	
	// To release the lock => wakeLock.release();
	
	SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
	String currentState = "";

	SharedPreferences.Editor editor = wmbPreference.edit();
	    
	startForeground(1987, buildForegroundNotification("Working..."));
	
	while(true)
	{
	    
	    currentState = wmbPreference.getString("STATE", "INIT");
	    
	    switch(currentState)
	    {
	    case "INIT":
		//region INIT
		AsyncTask<String, Void, Boolean> checkConnectivity = new IsServerReachable();
		checkConnectivity.execute("http://www.google.com");
		
		try
		{
		    checkConnectivity.get();
		}
		catch (InterruptedException e)
		{
		}
		catch (ExecutionException e)
		{
		}
		
		if(connected)
		{
		    connected = false;
		    AsyncTask<String, Void, Boolean> checkConnectivityServer = new IsServerReachable();
		    checkConnectivityServer.execute("http://pocketscience.bugs3.com");
		    
		    try
		    {
			checkConnectivityServer.get();
		    }
		    catch (InterruptedException e)
		    {
		    }
		    catch (ExecutionException e)
		    {
		    }
		    
		    if(connected)
		    {
			connected = false;
			Boolean thereIsAJob = true;
			
			// CHECK IF THERE IS A JOB TO DOWNLOAD
			// Get a download url to pass as a parameter			
			if(thereIsAJob)
			{
			    /*downloadURL = "process.jar";
			    className = "com.pocketscience.PSImplem1";
			    
			    editor.putString("STATE", "DOWN");
			    editor.putString("PREV_STATE", "INIT");
			    editor.commit();*/
			    
				AsyncTask<Void, Void, Exception> uploadJob = new UploadFilesTask();
				uploadJob.execute();
				
				try
				{
				    uploadJob.get();
				}
				catch (Exception e)
				{
				    e.printStackTrace();
				}
				
				editor.putString("STATE", "WAIT_J");
				editor.putString("PREV_STATE", "INIT");
				editor.commit();
			}
			else
			{
			    editor.putString("STATE", "WAIT_J");
			    editor.putString("PREV_STATE", "INIT");
			    editor.commit();
			}
		    }
		    else
		    {
			editor.putString("STATE", "WAIT_S");
			editor.putString("PREV_STATE", "INIT");
			editor.commit();
		    }
		}
		else
		{
		    editor.putString("STATE", "WAIT_I");
		    editor.putString("PREV_STATE", "INIT");
		    editor.commit();
		}
				
		break;
		//endregion
		
	    case "DOWN":
		//region DOWN
		AsyncTask<String, Void, Boolean> downloadJob = new DownloadFileTask();
		downloadJob.execute(downloadURL);
		
		try
		{
		    downloadJob.get();
		}
		catch (InterruptedException e)
		{
		}
		catch (ExecutionException e)
		{
		}
		
		if(downloaded)
		{
		    editor.putString("STATE", "PROC");
		    editor.putString("PREV_STATE", "DOWN");
		    editor.commit();		    
		}
		else
		{
		    // ????
		}

		//endregion
		break;
		
	    case "PROC":
		//region PROC
		if(shouldLoad)
		{
		    // load assembly first
		    //dexPath = getExternalFilesDir(null).getPath() + "/" + downloadURL;
		    dexPath = this.getFilesDir() + "/" + downloadURL;
		    optimizedDexOutputPath = getDir("outdex", Context.MODE_PRIVATE);
		    cl = new DexClassLoader(dexPath, optimizedDexOutputPath.getAbsolutePath(), null, this.getClass().getClassLoader());
		    
		    try
		    {
			implementation = cl.loadClass(className);
        		try
        		{
        		    object = (Interf) implementation.newInstance();        		    
        		}
        		catch (InstantiationException e)
        		{
        		    e.printStackTrace();
        		    Toast.makeText(getApplicationContext(), "InstantiationException!", Toast.LENGTH_LONG).show();
        		}
        		catch (IllegalAccessException e)
        		{
        		    e.printStackTrace();
        		    Toast.makeText(getApplicationContext(), "IllegalAccessException!", Toast.LENGTH_LONG).show();
        		}
		    }
		    catch (ClassNotFoundException e)
		    {
			editor.putString("STATE", "INIT");
			editor.commit();
			
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "ClassNotFoundException!", Toast.LENGTH_LONG).show();
		    }
		}

		// just start new thread and process
		if(object != null){
		    //object.show(getApplicationContext(), "PSImplem1 PLQSSSS");
		    
		    //new Thread(new Runnable() {
		    //    public void run() {
		            object.process(getApplicationContext());
		    //    }
		   // }).start();

		    
		    editor.putString("STATE", "UP");
		    editor.putString("PREV_STATE", "PROC");
		    editor.commit();
		}
		
		//endregion
		break;

	    case "UP":
		//region UP
		
		AsyncTask<Void, Void, Exception> uploadJob = new UploadFilesTask();
		uploadJob.execute();
		
		try
		{
		    uploadJob.get();
		}
		catch (InterruptedException e)
		{
		}
		catch (ExecutionException e)
		{
		}
		
		// if there is more data for the same job set shouldLoad to false!
		
		editor.putString("STATE", "INIT");
		editor.putString("PREV_STATE", "UP");
		editor.commit();
		
		
		//SharedPreferences.Editor editor = wmbPreference.edit();
		//editor.putString("STATE", "WAIT_I");
		//editor.putString("PREV_STATE", "UP");
		//editor.commit();
		
		//endregion
		break;

	    case "WAIT_I":
		// The next state after the wait depends on the PREV_STATE
		
		try
		{
		    Thread.sleep(60000); // 1 minute wait
		}
		catch (InterruptedException e)
		{
		    e.printStackTrace();
		}
				
		editor.putString("STATE", "INIT");
		editor.putString("PREV_STATE", "WAIT_I");
		editor.commit();
		
		break;

	    case "WAIT_S":
		
		try
		{
		    Thread.sleep(60000); // 1 minute wait
		}
		catch (InterruptedException e)
		{
		    e.printStackTrace();
		}
				
		editor.putString("STATE", "INIT");
		editor.putString("PREV_STATE", "WAIT_S");
		editor.commit();
		
		break;

	    case "WAIT_J":
		
		try
		{
		    Thread.sleep(3600000); // 1 hour wait
		}
		catch (InterruptedException e)
		{
		    e.printStackTrace();
		}
				
		editor.putString("STATE", "INIT");
		editor.putString("PREV_STATE", "WAIT_J");
		editor.commit();
		
		break;
		
	    default:
		break;
	    }
	}
    }

//    @Override
//    public void onStart(Intent intent, int startId) // THIS IS DEPRECATED, CHANGE IT TO onStartCommand()
//    {
//	Toast.makeText(this, "MainService Started", Toast.LENGTH_LONG).show();
//	Log.d(TAG, "onStart");
//	// Note: You can start a new thread and use it for long background
//	// processing from here.
//	
//	//Get CPU lock, so it doesn't fall asleep when the screen is off
//	PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//	WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
//	wakeLock.acquire();
//	
//	// To release the lock => wakeLock.release();
//	
//	SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
//	String currentState = "";
//
//	SharedPreferences.Editor editor = wmbPreference.edit();
//	    
//	while(true)
//	{
//	    currentState = wmbPreference.getString("STATE", "INIT");
//	    
//	    switch(currentState)
//	    {
//	    case "INIT":
//		//region INIT
//		AsyncTask<String, Void, Boolean> checkConnectivity = new IsServerReachable();
//		checkConnectivity.execute("http://www.google.com");
//		
//		try
//		{
//		    checkConnectivity.get();
//		}
//		catch (InterruptedException e)
//		{
//		}
//		catch (ExecutionException e)
//		{
//		}
//		
//		if(connected)
//		{
//		    connected = false;
//		    AsyncTask<String, Void, Boolean> checkConnectivityServer = new IsServerReachable();
//		    checkConnectivityServer.execute("http://pocketscience.bugs3.com");
//		    
//		    try
//		    {
//			checkConnectivityServer.get();
//		    }
//		    catch (InterruptedException e)
//		    {
//		    }
//		    catch (ExecutionException e)
//		    {
//		    }
//		    
//		    if(connected)
//		    {
//			connected = false;
//			Boolean thereIsAJob = true;
//			
//			// CHECK IF THERE IS A JOB TO DOWNLOAD
//			// Get a download url to pass as a parameter			
//			if(thereIsAJob)
//			{
//			    downloadURL = "process.jar";
//			    className = "com.pocketscience.PSImplem1";
//			    
//			    editor.putString("STATE", "DOWN");
//			    editor.putString("PREV_STATE", "INIT");
//			    editor.commit();
//			}
//			else
//			{
//			    editor.putString("STATE", "WAIT_J");
//			    editor.putString("PREV_STATE", "INIT");
//			    editor.commit();
//			}
//		    }
//		    else
//		    {
//			editor.putString("STATE", "WAIT_S");
//			editor.putString("PREV_STATE", "INIT");
//			editor.commit();
//		    }
//		}
//		else
//		{
//		    editor.putString("STATE", "WAIT_I");
//		    editor.putString("PREV_STATE", "INIT");
//		    editor.commit();
//		}
//				
//		break;
//		//endregion
//		
//	    case "DOWN":
//		//region DOWN
//		AsyncTask<String, Void, Boolean> downloadJob = new DownloadFileTask();
//		downloadJob.execute(downloadURL);
//		
//		try
//		{
//		    downloadJob.get();
//		}
//		catch (InterruptedException e)
//		{
//		}
//		catch (ExecutionException e)
//		{
//		}
//		
//		if(downloaded)
//		{
//		    editor.putString("STATE", "PROC");
//		    editor.putString("PREV_STATE", "DOWN");
//		    editor.commit();		    
//		}
//		else
//		{
//		    // ????
//		}
//
//		//endregion
//		break;
//		
//	    case "PROC":
//		//region PROC
//		if(shouldLoad)
//		{
//		    // load assembly first
//		    //dexPath = getExternalFilesDir(null).getPath() + "/" + downloadURL;
//		    dexPath = this.getFilesDir() + "/" + downloadURL;
//		    optimizedDexOutputPath = getDir("outdex", Context.MODE_PRIVATE);
//		    cl = new DexClassLoader(dexPath, optimizedDexOutputPath.getAbsolutePath(), null, this.getClass().getClassLoader());
//		    
//		    try
//		    {
//			implementation = cl.loadClass(className);
//        		try
//        		{
//        		    object = (Interf) implementation.newInstance();        		    
//        		}
//        		catch (InstantiationException e)
//        		{
//        		    e.printStackTrace();
//        		    Toast.makeText(getApplicationContext(), "InstantiationException!", Toast.LENGTH_LONG).show();
//        		}
//        		catch (IllegalAccessException e)
//        		{
//        		    e.printStackTrace();
//        		    Toast.makeText(getApplicationContext(), "IllegalAccessException!", Toast.LENGTH_LONG).show();
//        		}
//		    }
//		    catch (ClassNotFoundException e)
//		    {
//			editor.putString("STATE", "INIT");
//			editor.commit();
//			
//			e.printStackTrace();
//			Toast.makeText(getApplicationContext(), "ClassNotFoundException!", Toast.LENGTH_LONG).show();
//		    }
//		}
//
//		// just start new thread and process
//		if(object != null){
//		    //object.show(getApplicationContext(), "PSImplem1 PLQSSSS");
//		    
//		    //new Thread(new Runnable() {
//		    //    public void run() {
//		            object.process(getApplicationContext());
//		    //    }
//		   // }).start();
//
//		    
//		    editor.putString("STATE", "UP");
//		    editor.putString("PREV_STATE", "PROC");
//		    editor.commit();
//		}
//		
//		//endregion
//		break;
//
//	    case "UP":
//		//region UP
//		
//		AsyncTask<Void, Void, Exception> uploadJob = new UploadFilesTask();
//		uploadJob.execute();
//		
//		try
//		{
//		    uploadJob.get();
//		}
//		catch (InterruptedException e)
//		{
//		}
//		catch (ExecutionException e)
//		{
//		}
//		
//		// if there is more data for the same job set shouldLoad to false!
//		
//		editor.putString("STATE", "INIT");
//		editor.putString("PREV_STATE", "UP");
//		editor.commit();
//		
//		
//		//SharedPreferences.Editor editor = wmbPreference.edit();
//		//editor.putString("STATE", "WAIT_I");
//		//editor.putString("PREV_STATE", "UP");
//		//editor.commit();
//		
//		//endregion
//		break;
//
//	    case "WAIT_I":
//		// The next state after the wait depends on the PREV_STATE
//		
//		try
//		{
//		    Thread.sleep(60000);
//		}
//		catch (InterruptedException e)
//		{
//		    e.printStackTrace();
//		}
//				
//		editor.putString("STATE", "INIT");
//		editor.putString("PREV_STATE", "WAIT_I");
//		editor.commit();
//		
//		break;
//
//	    case "WAIT_S":
//		
//		try
//		{
//		    Thread.sleep(60000);
//		}
//		catch (InterruptedException e)
//		{
//		    e.printStackTrace();
//		}
//				
//		editor.putString("STATE", "INIT");
//		editor.putString("PREV_STATE", "WAIT_S");
//		editor.commit();
//		
//		break;
//
//	    case "WAIT_J":
//		
//		try
//		{
//		    Thread.sleep(60000);
//		}
//		catch (InterruptedException e)
//		{
//		    e.printStackTrace();
//		}
//				
//		editor.putString("STATE", "INIT");
//		editor.putString("PREV_STATE", "WAIT_J");
//		editor.commit();
//		
//		break;
//		
//	    default:
//		break;
//	    }
//	}
//    }
//
//    @Override
//    public void onDestroy()
//    {
//	Toast.makeText(this, "MainService Stopped", Toast.LENGTH_LONG).show();
//	Log.d(TAG, "onDestroy");
//    }

    public boolean IsWifiEnabled()
    {
	WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	
	return wifi.isWifiEnabled();
    }

    public boolean IsWifiConnected()
    {
	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

	if (wifiNetwork != null && wifiNetwork.isConnected())
	    return true;
	else
	    return false;
    }

    public boolean IsMobileConnected()
    {
	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

	if (mobileNetwork != null && mobileNetwork.isConnected())
	    return true;
	else
	    return false;
    }

    private class IsServerReachable extends AsyncTask<String, Void, Boolean>
    {
	Boolean reqResult = false;

	protected Boolean doInBackground(String... url)
	{
	    try
	    {
		URL reqUrl = new URL(url[0]);
		HttpGet request = new HttpGet(reqUrl.toString());
		HttpParams httpParameters = new BasicHttpParams();

		HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
		HttpClient httpClient = new DefaultHttpClient(httpParameters);
		HttpResponse response = httpClient.execute(request);

		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
		{
		    connected = true;
		    reqResult = true;
		}

		connected = reqResult;
		return reqResult;
	    }
	    catch (SocketTimeoutException e)
	    {
		e.printStackTrace();
		connected = false;
		return false;
	    }
	    catch (MalformedURLException e)
	    {
		e.printStackTrace();
		connected = false;
		return false;
	    }
	    catch (ClientProtocolException e)
	    {
		e.printStackTrace();
		connected = false;
		return false;
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
		connected = false;
		return false;
	    }
	}

/*	protected void onPostExecute(Boolean result)
	{
	    if (result)
		// continue with the next state
		//Toast.makeText(getApplicationContext(), "We have a connection!!!", Toast.LENGTH_LONG).show();
		connected = true;
	    else
		// go back, wait and check again
		//Toast.makeText(getApplicationContext(), "We do NOT have a connection!!!", Toast.LENGTH_LONG).show();
		connected = false;
	}*/
    }

    private class DownloadFileTask extends AsyncTask<String, Void, Boolean>
    {
	protected Boolean doInBackground(String... fileUrl)
	{
	    try
	    {
		//URL url = new URL("http://pocketscience.bugs3.com/drun.jar");
		URL url = new URL(SERVER + fileUrl[0]);
		HttpURLConnection c = (HttpURLConnection) url.openConnection();
		c.setRequestMethod("GET");
		c.setDoOutput(true);
		c.connect();

		//FileOutputStream fos = openFileOutput("drun.jar", MODE_PRIVATE);
		FileOutputStream fos = openFileOutput(fileUrl[0], MODE_PRIVATE);
		
		//File file = new File(getExternalFilesDir(null).getPath() + "/" + downloadURL);
		//FileOutputStream fos = new FileOutputStream(file);
		
		InputStream is = c.getInputStream();

		byte[] buffer = new byte[1024];
		int len1 = 0;
		while ((len1 = is.read(buffer)) != -1)
		{
		    fos.write(buffer, 0, len1);
		}
		fos.close();
		is.close();

		downloaded = true;
		return true;
	    }
	    catch (IOException e)
	    {
		downloaded = false;
		return false;
	    }
	}

/*	protected void onPostExecute(Boolean result)
	{
	    if (result)
	    {
		//Toast.makeText(getApplicationContext(), "Download successful!", Toast.LENGTH_LONG).show();
		downloaded = true;
	    }
	    else
	    {
		//Toast.makeText(getApplicationContext(), "Update error!", Toast.LENGTH_LONG).show();
		downloaded = false;
	    }

	}*/
    }

    private class UploadFilesTask extends AsyncTask<Void, Void, Exception>
    {
	protected Exception doInBackground(Void... params)
	{
	    try
	    {
	        String boundary = "*****";
	        String lineEnd = "\r\n";
	        String twoHyphens = "--";
	        int bytesRead, bytesAvailable, bufferSize;	     
	        
	        //String fileName = getExternalFilesDir(null).getPath() + "/process.jar"; 
	        String fileName = getFilesDir() + "/process.rst"; 
	        
	        fileName = "/pocketscience.txt";
	        File file = new File(getExternalFilesDir(null).getPath() + fileName);
	        if (!file.exists()) {
	            try {
	                file.createNewFile();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        
	        byte[] buffer;
	        int maxBufferSize = 1 * 1024 * 1024; 
		Exception nullException = null;
		Random rnd = new Random();
		Integer version = rnd.nextInt((999 - 1) + 1) + 1;

                //FileInputStream fileInputStream = new FileInputStream(fileName);
                FileInputStream fileInputStream = new FileInputStream(getExternalFilesDir(null).getPath() + fileName);
		URL url = new URL("http://pocketscience.bugs3.com/upload_script.php");
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                //conn.setRequestProperty("uploaded_file", fileName + version.toString()); 
                conn.setRequestProperty("uploaded_file", fileName); 
                
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                
                dos.writeBytes(twoHyphens + boundary + lineEnd); 
                //dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + version.toString() + "\"" + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
                 
                dos.writeBytes(lineEnd);
                
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available(); 
       
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
       
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
                   
                while (bytesRead > 0) {
                     
                  dos.write(buffer, 0, bufferSize);
                  bytesAvailable = fileInputStream.available();
                  bufferSize = Math.min(bytesAvailable, maxBufferSize);
                  bytesRead = fileInputStream.read(buffer, 0, bufferSize);   
                   
                 }
       
                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
         
                if(conn.getResponseCode() == 200){
                     
            /*        runOnUiThread(new Runnable() {
                         public void run() {                          
                             Toast.makeText(Download.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
                         }
                     });          */      
                }    
                else {
            /*        runOnUiThread(new Runnable() {
                        public void run() {                          
                            Toast.makeText(Download.this, "Code was NOT 200 - OK", Toast.LENGTH_SHORT).show();
                        }
                    });  */
                }
                 
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

             	return nullException;
                  
           } catch (MalformedURLException ex) {
                
               ex.printStackTrace();
                
              /* runOnUiThread(new Runnable() {
                   public void run() {
                       Toast.makeText(Download.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                   }
               }); */
                
           } catch (Exception e) {                
                
          /*     runOnUiThread(new Runnable() {
                   public void run() {
                       Toast.makeText(Download.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
                   }
               }); */
           }
	    return new Exception("null");	
	}

	protected void onPostExecute(Exception result)
	{
	    if (result != null)
	    {
		Toast.makeText(getApplicationContext(), "Upload NOT successful!", Toast.LENGTH_LONG).show();
	    }
	}
    }    
    
    public void DownloadWithManager(String Url)
    {
	dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
	//Request request = new Request(Uri.parse("http://pocketscience.bugs3.com/Conn.zip"));
	//Request request = new Request(Uri.parse("http://pocketscience.bugs3.com/drun.jar"));
	Request request = new Request(Uri.parse(Url));
	
	request.setAllowedNetworkTypes(
	     DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
	     .setAllowedOverRoaming(false).setTitle("Test")
	     .setDescription("Something useful. No, really.")
	     .setDestinationUri(Uri.fromFile(new File(getExternalFilesDir(null).getPath() + "/drun.jar")));
	enqueue = dm.enqueue(request);
    }
}