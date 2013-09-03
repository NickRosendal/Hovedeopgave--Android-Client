package com.example.anwebclient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.webClient.CommandClient;

class searchResult
{

	public String getName()
	{
		return name;
	}

	public String getId()

	{
		return id;
	}

	public String getImagePath()
	{
		return imagePath;
	}

	public Bitmap getImage()
	{
		return image;
	}

	public CommandClient getMyCommandClient()
	{
		return myCommandClient;
	}

	public String getAppPath()
	{
		return appPath;
	}

	private final String name;
	private final String id;
	private final String imagePath;
	private Bitmap image = null;
	private final CommandClient myCommandClient;
	private final String appPath;
	private int rowId;

	public int getRowId()
	{
		return rowId;
	}

	public void setRowId(int rowId)
	{
		this.rowId = rowId;
	}

	searchResult(String name, String id, String imagePath, CommandClient myCommandClient, String appPath)
	{
		this.name = name;
		this.id = id;
		this.imagePath = imagePath;
		this.myCommandClient = myCommandClient;
		this.appPath = appPath;
		checkIfImageIsLocal(imagePath);
		// this.getFilesDir().getAbsolutePath()
	}

	public void checkIfImageIsLocal(String path)
	{
		Bitmap bitmap = BitmapFactory.decodeFile(appPath + "/" + imagePath);
		if (bitmap == null)
		{
			myCommandClient.send("send picture from disk:" + imagePath + "#");

			// problemet er her at WEBLISTENERN f�r resultatet for image :/

		} else
		{
			image = bitmap;
		}
	}

}
