package com.hsae.platform.nfu.connect.connector.wifi;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import com.hsae.platform.nfu.connect.connector.ConnectorListener;
import com.hsae.platform.nfu.utils.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class WifiConnector {
    public static final int RECONNECT_COUNT = 2;
    public static final long RECONNECT_DELAY = 2000L;
    public static final String RECONNECT_MARK = "reconnect_mark";
    private static final String TAG = "WifiConnector";

    private volatile ConnectThread mConnectThread;
    private volatile ConnectedThread mConnectedThread;
    private volatile int state;
    private volatile ConnectorListener connectorListener;
    private volatile Handler reconnectHandler = new Handler(Looper.getMainLooper());
    private volatile String host;
    private volatile int port;
    private volatile int reconnect;

    public WifiConnector(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void setConnectorListener(ConnectorListener connectorListener) {
        this.connectorListener = connectorListener;
    }

    public int getState() {
        return state;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     */
    public synchronized void connect() {
        if (reconnectHandler == null) return;
        reconnectHandler.removeCallbacks(reconnectRunnable);

        if (state == ConnectorListener.State.STATE_CONNECTED ||
                state == ConnectorListener.State.STATE_CONNECTING) return;

        LogUtil.i("connect to: " + host + "-" + port, TAG);

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given host&port
        mConnectThread = new ConnectThread(host, port);
        mConnectThread.start();
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        LogUtil.i("stop: " + host + "-" + port, TAG);

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (reconnectHandler != null) {
            reconnectHandler.removeCallbacks(reconnectRunnable);
            reconnectHandler = null;
        }

        connectorListener = null;
        onStateChanged(ConnectorListener.State.STATE_NONE, null);
    }

    /**
     * close current connection
     */
    public synchronized void close() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (state != ConnectorListener.State.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        if (r != null) r.write(out);
    }

    public void heartBeat() {
        ConnectedThread r;
        synchronized (this) {
            if (state != ConnectorListener.State.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.heartBeat();
    }

    private Runnable reconnectRunnable = () -> {
        synchronized (WifiConnector.this) {
            reconnect++;
            LogUtil.e( "reconnect(" + reconnect + "): " + host + "-" + port, TAG);
            connect();
        }
    };

    private void reconnect() {
        if (reconnectHandler != null) {
            reconnectHandler.removeCallbacks(reconnectRunnable);
            reconnectHandler.postDelayed(reconnectRunnable, RECONNECT_DELAY);
        }
    }

    private synchronized void onStateChanged(@ConnectorListener.State int state, String msg) {
        this.state = state;
        if (state == ConnectorListener.State.STATE_NONE) {
            if (reconnect < RECONNECT_COUNT) {
                reconnect();
                if (reconnectHandler != null) {
                    msg = RECONNECT_MARK + "WIFI-" + port + "-重连" + (reconnect + 1) + "次";
                }
            } else {
                reconnect = 0;
                LogUtil.e( "reconnect cancel: " + host + "-" + port, TAG);
            }
        } else if (state == ConnectorListener.State.STATE_CONNECTED) {
            reconnect = 0;
        }
        if (connectorListener != null) connectorListener.onStateChanged(state, msg);
    }

    /**
     * Start the ConnectedThread to begin managing a connection
     *
     * @param socket The Socket on which the connection was made
     */
    private synchronized void connected(Socket socket) {
        LogUtil.i( "connected: " + host + "-" + port, TAG);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final String host;
        private final int port;
        private final Socket mmSocket;

        public ConnectThread(String host, int port) {
            this.host = host;
            this.port = port;
            mmSocket = new Socket();
            onStateChanged(ConnectorListener.State.STATE_CONNECTING, null);
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            LogUtil.i( "BEGIN mConnectThread: " + host + "-" + port, TAG);
            setName("ConnectThread-" + port);

            // Make a connection
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.setTcpNoDelay(true);
                mmSocket.setKeepAlive(true);
                mmSocket.setSoLinger(true, 0);
                mmSocket.bind(null);
                mmSocket.connect(new InetSocketAddress(host, port), 0);
            } catch (Exception e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    LogUtil.e(
                            "unable to close() socket during connection: " + host + "-" + port,
                            TAG
                    );
                }
                onStateChanged(
                        ConnectorListener.State.STATE_NONE,
                        "尝试连接" + host + "异常：" + e.toString()
                );
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (WifiConnector.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                LogUtil.e( "close() of connect socket failed: " + host + "-" + port, TAG);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final Socket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(Socket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the Socket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                LogUtil.e( "temp sockets not created: " + host + "-" + port, TAG);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            onStateChanged(ConnectorListener.State.STATE_CONNECTED, null);
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            byte[] buffer = new byte[8192]; //8K

            try {
                // Keep listening to the InputStream while connected
                while (state == ConnectorListener.State.STATE_CONNECTED) {
                    int haveRead = mmInStream.read(buffer);
                    if (haveRead == -1) {
                        LogUtil.e( "disconnected(-1): " + host + "-" + port, TAG);
                        onStateChanged(ConnectorListener.State.STATE_NONE, host + "对端已关闭！");
                        break;
                    }
                    byte[] data = new byte[haveRead];
                    System.arraycopy(buffer, 0, data, 0, haveRead);
                    if (connectorListener != null) connectorListener.onReceiveData(data, true);
                }

            } catch (IOException e) {
                LogUtil.e( "disconnected: " + host + "-" + port + "-" + e.getMessage(), TAG);
                onStateChanged(ConnectorListener.State.STATE_NONE, host + "对端异常：" + e.toString());
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                mmOutStream.flush();
            } catch (IOException e) {
                LogUtil.e( "Exception during write: " + host + "-" + port, TAG);
            }
        }

        void heartBeat() {
            try {
                mmSocket.sendUrgentData(0xFF);
            } catch (IOException e) {
                close();
            }
        }

        public void cancel() {
            if (mmOutStream != null) {
                try {
                    mmOutStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mmInStream != null) {
                try {
                    mmInStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                mmSocket.close();
            } catch (IOException e) {
                LogUtil.e( "close() of connected socket failed: " + host + "-" + port, TAG);
            }
        }
    }
}
