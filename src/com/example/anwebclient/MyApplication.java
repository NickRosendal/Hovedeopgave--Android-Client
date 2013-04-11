package com.example.anwebclient;
import com.example.webClient.CommandClient;

import android.app.Application;
import android.content.res.Configuration;

public class MyApplication extends Application
{
	CommandClient myCommandClient;
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	@Override
	public void onLowMemory()
	{
		super.onLowMemory();
	}

	@Override
	public void onTerminate()
	{
		super.onTerminate();
	}

	public CommandClient getCommandClient()
	{
		return myCommandClient;
	}

	public void setCommandClient(CommandClient CommandClient)
	{
		this.myCommandClient = CommandClient;
	}

}
