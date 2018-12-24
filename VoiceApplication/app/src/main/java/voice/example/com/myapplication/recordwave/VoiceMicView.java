package voice.example.com.myapplication.recordwave;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import voice.example.com.myapplication.recordwave.iview.IMicView;


public class VoiceMicView extends MicView implements View.OnClickListener, IMicView {
    private static final String TAG = "VoiceMicView";

    private boolean mIsStarted = false;
    private Paint mBgPaint;

    private static final int MSG_START = 1;
    private static final int MSG_LOADING = 2;
    private static final int MSG_END = 3;
    private static final int MSG_ERROR = 4;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_START: {
                    handler.removeMessages(MSG_START);
                    doStart();
                }
                break;
                case MSG_LOADING: {
                    handler.removeMessages(MSG_LOADING);
                    doLoading();
                }
                break;
                case MSG_END: {
                    handler.removeMessages(MSG_END);
                    doEnd();
                }
                break;
                case MSG_ERROR: {
                    handler.removeMessages(MSG_END);
                    retry();
                }
                break;
                default:
                    break;
            }
        }

    };

    public void doStart() {
        super.start();
    }

    public void doLoading() {
        super.loading();
    }

    public void doEnd() {
        super.pause();
    }

    public VoiceMicView(Context context) {
        this(context, null, 0);
    }

    public VoiceMicView(Context context, AttributeSet paramAttributeSet) {
        this(context, paramAttributeSet, 0);
    }

    // 初始化一些 final 变量
    public VoiceMicView(Context context, AttributeSet paramAttributeSet, int defStyle) {
        super(context, paramAttributeSet, defStyle);
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(Color.BLACK);
    }

    private UserOnClickListener userActionListener;

    @Override
    public void start() {
        //Log.d(TAG, "start - mIsStarted : %b", mIsStarted);
        if (mIsStarted) {
            return;
        }
        mIsStarted = true;

        handler.removeMessages(MSG_START);
        handler.sendEmptyMessage(MSG_START);
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop");
        mIsStarted = false;
        handler.removeMessages(MSG_LOADING);
        handler.sendEmptyMessage(MSG_LOADING);
    }

    @Override
    public void loading() {
        Log.d(TAG, "loading");
        handler.removeMessages(MSG_LOADING);
        handler.sendEmptyMessage(MSG_LOADING);
    }

    @Override
    public void updateVolume(double volume) {
        super.updateVolume((float) volume);
    }


    @Override
    public void success() {
        mIsStarted = false;
        end();
        Log.d(TAG, "success");

    }

    @Override
    public void unknown() {
        mIsStarted = false;
        end();
        Log.d(TAG, "unknown");

    }

    @Override
    public void error() {
        Log.d(TAG, "error");
        mIsStarted = false;
        end();
    }



    @Override
    public void end() {
        mIsStarted = false;
        handler.removeMessages(MSG_END);
        handler.sendEmptyMessage(MSG_END);
    }


    @Override
    public void cancel() {
        end();
        Log.d(TAG, "cancel");

    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick");

        if (userActionListener == null) {
            return;
        }
        super.onClick(view);
    }


}
