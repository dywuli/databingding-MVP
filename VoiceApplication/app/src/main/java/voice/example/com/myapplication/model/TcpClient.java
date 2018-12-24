package voice.example.com.myapplication.model;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import voice.example.com.myapplication.ActivityPresenterImpl;

/**
 * Created by liwu on 18-11-21.
 */

public class TcpClient {
    private static final String TAG = "RecorderVoice";
    private static TcpClient mTcpClient;
    private volatile String mDspFileName;
    public static final String START = "Start";
    public static final String STOP = "Stop";
    public static final String SUCCESS = "Success";
    public static final String TYPE_SHORT = "shortRecord";
    public static final String TYPE_LONG = "longRecord";

    private ConnectListener mListener;
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(5);
    private WebSocketClient mWebSocket;
    private volatile boolean mIsRecordable = false;


    public interface ConnectListener {
        void onErrorData(String data);

        void onConnected(boolean success);

        void onRecordSuccess(boolean isSuccess);

        void onQueryListLoadFinished(List<RecordItem> itemList);
    }

    public void setOnConnectListener(ConnectListener listener) {
        this.mListener = listener;
    }

    public synchronized static TcpClient getInstance() {
        if (mTcpClient == null) mTcpClient = new TcpClient();
        return mTcpClient;
    }

    public void onCreateConnect(final String strUrl) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                mWebSocket = new WebSocketClient(URI.create(strUrl)) {

                    @Override
                    public void onOpen(ServerHandshake handshakedata) {
                        Log.d(TAG, ">>>>>>>>onOpen ");
                        File file = new File(ActivityPresenterImpl.RECORD_SHORT_DIRECTORY);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                    }

                    @Override
                    public void onMessage(String message) {
                        Log.d(TAG, ">>>>>>>>onMessage " + message);
                    }

                    @Override
                    public void onMessage(ByteBuffer bytes) {
                        super.onMessage(bytes);
                        Log.d(TAG, ">>>>>>>>onMessage " + bytes.array().length);
                        onAcceptRecData(bytes);
                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        Log.d(TAG, ">>>>>>>>onClose " + reason);
                    }

                    @Override
                    public void onError(Exception ex) {
                        Log.d(TAG, ">>>>>>>>onError " + ex);
                    }
                };
                mWebSocket.connect();
            }
        });

    }

    public boolean connectIsOpen() {
        boolean isOpen = false;
        if (mWebSocket != null) {
            isOpen = mWebSocket.isOpen();
        }
        return isOpen;
    }

    private void onAcceptRecData(ByteBuffer byteBuffer) {
        if (!byteBuffer.hasRemaining()) {
            mListener.onRecordSuccess(false);
            return;
        }

        if (mIsRecordable) {
            Log.d(TAG, ">>>>>>>>> onAcceptRecData " + mDspFileName);
            File file = new File(mDspFileName);
            if (file.exists()) file.delete();
            FileOperation.writeFile(mDspFileName, byteBuffer);
            mIsRecordable = false;
            mListener.onRecordSuccess(true);
        } else {
            String QueryListFile = ActivityPresenterImpl.RECORD_SHORT_DIRECTORY + "/QueryList.txt";
            File file = new File(QueryListFile);
            if (file.exists()) file.delete();
            FileOperation.writeFile(QueryListFile, byteBuffer);
            mListener.onQueryListLoadFinished(FileOperation.readQueryFromFile(file));
        }
    }


    /**
     * 发送数据
     *
     * @param data 需要发送的内容
     */
    public void send(final String data) {
        if (STOP.equals(data)) {
            mIsRecordable = true;
        } else if (TYPE_LONG.equals(data) || TYPE_SHORT.equals(data)) {
            mIsRecordable = false;
        }
         mWebSocket.send(data);
    }

    /**
     * 断开连接
     *
     * @throws IOException
     */
    public void disconnect() {
        try {
            mExecutor.shutdown();
            if (mWebSocket != null) {
                mWebSocket.close();
                mWebSocket = null;
            }
        } catch (Exception e) {
            Log.d(TAG, ">>>>>>>>> disconnect " + e);
        }
    }

    public void setFilePath(String path, String fileName) {
        mDspFileName = path + File.separator + fileName + ".pcm";
    }

}

