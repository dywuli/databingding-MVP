package voice.example.com.myapplication;

import android.app.Service;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
    private final int UPDATE_MICWAVE =3;//更新动画
    private final int UPDATE_PLAYSTATE = 4;//更新播放状态

    private final int DELAY_TIME = 8*1000;//每段录音秒时间间隔

    private static final int SPACE = 200;// 间隔取样时间

    private final ExecutorService mExecutor = Executors.newFixedThreadPool(3);

    private final static String SAVE_FILE_NAME = "TempRecordData";

    public static final String RECORD_SHORT_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "RecordTool" + File.separator + "shortRecord";

    private volatile boolean mRecordPlayStopped = true;
    private boolean mIsRecordStop = true;


    private List<RecordItem> mRecordAllItemList = new ArrayList<>();

    private RecordItem mCurRecordItem;
    private RecordItem mNextRecordItem;
    private String mCurType = TcpClient.TYPE_SHORT;

    private DataInputStream mStream;
    private AudioTrack mAudioTrack;

    private RecordUtil mRecordUtil;
    private File mCurRecordFile;
    private Context mContext;

    public ActivityPresenterImpl(ActivityContract.IActivityView View) {
        this.mActivityView = View;
        mContext = (MainActivity)mActivityView;
        mTcpClient = TcpClient.getInstance();
        mTcpClient.setOnConnectListener(this);
    }

    @Override
    public void start() {
        Log.d(TAG, ">>>>>>>start");
        mTcpClient.onCreateConnect(URL);
        mAudioManager = (AudioManager)mContext.getSystemService(Service.AUDIO_SERVICE);

    }
    ///socket client 回调
    @Override
    public void onErrorData(String data) {
        postErrorMsg(data);
    }
    //socket client 回调
    @Override
    public void onConnected(boolean success) {
        if (success) {
            updateSpeakIcon(ActivityContract.SpeakType.START);
        }
    }
    //socket client 回调
    @Override
    public void onRecordSuccess(boolean isSuccess) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mActivityView.onShowStopView();
            }
        });

    }
    //socket client 回调
    @Override
    public void onQueryListLoadFinished(List<RecordItem> itemList) {

        mRecordAllItemList.addAll(itemList);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mRecordAllItemList.isEmpty()) {
                    Toast.makeText(mContext, "QueryList文件为空,请检查文件格式...", Toast.LENGTH_LONG).show();
                    return;
                }
                mCurRecordItem = mRecordAllItemList.get(0);
                mNextRecordItem = mRecordAllItemList.get(1);
                mActivityView.onShowText(mCurRecordItem, mNextRecordItem);
            }
        });
    }

    @Override
    public void changeMode(String type) {
        mActivityView.onShowStopView();
        mCurType = type;
        mTcpClient.send(type);
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
        startSpeakOnOff();
    }

    @Override
    public void next() {
        if (mCurRecordItem != null && mNextRecordItem == null) {
            Toast.makeText(mContext, "录音完成", Toast.LENGTH_LONG).show();
            mActivityView.onShowStopView();
            return;
        }
        mCurRecordItem = mNextRecordItem;
        int curIndex = mRecordAllItemList.indexOf(mCurRecordItem);
        if (++curIndex < mRecordAllItemList.size()) {
            mNextRecordItem = mRecordAllItemList.get(curIndex);
        } else {
            mNextRecordItem = null;
        }
        startSpeakOnOff();
    }

    @Override
    public void startSpeakOnOff() {
        if (mRecordAllItemList.isEmpty()) {
            Toast.makeText(mContext, "请添加QueryList文件", Toast.LENGTH_LONG).show();
            mActivityView.setSpeakIcon(ActivityContract.SpeakType.START);
            return;
        }
        if (mIsRecordStop) {
            startSoundRecord();
            mIsRecordStop = false;
            mActivityView.setSpeakIcon(ActivityContract.SpeakType.STOP);

        } else {
            stopSoundRecord();
            mIsRecordStop = true;
        }
    }

    @Override
    public void destroy() {
        recordPlayingStop();
        mExecutor.shutdown();
        mTcpClient.disconnect();
    }


    @Override
    public void disConnect() {
        mTcpClient.disconnect();
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
    private void startSoundRecord() {
        mTcpClient.send(TcpClient.START);
        mTcpClient.send(mCurRecordItem.getFileName());
        updateMicStatus();
        mActivityView.onShowText(mCurRecordItem, mNextRecordItem);
        mActivityView.onShowStartView();
    }

    /**
     * 停止录音
     */
    private void stopSoundRecord() {
        String pathName = RECORD_SHORT_DIRECTORY + File.separator + SAVE_FILE_NAME + ".pcm";
        mCurRecordFile = new File(pathName);
        mTcpClient.setFilePath(RECORD_SHORT_DIRECTORY, SAVE_FILE_NAME);
        mTcpClient.send(TcpClient.STOP);
    }


    /**
     * 开始播放
     */
    private void recordPlayStart() {

        if (mCurRecordFile == null || !mCurRecordFile.exists()) {
            Toast.makeText(mContext, "当前录音文件不存在", Toast.LENGTH_LONG).show();
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
    private  class FocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.d(TAG, ">>>>>>>>onAudioFocusChange " + focusChange);
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                mAudioManager.abandonAudioFocus(mFocusChangeListener);
                mFocusChangeListener = null;
//                mHandler.sendEmptyMessage(UPDATE_PLAYSTATE);
            }
            if(focusChange == AudioManager.AUDIOFOCUS_GAIN && !mRecordPlayStopped) {
                onPlay();
            }
        }
    };

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
        mHandler.postDelayed(mUpdateMicWaveTimer, SPACE);
        mHandler.sendEmptyMessage(UPDATE_MICWAVE);
    }
    private Runnable mUpdateMicWaveTimer = new Runnable() {
        public void run() {
            updateMicStatus();
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
                    mActivityView.onShowRecData((String) msg.obj);
                    break;
                case MSG_CONNECT_STATE:
                    int state = (int)msg.obj;
                    mActivityView.setSpeakIcon(state);
                    break;
                case UPDATE_MICWAVE:
                    mActivityView.onUpdateMicWave();
                    break;
                case UPDATE_PLAYSTATE:
                    mActivityView.onPlayFinishedView();
                    break;

            }
        }
    };

}
