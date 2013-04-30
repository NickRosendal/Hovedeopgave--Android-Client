package com.example.anwebclient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.designPatterns.ObserverPattern_Observer;
import com.example.webClient.CommandClient;

public class MainActivity extends Activity implements ObserverPattern_Observer
{

	Button PUSHDABUTTON;
	EditText textboxIp;
	CommandClient myCommandClient;
	String serverIp = "";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textboxIp = (EditText) findViewById(R.id.textboxip);

		final SharedPreferences settings = getPreferences(MODE_PRIVATE);
		textboxIp.setText(settings.getString("serverIp", "192.168.1.0"));

		PUSHDABUTTON = (Button) findViewById(R.id.pushDaButtonButton);
		PUSHDABUTTON.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				settings.edit().putString("serverIp", textboxIp.getText().toString()).commit();
				connectToCommandServer(textboxIp.getText().toString());
				Log.i("anwebclient", "connect clicked");
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void startWebListenerActivity()
	{
		Intent webListenerIntent = new Intent(MainActivity.this, WebListener.class);
		startActivity(webListenerIntent);
	}

	private void connectToCommandServer(String Adress)
	{
		MyApplication mApplication = (MyApplication) getApplicationContext();
		
		myCommandClient = new CommandClient(Adress, 5000, this);
		mApplication.setCommandClient(myCommandClient);
		myCommandClient.registerObserver(this);
		myCommandClient.send("pending");
		myCommandClient.connect();
	}

	@Override
	public void update(final String eventData)
	{
		if (eventData.equals("connection accepted"))
		{
			myCommandClient.removeObserver(this);
			startWebListenerActivity();
		} else // show what the CommandClient says
		{
			this.runOnUiThread(new Runnable()
			{
				public void run()
				{
					Toast.makeText(MainActivity.this, eventData, Toast.LENGTH_SHORT).show();

				}
			});

		}

	}

}
