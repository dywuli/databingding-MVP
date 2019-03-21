package voice.example.com.myapplication;

import android.app.Service;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import voice.example.com.myapplication.model.RecordItem;
import voice.example.com.myapplication.model.RecordUtil;
import voice.example.com.myapplication.model.TcpClient;

/**
 * Created by liwu on 18-11-21.
 */

public class ActivityPresenterImpl implements ActivityContract.IActivityPresenter, TcpClient.ConnectListener {
    private ActivityContract.IActivityView mActivityView;

    private TcpClient mTcpClient;
    private static final String URL = "ws://192.168.2.1:55555";
    private static final String TAG = "RecorderVoice";

    private AudioManager mAudioManager;
    private FocusChangeListener mFocusChangeListener;

    private static final int MSG_CONNECT_STATE = 1;
    private static final int MSG_ERROR = 2;
    private final int UPDATE_MICWAVE = 3;//更新动画
    private final int UPDATE_PLAYSTATE = 4;//更新播放状态
    private final int AUTO_CLOSE = 5;//长录音空间5%，五秒后自动关闭

    public final static String ERROR_MSG_NO_QUERY_TXT_FILE_NAME_LIST = "录音文本文件列表为空,请检查U盘录音文本文件是否存在,\n然后点击 \"开始\"";
    private final static String ERROR_MSG_NO_QUERY_LIST = "请检查U盘录音文件是否存在,\n 然后点击 刷新 ";
    private final static String ERROR_MSG_NO_RECORD_FILE = "录音文件不存在, 请点击 重录 录音";
    private final static String ERROR_MSG_NO_AGE = "请选择年龄范围,\n然后点击 \"开始\"";
    private final static String ERROR_MSG_NO_GENDER = "请选择性别,\n然后点击 \"开始\"";
    private final static String ERROR_MSG_NO_FILE = "请点击 \"选择录音文本\" 选取相应的QueryList文本文件,\n然后点击 \"开始\"";
    private final static String MSG_CHANGE_AFTER_CLICK_FINISHED = "请点击 完成 然后切换";
    private final static String MSG_RECORDING_CLICK_FINISHED = "请点击 \n完成 \n或者5秒后自动关闭录音";
    private final static String MSG_RECORDING_FINISHED = "录音完成！\n可选择其他录音文本 \n进行录音操作";
    public final static String ERROR_MSG_NO_USB = "当前没有U盘, 请插入U盘";
    public final static String MSG_USB_MOUNTED = "U盘已挂载完成";
    public final static String MSG_USB_UNMOUNTED = "U盘已经拔出";
    public final static String MSG_RETRY = "抱歉, 由于socket 异常, 录音操作失败, \n请重试!";

    public final static String KEY_WORD_ERROR = "异常";
    public final static String KEY_WORD_CLOSE = "onClose";
    public final static String KEY_WORD_MOUNT = "Mount";
    public final static String KEY_WORD_UNMOUNT = "UnMount";
    public final static String KEY_WORD_FILE_SIZE = "FileSize";
    public final static String KEY_WORD_DISK_FULL = "DiskIsFull";
    public final static String KEY_WORD_MOVING_TO_USB = "MvAudioToUDisk";
    public final static String KEY_WORD_MOVING_TO_USB_FINISHED = "MvAudioFileSuccess";

    private final int DELAY_TIME = 30 * 1000;//每段录音秒时间间隔
    private final int USB_CHECK_DELAY_TIME = 2 * 1000;//USB check

    private final int DELAY_CLOSE_TIME = 5 * 1000;//每段录音秒时间间隔

    private static final int SPACE = 200;// 间隔取样时间

    private static final int RE_CONNECTED_DELAY = 200;
    private static final int RETRY_COUNT = 5;

    private final ExecutorService mExecutor = Executors.newFixedThreadPool(3);

    private final static String SAVE_FILE_NAME = "TempRecordData";

    public static final String RECORD_SHORT_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "RecordTool" + File.separator + "shortRecord";

    private volatile boolean mRecordPlayStopped = true;
    private boolean mIsRecordStop = true;
    private boolean mIsRecordSuccess = false;
    private int mRetryCount = 0;


    private String mCurQueryListFileName = "";
    private String mCurGender = "";
    private String mCurAgeGroup = "";

    private List<RecordItem> mRecordAllItemList = new ArrayList<>();

    private RecordItem mCurRecordItem;
    private RecordItem mNextRecordItem;
    private String mCurType = TcpClient.REQUEST_SHORT_RECORD;

