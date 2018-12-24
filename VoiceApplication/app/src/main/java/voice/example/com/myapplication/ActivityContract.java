package voice.example.com.myapplication;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import voice.example.com.myapplication.model.RecordItem;

/**
 * Created by liwu on 18-11-21.
 */

public interface ActivityContract {
    @Retention(RetentionPolicy.SOURCE)
    @interface SpeakType {
        int START = 0;
        int STOP = 1;
    }
    interface IActivityView{
        void onShowRecData(String data);
        void setSpeakIcon(int type);
        void onShowNoUSBDialog();
        void onUpdateMicWave();
        void onShowStopView();
        void onShowStartView();
        void onPlayFinishedView();
        void onShowText(RecordItem curItem, RecordItem nextItem);
        void onChangeRecordView(String type);
    }
    interface IActivityPresenter extends BasePresenter {
        void playOrStopCurRecorder();
        void reset();
        void next();
        void changeMode(String type);
        void startSpeakOnOff();
        void destroy();
        void disConnect();
    }
}
