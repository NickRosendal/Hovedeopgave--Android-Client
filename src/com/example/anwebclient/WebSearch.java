package com.example.anwebclient;

import java.util.ArrayList;

import com.example.Mjpeg.MjpegView;
import com.example.webClient.CommandClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class WebSearch extends Activity implements com.example.designPatterns.ObserverPattern_Observer
{
	RadioButton female;
	RadioButton male;
	Button search;
	EditText name;
	

	CommandClient myCommandClient;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_search);
		female = (RadioButton) findViewById(R.id.radioFemale);
		male = (RadioButton) findViewById(R.id.RadioMale);
		search = (Button) findViewById(R.id.buttonForSerch);
		name = (EditText) findViewById(R.id.editTextSerchName);

		search.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				String toSearch = search.getText() + "";
				Boolean isMale = null;

				if (female.isChecked() && male.isChecked())
				{
					isMale = null;
				} else if (female.isChecked() && !male.isChecked())
				{
					isMale = false;
				} else if (!female.isChecked() && male.isChecked())
				{
					isMale = true;
				}
				askServerForHits(toSearch, isMale);

			}
		});
		MyApplication mApplication = (MyApplication) getApplicationContext();

		if (mApplication.getCommandClient() == null)
		{
			Log.i("AnWebClient", "Error Weblistner not using application commandClient");
			// to back to previous activity if the commandS
			startActivity(new Intent(WebSearch.this, MainActivity.class));
		} else
		{
			myCommandClient = mApplication.getCommandClient();

			myCommandClient.registerObserver(this);
//			askForVideo();
		}
	}

	private void askServerForHits(String name, Boolean male)
	{
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_search, menu);
		return true;
	}

	@Override
	public void update(String eventData)
	{
		// TODO Auto-generated method stub
		
	}

}
