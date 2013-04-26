package com.example.anwebclient;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class ReciveFile extends AsyncTask<String, Void, Bitmap>
{

	@Override
	protected Bitmap doInBackground(String... params)
	{

		byte[] aByte = new byte[1];
		int bytesRead;

		InputStream inputStream = null;
		Socket clientSocket = null;

		try
		{
			clientSocket = new Socket(params[0], fileServerPort);
			inputStream = clientSocket.getInputStream();
		} catch (IOException ex)
		{
			// Do exception handling
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		if (inputStream != null)
		{

			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			try
			{

				bytesRead = inputStream.read(aByte, 0, aByte.length);

				do
				{
					baos.write(aByte);
					bytesRead = inputStream.read(aByte);
				} while (bytesRead != -1);

				byte[] rawStream = baos.toByteArray();
				String fileString = "";
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
				fos = new FileOutputStream(fileString);
				bos = new BufferedOutputStream(fos);
				bos.write(result);
				bos.flush();
				bos.close();
				clientSocket.close();
				// notifyObservers("file:" + fileString +
				// "have been downloaded");
			} catch (IOException ex)
			{
				ex.printStackTrace();
				// notifyObservers("FileReceiver had a error");
			}
		}

		return null;
	}
}