package com.example.anwebclient;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import com.example.designPatterns.ObserverPattern_Observer;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class ReciveFile implements com.example.designPatterns.ObserverPattern_Subject, Runnable
{
	private com.example.designPatterns.ObserverPattern_Observer Observer;
	private final String path;
	private final String applicationPath;

	ReciveFile(String applicationPath, String serverAdresse, com.example.designPatterns.ObserverPattern_Observer o)
	{
		registerObserver(o);
		this.applicationPath = applicationPath;
		this.path = serverAdresse;
		new Thread(this).start();
		// this.run();
	}

	@Override
	public void run()
	{

		byte[] aByte = new byte[1];
		int bytesRead;

		InputStream inputStream = null;
		Socket clientSocket = null;
		String fileString = "";

		try
		{
			clientSocket = new Socket(path, 5001);
			inputStream = clientSocket.getInputStream();
		} catch (IOException ex)
		{
			// Do exception handling
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		if (inputStream != null)
		{

			FileOutputStream fos;
			BufferedOutputStream bos = null;
			try
			{
				Log.i("anwebclient", "starting to read image..");


                bytesRead = inputStream.read(aByte, 0, aByte.length);

                do
				{
					baos.write(aByte);
					bytesRead = inputStream.read(aByte);


                } while (bytesRead != -1);

				byte[] rawStream = baos.toByteArray();

				int offset = 0;
				for (int i = 0; i < rawStream.length; i++)
				{
					if ((char) rawStream[i] == '#')
					{
						offset = i + 1;
						break;
					}
					fileString += (char) rawStream[i];

				}
				// System.out.println("file_string: " + fileString );

				byte[] result = new byte[rawStream.length - offset];
				System.arraycopy(rawStream, offset, result, 0, result.length);
				fileString = applicationPath + "/" + fileString;
				// fileString = Environment.getExternalStorageDirectory() +
				// File.separator +fileString;
				Log.i("anwebclient", "fileString: " + fileString);

				try
				{
					File directory = new File(fileString.substring(0, fileString.lastIndexOf("/")));
                    //noinspection ResultOfMethodCallIgnored
                    directory.mkdirs();
				} catch (Exception E)
				{
					Log.i("anwebclient", E.toString());
				}

				Log.i("anwebclient", "folderPath: " + fileString.substring(0, fileString.lastIndexOf("/")));

				fos = new FileOutputStream(fileString);
				bos = new BufferedOutputStream(fos);
				bos.write(result);
				bos.flush();
				bos.close();
				clientSocket.close();
				notifyObservers("FILE DOWNLOADED: " + fileString);
				// notifyObservers("file:" + fileString +
				// "have been downloaded");
			} catch (IOException ex)
			{
				ex.printStackTrace();
				// notifyObservers("FileReceiver had a error");
			}
		}

		// return fileString;
	}

	@Override
	public void registerObserver(ObserverPattern_Observer o)
	{
		Observer = o;
	}

	@Override
	public void removeObserver(ObserverPattern_Observer o)
	{
		Observer = null;
	}

	@Override
	public void notifyObservers(String info)
	{
		Observer.update(info);
	}

}