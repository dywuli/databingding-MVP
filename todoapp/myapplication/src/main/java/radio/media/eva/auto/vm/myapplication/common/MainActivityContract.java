package radio.media.eva.auto.vm.myapplication.common;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

import org.jetbrains.annotations.NotNull;

import radio.media.eva.auto.vm.myapplication.model.TemperatureData;

/**
 * Created by liwu on 18-11-2.
 */

public interface MainActivityContract {

    public interface Presenter extends LifecycleObserver {
        void onShowData(TemperatureData temperatureData);
        void showList();

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        void onCreate(@NotNull LifecycleOwner owner);

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        void onDestory(@NotNull LifecycleOwner owner);

        @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
        void onLifeCycleChanged(@NotNull LifecycleOwner owner, @NotNull Lifecycle.Event event);

    }

    public interface View {
        void showData(TemperatureData temperatureData);
        void setView(Presenter presenter);
    }
}