    private DataInputStream mStream;
    private AudioTrack mAudioTrack;

    private RecordUtil mRecordUtil;
    private File mCurRecordFile;
    private Context mContext;

    private boolean mIsCheckUsbStoped = false;

    public ActivityPresenterImpl(ActivityContract.IActivityView View) {
        this.mActivityView = View;
        mContext = (MainActivity) mActivityView;
        mTcpClient = TcpClient.getInstance();
        mTcpClient.setOnConnectListener(this);
    }

    @Override
    public void start() {
        Log.d(TAG, ">>>>>>>start");
        mTcpClient.onCreateConnect(URL);
        mAudioManager = (AudioManager) mContext.getSystemService(Service.AUDIO_SERVICE);
//        changeMode(TcpClient.REQUEST_SHORT_RECORD);
    }

    ///socket client 回调
    @Override
    public void onShowMessage(final String data) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (data.contains(KEY_WORD_ERROR)) {
                    postErrorMsg(data + "\n" + "请检查防火墙是否关闭,然后退出程序,重新进入录音应用 或者 重启车机！");
                    mIsRecordStop = true;
                } else {
                    if (mCurType.equals(TcpClient.REQUEST_LONG_RECORD)) {
                        Log.d(TAG, ">>>>>>" + data);
                        if (data.contains(KEY_WORD_MOVING_TO_USB)) {
                            mActivityView.onShowMovingToUSB();
                        } else if (data.contains(KEY_WORD_MOVING_TO_USB_FINISHED)) {
                            mActivityView.onMovingToUSBFinished();
                            onLongRecordFinished();

                        } else if (data.contains(KEY_WORD_DISK_FULL)) {
                            mActivityView.onShowDialog(MSG_RECORDING_CLICK_FINISHED);
                            mHandler.sendEmptyMessageDelayed(AUTO_CLOSE, DELAY_CLOSE_TIME);

                        } else if (data.contains(KEY_WORD_CLOSE)) {
                            mActivityView.onMovingToUSBFinished();
                            onLongRecordFinished();
                        } else if (data.contains(KEY_WORD_UNMOUNT)) {
                            if (!mIsRecordStop) {
                                mActivityView.onShowDialog(MSG_USB_UNMOUNTED + ", 请插入U盘, 以免造成长录音文件丢失！");
                            } else {
                                mActivityView.onShowDialog(MSG_USB_UNMOUNTED + ", 请插入U盘, 然后点击 \"开始\"！");
                            }

                        } else if (data.contains(KEY_WORD_MOUNT)) {
                            if (!mIsRecordStop) {
                                mActivityView.onShowDialog(MSG_USB_MOUNTED);
                            } else {
                                mActivityView.onShowDialog(MSG_USB_MOUNTED + ", 请点击 \"开始\" 进行录音操作！");
                            }

                        } else {
                            postErrorMsg(data);
                        }
                    } else {
                        if (data.contains(KEY_WORD_CLOSE)) {
                            onRecordSuccess();
                        } else if (data.contains(KEY_WORD_UNMOUNT)) {
                            mActivityView.onShowDialog(MSG_USB_UNMOUNTED + ", 请插入U盘, 然后点击 \"开始\" 或者 \"重录\"！");
                        } else if (data.contains(KEY_WORD_MOUNT)) {

                            mActivityView.onShowDialog(MSG_USB_MOUNTED + ", 请点击 \"开始\" 或者 \"重录\"！");
                        }
                    }
                }
            }
        });

    }

    //socket client 回调
    @Override
    public void onRecordSuccess() {
        mIsRecordStop = true;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (TcpClient.REQUEST_SHORT_RECORD.equals(mCurType)) {
                    mActivityView.onShowStopView();
                    if (mCurRecordItem != null && mNextRecordItem == null) {
                        mActivityView.onShowDialog(MSG_RECORDING_FINISHED);
                        ((MainActivity) mActivityView).initSelectQueryTxt();
                    }
                } else {
                    mActivityView.onShowStopView();
                    mActivityView.setSpeakIcon(ActivityContract.SpeakType.START);
                    ((MainActivity) mActivityView).setChangBtnEnable(true);
                }
            }
        });

    }

    private void onLongRecordFinished() {
        mIsRecordStop = true;
        mActivityView.onShowStopView();
        mActivityView.setSpeakIcon(ActivityContract.SpeakType.START);
        ((MainActivity) mActivityView).setChangBtnEnable(true);
    }

    //socket client 回调
    @Override
    public void onQueryListLoadFinished(List<RecordItem> itemList) {
        mRecordAllItemList.clear();
        mRecordAllItemList.addAll(itemList);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mRecordAllItemList.isEmpty()) {
                    mActivityView.onShowDialog(ERROR_MSG_NO_QUERY_LIST);
                    mActivityView.setSpeakIcon(ActivityContract.SpeakType.REFRESH);
                    return;
                }
                mNextRecordItem = mRecordAllItemList.get(1);
                mCurRecordItem = mRecordAllItemList.get(0);
                mActivityView.setSpeakIcon(ActivityContract.SpeakType.START);
                mActivityView.onShowText(mCurRecordItem, mNextRecordItem);
            }
        });
    }

    //socket client 回调
    @Override
    public void onUpdateQueryTxtFileList(final List<String> fileNameList) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (fileNameList.isEmpty()) {
                    mActivityView.onShowDialog(ERROR_MSG_NO_QUERY_TXT_FILE_NAME_LIST);
                } else {
                    mActivityView.onShowQueryTxtFileList(fileNameList);
                }

            }
        });
    }

    //socket client 回调
    @Override
    public void onUpdateRecordFileSize(final String data) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (data.contains(KEY_WORD_FILE_SIZE)) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String[] strings = data.split(":");
                            if (strings[1].matches("\\d+")) {
                                long size = Long.parseLong(strings[1]);
                                if (size > 1024) {
                                    size = size >> 10;//KB
                                    if (size > 1024) {
                                        size = size >> 10; //MB
                                        strings[1] = ":" + size + "MB";
                                    } else {
                                        strings[1] = ":" + size + "KB";
                                    }
                                }
                                mActivityView.onShowRecordFileSize(strings[0] + strings[1]);
                            }
                        }
                    });
                }
            }
        });
    }

    //socket client 回调
    @Override
    public void onMoveRecorderDataFinished() {

    }

    //socket connected
    @Override
    public void onConnected() {
        checkUsb();
    }

    //socket
    @Override
    public void reConnect() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mRetryCount < RETRY_COUNT) {
                    mTcpClient.onConnect(URL);
                    mRetryCount++;
                } else {
                    onShowMessage(KEY_WORD_ERROR + " : " + mRetryCount);
                    mActivityView.onChangeLayout(mCurType);
                }

            }
        }, RE_CONNECTED_DELAY);

    }

    @Override
    public void changeMode(String type) {
        if (mIsRecordStop) {
            initShortRecordFileNameInfo();
            mCurType = type;
            mTcpClient.send(type);
            mActivityView.onShowStopView();
            mActivityView.onChangeLayout(type);
        } else {
            mActivityView.onShowDialog(MSG_CHANGE_AFTER_CLICK_FINISHED);
        }

    }

    @Override
    public void playOrStopCurRecorder() {

        if (mRecordPlayStopped) {
            mRecordPlayStopped = false;
            recordPlayStart();
        } else {
            Log.d(TAG, ">>>>>>recordPlayingStop");
            recordPlayingStop();
            mActivityView.onPlayFinishedView();
        }

    }

    @Override
    public void reset() {

        if (mCurRecordFile != null && mCurRecordFile.exists()) {
            mCurRecordFile.delete();
        }
        speakOnOff();
    }

    @Override
    public void next() {
        if (null == mNextRecordItem) {
            mActivityView.onShowDialog(MSG_RECORDING_FINISHED);
            return;
        }
        mCurRecordItem = mNextRecordItem;
        int curIndex = mRecordAllItemList.indexOf(mCurRecordItem);
        if (++curIndex < mRecordAllItemList.size()) {
            mNextRecordItem = mRecordAllItemList.get(curIndex);
        } else {
            mNextRecordItem = null;
        }
        mActivityView.onShowText(mCurRecordItem, mNextRecordItem);
        speakOnOff();
    }

    @Override
    public void speakOnOff() {
        if (mIsRecordStop) {
            startSoundRecord();

        } else {
            stopSoundRecord();
        }
    }

    public void updateQueryListFileName(String fileName) {
        mCurQueryListFileName = fileName;
    }

    public void initShortRecordFileNameInfo() {
        mCurQueryListFileName = "";
        mCurGender = "";
        mCurAgeGroup = "";
    }

    @Override
    public void acquireQueryList() {
        mTcpClient.send(TcpClient.REQUEST_SHORT_RECORD);
        mTcpClient.send(TcpClient.REQUEST_QUERY_LIST);
        mTcpClient.send(mCurQueryListFileName);
    }

    @Override
    public void destroy() {
        mIsCheckUsbStoped = true;
        recordPlayingStop();
        mExecutor.shutdown();
        mTcpClient.disconnect();
    }

    @Override
    public void updateGender(String gender) {
        mCurGender = gender;
    }

    @Override
    public void updateAgeGroup(String ageGroup) {
        mCurAgeGroup = ageGroup;
    }

    @Override
    public void moveDataToUSB() {
        if (!mIsRecordStop) {
            stopSoundRecord();
        }
        mTcpClient.send(TcpClient.REQUEST_MOVE_DATA);
        onRecordSuccess();
    }

    @Override
    public void requestShowRecordFileSize() {

    }

    @Override
    public void acquireQueryFileNameList() {
        mCurType = TcpClient.REQUEST_SHORT_RECORD;
        mTcpClient.send(mCurType);
        mTcpClient.send(TcpClient.REQUEST_QUERY_FILE_NAME_LIST);

    }

    @Override
    public void disConnect() {
//        mTcpClient.disconnect();
    }

    private void postErrorMsg(String error) {
        Message msg = new Message();
        msg.what = MSG_ERROR;
        msg.obj = error;
        mHandler.sendMessage(msg);

    }

    private void updateSpeakIcon(int type) {
        Message message = new Message();
        message.what = MSG_CONNECT_STATE;
        message.obj = type;
        mHandler.sendMessage(message);
    }

    /**
     * 开始录音
     */
    public void startSoundRecord() {

        if (TcpClient.REQUEST_SHORT_RECORD.equals(mCurType)) {

            if (TextUtils.isEmpty(mCurGender)) {
                mActivityView.onShowDialog(ERROR_MSG_NO_GENDER);
                return;
            }

            if (TextUtils.isEmpty(mCurAgeGroup)) {
                mActivityView.onShowDialog(ERROR_MSG_NO_AGE);
                return;
            }

            if (TextUtils.isEmpty(mCurQueryListFileName)) {
                mActivityView.onShowDialog(ERROR_MSG_NO_FILE);
                return;
            }

            if (mRecordAllItemList.isEmpty()) {
                mActivityView.onShowDialog(ERROR_MSG_NO_QUERY_LIST);
                mActivityView.setSpeakIcon(ActivityContract.SpeakType.REFRESH);
                return;
            }
            String strFileName = mCurQueryListFileName.substring(0, mCurQueryListFileName.indexOf("."));
            Log.d(TAG, ">>>>>>>>>>>>" + strFileName);
            String fileName = "00_" + strFileName + "_" + mCurGender
                    + "_" + mCurAgeGroup + "_" + mCurRecordItem.getFileName();
            mTcpClient.send(TcpClient.REQUEST_SHORT_RECORD);
            mTcpClient.send(TcpClient.REQUEST_START);
            mTcpClient.send(fileName);
            mIsRecordStop = false;
            updateMicStatus();
            mActivityView.onShowStartView();
            mActivityView.setSpeakIcon(ActivityContract.SpeakType.STOP);
            mActivityView.onShowCurText(false);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mActivityView.onShowCurText(true);
                }
            }, 5 * 1000);

        } else {
            mTcpClient.send(TcpClient.REQUEST_LONG_RECORD);
            mTcpClient.send(TcpClient.REQUEST_START);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            String fileName = sdf.format(new Date());
            mIsRecordStop = false;
            updateMicStatus();
            mActivityView.onShowStartView();
//            mTcpClient.send(fileName);
            getFileSizeByTiming();
            mActivityView.setSpeakIcon(ActivityContract.SpeakType.STOP);
        }


    }

    /**
     * 停止录音
     */
    public void stopSoundRecord() {

        if (TextUtils.equals(mCurType, TcpClient.REQUEST_SHORT_RECORD)) {
            mIsRecordStop = true;
            String pathName = RECORD_SHORT_DIRECTORY + File.separator + SAVE_FILE_NAME + ".pcm";
            mCurRecordFile = new File(pathName);
            mTcpClient.setFilePath(RECORD_SHORT_DIRECTORY, SAVE_FILE_NAME);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTcpClient.send(TcpClient.REQUEST_STOP);
                }
            }, 5 * 100);
        } else {
//            mActivityView.onShowMovingToUSB();
            mTcpClient.send(TcpClient.REQUEST_STOP);
        }

    }


    /**
     * 开始播放
     */
    private void recordPlayStart() {

        if (mCurRecordFile == null || !mCurRecordFile.exists()) {
            mActivityView.onShowDialog(ERROR_MSG_NO_RECORD_FILE);
            mRecordPlayStopped = true;
            mActivityView.onPlayFinishedView();
            return;
        }

        if (null == mFocusChangeListener)
            mFocusChangeListener = new FocusChangeListener();
        int typ = mAudioManager.requestAudioFocus(mFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        Log.d(TAG, ">>>>>>>>>requestAudioFocus " + typ);
        if (typ == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            onPlay();
        }


    }

    private void onPlay() {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {

                int minBuffSize = AudioTrack.getMinBufferSize(16000,
                        AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
                final byte[] mBuffer = new byte[minBuffSize];
                mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,
                        AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minBuffSize,
                        AudioTrack.MODE_STREAM);
                mAudioTrack.play();
                try {
                    InputStream is = new FileInputStream(mCurRecordFile);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    mStream = new DataInputStream(bis);
                    while (!mRecordPlayStopped) {
                        if (mStream != null) {
                            long i;
                            if ((i = mStream.read(mBuffer, 0, mBuffer.length)) == -1) {
                                break;
                            } else {
                                mAudioTrack.write(mBuffer, 0, (int) i);
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.d(TAG, ">>>>>>>>>play " + e);
                    e.printStackTrace();
                } finally {
                    releaseStream();
//                    recordPlayingStop();
//                    mHandler.sendEmptyMessage(UPDATE_PLAYSTATE);
                }
            }
        });
    }

    private class FocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.d(TAG, ">>>>>>>>onAudioFocusChange " + focusChange);
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                mAudioManager.abandonAudioFocus(mFocusChangeListener);
                mFocusChangeListener = null;
//                mHandler.sendEmptyMessage(UPDATE_PLAYSTATE);
            }
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN && !mRecordPlayStopped) {
                onPlay();
            }
        }
    }

    /**
     * 停止播放
     */
    private void recordPlayingStop() {
        mRecordPlayStopped = true;
        if (null != mAudioTrack) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    /**
     * 更新动画
     */
    private void updateMicStatus() {
        if (!mIsRecordStop) {
            mHandler.postDelayed(mUpdateMicWaveTimer, SPACE);
            mHandler.sendEmptyMessage(UPDATE_MICWAVE);
        }

    }

    private Runnable mUpdateMicWaveTimer = new Runnable() {
        public void run() {
            updateMicStatus();
        }
    };

    private Runnable mGetFileSizeOnTimer = new Runnable() {
        @Override
        public void run() {
            if (!mIsRecordStop) {
                getFileSizeByTiming();
                mTcpClient.send(TcpClient.REQUEST_LONG_RECORD);
                mTcpClient.send(TcpClient.REQUEST_DATA_SIZE);
            }

        }
    };

    private void getFileSizeByTiming() {
        mHandler.postDelayed(mGetFileSizeOnTimer, DELAY_TIME);

    }

    private void checkUsb() {
        mHandler.postDelayed(mUsbCheckTiming, USB_CHECK_DELAY_TIME);

    }

    private Runnable mUsbCheckTiming = new Runnable() {
        public void run() {
            mTcpClient.send(TcpClient.REQUEST_USB_CHECK);
            checkUsb();
        }
    };


    /**
     * 释放资源
     */
    private void releaseStream() {
        if (mStream != null) {
            try {
                mStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mStream = null;
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ERROR:
                    mActivityView.onShowDialog((String) msg.obj);
                    if (TcpClient.INVALID.equals(mCurType)) {
                        return;
                    }
                    mActivityView.onShowStopView();
                    mActivityView.onChangeLayout(mCurType);

                    break;
                case MSG_CONNECT_STATE:
                    int state = (int) msg.obj;
                    mActivityView.setSpeakIcon(state);
                    break;
                case UPDATE_MICWAVE:
                    mActivityView.onUpdateMicWave();
                    break;
                case UPDATE_PLAYSTATE:
                    mActivityView.onPlayFinishedView();
                    break;
                case AUTO_CLOSE:
                    if (!mIsRecordStop) {
                        stopSoundRecord();
                    }
                    break;

            }
        }
    };

}
