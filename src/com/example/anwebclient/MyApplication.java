package com.example.anwebclient;
import android.util.Log;
import com.example.webClient.CommandClient;

import android.app.Application;
import android.content.res.Configuration;

import java.util.ArrayList;
import java.util.Date;

public class MyApplication extends Application
{
    private doorman lastUsage;
	private CommandClient myCommandClient;
    private static final ArrayList<String> DoorMen = new ArrayList<String>();


    public CommandClient getCommandClient()
	{
		return myCommandClient;
	}
    public void setLastDoorManWhoUsedDevice(String name, String password){
        Log.i("door","doorman is sat!");
          lastUsage = new doorman(name,password,new Date());
    }
    public doorman getLastDoorManWhoUsedDevice(){
         Date now = new Date();

        //5minuter, til miliseconds
        long MAX_DURATION = 5*60*1000;

           try{
        long duration = now.getTime() - lastUsage.timeOfSet.getTime();
        Log.i("door",""+duration);

        if (duration <= MAX_DURATION) {


        return lastUsage;
        } else return null;
           }catch(NullPointerException ignored){}
         return null;
    }
        public ArrayList<String> getDoorMen(){
            return DoorMen;
        }
    public void addDoorMan(String in){
                if(!DoorMen.contains(in))  {
                    DoorMen.add(in);
                }
    }

	public void setCommandClient(CommandClient CommandClient)
	{
		this.myCommandClient = CommandClient;
	}
    class doorman{
        String name;
        String password;
        Date timeOfSet;
        public doorman(String name, String password, Date timeOfSet){
            this.name = name;
            this.password = password;
            this.timeOfSet = timeOfSet;
        }

    }
}
