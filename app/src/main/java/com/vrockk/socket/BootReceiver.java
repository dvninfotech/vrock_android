package com.vrockk.socket;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by Mahabali on 11/14/15.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        try
        {
            Intent serviceIntent = new Intent(SocketIOService.class.getName());
            context.startService(serviceIntent);
        }
        catch (Exception exp)
        {

        }
    }
}