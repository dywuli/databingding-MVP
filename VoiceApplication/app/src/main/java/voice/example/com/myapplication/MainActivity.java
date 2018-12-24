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
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import voice.example.com.myapplication.ActivityContract.IActivityPresenter;
import voice.example.com.myapplication.ActivityContract.IActivityView;
import voice.example.com.myapplication.model.RecordItem;
import voice.example.com.myapplication.model.TcpClient;
import voice.example.com.myapplication.recordwave.VoiceMicView;

public class MainActivity extends AppCompatActivity implements IActivityView, View.OnClickListener{
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 11;


    private static final String IP = "192.168.2.1";

    private Button mBtnPlay;
    private Button mBtnNext;
    private Button mBtnReset;
    private TextView mTitle;
    private Button mBtnCh;
    private VoiceMicView mMicView;
    private TextView mCurRecoderText;
    private TextView mNextRecoderText;
    private Chronometer mRecoderTime;
    private Button mBtnSpeakOnOff;
    private Button mPlayStop;
    private IActivityPresenter mPresenter;
    private ConstraintLayout mRecorLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
        mBtnNext = findViewById(R.id.btnNext);
        mBtnPlay = findViewById(R.id.btnPlay);
        mBtnReset = findViewById(R.id.btnRemake);
        mBtnSpeakOnOff = findViewById(R.id.btnSpeakOnOff);
        mMicView = findViewById(R.id.micView);
        mCurRecoderText = findViewById(R.id.curRecoder);
        mNextRecoderText = findViewById(R.id.textNextRecoder);
        mRecoderTime = findViewById(R.id.record_time);
        mRecorLayout = findViewById(R.id.layoutRecoder);
        mPlayStop = findViewById(R.id.Stop);
        mTitle = findViewById(R.id.txtType);
        mBtnCh = findViewById(R.id.btnChMode);

        mBtnNext.setOnClickListener(this);
        mBtnReset.setOnClickListener(this);
        mBtnPlay.setOnClickListener(this);
        mBtnSpeakOnOff.setOnClickListener(this);

        mPresenter = new ActivityPresenterImpl(this);
        mPresenter.start();
        checkRecordPermission();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPlay:
                mPresenter.playOrStopCurRecorder();
                mRecorLayout.setVisibility(View.GONE);
                mPlayStop.setVisibility(View.VISIBLE);
                break;
            case R.id.btnNext:
                mPresenter.next();
                break;
            case R.id.btnRemake:
                mPresenter.reset();
                break;
            case R.id.btnSpeakOnOff:
                mPresenter.startSpeakOnOff();
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
                    mPresenter.changeMode(TcpClient.TYPE_SHORT);
                } else {
                    mPresenter.changeMode(TcpClient.TYPE_LONG);
                }
                break;
        }
    }
    @Override
    public void onShowRecData(String data) {
        //Toast.makeText(this, "应用出现问题：" + data, Toast.LENGTH_LONG).show();

    }

    @Override
    public void setSpeakIcon(int type) {
        mRecorLayout.setVisibility(View.GONE);
        mBtnSpeakOnOff.setVisibility(View.VISIBLE);
        if (type == ActivityContract.SpeakType.START) {
            mBtnSpeakOnOff.setText("开始");
        } else {
            mBtnSpeakOnOff.setText("完成");
        }
    }

    @Override
    public void onShowNoUSBDialog() {
        new AlertDialog.Builder(this)
                .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .setMessage("请插入USB设备")
                .show();
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
        mBtnSpeakOnOff.setVisibility(View.GONE);
        mRecorLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void onShowStartView() {
        mMicView.start();
        mRecoderTime.setBase(SystemClock.elapsedRealtime());//计时器清零
        int hour = (int) ((SystemClock.elapsedRealtime() - mRecoderTime.getBase()) / 1000 / 60);
        mRecoderTime.setFormat("0"+String.valueOf(hour)+":%s");
        mRecoderTime.start();
    }

    @Override
    public void onPlayFinishedView() {
        mPlayStop.setVisibility(View.GONE);
        mRecorLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onShowText(RecordItem curItem, RecordItem nextItem) {
        mCurRecoderText.setText(curItem.getQueryText());
        if (nextItem != null) {
            mNextRecoderText.setText("下一条： " + nextItem.getQueryText());
        } else {
            mNextRecoderText.setText(" ");
        }

    }

    @Override
    public void onChangeRecordView(String type) {
        if (TcpClient.TYPE_SHORT.equals(type)) {
            mBtnReset.setVisibility(View.VISIBLE);
            mTitle.setText(R.string.short_record_text);
            mBtnCh.setText(R.string.long_record_text);

        }else {
            mBtnReset.setVisibility(View.GONE);
            mTitle.setText(R.string.long_record_text);
            mBtnCh.setText(R.string.short_record_text);
        }
        setSpeakIcon(ActivityContract.SpeakType.START);
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
}
