package voice.example.com.myapplication.recordwave.iview;

/**
 * 提供了MicView的基本功能接口
 * Created by kael on 16/7/5.
 */

public interface IMicView {
    void start();

    void stop();

    void loading();

    void updateVolume(double volume);

    void cancel();

    void end();

    void success();

    void unknown();

    void error();

    //void setUserActionListener(ticauto.design.widget.MicView.UserOnClickListener listener);
}