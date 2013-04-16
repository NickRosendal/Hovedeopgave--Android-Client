package com.example.anwebclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.Mjpeg.MjpegInputStream;
import com.example.Mjpeg.MjpegView;
import com.example.webClient.CommandClient;

public class WebListener extends Activity implements com.example.designPatterns.ObserverPattern_Observer
{
	String videoURL;
	String imageURL;

	CommandClient myCommandClient;
	com.example.Mjpeg.MjpegView mj;

	Button debugButton;
	TextView name;
	TextView birthday;
	TextView zipcode;
	TextView status;
	TextView lastvisit;
	TextView gender;
	ImageView imageview;

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
		gender = (TextView)findViewById(R.id.textGender);
		imageview = (ImageView)findViewById(R.id.imageV);
		
		
		debugButton = (Button) findViewById(R.id.debugButton);
		debugButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (!(myCommandClient == null))
				{
					askForImage();
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
			imageURL = "http://" + myCommandClient.getAddress().getHostAddress() + ":8080/GetImage";
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
					Log.i("AnWebClient", "video()");
				} else if (eventData.equals("image is ready"))
				{
					image();
					Log.i("AnWebClient", "image()");

				} else if (eventData.contains("guestInfo"))
				{
					swipe(eventData);
					Log.i("AnWebClient", "swipe()");

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
		 *  guestInfo:name:SIGNE JOHANSEN# birthday:1986-12-23# zipcode:3500# sex:Female# status:welcomed# lastVisit:NA##
		 * 	status can be welcomed or banned 
		 * 	lastVisit can be a date, or NA if NA its a new guest.
		 * 
		 */

		guestInfo = guestInfo.substring(guestInfo.indexOf(":") + 1);
		String guestInfoArray[] = guestInfo.split("#");
		String Name = guestInfoArray[0].substring(guestInfoArray[0].indexOf(":") + 1);
		String Birthday = guestInfoArray[1].substring(guestInfoArray[1].indexOf(":") + 1);
		String Zipcode = guestInfoArray[2].substring(guestInfoArray[2].indexOf(":") + 1);
		String Gender = guestInfoArray[3].substring(guestInfoArray[3].indexOf(":") + 1);
		String Status = guestInfoArray[4].substring(guestInfoArray[4].indexOf(":") + 1);
		String LastVisit = guestInfoArray[5].substring(guestInfoArray[5].indexOf(":") + 1);


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
			askForImage();
		}

		
		 name.setText(Name);
		 birthday.setText(Birthday);
		 zipcode.setText(Zipcode);
		 status.setText(Status);
		 lastvisit.setText(LastVisit);
		 gender.setText(Gender);
		


	}

	// video reviced
	private void video()
	{
		//ReadVideoStream myReadVideoStream = new ReadVideoStream();
		//myReadVideoStream.execute(videoURL);
		new ReadVideoStream().execute(videoURL);
		Log.i("AnWebClient", "video thread started");
		


	}

	// image recived
	private void image()
	{
		//ReadImage readImageStream = new ReadImage();
		
		//readImageStream.execute(imageURL);
		new ReadImage().execute(imageURL);
		Log.i("AnWebClient", "image thread started");

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
			imageview.setVisibility(View.INVISIBLE);
			mj.setVisibility(View.VISIBLE);
			mj.setMinimumHeight(2000);
			mj.setSource(result);
			mj.setDisplayMode(MjpegView.SIZE_BEST_FIT);
			mj.showFps(true);
		}
	}
	
	public class ReadImage extends AsyncTask<String, Void, Bitmap>{

		@Override
		protected Bitmap doInBackground(String... url)
		{
			HttpResponse res = null;
			DefaultHttpClient httpclient = new DefaultHttpClient();
			try{
				res = httpclient.execute(new HttpGet(URI.create(url[0])));
				InputStream inStream = res.getEntity().getContent();
								
			    Drawable d = Drawable.createFromStream(inStream, "imagename");
				Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
				
				return bitmap;
				
			} catch(Exception E){
				E.printStackTrace();

			}
			// TODO Auto-generated method stub
			return null;
		}
		protected void onPostExecute(Bitmap result)
		{
			mj.setVisibility(View.INVISIBLE);
			imageview.setImageBitmap(result);
			imageview.setVisibility(View.VISIBLE);
			

		}
		
	}
	
	/*
	 * lav en billed async task
	 * 
	 * lav scroll view
	 */
}
