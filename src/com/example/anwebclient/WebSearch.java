package com.example.anwebclient;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.example.webClient.CommandClient;

public class WebSearch extends Activity implements com.example.designPatterns.ObserverPattern_Observer, OnClickListener
{
	private RadioButton female;
    private TableLayout searchtable;
	private final ArrayList<searchResult> guestsResults = new ArrayList<searchResult>();

	private EditText name;

	private CommandClient myCommandClient;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_search);
		female = (RadioButton) findViewById(R.id.radioFemale);
        ImageButton search = (ImageButton) findViewById(R.id.buttonForSerch);
		name = (EditText) findViewById(R.id.editTextSerchName);
		searchtable = (TableLayout) findViewById(R.id.searchTableLayout);
		search.setOnClickListener(this);

		Point size = new Point();
		Display display = getWindowManager().getDefaultDisplay();

		display.getSize(size);
		int width = size.x;
        //search.getLayoutParams().height = (screenheight / 100) * 30;
		search.getLayoutParams().width = (width/3);
		name.getLayoutParams().width = (width/3)*2;
		
		//For catching the OK button key on the keyboard
		name.setOnEditorActionListener(new OnEditorActionListener()
		{
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				// TODO Auto-generated method stub
				search();
				return false;
			}
		});
		
//        android:layout_alignTop="@+id/editTextSerchName"

	}

	@Override
	protected void onResume()
	{
		super.onResume();

        MyApplication mApplication = (MyApplication) getApplicationContext();

        if (mApplication.getCommandClient() == null)
        {

            startActivity(new Intent(WebSearch.this, MainActivity.class));
        } else
        {
            myCommandClient = mApplication.getCommandClient();

        }


		// WHEN ever this window is active it should be the observer!
		myCommandClient.registerObserver(this);
		// myCommandClient only registers one observer, registring this
		// means removing the weblistener..
	}

	private void askServerForHits(String name, String Sex)
	{
		// Search:Name:kim lindhard#sex:M##
		// example: "Search for: Kim Lindhard is male: true"
		// Search:Name:Button#M##

		myCommandClient.send("search:name:" + name + "#sex:" + Sex + "##");
	}

	private void serverResults(String in)
	{
		guestsResults.clear();
		String resultList[] = in.split("guestInfo");
		for (String result : resultList)
		{

			if (result.contains("name") && result.contains("guestId") && result.contains("Image"))
			{
				String Name = result.substring(result.indexOf("name") + 5);
				Name = Name.substring(0, Name.indexOf("#"));
				String Id = result.substring(result.indexOf("guestId") + 8);
				Id = Id.substring(0, Id.indexOf("#"));
				String Image = result.substring(result.indexOf("Image") + 6);
				Image = Image.substring(0, Image.indexOf("#"));
				Log.i("something", "did find a guest");

                guestsResults.add(new searchResult(Name, Id, Image, myCommandClient, this.getFilesDir().getAbsolutePath()));
			}

		}

		generateList(guestsResults);
	}

	private void generateList(ArrayList<searchResult> in)
	{
		searchtable.removeAllViews();
		for (searchResult guest : in)
		{

			TableRow tableRow = new TableRow(this);
			tableRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			// tableRow.setGravity(Gravity.RIGHT);
			int playerwidth = searchtable.getWidth();
			int femtedel = playerwidth / 5;
            TextView guestName = new TextView(this);
			guestName.setText(guest.getName());

			/*
			 * Width, bliver sat til 3 femtedel, da billedes width er 2 femtedel
			 */
			guestName.setWidth((femtedel * 3));
			guestName.setTextColor(Color.WHITE);
            guestName.setTextSize(30);
			ImageView guestImage = new ImageView(this);
			tableRow.addView(guestName);

			if (guest.getImage() != null)
			{

				/*
				 * Vores billeder er 4 / 3 format.
				 * 
				 * da textviewt er 3 femtedele bredt skal væres billede være 2
				 * femtedele
				 * 
				 * højden på bilede skal så passes til 4/3 det gør vi ved at
				 * først tage bredent 2femtedele og dividere med 4 for derefter
				 * at gange med 3
				 */
				guestImage.setLayoutParams(new TableRow.LayoutParams(femtedel * 2, ((femtedel * 2 / 4) * 3)));

				// guestImage.setGravity(Gravity.RIGHT);
				guestImage.setImageBitmap(guest.getImage());
				/*
				 * vi tilføjer lidt padding så der kommer mellemrum mellem
				 * billederne
				 */
				int PADDING = 4;
				guestImage.setPadding(PADDING, PADDING, PADDING, PADDING);

				tableRow.addView(guestImage);
			}

			searchtable.addView(tableRow);
			guest.setRowId(searchtable.getChildCount() - 1);
			//sets the ID for the onclickListener
			tableRow.setId(searchtable.getChildCount() - 1);
			tableRow.setOnClickListener(this);

		}
		/*
		 * TableLayout tl = (TableLayout)findViewById(R.id.tl); TableRow tr =
		 * new TableRow(this); TextView tv = new TextView(this);
		 * tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
		 * LayoutParams.WRAP_CONTENT, 1f)); tv.setText("Test");
		 * tv.setGravity(Gravity.LEFT); TextView tv2 = new TextView(this);
		 * tv2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
		 * LayoutParams.WRAP_CONTENT, 1f)); tv2.setGravity(Gravity.RIGHT);
		 * tv2.setText("Test"); tr.addView(tv); tr.addView(tv2); tl.addView(tr);
		 * setContentView(tl);
		 */
	}
	private void search(){
		
		//HIDES THE KEYBOARD
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

		
		
		String toSearch = name.getText() + "";
		String sex;
		if (female.isChecked())
			sex = "F";
		else
			sex = "M";
		askServerForHits(toSearch, sex);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_search, menu);
		return true;
	}

	private void image()
	{
		Log.i("something", "downloadfile");
		new ReciveFile(this.getFilesDir().getAbsolutePath(), myCommandClient.getAddress().getHostAddress(), WebSearch.this);
	}

	@Override
	public void update(final String eventData)
	{
		// give me somthing to work with kiiim
		// SearchResults:Name:Jens
		// Peter#Id:342#Image:20:10:30:2013##SearchResults:Name:Ole......
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				// Log.i("something", eventData);
				try
				{
					if (eventData.contains("searchResult"))
					{
						serverResults(eventData);

					} else if (eventData.contains("FILE DOWNLOADED: "))
					{

						String path = eventData.substring(eventData.indexOf(":") + 2);
						for (searchResult guest : guestsResults)
						{
							Log.i("something", "test :" + guest.getAppPath() + "/" + guest.getImagePath() + " equals: " + path);
							// path er absolute path på telefonen..
							if ((guest.getAppPath() + "/" + guest.getImagePath()).equals(path))
							{

								//adds the image to searchResult
								guest.checkIfImageIsLocal(path);

								TableRow tableRow = (TableRow) searchtable.getChildAt(guest.getRowId());
								ImageView guestImage = new ImageView(WebSearch.this);

								int playerwidth = searchtable.getWidth();
								int femtedel = playerwidth / 5;

                                // størrelsen på vores billede.
								guestImage.setLayoutParams(new TableRow.LayoutParams(femtedel * 2, ((femtedel * 2 / 4) * 3)));
								int PADDING = 4;
								guestImage.setPadding(PADDING, PADDING, PADDING, PADDING);
								guestImage.setImageBitmap(guest.getImage());

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

						Intent webListenerIntent = new Intent(WebSearch.this, WebListener.class);
						webListenerIntent.putExtra("swype", eventData);
						startActivity(webListenerIntent);
					}
				} catch (NullPointerException E)
				{
					myCommandClient.interruptReader();
					finish();
					startActivity(new Intent(WebSearch.this, MainActivity.class));
				}
			}
		});
	}

	@Override
	public void onClick(View v)
	{
Log.i("something", "some one clicked on: "+v.getId());
		int subject = v.getId();

		// TODO Auto-generated method stub
		if(subject == R.id.buttonForSerch){
			search();
		}else {
			for(searchResult guest: guestsResults){
				if(guest.getRowId() == subject){
                    MyApplication mApplication = (MyApplication) getApplicationContext();

                    AlertDialog.Builder dialogBuilder = new doorManLogin(this, guest.getId(), mApplication);
					dialogBuilder.show();

					
				}
			}
		}
	}

}
