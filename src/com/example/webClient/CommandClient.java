package com.example.webClient;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.example.designPatterns.ObserverPattern_Subject;
import com.example.designPatterns.ObserverPattern_Observer;

public class CommandClient implements ObserverPattern_Subject, Runnable
{
	private Socket socket = null;
	private BufferedReader reader = null;
	private BufferedWriter writer = null;
	private InetAddress address = null;
	private int port;
	BlockingQueue<String> sendMessageQueue = new LinkedBlockingQueue<String>(64);
	private Thread readerThead, writerThread = null;
	private ObserverPattern_Observer o = null;
	private Thread myThread;

	public CommandClient(String address, int port)
	{
		try
		{
			this.address = InetAddress.getByName(address);
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		this.port = port;
		myThread = new Thread(this, "CommandClient");
		myThread.start();
	}

	private void connect() throws IOException
	{
		socket = new Socket(address, port);
		socket.setSoTimeout(5000);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

		readerThead = new Thread("CommandClient readThread")
		{
			@Override
			public void run()
			{
				while (!Thread.currentThread().isInterrupted())
				{
					try
					{
						/*
						 * The socket the buffered reader reads has a time out
						 * of 5000ms
						 */
						notifyObservers(reader.readLine());

					}
					/*
					 * The socket exception is cough if the connection is
					 * broken, and the thread should be interrupted
					 */
					catch (SocketException e)
					{
						this.interrupt();
					}
					/*
					 * This exception is cough when the time out of the
					 * "socket buffered reader" is meet, we do nothing :)
					 */
					catch (IOException e)
					{
					}
				}
			}
		};
		readerThead.start();

		writerThread = new Thread("CommandClient writeThread")
		{
			@Override
			public void run()
			{
				try
				{
					while (!Thread.currentThread().isInterrupted())
					{
						if (!sendMessageQueue.isEmpty())
						{
							String msg = sendMessageQueue.poll();
							msg += "\n";
							writer.write(msg, 0, msg.length());
							writer.flush();
						}

					}
				} catch (SocketException e)
				{
					this.interrupt();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		};
		writerThread.start();
	}

	public void disconnect()
	{

		try
		{
			/*
			 * When the socket is closed, the two threads, using it will get a
			 * "socketexception"
			 */
			socket.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void send(String messege)
	{
		sendMessageQueue.add(messege);
	}

	@Override
	public void registerObserver(ObserverPattern_Observer o)
	{
		this.o = o;
	}

	@Override
	public void removeObserver(ObserverPattern_Observer o)
	{
		this.o = null;
	}

	@Override
	public void notifyObservers(String eventData)
	{
		o.update(eventData);
	}

	@Override
	public void run()
	{
		try
		{
			connect();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public InetAddress getAddress()
	{
		return address;
	}



}
