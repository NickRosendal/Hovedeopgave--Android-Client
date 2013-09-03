package com.example.webClient;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

import com.example.designPatterns.ObserverPattern_Subject;
import com.example.designPatterns.ObserverPattern_Observer;

public class CommandClient implements ObserverPattern_Subject, Runnable
{
	private Socket socket = null;
	private BufferedReader reader = null;
	private BufferedWriter writer = null;
	private InetAddress address = null;
	private final int port;
	private final BlockingQueue<String> sendMessageQueue = new LinkedBlockingQueue<String>(64);
	private Thread readerThead;
    private ObserverPattern_Observer o = null;
	private final Thread myThread;

	public CommandClient(String address, int port, ObserverPattern_Observer observer)
	{
		o = observer;
		try
		{
			this.address = InetAddress.getByName(address);
		} catch (UnknownHostException e)
		{

			e.printStackTrace();
		}
		this.port = port;
		myThread = new Thread(this, "CommandClient");
		// myThread.start();

	}

	private void openStreams() throws IOException
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
					 * 
					 * 
					 * this shit is not true... the statement above just retursn
					 * null to the observer... there for we have to catch the
					 * nullpoint exception, in the update statement of the other
					 * classes.
					 */
					catch (SocketException e)
					{
						notifyObservers("connection broke");
						this.interrupt();
						try
						{
							Thread.sleep(10);
						} catch (InterruptedException e1)
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					/*
					 * This exception is cough when the time out of the
					 * "socket buffered reader" is meet, we do nothing :)
					 */
					catch (IOException ignored) {}

				}
			}
		};
		readerThead.start();

        Thread writerThread = new Thread("CommandClient writeThread") {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        if (!sendMessageQueue.isEmpty()) {
                            String msg = sendMessageQueue.poll();
                            msg += "\n";
                            writer.write(msg, 0, msg.length());
                            writer.flush();
                        }

                    }
                } catch (SocketException e) {
                    this.interrupt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
		writerThread.start();
	}

	public void interruptReader()
	{
		readerThead.interrupt();
	}

	public void connect()
	{

		try
		{
			myThread.start();
		} catch (RuntimeException e)
		{
			e.printStackTrace();
			if (e.getMessage().contains("Thread already started"))
				notifyObservers("CommandClient is already started");
			// e.printStackTrace();
			// if (e.getCause().toString().contains("No route to host"))
			// {

			// }
		}

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
		if (this.o != null)
			o.update(eventData);
	}

	@Override
	public void run()
	{
		try
		{
			openStreams();
		} catch (IOException e)
		{
			if (e.getCause().toString().contains("No route to host"))
			{
				notifyObservers("No route to host");
			}
		}
	}

	public InetAddress getAddress()
	{
		return address;
	}

}
