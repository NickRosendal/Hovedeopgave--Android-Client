package com.example.anwebclient;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.Mjpeg.MjpegInputStream;
import com.example.Mjpeg.MjpegView;
import com.example.webClient.CommandClient;

public class WebListener extends Activity implements com.example.designPatterns.ObserverPattern_Observer
{
	String videoURL;
	String imageURL;
	String fileIP;
	String Id;

	CommandClient myCommandClient;
	com.example.Mjpeg.MjpegView mj;

	Button debugButton;
	TextView name;
	TextView birthday;
	TextView zipcode;
	TextView gender;
	ImageView imageview;
	ReadVideoStream myReadVideoStream;
	TableLayout eventLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_listener);
		MyApplication mApplication = (MyApplication) getApplicationContext();

		name = (TextView) findViewById(R.id.textName);
		birthday = (TextView) findViewById(R.id.textBirthday);
		zipcode = (TextView) findViewById(R.id.textZip);
		// status = (TextView) findViewById(R.id.textStatus);
		// lastvisit = (TextView) findViewById(R.id.textLastVisit);
		gender = (TextView) findViewById(R.id.textGender);
		imageview = (ImageView) findViewById(R.id.imageV);
		eventLayout = (TableLayout) findViewById(R.id.tableLayoutForEvents);

		debugButton = (Button) findViewById(R.id.debugButton);
		debugButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (!(myCommandClient == null))
				{

					askForShootImage(Id);
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
			fileIP = myCommandClient.getAddress().getHostAddress();
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

				} else if (eventData.contains("guestInfo"))
				{
					swipe(eventData);
					Log.i("AnWebClient", "swipe()");

				} else if (eventData.contains("Image from disk is ready:"))
				{
					image();
				} else if (eventData.contains("FILE DOWNLOADED: "))
				{
					String path = eventData.substring(eventData.indexOf(":") + 2);
					fileRecived(path);
				}
			}
		});

	}

	private String getSubStringOf(String target, String substring, String endPatern)
	{
		String returnString = target.substring(target.indexOf(substring) + substring.length() + 1);
		returnString = returnString.substring(0, returnString.indexOf(endPatern));
		return returnString;
	}

	private void swipe(String guestInfo)
	{
		/*
		 * 
		 * 
		 * guestInfo:name:KIM GRAVE LINDHARD# birthday:1982-07-21# sex:M#
		 * zipcode:2400#
		 * id:7##Image:2013/06/12/12-22-11.jpeg#DocumentationImage:NA#
		 * Events:Event:dateTime:2013-04-19 09:55:29#Description:Guest
		 * Created###
		 * 
		 * Image:2013/06/12/12-22-11.jpeg# example of guestInfo
		 * guestInfo:name:SIGNE JOHANSEN# birthday:1986-12-23# zipcode:3500#
		 * sex:Female# status:welcomed# lastVisit:NA## status can be welcomed or
		 * banned lastVisit can be a date, or NA if NA its a new guest.
		 */
		String guestEvent[] = null;

		// String Id ="";
		String Name = "";
		String Birthday = "";
		String Gender = "";
		String Zipcode = "";
		String imgPath = "";
		String guestEventTemp;

		if (guestInfo.contains("Events"))
		{
			guestEventTemp = guestInfo.substring(guestInfo.indexOf("Events"));
			guestEvent = guestEventTemp.substring(guestEventTemp.indexOf(":") + 1).split("#");

			guestInfo = guestInfo.substring(0, guestInfo.indexOf("Events"));
		}
		if (guestInfo.contains("name"))
		{
			Name = getSubStringOf(guestInfo, "name", "#");
		}
		if (guestInfo.contains("birthday"))
		{
			Birthday = getSubStringOf(guestInfo, "birthday", "#");
		}
		if (guestInfo.contains("sex"))
		{
			Gender = getSubStringOf(guestInfo, "sex", "#");
		}
		if (guestInfo.contains("zipcode"))
		{
			Zipcode = getSubStringOf(guestInfo, "zipcode", "#");
		}
		if (guestInfo.contains("Image"))
		{
			imgPath = getSubStringOf(guestInfo, "Image", "#");
		}
		if (guestInfo.contains("guestId"))
		{
			Id = getSubStringOf(guestInfo, "guestId", "#");
		} else
		{
			Id = null;
		}

		int count = 0;

		Boolean banned = false;
		// Events:Event:dateTime:2013-04-19 09:55:29#Description:Guest
		// Created###

		// håber den fjerner alt der skulle ligge i events i forvejen..
		eventLayout.removeAllViews();

		for (String text : guestEvent)
		{

			// should look for expering date though!.
			// We need to agree on how a ban event looks

			// DATE TIME, hvorn�r,
			// DESCRIPTION :
			// BAN 2014-03-02
			if (text.contains("BAN"))
			{

				String date = text.substring(text.indexOf(" ") + 1);
				int year = Integer.parseInt(date.substring(0, 4));
				int month = Integer.parseInt(date.substring(5, 7));
				int day = Integer.parseInt(date.substring(8, 10));
				// System.out.println("year: " + year + " month: " + month +
				// "day: " + day);

				Date now = new Date();
				/*
				 * Parameters year the year, 0 is 1900. month the month, 0 - 11.
				 * day the day of the month, 1 - 31.
				 */
				year = year - 1900;
				month = month - 1;

				Date bannedTo = new Date(year, month, day);

				if (!bannedTo.before(now))
				{
					banned = true;

				}
			}
			/*
			 * 
			 * her skal laves noget der fylder alle evens i en lille log som
			 * dørmanden kan se.
			 */

			// Events:Event:dateTime:2013-04-19 09:55:29#Description:Guest

			if (text.contains("Description"))
			{
				text = text.substring(text.indexOf("Description") + 12);
			} else
			{
				text = text.substring(text.indexOf("dateTime") + 9);
			}

			TableRow tableRow = new TableRow(this);
			tableRow.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			TextView eventTextView = new TextView(this);
			eventTextView.setText(text);
			tableRow.addView(eventTextView);
			eventLayout.addView(tableRow, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			count++;
		}

		if (banned == false)
		{
			debugButton.setBackgroundColor(Color.GREEN);
		} else
		{
			debugButton.setBackgroundColor(Color.RED);
		}

		if (imgPath.length() > 4)
		{
			askForOldImage(imgPath);
		} else
		{
			askForShootVideo();
		}

		name.setText(Name);
		birthday.setText(Birthday);
		zipcode.setText(Zipcode);
		// status.setText(banned + "");
		gender.setText(Gender);

	}

	// video reviced
	private void video()
	{

		myReadVideoStream = new ReadVideoStream();
		myReadVideoStream.execute(videoURL);

		Log.i("AnWebClient", "video thread started");

	}


	private void image()
	{
		new ReciveFile(this.getFilesDir().getAbsolutePath(), fileIP, WebListener.this);
	}

	private void fileRecived(String path)
	{
		File imageFile = new File(path);
		BitmapDrawable imageBitmap = new BitmapDrawable(path);

		mj.setVisibility(View.INVISIBLE);
		imageview.setImageDrawable(imageBitmap);
		imageview.setVisibility(View.VISIBLE);

	}

	private void askForShootVideo()
	{
		myCommandClient.send("start video server");
	}

	private void askForShootImage(String id)
	{
		myCommandClient.send("take picture for " + id);
	}

	private void askForOldImage(String path)
	{
		// SOME KIND OF CHECK IF THE IMAGE IS STORED LOCALY
		//if image does not excists the bitmap factory retuns null
		
		Bitmap bitmap = BitmapFactory.decodeFile(this.getFilesDir().getAbsolutePath() + "/" + path);
		if (bitmap == null)
		{
			myCommandClient.send("send picture from disk:" + path + "#");
		}else{
			
			mj.setVisibility(View.INVISIBLE);
			imageview.setImageBitmap(bitmap);
			imageview.setVisibility(View.VISIBLE);
		}

	}

	public class ReadVideoStream extends AsyncTask<String, Void, MjpegInputStream>
	{
		HttpResponse res;
		DefaultHttpClient httpclient;

		protected MjpegInputStream doInBackground(String... url)
		{
			res = null;
			httpclient = new DefaultHttpClient();
			mj.init(WebListener.this);
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

			imageview.setVisibility(View.INVISIBLE);
			mj.setVisibility(View.VISIBLE);
			mj.setMinimumHeight(2000);
			mj.setSource(result);
			mj.setDisplayMode(MjpegView.SIZE_BEST_FIT);
			mj.showFps(true);
		}

	}



	

}
