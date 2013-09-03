package com.example.anwebclient;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.*;


class doorManLogin extends Builder
{

	doorManLogin(Context context, final String guestId, final MyApplication applicatinContext)
	{

        super(context);
		// TODO Auto-generated constructor stub

		
//		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		this.setTitle("Log In");

		final EditText password = new EditText(context);
        password.setTextSize(35);


		LinearLayout ll = new LinearLayout(context);
		ll.setOrientation(LinearLayout.VERTICAL);
        String lastUsedDoorman = "noooobody";
        if(applicatinContext.getLastDoorManWhoUsedDevice() != null){
         lastUsedDoorman = applicatinContext.getLastDoorManWhoUsedDevice().name;
        }
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final RadioGroup radiog = new RadioGroup(context);

        int count = 0;

        for (String doorman : applicatinContext.getDoorMen())
        {
            RadioButton temp = new RadioButton(context);
            temp.setTextSize(40);
            temp.setButtonDrawable(R.drawable.button_radio);
            temp.setText(doorman);
            temp.setId(count);
            radiog.addView(temp);
            if(doorman.equals(lastUsedDoorman)){
                temp.setChecked(true);
            }
            count++;
        }

        ll.addView(radiog, p);
        ll.addView(password);
        this.setView(ll);

		this.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				// sendGuestInfo:id:17#user:username:kim#passowrd:1234###
                if (radiog.getCheckedRadioButtonId() != -1)
                {
                String doormanName = applicatinContext.getDoorMen().get(radiog.getCheckedRadioButtonId());
                String doormanPassword = password.getText()+"";


                applicatinContext.getCommandClient().send("sendGuestInfo:guestId:" + guestId + "#user:username:" + doormanName + "#password:"
                            + doormanPassword + "###");
                applicatinContext.setLastDoorManWhoUsedDevice(doormanName, doormanPassword);
                }
			}

		});
		this.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				dialog.cancel();
			}
		});
	}


}
