package com.kha.loomoconnection.restserver;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.kha.loomoconnection.R;
import com.kha.loomoconnection.utils.NetworkUtils;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import java.util.concurrent.TimeUnit;

public class ServerManager {

    private Server mServer;
    private Activity bindActivity;
    private TextView ipText;
    private TextView infoText;
    private int port = 8888;
    private static ServerManager instance = new ServerManager();

    public static ServerManager getInstance() {return instance;}

    public void bindActivity(Activity activity) {
        bindActivity = activity;
        ipText = bindActivity.findViewById(R.id.ip_text);
        infoText = bindActivity.findViewById(R.id.info_text);
        mServer = AndServer.webServer(activity)
                .port(port)
                .timeout(10, TimeUnit.SECONDS)
                .listener(new Server.ServerListener() {
                    @Override
                    public void onStarted() {
                        String localIP = NetworkUtils.getIPAddress(true);
                        ipText.setText(localIP + ":" + port);
                        Log.w("Andserver", "Server Started at " + localIP + ":" +
                                port);
                    }

                    @Override
                    public void onStopped() {
                        // TODO The server has stopped.
                    }

                    @Override
                    public void onException(Exception e) {
                        // TODO An exception occurred while the server was starting.
                    }
                })
                .build();
    }

    /**
     * Start server.
     */
    public void startServer() {
        if (mServer.isRunning()) {
            // TODO The server is already up.
        } else {
            mServer.startup();
        }
    }

    public void refreshText() {
        String localIP = NetworkUtils.getIPAddress(true);
        ipText.setText(localIP + ":" + port);
    }

    /**
     * Stop server.
     */
    public void stopServer() {
        if (mServer.isRunning()) {
            mServer.shutdown();
        } else {
            Log.w("AndServer", "The server has not started yet.");
        }
    }
}
