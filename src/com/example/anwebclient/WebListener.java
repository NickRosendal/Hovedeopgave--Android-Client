package com.example.anwebclient;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.example.Mjpeg.MjpegInputStream;
import com.example.Mjpeg.MjpegView;
import com.example.webClient.CommandClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;

public class WebListener extends Activity implements com.example.designPatterns.ObserverPattern_Observer
{
	String videoURL;

	CommandClient myCommandClient = null;
	com.example.Mjpeg.MjpegView mj;
	ReadVideoStream myReadVideoStream =new ReadVideoStream();
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
	//s	MjpegView myMv = (MjpegView) findViewById(R.id.mv);
		//myMv.setMinimumHeight((int)(myMv.getWidth() * 0.75));
		
		
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_listener);
		MyApplication mApplication = (MyApplication) getApplicationContext();
		if (mApplication.getCommandClient() == null)
		{
			Log.i("AnWebClient", "Error Weblistner not using application commandClient");
			// to back to previous activity if the commandS
			startActivity(new Intent(WebListener.this, MainActivity.class));
		} else
		{
			myCommandClient = mApplication.getCommandClient();

			myCommandClient.registerObserver(this);
			mj = (MjpegView) findViewById(R.id.mv);
			 videoURL = "http://" + myCommandClient.getAddress().getHostAddress() + ":8080/GetStream";
			askForVideo();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_listener, menu);
		return true;
	}

	@Override
	public void update(String eventData)
	{
		// TODO Auto-generated method stub
		// "video server is ready"
		// image is ready

		if (eventData.equals("video server is ready"))
		{
			
			this.video();
		} else if (eventData.equals("image is ready"))
		{
			image();
		} else if (eventData.equals("swipe"))
		{
			swipe();
		}
	}

	// swipe recived
	private void swipe()
	{

	}

	// video reviced
	private void video()
	{
		myReadVideoStream.execute(videoURL);
	}

	// image recived
	private void image()
	{

	}

	private void askForVideo()
	{
		myCommandClient.send("start video server");
	}

	private void askForImage()
	{
		myCommandClient.send("take picture");
	}

	public class ReadVideoStream extends AsyncTask<String, Void, MjpegInputStream>
	{
		protected MjpegInputStream doInBackground(String... url)
		{
			HttpResponse res = null;
			DefaultHttpClient httpclient = new DefaultHttpClient();
			// Log.d(TAG, "1. Sending http request");
			try
			{
				res = httpclient.execute(new HttpGet(URI.create(url[0])));
				// Log.d(TAG, "2. Request finished, status = " +
				// res.getStatusLine().getStatusCode());
				if (res.getStatusLine().getStatusCode() == 401)
				{
					// You must turn off camera User Access Control before this
					// will work
					return null;
				}
				return new MjpegInputStream(res.getEntity().getContent());
			} catch (ClientProtocolException e)
			{
				e.printStackTrace();
				// Log.d(TAG, "Request failed-ClientProtocolException", e);
				// Error connecting to camera
			} catch (IOException e)
			{
				e.printStackTrace();
				// Log.d(TAG, "Request failed-IOException", e);
				// Error connecting to camera
			}

			return null;
		}

		protected void onPostExecute(MjpegInputStream result)
		{
		//	mj.setMinimumHeight((int)(mj.getWidth() * 0.75));
			mj.setMinimumHeight(2000);
			mj.setSource(result);
			mj.setDisplayMode(MjpegView.SIZE_BEST_FIT);
			mj.showFps(true);
		}
	}
}
