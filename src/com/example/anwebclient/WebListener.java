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
import android.util.Log;
import android.view.Menu;

public class WebListener extends Activity implements com.example.designPatterns.ObserverPattern_Observer
{
	String videoURL;

	CommandClient client = null;
	com.example.Mjpeg.MjpegView mj;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_listener);
		Bundle b = this.getIntent().getExtras();
		//client = (CommandClient) this.getIntent().getSerializableExtra("CommandClient");
		client = (CommandClient) b.getSerializable("CommandClient");
		client.registerObserver(this);
		mj = (MjpegView) findViewById(R.id.mv);
		videoURL = "http://" + savedInstanceState.get("SERVER ADRESS") + "8080/GetStream";
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
			video();
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
		new ReadVideoStream().execute(videoURL);
	}

	// image recived
	private void image()
	{

	}

	private void askForVideo()
	{
		client.send("start video server");
	}

	private void askForImage()
	{
		client.send("take picture");
	}

	public class ReadVideoStream extends AsyncTask<String, Void, MjpegInputStream>
	{
		protected MjpegInputStream doInBackground(String... url)
		{
			// TODO: if camera has authentication deal with it and don't just
			// not work
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
			mj.setSource(result);
			mj.setDisplayMode(MjpegView.SIZE_BEST_FIT);
			mj.showFps(true);
		}
	}
}
