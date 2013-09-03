package com.example.anwebclient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.designPatterns.ObserverPattern_Observer;
import com.example.webClient.CommandClient;

public class MainActivity extends Activity implements ObserverPattern_Observer
{

    private EditText editIpBox;

    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editIpBox = (EditText) findViewById(R.id.textboxip);

		final SharedPreferences settings = getPreferences(MODE_PRIVATE);
		editIpBox.setText(settings.getString("serverIp", "192.168.1.0"));

        Button connectButton = (Button) findViewById(R.id.pushDaButtonButton);
		connectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                settings.edit().putString("serverIp", editIpBox.getText().toString()).commit();
                connectToCommandServer(editIpBox.getText().toString());
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

	private void startWebListenerActivity(String in)
	{
		Intent webListenerIntent = new Intent(MainActivity.this, WebListener.class);
		webListenerIntent.putExtra("users", in);
        webListenerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(webListenerIntent);
	}

	private void connectToCommandServer(String Adress)
	{
		MyApplication mApplication = (MyApplication) getApplicationContext();

        CommandClient myCommandClient = new CommandClient(Adress, 5000, this);
		mApplication.setCommandClient(myCommandClient);
		myCommandClient.registerObserver(this);
		myCommandClient.send("pending");
		myCommandClient.connect();
	}

	@Override
	public void update(final String eventData)
	{
		if (eventData.contains("connection accepted"))
		{
			String userString = "";
			try{
			 userString = eventData.substring(eventData.indexOf("#users:")+7);
			}catch(Exception E){
				Log.i("exception", "users at connected messed up");
			}
			
			//myCommandClient.removeObserver(this);
			startWebListenerActivity(userString);
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
