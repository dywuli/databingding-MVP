package radio.media.eva.auto.vm.myapplication.presenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import radio.media.eva.auto.vm.myapplication.view.SecondActivity1;
import radio.media.eva.auto.vm.myapplication.model.TemperatureData;
import radio.media.eva.auto.vm.myapplication.common.MainActivityContract;

/**
 * Created by liwu on 18-11-2.
 */

public class MainActivityPresenter implements MainActivityContract.Presenter {
    private static final String TAG = "ActivityPresenter";
    private MainActivityContract.View view;
    private Context ctx;

    public MainActivityPresenter(MainActivityContract.View view, Context ctx) {
        this.view = view;
        this.ctx = ctx;
        view.setView(this);
    }

    @Override
    public void onShowData(TemperatureData temperatureData) {
        view.showData(temperatureData);
    }

    @Override
    public void showList() {
        Intent i = new Intent(ctx, SecondActivity1.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    @Override
    public void onCreate(@NotNull LifecycleOwner owner) {
        Log.d(TAG , ">>>>>onCreate" + owner.getLifecycle().getCurrentState());
    }

    @Override
    public void onDestory(@NotNull LifecycleOwner owner) {
        Log.d(TAG , ">>>>>onLifeCycleChanged" + owner.getLifecycle().getCurrentState());
    }

    @Override
    public void onLifeCycleChanged(@NotNull LifecycleOwner owner, @NotNull Lifecycle.Event event) {
        Log.d(TAG , ">>>>>onLifeCycleChanged" + owner.getLifecycle().getCurrentState());
    }
}
