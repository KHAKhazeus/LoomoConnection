package com.kha.loomoconnection.socketserver;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.kha.loomoconnection.R;
import com.kha.loomoconnection.loomocontrol.VisionModule;
import com.kha.loomoconnection.restserver.controller.ContextBinder;
import com.kha.loomoconnection.utils.NetworkUtils;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SocketServer {
    Activity feedBackActivity;
    AsyncHttpServer httpServer;
    TextView ipText;
    TextView infoText;
    int port = 8080;
    private List<WebSocket> vision_callback_sockets = new ArrayList<WebSocket>();
    private List<WebSocket> vision_broadcast_sockets = new ArrayList<WebSocket>();
    private static SocketServer instance = new SocketServer();

    public static SocketServer getInstance() {return instance;}

    public void bindActivity(final Activity activity) {
        feedBackActivity = activity;
        ipText = (TextView)feedBackActivity.findViewById(R.id.ip_text);
        infoText = (TextView)feedBackActivity.findViewById(R.id.info_text);
    }

    public void broadCast(byte[] data) {
        for (WebSocket bcsocket : vision_broadcast_sockets) {
            bcsocket.send(data);
        }
    }


    public void startServer() {
        httpServer = new AsyncHttpServer();
        httpServer.listen(AsyncServer.getDefault(), port);
        String localIP = NetworkUtils.getIPAddress(true);
        ((Activity) ContextBinder.context).runOnUiThread(() -> {
            ipText.setText(localIP + ":" + port);
        });
        httpServer.websocket("/visionCallback", new AsyncHttpServer.WebSocketRequestCallback() {
            @Override
            public void onConnected(final WebSocket webSocket, AsyncHttpServerRequest request) {
                vision_callback_sockets.add(webSocket);
                //Use this to clean up any references to your websocket
                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        try {
                            if (ex != null)
                                Log.e("WebSocket", "An error occurred", ex);
                        } finally {
                            vision_callback_sockets.remove(webSocket);
                        }
                    }
                });

                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        byte[] data;
                        if ("colorfetch".equals(s)) {
                            Log.i("WebSocket", "ColorFetch");
                            data = VisionModule.getInstance().getColorData();
//                            Log.i("WebSocket", Arrays.toString(data));
                            int size = data.length;
                            int MAX_LENGTH = 65535;
                            int start = 0;
//                            while(size > 0){
//                                if (size > MAX_LENGTH) {
//                                    webSocket.send(Arrays.copyOfRange(data, start, start + MAX_LENGTH));
//                                    start += MAX_LENGTH;
//                                    size -= MAX_LENGTH;
//                                } else {
//                                    webSocket.send(Arrays.copyOfRange(data, start, data.length));
//                                    size = 0;
//                                }
//                            }
//                            webSocket.send(Arrays.copyOfRange(data, 0, 640*480 - 1));
//                            webSocket.send(Arrays.copyOfRange(data, 640*480, 640*480 * 2 - 1));
//                            webSocket.send(Arrays.copyOfRange(data, 640*480 * 2, 640*480 * 3 - 1));
//                            webSocket.send(Arrays.copyOfRange(data, 640*480 * 3, 640*480 * 4 - 1));
//                            webSocket.send(data.clone(), 0, 640 * 480 * 4);
//                            webSocket.send(data.clone(), 640*480, 640*480 * 2);
//                            webSocket.send(data.clone(), 640*480*2, 640*480 * 3);
//                            webSocket.send(data.clone(), 640*480*3, 640*480 * 4);
                            webSocket.send("not supported");
                            Log.i("WebSocket", "sendOver");
                        } else if ("depthfetch".equals(s)) {
                            Log.i("WebSocket", "DepthFetch");
                            data = VisionModule.getInstance().getDepthData();
                            webSocket.send(data);
                        } else {
                            Log.i("WebSocket", "Unknown instruction");
                            webSocket.send("Wrong instruction");
                        }
                    }
                });


            }
        });
        httpServer.websocket("/visionBroadcast", new AsyncHttpServer.WebSocketRequestCallback() {
            @Override
            public void onConnected(final WebSocket webSocket, AsyncHttpServerRequest request) {
                vision_broadcast_sockets.add(webSocket);
                //Use this to clean up any references to your websocket
                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        try {
                            if (ex != null)
                                Log.e("WebSocket", "An error occurred", ex);
                        } finally {
                            vision_broadcast_sockets.remove(webSocket);
                        }
                    }
                });
            }
        });
    }

}
