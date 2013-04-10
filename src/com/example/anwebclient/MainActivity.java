package com.example.anwebclient;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.designPatterns.ObserverPattern_Observer;
import com.example.webClient.CommandClient;

public class MainActivity extends Activity implements ObserverPattern_Observer
{

	Button PUSHDABUTTON;
	EditText textboxip;
	CommandClient myCommandClient;
	String serverIp ="";
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textboxip = (EditText) findViewById(R.id.textboxip);
				
		PUSHDABUTTON = (Button) findViewById(R.id.pushDaButtonButton);
		PUSHDABUTTON.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				serverIp = textboxip.getText()+"";
				connectToCommandServer();
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
	
	private void nextActivity(){
		Intent newX = new Intent(MainActivity.this, WebListener.class);
		newX.putExtra("SERVER ADRESS", serverIp);
		
		Bundle b = new Bundle();
		b.putParcelable("CommandClient", myCommandClient);
		newX.putExtras(b);
	//	newX.putExtra("commandClient", myCommandClient);
		startActivity(newX);
	}
	private void connectToCommandServer(){

	
			//myCommandClient = CommandClient.INSTANCE;
			myCommandClient = new CommandClient("10.36.98.82", 5000);
//	myCommandClient.CommandClientSetup("10.36.98.82", 5000);
			myCommandClient.registerObserver(this);
			myCommandClient.send("pending");

		
	
	}

	@Override
	public void update(String eventData)
	{
		if(eventData.equals("connection accepted")){
			myCommandClient.removeObserver(this);
			nextActivity();
		}

	}



}
