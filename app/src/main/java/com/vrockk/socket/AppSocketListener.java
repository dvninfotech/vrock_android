package com.vrockk.socket;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.vrockk.VrockkApplication;

import io.socket.client.Ack;
import io.socket.emitter.Emitter;

/**
 * Created by Mahabali on 11/14/15.
 */
public class AppSocketListener implements SocketListener {
    private static AppSocketListener sharedInstance;
    private SocketIOService socketServiceInterface;
    public SocketListener activeSocketListener;
    private Context mContext;

    public void setActiveSocketListener(SocketListener activeSocketListener) {
        this.activeSocketListener = activeSocketListener;
        if (socketServiceInterface != null && socketServiceInterface.isSocketConnected()) {
            onSocketConnected();
        }
    }

    public static AppSocketListener getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new AppSocketListener();
        }
        return sharedInstance;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            socketServiceInterface = ((SocketIOService.LocalBinder) service).getService();
            socketServiceInterface.setServiceBinded(true);
            socketServiceInterface.setSocketListener(sharedInstance);
            if (socketServiceInterface.isSocketConnected()) {
                onSocketConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            socketServiceInterface.setServiceBinded(false);
            socketServiceInterface = null;
            onSocketDisconnected();
        }
    };

    /**
     * public void initialize(){
     * <p>
     * try{
     * Intent intent = new Intent(new MyApplication().getContext(), SocketIOService.class);
     * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
     * new MyApplication().getContext().startService(intent);
     * //  new MyApplication().getContext().startForegroundService(intent);
     * } else {
     * new MyApplication().getContext().startService(intent);
     * }
     * <p>
     * new MyApplication().getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
     * LocalBroadcastManager.getInstance(new MyApplication().getContext()).
     * registerReceiver(socketConnectionReceiver, new IntentFilter(SocketUrls.
     * socketConnection));
     * <p>
     * }
     * <p>
     * catch (Exception e){e.printStackTrace();}
     * <p>
     * //        new MyApplication().getContext().startService(intent);
     * <p>
     * <p>
     * <p>
     * //        LocalBroadcastManager.getInstance(new ChatApplication().getContext()).
     * //                registerReceiver(connectionFailureReceiver, new IntentFilter(SocketUrls.
     * //                        connectionFailure));
     * <p>
     * }
     */


    public void initialize(Context applicationContext) {
        try {
            if (applicationContext != null) {
                Log.e("initialize", "try");
                mContext = applicationContext;
                Intent intent = new Intent(applicationContext, SocketIOService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent intent1 = new Intent(applicationContext, ForegroundService.class);
                    intent.setAction(ForegroundService.ACTION_START_FOREGROUND_SERVICE);
                    applicationContext.startService(intent1);              //  applicationContext.startForegroundService(intent);
                } else {
                    applicationContext.startService(intent);
                }
                applicationContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                LocalBroadcastManager.getInstance(applicationContext).
                        registerReceiver(socketConnectionReceiver, new IntentFilter(SocketUrls.
                                socketConnection));
            }
        } catch (Exception e) {
            Log.e("initialize", "catch");
            e.printStackTrace();
        }
    }

    /*
     { cmp=com.project.cash/.socket.SocketIOService }: app is in background uid UidRecord{d1a655d u0a288 SVC  idle change:idle|uncached procs:1 seq(0,0,0)}
     */

    private BroadcastReceiver socketConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean connected = intent.getBooleanExtra("connectionStatus", false);
            if (connected) {
                Log.e("AppSocketListener", "Socket connected");
                onSocketConnected();
            } else {
                onSocketDisconnected();
            }
        }
    };

    private BroadcastReceiver connectionFailureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast toast = Toast.
//                    makeText(new ChatApplication().getContext(), "Please check your network connection",
//                            Toast.LENGTH_SHORT);
//            toast.show();
        }
    };


    public void destroy() {
        if (socketServiceInterface != null)
            socketServiceInterface.setServiceBinded(false);
        new VrockkApplication().getContext().unbindService(serviceConnection);
        LocalBroadcastManager.getInstance(new VrockkApplication().getContext()).unregisterReceiver(socketConnectionReceiver);
    }

    @Override
    public void onSocketConnected() {
        if (activeSocketListener != null) {
            activeSocketListener.onSocketConnected();
        }
    }

    @Override
    public void onSocketDisconnected() {
        if (activeSocketListener != null) {
            activeSocketListener.onSocketDisconnected();
        }
    }

    @Override
    public void onSocketConnectionError() {
        if (activeSocketListener != null) {
            activeSocketListener.onSocketConnectionError();
        }
    }

    @Override
    public void onSocketConnectionTimeOut() {
        if (activeSocketListener != null) {
            activeSocketListener.onSocketConnectionTimeOut();
        }
    }

    public void addOnHandler(String event, Emitter.Listener listener) {
        if (socketServiceInterface != null)
            socketServiceInterface.addOnHandler(event, listener);
    }

    public void emit(String event, Object[] args, Ack ack) {
        if (socketServiceInterface != null)
            socketServiceInterface.emit(event, args, ack);
    }

    public void emit(String event, Object... args) {
        if (socketServiceInterface != null)
            socketServiceInterface.emit(event, args);
    }

    public void connect() {
        if (socketServiceInterface != null)
            socketServiceInterface.connect();
    }

    public void disconnect() {
        if (socketServiceInterface != null)
            socketServiceInterface.disconnect();
    }

    public void off(String event, Emitter.Listener ongetChatList) {
        if (socketServiceInterface != null) {
            socketServiceInterface.off(event, ongetChatList);
        }
    }

    public void off(String event) {
        if (socketServiceInterface != null) {
            socketServiceInterface.off(event);
        }
    }

    public boolean isSocketConnected() {
        if (socketServiceInterface == null) {
            return false;
        }
        return socketServiceInterface.isSocketConnected();
    }

    public void setAppConnectedToService(Boolean status) {
        if (socketServiceInterface != null) {
            socketServiceInterface.setAppConnectedToService(status);
        }
    }

    public void restartSocket() {
        if (socketServiceInterface != null) {
            socketServiceInterface.restartSocket();
        }
    }

    public void signOutUser() {
        AppSocketListener.getInstance().disconnect();
        AppSocketListener.getInstance().connect();
    }
}