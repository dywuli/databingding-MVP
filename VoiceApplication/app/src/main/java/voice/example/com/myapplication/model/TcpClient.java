package voice.example.com.myapplication.model;

import android.text.TextUtils;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
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

    public static final String INVALID = "";
    public static final String REQUEST_START = "Start";
    public static final String REQUEST_STOP = "Stop";
    public static final String REQUEST_QUERY_LIST = "QueryList";
    public static final String REQUEST_QUERY_FILE_NAME_LIST = "QueryFileList";
    public static final String REQUEST_MOVE_DATA = "MoveToUSB";
    public static final String REQUEST_DATA_SIZE = "ReadFileSize";
    public static final String REQUEST_SHORT_RECORD = "ShortRecorder";
    public static final String REQUEST_LONG_RECORD = "LongRecorder";
    public static final String REQUEST_USB_CHECK = "USBCheck";
    public static final String SUCCESS = "Success";


    private ConnectListener mListener;
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(5);
    private ClientSocket mWebSocket;
    private volatile String mRequest = INVALID;
    private boolean mIsSocketClose = true;


    public interface ConnectListener {
        void onShowMessage(String data);

        void onRecordSuccess();

        void onQueryListLoadFinished(List<RecordItem> itemList);

        void onUpdateQueryTxtFileList(List<String> fileList);

        void onUpdateRecordFileSize(String fileSize);

        void onMoveRecorderDataFinished();

        void onConnected();

        void reConnect();

    }

    public void setOnConnectListener(ConnectListener listener) {
        this.mListener = listener;
    }

    public synchronized static TcpClient getInstance() {
        if (mTcpClient == null) mTcpClient = new TcpClient();
        return mTcpClient;
    }

    public void onCreateConnect(final String strUrl) {
        onConnect(strUrl);
//        mExecutor.submit(new Runnable() {
//            @Override
//            public void run() {
//                mWebSocket = new WebSocketClient(URI.create(strUrl)) {
//
//                    @Override
//                    public void onOpen(ServerHandshake handshakedata) {
//                        Log.d(TAG, ">>>>>>>>onOpen ");
//                        File file = new File(ActivityPresenterImpl.RECORD_SHORT_DIRECTORY);
//                        if (!file.exists()) {
//                            file.mkdirs();
//                        }
//                        mListener.onConnected();
//                    }
//
//                    @Override
//                    public void onMessage(String message) {
//                        Log.d(TAG, ">>>>>>>>onMessage " + message);
//                        if (message.contains(ActivityPresenterImpl.KEY_WORD_UNMOUNT) || message.contains(ActivityPresenterImpl.KEY_WORD_MOUNT)) {
//                            mListener.onShowMessage(message);
//                        } else if(message.contains("Error")) {
//                            mListener.onShowMessage(ActivityPresenterImpl.KEY_WORD_ERROR + "SystemError: " + message);
//                            mRequest = INVALID;
//                        } else {
//                            if (REQUEST_DATA_SIZE.equals(mRequest)) {
//                                mListener.onUpdateRecordFileSize(message);
//                                mRequest = INVALID;
//                            } else if (REQUEST_QUERY_FILE_NAME_LIST.equals(mRequest)) {
//                                mListener.onUpdateQueryTxtFileList(Arrays.asList(message.split("\n")));
//                                mRequest = INVALID;
//                            }else {
//                                mListener.onShowMessage(message);
//                            }
//                        }
//
//                    }
//
//                    @Override
//                    public void onMessage(ByteBuffer bytes) {
//                        super.onMessage(bytes);
//                        Log.d(TAG, ">>>>>>>>onMessage " + bytes.array().length + "B");
//                        onAcceptRecData(bytes);
//                    }
//
//                    @Override
//                    public void onClose(int code, String reason, boolean remote) {
//                        Log.d(TAG, ">>>>>>>>onClose " + reason);
//                        mListener.onShowMessage("onClose");
//                        mRequest = INVALID;
//                    }
//
//                    @Override
//                    public void onError(Exception ex) {
//                        Log.d(TAG, ">>>>>>>>onError " + ex);
//                        mRequest = INVALID;
//                        mListener.onShowMessage(ActivityPresenterImpl.KEY_WORD_ERROR + " " + ex);
//                    }
//                };
//                mWebSocket.connect();
//            }
//        });

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
            mListener.onRecordSuccess();
            return;
        }

        if (REQUEST_STOP.equals(mRequest)) {
            Log.d(TAG, ">>>>>>>>> onAcceptRecData " + mDspFileName);
            File file = new File(mDspFileName);
            if (file.exists()) file.delete();
            FileOperation.writeFile(mDspFileName, byteBuffer);
            mListener.onRecordSuccess();
            mRequest = INVALID;
        } else if (REQUEST_QUERY_LIST.equals(mRequest)) {
            String QueryListFile = ActivityPresenterImpl.RECORD_SHORT_DIRECTORY + "/QueryList.txt";
            File file = new File(QueryListFile);
            if (file.exists()) file.delete();
            FileOperation.writeFile(QueryListFile, byteBuffer);
            mListener.onQueryListLoadFinished(FileOperation.readQueryFromFile(file));
            file.delete();
            mRequest = INVALID;
        } else {

        }

    }


    /**
     * 发送数据
     *
     * @param data 需要发送的内容
     */
    public void send(final String data) {
        Log.d(TAG, ">>>>>>>>> send " + data);
        try {
            switch (data) {
                case REQUEST_STOP:
                    mRequest = REQUEST_STOP;
                    break;
                case REQUEST_START:
                    mRequest = REQUEST_START;
                    break;
                case REQUEST_DATA_SIZE:
                    if (!TextUtils.equals(mRequest, REQUEST_STOP)) {
                        mRequest = REQUEST_DATA_SIZE;
                        break;
                    } else {
                        return;
                    }
                case REQUEST_QUERY_FILE_NAME_LIST:
                    mRequest = REQUEST_QUERY_FILE_NAME_LIST;
                    break;
                case REQUEST_MOVE_DATA:
                    mRequest = REQUEST_MOVE_DATA;
                    break;
                case REQUEST_LONG_RECORD:
                    mRequest = REQUEST_LONG_RECORD;
                    break;
                case REQUEST_SHORT_RECORD:
                    mRequest = REQUEST_SHORT_RECORD;
                    break;
                case REQUEST_QUERY_LIST:
                    mRequest = REQUEST_QUERY_LIST;
                    break;
                default:
//                    if (data.contains("00_")) {
////                        mRequest = INVALID;
//                    } else {
//                        mRequest = REQUEST_QUERY_LIST;
//                    }

            }

            mWebSocket.send(data);
        } catch (Exception e) {
            mListener.onShowMessage(ActivityPresenterImpl.KEY_WORD_ERROR + " " + e);
            mRequest = INVALID;
        }
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

    public void onConnect(final String strUrl) {
        if (mIsSocketClose) {
            mWebSocket = new ClientSocket(URI.create(strUrl));
            mWebSocket.connect();
        }
    }

    private class ClientSocket extends WebSocketClient {

        public ClientSocket(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d(TAG, ">>>>>>>>onOpen ");
            File file = new File(ActivityPresenterImpl.RECORD_SHORT_DIRECTORY);
            if (!file.exists()) {
                file.mkdirs();
            }
            mListener.onConnected();
            mIsSocketClose = false;
            if (mRequest.equals(REQUEST_START) || mRequest.equals(REQUEST_STOP)) {
                mWebSocket.send(mRequest);
            } else if (!mRequest.equals(INVALID)) {
                mListener.onRecordSuccess();
                mListener.onShowMessage(ActivityPresenterImpl.MSG_RETRY);
            }
        }

        @Override
        public void onMessage(String message) {
            Log.d(TAG, ">>>>>>>>onMessage " + message);
            if (message.contains(ActivityPresenterImpl.KEY_WORD_UNMOUNT) || message.contains(ActivityPresenterImpl.KEY_WORD_MOUNT)) {
                mListener.onShowMessage(message);
            } else if (message.contains("Error")) {
                mListener.onShowMessage(ActivityPresenterImpl.KEY_WORD_ERROR + "SystemError: " + message);
                mRequest = INVALID;
            } else {
                if (REQUEST_DATA_SIZE.equals(mRequest)) {
                    mListener.onUpdateRecordFileSize(message);
                    mRequest = INVALID;
                } else if (REQUEST_QUERY_FILE_NAME_LIST.equals(mRequest)) {
                    mListener.onUpdateQueryTxtFileList(Arrays.asList(message.split("\n")));
                    mRequest = INVALID;
                } else {
                    mListener.onShowMessage(message);
                }
            }

        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            super.onMessage(bytes);
            Log.d(TAG, ">>>>>>>>onMessage " + bytes.array().length + "B");
            onAcceptRecData(bytes);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.d(TAG, ">>>>>>>>onClose " + reason);
            mIsSocketClose = true;
            mListener.reConnect();

        }

        @Override
        public void onError(Exception ex) {
            Log.d(TAG, ">>>>>>>>onError " + ex);
//            mListener.onShowMessage(ActivityPresenterImpl.KEY_WORD_ERROR + " " + ex);
        }
    }

}

