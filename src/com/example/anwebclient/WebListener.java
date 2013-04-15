package com.example.anwebclient;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Mjpeg.MjpegInputStream;
import com.example.Mjpeg.MjpegView;
import com.example.webClient.CommandClient;

public class WebListener extends Activity implements com.example.designPatterns.ObserverPattern_Observer
{
	String videoURL;

	CommandClient myCommandClient;
	com.example.Mjpeg.MjpegView mj;
	ReadVideoStream myReadVideoStream = new ReadVideoStream();
	Button debugButton;
	TextView name;
	TextView birthday;
	TextView zipcode;
	TextView status;
	TextView lastvisit;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_listener);
		MyApplication mApplication = (MyApplication) getApplicationContext();
		
		name = (TextView) findViewById(R.id.textName);
		birthday = (TextView) findViewById(R.id.textBirthday);
		zipcode = (TextView)findViewById(R.id.textZip);
		status = (TextView)findViewById(R.id.textStatus);
		lastvisit = (TextView)findViewById(R.id.textLastVisit);
		
		debugButton = (Button) findViewById(R.id.debugButton);
		debugButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (!(myCommandClient == null))
				{
					myCommandClient.send("pretendToSwipe");
				}
			}
		});

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
//			askForVideo();
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
	public void update(final String eventData)
	{
		
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				if (eventData.equals("video server is ready"))
				{

					video();
				} else if (eventData.equals("image is ready"))
				{
					image();
				} else if (eventData.contains("guestInfo"))
				{
					swipe(eventData);
				}
			}
		});
		
		// "video server is ready"
		// image is ready

	}

	// swipe recived
	private void swipe(String guestInfo)
	{
		/*
		 *  example of guestInfo
		 * 	guestInfo:name:Kim Lindhard# birthday:1982-07-21# zipcode:2400# status:welcomed# lastVisit:2013-01-02##
		 * 
		 * 	status can be welcomed or banned 
		 * 	lastVisit can be a date, or NA if NA its a new guest.
		 * 
		 */

		guestInfo = guestInfo.substring(guestInfo.indexOf(":") + 1);
		String guestInfoArray[] = guestInfo.split("#");
		String Name = guestInfoArray[0].substring(guestInfoArray[0].indexOf(":") + 1);
		String Birthday = guestInfoArray[1].substring(guestInfoArray[1].indexOf(":") + 1);
		String Zipcode = guestInfoArray[2].substring(guestInfoArray[2].indexOf(":") + 1);
		String Status = guestInfoArray[3].substring(guestInfoArray[3].indexOf(":") + 1);
		String LastVisit = guestInfoArray[4].substring(guestInfoArray[4].indexOf(":") + 1);


		if (Status.equals("welcomed"))
		{
			status.setBackgroundColor(Color.GREEN);
		} else
		{
			status.setBackgroundColor(Color.RED);
		}

		if (LastVisit.equals("NA"))
		{
			askForVideo();
		} else
		{
			askForVideo();
		}

		
		 name.setText(Name);
		 birthday.setText(Birthday);
		 zipcode.setText(Zipcode);
		 status.setText(Status);
		 lastvisit.setText(LastVisit);
		


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
				return new MjpegInputStream(res.getEntity().getContent());
			} catch (ClientProtocolException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}

			return null;
		}

		protected void onPostExecute(MjpegInputStream result)
		{
			// mj.setMinimumHeight((int)(mj.getWidth() * 0.75));
			mj.setMinimumHeight(2000);
			mj.setSource(result);
			mj.setDisplayMode(MjpegView.SIZE_BEST_FIT);
			mj.showFps(true);
		}
	}
}
