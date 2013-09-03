package com.example.anwebclient;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import android.annotation.TargetApi;
import android.os.Build;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.Mjpeg.MjpegInputStream;
import com.example.Mjpeg.MjpegView;
import com.example.webClient.CommandClient;

public class WebListener extends Activity implements com.example.designPatterns.ObserverPattern_Observer, OnClickListener
{
	private String videoURL;
    private String fileIP;
	private String Id;
	private String ImageIAmWaitingFor = null;
	//public static final ArrayList<String> DoorMen = new ArrayList<String>();

	private CommandClient myCommandClient;
	private com.example.Mjpeg.MjpegView mj;

	private Button entranceEndicator;
	private ImageButton banButton;
	private ImageButton showNightButton;
	private ImageButton searchButton;
	private TextView name;
	private TextView dbID;
	private TextView birthday;
	private TextView gender;
	private ImageView imageview;
    private TableLayout eventLayout;
    private RelativeLayout personalDetailsContainer;
	private ImageView lockIcon;
    private boolean isVideoRunning = false;

	@Override
	protected void onResume()
	{
		super.onResume();
		// WHEN ever this window is active it should be the observer!
		myCommandClient.registerObserver(this);
		Intent intent = getIntent();

		// IF the intent has been invoken by the observer pattern from webserach
		// (a card has been swyped while u where searching)
		// the event is passed on to the weblistener
		String swype = intent.getStringExtra("swype");
		if (!(swype == null))
		{
			update(swype);
		}
        isVideoRunning = false;

	}

    @Override
    protected void onDestroy(){
        super.onDestroy();
        myCommandClient.removeObserver(this);
        myCommandClient.disconnect();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Remove notification bar
		// this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_listener);
		MyApplication mApplication = (MyApplication) getApplicationContext();

		/*
		 * textviews
		 */

		name = (TextView) findViewById(R.id.textName);
		birthday = (TextView) findViewById(R.id.textBirthday);
		gender = (TextView) findViewById(R.id.textGender);

		/*
		 * ImageView
		 * 
		 * adds the onclicklistener so that we can shoot a new image..
		 */

		imageview = (ImageView) findViewById(R.id.imageV);
		imageview.setOnClickListener(this);

		/*
		 * Layouts
		 */

		eventLayout = (TableLayout) findViewById(R.id.tableLayoutForEvents);

		/*
		 * Tests if we have a command client, IT should not be passable for this
		 * to be null! how ever if it for some reason is we need to return to
		 * the mainActivity//connectScreen
		 */
		if (mApplication.getCommandClient() == null)
		{
			Log.i("AnWebClient", "Error Weblistner not using application commandClient");
			finish();
			startActivity(new Intent(WebListener.this, MainActivity.class));
		} else
		{
			myCommandClient = mApplication.getCommandClient();

			mj = (MjpegView) findViewById(R.id.mv);
			mj.setOnClickListener(this);
			videoURL = "http://" + myCommandClient.getAddress().getHostAddress() + ":8080/GetStream";
            String imageURL = "http://" + myCommandClient.getAddress().getHostAddress() + ":8080/GetImage";
			fileIP = myCommandClient.getAddress().getHostAddress();
		}

		/*
		 * Buttons
		 */
		banButton = (ImageButton) findViewById(R.id.banButton);
		banButton.setVisibility(View.INVISIBLE);
		banButton.setOnClickListener(this);

		showNightButton = (ImageButton) findViewById(R.id.buttonNight);
		showNightButton.setOnClickListener(this);

		searchButton = (ImageButton) findViewById(R.id.ButtonForSearch);
		searchButton.setOnClickListener(this);

		entranceEndicator = (Button) findViewById(R.id.debugButton);
		entranceEndicator.setOnClickListener(this);

		/*
		 * Caried Data.
		 */
		Intent intent = getIntent();
		if (intent.getStringExtra("users") != null)
		{
			String usersIn = intent.getStringExtra("users");
			String userArray[] = usersIn.split("username:");
			for (String user : userArray)
			{
				if (user.length() > 1)
				{
                    mApplication.addDoorMan(user.substring(0, user.indexOf("#")));
					//DoorMen.add(user.substring(0, user.indexOf("#")));
					// status:connection accepted#users:user:Ole#user:Bent
					// Hansen#user:Hannibal Andersen##
				}
			}
		}

		/*
		 * Draws a lock in the buttom center of the screen
		 * 
		 * makes it centered, and sets an onclicklistener
		 */

		lockIcon = new ImageView(this);
		lockIcon.setImageResource(R.drawable.lock);
		
		dbID = new TextView(this); 
		
    	dbID.setTextSize(40);
    	dbID.setTextColor(Color.WHITE);
    	//dbID.setBackgroundColor(Color.WHITE);

        RelativeLayout detailsContainer = (RelativeLayout) findViewById(R.id.detailsRelativeLayout);
		detailsContainer.addView(lockIcon);
		detailsContainer.addView(dbID);
		
		personalDetailsContainer = (RelativeLayout) findViewById(R.id.relativeLayout1);

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) lockIcon.getLayoutParams();
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		lockIcon.setLayoutParams(layoutParams);
		lockIcon.setOnClickListener(this);
		
		RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) dbID.getLayoutParams();
		layoutParams1.addRule(RelativeLayout.CENTER_HORIZONTAL);
		layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		

		/*
		 * runs the method that manipulate element sizes
		 */
		manipulateScreenElements();
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		// getId() returns this view's identifier.

		// THIS SHOULD BE AN ONCLICK LISTENER FOR THE IMAGE OR VIDEO FEED
		int subject = v.getId();
		Log.i("something", "is clicked:" + subject);

		if (subject == R.id.mv || subject == R.id.imageV)
		{
			if (!(Id == null))
			{
                if(isVideoRunning){
				askForShootImage(Id);

            }else{
                    askForShootVideo();
                }
			}
		} else if (subject == R.id.buttonNight)
		{
			Intent webnightlistintent = new Intent(WebListener.this, WebNightList.class);
			startActivity(webnightlistintent);
		} else if (subject == R.id.ButtonForSearch)
		{
			Intent websearchintent = new Intent(WebListener.this, WebSearch.class);
			startActivity(websearchintent);
		} else if (subject == R.id.banButton)
		{
			/*
			 * Creates an alertdialog, that contains a LinarLayout, the layout
			 * contains a radiogroup, that contains radiobuttons equal to the
			 * number of strings in banOption[]
			 * 
			 * A alertdialog also have a positive, and a negative button the
			 * positive sends the information to the server
			 * 
			 * the negative just cansle the alertdialog
			 */
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			LinearLayout ll = new LinearLayout(this);
			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

			final RadioGroup radiog = new RadioGroup(this);

			final String[] banOptions = new String[]
			{ "1 Month", "2 Months", "3 Months", "6 Months", "1 Year", "Life" };

			int count = 0;

            for (String option : banOptions)
			{
				RadioButton temp = new RadioButton(this);
                temp.setTextSize(40);
                temp.setButtonDrawable(R.drawable.button_radio);

                temp.setText(option);
				temp.setId(count);
				radiog.addView(temp);
				count++;

			}

			ll.setOrientation(LinearLayout.VERTICAL);
			ll.addView(radiog, p);

			dialogBuilder.setView(ll);

			dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
			{

				public void onClick(DialogInterface dialog, int whichButton)
				{
					// checks if a radio box is selected
					if (radiog.getCheckedRadioButtonId() != -1)
					{
						String banTime = banOptions[radiog.getCheckedRadioButtonId()];
						if (banTime.equals("1 YEAR"))
							banTime = "12 Months";
						else if (banTime.equals("Life"))
							banTime = "-1";
						else if (banTime.equals("1 Month"))
							banTime = "1 Months";

						// BAN:guestId:1#timeFrame:1 Months#user:username:Ole
						// Andersen#password:A###
                        MyApplication mApplication = (MyApplication) getApplicationContext();
                        if(mApplication.getLastDoorManWhoUsedDevice()!=null)  {
                        String tempname =  mApplication.getLastDoorManWhoUsedDevice().name;
                        String temppassword = mApplication.getLastDoorManWhoUsedDevice().password;
                        String sendString = "BAN:guestId:" + Id + "#timeFrame:" + banTime + "#username:" + tempname + "#password:"+ temppassword+"###";
						myCommandClient.send(sendString);
                            Log.i("something", "yes we are sending information");
                        } else{
                            //need somthing that shows that it has been too long since the doorman logged in.
                            Log.i("something", "we are not sending!");

                        }
					}
				}
			});
			dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.cancel();
				}
			});
			dialogBuilder.show();

		} else if (subject == -1)
		{

			/*
			 * Elements created in java code get id -1 as default, u can set
			 * what ever int u like.
			 * 
			 * but as this screen only have one item with an onclicklistener
			 * created in java code we just keep it
			 * 
			 * the element is the lock in the buttom center of the screen, that
			 * must be opened to show the personal data
			 * 
			 * when the lock is clicked we create a dialog, where the doorman
			 * need to pass his login information
			 */

			Log.i("something", "U CLICKED THE LOCK");
			if (!(Id == null))
			{
                MyApplication mApplication = (MyApplication) getApplicationContext();
				AlertDialog.Builder dialogBuilder = new doorManLogin(this, Id, mApplication);
				dialogBuilder.show();

			}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_listener, menu);
		return true;
	}


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void manipulateScreenElements()
	{

		/*
		 * XML does not suport %, and it is far easier to set the size of certen
		 * elements in java
		 * 
		 * how ever this is not adviced!.
		 */

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int screenwidth = size.x;
		int screenheight = size.y;

		banButton.getLayoutParams().height = screenwidth / 5;
		banButton.getLayoutParams().width = screenwidth / 5;

		/*
		 * The shownight & search button are sharing the buttom of the screen
		 * 
		 * we set the to 47% each, on our china table 50% would overlap for some
		 * reason this is not the case of the samsung galaxy s, phone we also
		 * used to test, so this is an example of android inconsistensy
		 */

		showNightButton.getLayoutParams().height = (screenheight / 100) * 8;
		showNightButton.getLayoutParams().width = (screenwidth / 100) * 47;

		searchButton.getLayoutParams().height = (screenheight / 100) * 8;
		searchButton.getLayoutParams().width = (screenwidth / 100) * 47;

		entranceEndicator.getLayoutParams().height = (screenheight / 100) * 8;

		RelativeLayout topContent = (RelativeLayout) findViewById(R.id.topcontent);

		topContent.getLayoutParams().height = ((screenwidth / 4) * 3);

		personalDetailsContainer.getLayoutParams().width = screenwidth / 3;
	}

	@Override
	public void update(final String eventData)
	{

		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				try
				{

					if (eventData.equals("video server is ready"))
					{

						video();
						Log.i("AnWebClient", "video()");

					} else if (eventData.contains("guestSwipeInfo"))
					{

						swipe(eventData, false);
						Log.i("AnWebClient", "swipe()");

					} else if (eventData.contains("Image from disk is ready:"))
					{
						image();
					} else if (eventData.contains("FILE DOWNLOADED: "))
					{
						String path = eventData.substring(eventData.indexOf(":") + 2);
						fileRecived(path);
					} else if (eventData.contains("guestInfo"))
					{
						Log.i("something", "guestInfo arived");
						swipe(eventData, true);

					} else if (eventData.equals("connection broke"))
					{
						finish();
						startActivity(new Intent(WebListener.this, MainActivity.class));

					}
				} catch (NullPointerException E)
				{
					Log.i("someting", "i think the server crashed!");

					myCommandClient.interruptReader();
					finish();
					startActivity(new Intent(WebListener.this, MainActivity.class));
				}
			}
		});

	}

	private String getSubStringOf(String target, String substring)
	{
		String returnString = target.substring(target.indexOf(substring) + substring.length() + 1);
		returnString = returnString.substring(0, returnString.indexOf("#"));
		return returnString;
	}

	/**
	 * 
	 * @param guestInfo
	 *            string sent from the server eg: guestInfo:name:KIM GRAVE
	 *            LINDHARD# birthday:1982-07-21# sex:M# zipcode:2400#
	 *            id:7##Image:2013/06/12/12-22-11.jpeg#DocumentationImage:NA#
	 *            Events:Event:dateTime:2013-04-19 09:55:29#Description:Guest
	 *            Created###
	 * 
	 * @param showDetails
	 *            this boolen represents wheater the doorman is alowed to see
	 *            the personal information or if a lock should be displayed
	 */
	private void swipe(String guestInfo, boolean showDetails)
	{

		String guestEvent[] = null;
		String Name = "";
		String Birthday = "";
		String Gender = "";
        String imgPath = "";
		String guestEventTemp;

		/*
		 * Here we scan the string for information, it does not nesseserely contain em all thats why we test!
		 */
		if (guestInfo.contains("Events"))
		{
			guestEventTemp = guestInfo.substring(guestInfo.indexOf("Events"));
			guestEvent = guestEventTemp.substring(guestEventTemp.indexOf(":") + 1).split("Event");

			guestInfo = guestInfo.substring(0, guestInfo.indexOf("Events"));
		}
		if (guestInfo.contains("name"))
		{
			Name = getSubStringOf(guestInfo, "name");
		}
		if (guestInfo.contains("birthday"))
		{
			Birthday = getSubStringOf(guestInfo, "birthday");
		}
		if (guestInfo.contains("sex"))
		{
			Gender = getSubStringOf(guestInfo, "sex");
		}
		if (guestInfo.contains("Image"))
		{
			imgPath = getSubStringOf(guestInfo, "Image");
		}
		if (guestInfo.contains("guestId"))
		{
			Id = getSubStringOf(guestInfo, "guestId");
		} else
		{
			Id = null;
		}

		Boolean banned = false;
		// Events:Event:dateTime:2013-04-19 09:55:29#Description:Guest
		// Created###

		// håber den fjerner alt der skulle ligge i events i forvejen..
		if (!(eventLayout == null))
			eventLayout.removeAllViews();

		for (String text : guestEvent)
		{

			TableRow tableRow = new TableRow(this);
			tableRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			TextView eventDateView = new TextView(this);
			TextView eventTextView = new TextView(this);
			Log.i("anwebclient", text);

			// String text
			// =":dateTime:2013-04-30 10:03:28#Description:Entered#";
			// :dateTime:2013-04-29 09:47:16#Description:Guest Created###

			// :dateTime:2013-04-29 09:47:16#Description:BAN 20131231###

			// the last index is to remove the hashtag
			String Description = "";
			String DateTime = "";
			if (text.contains("Description") && text.contains("dateTime"))
			{
				Description = text.substring(text.indexOf("Description") + 12, text.lastIndexOf("#"));

				// +9 = the lenght of dateTime: , -1 to remove the #
				DateTime = text.substring(text.indexOf("dateTime") + 9, text.indexOf("Description") - 1) + " ";
			}

			if (Description.contains("BAN"))
			{
				//BAN 2013-12-31
				//BAN YEAR-MONTH-DAY

				tableRow.setBackgroundColor(Color.RED);
				eventTextView.setBackgroundColor(Color.RED);

				String date = Description.substring(Description.indexOf(" ") + 1);
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

                //noinspection deprecation
                Date bannedTo = new Date(year, month, day);

				if (!bannedTo.before(now))
				{
					banned = true;

				}
			}

			eventDateView.setTextColor(Color.WHITE);
			eventDateView.setText(DateTime);

			Description = Description.replace("#", "");

			eventTextView.setTextColor(Color.WHITE);
			eventTextView.setTextSize(20);
			eventTextView.setText(Description);

			if (Description.length() > 1)
			{
				tableRow.addView(eventDateView);

				tableRow.addView(eventTextView);
				eventLayout.addView(tableRow, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			}
		}

		if (banned != null && !banned)
		{
			entranceEndicator.setBackgroundColor(Color.GREEN);
		} else
		{
			entranceEndicator.setBackgroundColor(Color.RED);
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
		gender.setText(Gender);
		dbID.setText(Id);


		hidePersonalData(showDetails);

	}

	private void video()
	{
        isVideoRunning = true;
        ReadVideoStream myReadVideoStream = new ReadVideoStream();
		myReadVideoStream.execute(videoURL);

		Log.i("AnWebClient", "video thread started");

	}

	private void image()
	{
		Log.i("something", "downloadfile");
		new ReciveFile(this.getFilesDir().getAbsolutePath(), fileIP, WebListener.this);
        isVideoRunning  = false;
	}

	private void fileRecived(String path)
	{

		/*
		 * 
		 * THIS HAVE NOT BEEN TESTET YET!
		 */
		if (ImageIAmWaitingFor.equals(path) || ImageIAmWaitingFor.equals("new image"))
		{
            //noinspection deprecation
            BitmapDrawable imageBitmap = new BitmapDrawable(path);

			mj.setVisibility(View.INVISIBLE);
			imageview.setImageDrawable(imageBitmap);
			imageview.setVisibility(View.VISIBLE);
		}

	}

	private void askForShootVideo()
	{
		myCommandClient.send("start video server");
	}

	private void askForShootImage(String id)
	{
		ImageIAmWaitingFor = "new image";
		myCommandClient.send("take picture for " + id);
	}

	private void askForOldImage(String path)
	{
		// SOME KIND OF CHECK IF THE IMAGE IS STORED LOCALY
		// if image does not excists the bitmap factory retuns null

		Bitmap bitmap = BitmapFactory.decodeFile(this.getFilesDir().getAbsolutePath() + "/" + path);
		if (bitmap == null)
		{
			myCommandClient.send("send picture from disk:" + path + "#");
			ImageIAmWaitingFor = this.getFilesDir().getAbsolutePath() + "/" + path;
		} else
		{

			mj.setVisibility(View.INVISIBLE);
			imageview.setImageBitmap(bitmap);
			imageview.setVisibility(View.VISIBLE);
		}

	}
    private void hidePersonalData(boolean show)
    {

        if (show)
        {
            eventLayout.setVisibility(View.VISIBLE);
            lockIcon.setVisibility(View.INVISIBLE);
            dbID.setVisibility(View.INVISIBLE);
            banButton.setVisibility(View.VISIBLE);

        } else
        {
        	
        	
            eventLayout.setVisibility(View.INVISIBLE);
            lockIcon.setVisibility(View.VISIBLE);
            dbID.setVisibility(View.VISIBLE);

            banButton.setVisibility(View.INVISIBLE);

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
			mj.setSource(result);
			mj.setDisplayMode(MjpegView.SIZE_BEST_FIT);
			mj.showFps();
		}

	}



}
