package com.example.anwebclient;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.webClient.CommandClient;

public class WebNightList extends Activity implements com.example.designPatterns.ObserverPattern_Observer, OnClickListener
{
	private ArrayList<searchResult> nightList = null;
	private CommandClient myCommandClient;
	private TableLayout nightlistTableLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_night_list);

		nightlistTableLayout = (TableLayout) findViewById(R.id.TableLayoutForNight);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

        MyApplication mApplication = (MyApplication) getApplicationContext();
        myCommandClient = mApplication.getCommandClient();

        // WHEN ever this window is active it should be the observer!
		myCommandClient.registerObserver(this);
		// myCommandClient only registers one observer, registring this
		// means removing the weblistener..

		myCommandClient.send("give me the night list");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_night_list, menu);
		return true;
	}

	void generateList(ArrayList<searchResult> in)
	{
		nightlistTableLayout.removeAllViews();
		for (searchResult guest : in)
		{
			TableRow tableRow = new TableRow(this);
			tableRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			int playerwidth = nightlistTableLayout.getWidth();
			int femtedel = playerwidth / 5;

			TextView guestName = new TextView(this);
			guestName.setTextSize(30);

            ImageView guestImage = new ImageView(this);

			guestName.setWidth((femtedel * 3));
			guestName.setTextColor(Color.WHITE);

			guestName.setText(guest.getName());
			tableRow.addView(guestName);

			if (guest.getImage() != null)
			{

				// størrelsen på vores billede.
				guestImage.setLayoutParams(new TableRow.LayoutParams(femtedel * 2, ((femtedel * 2 / 4) * 3)));

				guestImage.setImageBitmap(guest.getImage());
				int PADDING = 4;
				guestImage.setPadding(PADDING, PADDING, PADDING, PADDING);

				tableRow.addView(guestImage);
			}
			nightlistTableLayout.addView(tableRow);
			guest.setRowId(nightlistTableLayout.getChildCount() - 1);
			tableRow.setId(nightlistTableLayout.getChildCount() - 1);
			tableRow.setOnClickListener(this);
		}

	}

	private void serverResults(String in)
	{
		nightList = new ArrayList<searchResult>();

		String resultList[] = in.split("guestInfo");
		Log.i("something", "did i go to server results");
		for (String result : resultList)
		{
			Log.i("something", "did i find a string");

			if (result.contains("name") && result.contains("guestId") && result.contains("Image"))
			{
				String Name = result.substring(result.indexOf("name") + 5);
				Name = Name.substring(0, Name.indexOf("#"));
				String Id = result.substring(result.indexOf("guestId") + 8);
				Id = Id.substring(0, Id.indexOf("#"));
				String Image = result.substring(result.indexOf("Image") + 6);
				Image = Image.substring(0, Image.indexOf("#"));
				Log.i("something", "did find a guest");

				nightList.add(new searchResult(Name, Id, Image, myCommandClient, this.getFilesDir().getAbsolutePath()));
			}

		}

		generateList(nightList);
	}

	private void image()
	{
		Log.i("something", "downloadfile");
		new ReciveFile(this.getFilesDir().getAbsolutePath(), myCommandClient.getAddress().getHostAddress(), WebNightList.this);
	}

	@Override
	public void update(final String eventData)
	{
		// TODO Auto-generated method stub
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				try
				{
					Log.i("SUPER MAN", eventData);
					if (eventData.contains("guestsForNight"))
					{
						serverResults(eventData);

					} else if (eventData.contains("FILE DOWNLOADED: "))
					{

						String path = eventData.substring(eventData.indexOf(":") + 2);
						for (searchResult guest : nightList)
						{
							Log.i("something", "test :" + guest.getAppPath() + "/" + guest.getImagePath() + " equals: " + path);
							// path er absolute path på telefonen..
							if ((guest.getAppPath() + "/" + guest.getImagePath()).equals(path))
							{
								Log.i("something", "trying to add image");
								guest.checkIfImageIsLocal(path);

								TableRow tableRow = (TableRow) nightlistTableLayout.getChildAt(guest.getRowId());
								ImageView guestImage = new ImageView(WebNightList.this);

								int playerwidth = nightlistTableLayout.getWidth();
								int femtedel = playerwidth / 5;

								// størrelsen på vores billede.
								guestImage.setLayoutParams(new TableRow.LayoutParams(femtedel * 2, ((femtedel * 2 / 4) * 3)));

								guestImage.setImageBitmap(guest.getImage());
								Log.i("something", "trying to add image to row " + guest.getRowId());
								tableRow.addView(guestImage);
								// searchtable.
							}
						}

					} else if (eventData.contains("Image from disk is ready:"))
					{
						image();
					} else if (eventData.contains("guestInfo")||eventData.contains("guestSwipeInfo"))
					{
						// if a card is swyped, we need to start the
						// weblistener,
						// and pass it the string event.

						Intent webListenerIntent = new Intent(WebNightList.this, WebListener.class);
						webListenerIntent.putExtra("swype", eventData);
						startActivity(webListenerIntent);
					}
				} catch (NullPointerException E)
				{
					Log.i("someting", "i think the server crashed!");
					myCommandClient.interruptReader();
					finish();
					startActivity(new Intent(WebNightList.this, MainActivity.class));
				}
			}
		});
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		int subject = v.getId();

		for(searchResult guest: nightList){
			if(guest.getRowId() == subject){

                MyApplication mApplication = (MyApplication) getApplicationContext();

                AlertDialog.Builder dialogBuilder = new doorManLogin(this, guest.getId(), mApplication);
				dialogBuilder.show();

				
			}
		}
	}
}
