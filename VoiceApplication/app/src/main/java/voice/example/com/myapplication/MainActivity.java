package voice.example.com.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import voice.example.com.myapplication.ActivityContract.IActivityPresenter;
import voice.example.com.myapplication.ActivityContract.IActivityView;
import voice.example.com.myapplication.model.RecordItem;
import voice.example.com.myapplication.model.TcpClient;
import voice.example.com.myapplication.recordwave.VoiceMicView;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GRAY;

public class MainActivity extends AppCompatActivity implements IActivityView, View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 11;


    private static final String IP = "192.168.2.1";

    private static final String MALE = "M";
    private static final String FEMALE = "F";
    private static final String AGE_16_TO_30 = "16_30";
    private static final String AGE_31_TO_40 = "31_40";
    private static final String AGE_41_TO_50 = "41_50";


    private Button mBtnPlay;
    private Button mBtnNext;
    private Button mBtnReset;
    private TextView mTitle;
    private TextView mTextFileSize;
    private Button mBtnCh;
    private VoiceMicView mMicView;
    private TextView mNextRecoderText;
    private TextView mCurRecoderText;
    private Chronometer mRecoderTime;
    private Button mBtnStart;
    private Button mPlayStop;
    private IActivityPresenter mPresenter;
    private ConstraintLayout mRecorOperLayout;
    private ConstraintLayout mshortRecordLayout;
    private ConstraintLayout mLongRecordLayout;
    private RadioGroup mAgeRadioGrp;
    private RadioGroup mGenderRadioGrp;
    private Button mBtnSelectQueryTxt;

    private PopupWindow mPopupWindow;
    private WheelListDialog mSelectQueryTxtDialog;
    List<String> mQueryTxtFileNameList = new ArrayList<>();
    private boolean mIsShowLoading = false;

    private AlertDialog mMsgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
