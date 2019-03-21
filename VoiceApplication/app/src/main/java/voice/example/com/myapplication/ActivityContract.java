package voice.example.com.myapplication;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import voice.example.com.myapplication.model.RecordItem;

/**
 * Created by liwu on 18-11-21.
 */

public interface ActivityContract {
    @Retention(RetentionPolicy.SOURCE)
    @interface SpeakType {
        int START = 0;
        int STOP = 1;
        int REFRESH = 2;
    }
    interface IActivityView{
        void setSpeakIcon(int type);
        void onShowDialog(String message);
        void onUpdateMicWave();
        void onShowStopView();
        void onShowStartView();
        void onPlayFinishedView();
        void onShowText(RecordItem curItem, RecordItem nextItem);
        void onChangeLayout(String type);
        void onShowRecordFileSize(String fileSize);
        void onShowQueryTxtFileList(List<String> stringList);
        void onShowMovingToUSB();
        void onMovingToUSBFinished();
        void onShowCurText(boolean isShow);
    }
    interface IActivityPresenter extends BasePresenter {
        void playOrStopCurRecorder();
        void reset();
        void next();
        void changeMode(String type);
        void speakOnOff();
        void acquireQueryList();
        void destroy();
        void updateGender(String strGgender);
        void updateAgeGroup(String stAgeGroup);
        void moveDataToUSB();
        void requestShowRecordFileSize();
        void acquireQueryFileNameList();
        void disConnect();
    }
}