//
        mBtnNext = findViewById(R.id.btnNext);
        mBtnPlay = findViewById(R.id.btnPlay);
        mBtnReset = findViewById(R.id.btnRemake);
        mBtnStart = findViewById(R.id.btnStart);
        mMicView = findViewById(R.id.micView);
        mNextRecoderText = findViewById(R.id.textNextRecoder);
        mCurRecoderText = findViewById(R.id.curRecoder);
        mRecoderTime = findViewById(R.id.record_time);
        mRecorOperLayout = findViewById(R.id.recorderOperLayout);
        mPlayStop = findViewById(R.id.Stop);
        mTitle = findViewById(R.id.txtType);
        mBtnCh = findViewById(R.id.btnChMode);
        mshortRecordLayout = findViewById(R.id.shortRecorderLayout);
        mAgeRadioGrp = findViewById(R.id.ageRadioGrp);
        mGenderRadioGrp = findViewById(R.id.genderRadioGrp);
        mLongRecordLayout = findViewById(R.id.longRecordLayout);
        mTextFileSize = findViewById(R.id.textFileSize);
        mBtnSelectQueryTxt = findViewById(R.id.btnSelectQueryTxt);

        mBtnNext.setOnClickListener(this);
        mBtnReset.setOnClickListener(this);
        mBtnPlay.setOnClickListener(this);
        mBtnStart.setOnClickListener(this);
        mGenderRadioGrp.setOnCheckedChangeListener(this);
        mAgeRadioGrp.setOnCheckedChangeListener(this);

        mPresenter = new ActivityPresenterImpl(this);
        mPresenter.start();
        checkRecordPermission();
        mSelectQueryTxtDialog = new WheelListDialog(this, mQueryTxtFileNameList);
        mSelectQueryTxtDialog.setListener(this);

        mBtnStart.setSoundEffectsEnabled(false);
        mBtnNext.setSoundEffectsEnabled(false);
        mPlayStop.setSoundEffectsEnabled(false);
        mBtnReset.setSoundEffectsEnabled(false);
        findViewById(R.id.Stop).setSoundEffectsEnabled(false);
        mBtnPlay.setSoundEffectsEnabled(false);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPlay:
                mPresenter.playOrStopCurRecorder();
                mRecorOperLayout.setVisibility(View.GONE);
                mPlayStop.setVisibility(View.VISIBLE);
                mRecoderTime.setVisibility(View.GONE);
                break;
            case R.id.btnNext:
                mPresenter.next();
                break;
            case R.id.btnRemake:
                mPresenter.reset();
                break;
            case R.id.btnStart:
                String string = mBtnStart.getText().toString();
                if (string.contains("刷新")) {
                    mPresenter.acquireQueryList();
                } else if (string.contains("开始")){
                    ((ActivityPresenterImpl)mPresenter).startSoundRecord();
                    mTextFileSize.setText("");
                } else if (string.contains("完成")) {
                    ((ActivityPresenterImpl)mPresenter).stopSoundRecord();
                    mTextFileSize.setText("");
                }
                break;
            case R.id.back:
                finish();
                break;
            case R.id.Stop:
                mPresenter.playOrStopCurRecorder();
                break;
            case R.id.btnChMode:
                String type = mBtnCh.getText().toString();
                if (type.contains("短")) {
                    mPresenter.changeMode(TcpClient.REQUEST_SHORT_RECORD);
                } else {
                    mPresenter.changeMode(TcpClient.REQUEST_LONG_RECORD);
                }
                break;
            case R.id.btnMoveData:
                mPresenter.moveDataToUSB();
                showPopupWindow(v);
                break;
            case R.id.btnShowRecordFileSize:
                mPresenter.requestShowRecordFileSize();
                break;
            case R.id.btnSelectQueryTxt:
                if (mQueryTxtFileNameList.isEmpty()) {
                    mPresenter.acquireQueryFileNameList();
                } else {
                    if (mQueryTxtFileNameList.size() < 3) {
                        int textColor[] = {BLACK, GRAY};
                        int textSize[] = {15, 13};
                        float textAlpha[] = {1.0f, 0.8f};
                        mSelectQueryTxtDialog.setShowNum(1);
                        mSelectQueryTxtDialog.setTextAlpha(textAlpha);
                        mSelectQueryTxtDialog.setTextColor(textColor);
                        mSelectQueryTxtDialog.setTextSize(textSize);
                    }
                    mSelectQueryTxtDialog.setCanceledOnTouchOutside(false);
                    if (!"选择录音文本".equals(mBtnSelectQueryTxt.getText())) {
                        showShortRecordInitInfoDialog();
                    } else {
                        mSelectQueryTxtDialog.show();
                        initQueryItemText();
                        initRadioGrpCheck();
                        ((ActivityPresenterImpl) mPresenter).initShortRecordFileNameInfo();

                    }
                }
                break;
        }
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getCheckedRadioButtonId()) {
            case R.id.radioButton_16_to_30:
                mPresenter.updateAgeGroup(AGE_16_TO_30);
                break;
            case R.id.radioButton_31_to_40:
                mPresenter.updateAgeGroup(AGE_31_TO_40);
                break;
            case R.id.radioButton_41_to_50:
                mPresenter.updateAgeGroup(AGE_41_TO_50);
                break;
            case R.id.radioButton_male:
                mPresenter.updateGender(MALE);
                break;
            case R.id.radioButton_female:
                mPresenter.updateGender(FEMALE);
                break;
        }
    }

    public void disableRadioGroup(RadioGroup radioGroup) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(false);
        }
    }

    public void enableRadioGroup(RadioGroup radioGroup) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(true);
        }
    }

    @Override
    public void setSpeakIcon(int type) {
        mRecorOperLayout.setVisibility(View.GONE);
        mBtnStart.setVisibility(View.VISIBLE);
        if (type != ActivityContract.SpeakType.START) {
            disableRadioGroup(mAgeRadioGrp);
            disableRadioGroup(mGenderRadioGrp);
            mBtnSelectQueryTxt.setEnabled(false);
            setChangBtnEnable(false);
        }
        if (type == ActivityContract.SpeakType.START) {
            mBtnStart.setText(R.string.record_start);
            setChangBtnEnable(true);
        } else if (type == ActivityContract.SpeakType.STOP) {
            mBtnStart.setText(R.string.record_finish);
        } else {
            mBtnStart.setText(R.string.record_refresh);
        }
    }

    private void initQueryItemText() {
        mCurRecoderText.setText("");
        mNextRecoderText.setText("");
    }

    private void showShortRecordInitInfoDialog() {
        TextView msg = new TextView(this);
        msg.setText("当前录音没有完成 是否重新选择QueryList文件进行录音!");
        msg.setPadding(10, 10, 10, 10);
        msg.setGravity(Gravity.CENTER);
        msg.setTextSize(20);
        msg.setTextColor(getResources().getColor(R.color.black));
        new AlertDialog.Builder(this)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectQueryTxtDialog.show();
                        ((ActivityPresenterImpl) mPresenter).stopSoundRecord();
                        initRadioGrpCheck();
                        ((ActivityPresenterImpl) mPresenter).initShortRecordFileNameInfo();
                        initQueryItemText();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setCancelable(false)
                .setView(msg)
                .show();
    }

    @Override
    public void onShowDialog(String message) {
        TextView msg = new TextView(this);
        msg.setText(message);
        msg.setPadding(10, 10, 10, 10);
        msg.setGravity(Gravity.CENTER);
        msg.setTextSize(20);
        msg.setTextColor(getResources().getColor(R.color.black));
        if (mMsgDialog != null && mMsgDialog.isShowing()) {
            mMsgDialog.dismiss();
        }
        mMsgDialog = new AlertDialog.Builder(this)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setCancelable(false)
                .setView(msg)
                .show();
    }

    @Override
    public void onShowRecordFileSize(String fileSize) {
        mTextFileSize.setText(fileSize);
    }

    @Override
    public void onUpdateMicWave() {
        int[] random = {0, 60, 8};
        int randomNum = (int) (Math.random() * random.length - 1);
        mMicView.updateVolume(random[randomNum]);
    }

    @Override
    public void onShowStopView() {
        mMicView.end();
        mRecoderTime.setBase(SystemClock.elapsedRealtime());//计时器清零
        mRecoderTime.stop();
        mBtnStart.setVisibility(View.GONE);
        mRecorOperLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void onShowStartView() {
        mMicView.start();
        mRecoderTime.setBase(SystemClock.elapsedRealtime());//计时器清零
        int hour = (int) ((SystemClock.elapsedRealtime() - mRecoderTime.getBase()) / 1000 / 60);
        mRecoderTime.setFormat("0" + String.valueOf(hour) + ":%s");
        mRecoderTime.start();
    }

    @Override
    public void onPlayFinishedView() {
        mPlayStop.setVisibility(View.GONE);
        mRecorOperLayout.setVisibility(View.VISIBLE);
        mRecoderTime.setVisibility(View.VISIBLE);
    }

    @Override
    public void onShowText(RecordItem curItem, RecordItem nextItem) {
        mNextRecoderText.setVisibility(View.GONE);
        if (nextItem != null) {
            mNextRecoderText.setText("下一条：" + nextItem.getFileName() + "." + nextItem.getQueryText());
        } else {
            mNextRecoderText.setText("");
        }
        if (curItem != null) {
            mCurRecoderText.setText(curItem.getFileName() + "." + curItem.getQueryText());
        } else {
            mCurRecoderText.setText(" ");
        }

    }

    @Override
    public void onChangeLayout(String type) {
        if (TcpClient.REQUEST_SHORT_RECORD.equals(type)) {
            mshortRecordLayout.setVisibility(View.VISIBLE);
            mLongRecordLayout.setVisibility(View.GONE);
//            mNextRecoderText.setVisibility(View.VISIBLE);
            mCurRecoderText.setVisibility(View.VISIBLE);
            mTitle.setText(R.string.short_record_text);
            mBtnCh.setText(R.string.change_to_long_record_text);
            initSelectQueryTxt();
            initQueryItemText();
            initRadioGrpCheck();
        } else {
            initRadioGrpCheck();
            initSelectQueryTxt();
            mshortRecordLayout.setVisibility(View.GONE);
            mLongRecordLayout.setVisibility(View.VISIBLE);
            mNextRecoderText.setText("");
            mCurRecoderText.setText("");
            mNextRecoderText.setVisibility(View.GONE);
            mCurRecoderText.setVisibility(View.GONE);
            mTitle.setText(R.string.long_record_text);
            mBtnCh.setText(R.string.change_to_short_record_text);
            mQueryTxtFileNameList.clear();
        }
        setSpeakIcon(ActivityContract.SpeakType.START);
    }

    @Subscribe
    public void showCurQueryListFileName(String name) {
        mBtnSelectQueryTxt.setText("已选择录音文本： " + name);
        mQueryTxtFileNameList.remove(name);
        ((ActivityPresenterImpl) mPresenter).updateQueryListFileName(name);
        mPresenter.acquireQueryList();
    }

    @Override
    public void onShowQueryTxtFileList(List<String> fileNameList) {
        mQueryTxtFileNameList.addAll(fileNameList);
        mSelectQueryTxtDialog.setCanceledOnTouchOutside(false);
        mSelectQueryTxtDialog.show();
    }

    @Override
    public void onShowMovingToUSB() {
        View view = getWindow().getDecorView();
        showPopupWindow(view);
    }

    @Override
    public void onMovingToUSBFinished() {
        Log.d("RecorderVoice", ">>>>>>onMovingToUSBFinished");
        if (mPopupWindow != null) {
            Log.d("RecorderVoice", ">>>>>>dismiss");
            mPopupWindow.dismiss();
        }
        mTextFileSize.setText("");
    }

    @Override
    public void onShowCurText(boolean isShow) {
        if (isShow) {
            mCurRecoderText.setVisibility(View.VISIBLE);
            String string = mBtnStart.getText().toString();
            if (string.contains("完成")) {
                mBtnStart.setEnabled(true);
            }
        }else {
            mCurRecoderText.setVisibility(View.INVISIBLE);
            String string = mBtnStart.getText().toString();
            if (string.contains("完成")) {
                mBtnStart.setEnabled(false);
            }
        }
    }

    public void initRadioGrpCheck() {
        mAgeRadioGrp.clearCheck();
        mGenderRadioGrp.clearCheck();
        enableRadioGroup(mAgeRadioGrp);
        enableRadioGroup(mGenderRadioGrp);

    }
    public void initSelectQueryTxt() {
        mBtnSelectQueryTxt.setText("选择录音文本");
        mBtnSelectQueryTxt.setEnabled(true);
        setChangBtnEnable(true);
    }

    public void setChangBtnEnable(boolean enable) {
        if (enable) {
            mBtnCh.setClickable(true);
            mBtnCh.setAlpha(1f);
        }else {
            mBtnCh.setClickable(false);
            mBtnCh.setAlpha(0.20f);
        }
    }

    /**
     * 申请权限
     */
    private void checkRecordPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            } else {
                //startSoundRecord();
            }
        } else {
            //startSoundRecord();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // startSoundRecord();
                } else {
                    Toast.makeText(this, "请在设置中打开权限后继续", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 200);
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showPopupWindow(View view) {

        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.pop_window, null);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(contentView,
                    dm.widthPixels * 3 / 4, dm.heightPixels * 3 / 4, true);
        }

        mPopupWindow.setTouchable(true);

        mPopupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mPopupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.age_select_bg));
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            return;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                mPopupWindow.update();
            } else {
                if (mPopupWindow != null) {
                    mPopupWindow.showAsDropDown(view, 0, 0);
                    mPopupWindow.update();
                }
            }
        }

    }
}
