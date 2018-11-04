package radio.media.eva.auto.vm.myapplication.presenter;

import android.content.Context;
import android.content.Intent;

import radio.media.eva.auto.vm.myapplication.view.SecondActivity1;
import radio.media.eva.auto.vm.myapplication.model.TemperatureData;
import radio.media.eva.auto.vm.myapplication.common.MainActivityContract;

/**
 * Created by liwu on 18-11-2.
 */

public class MainActivityPresenter implements MainActivityContract.Presenter {
    private MainActivityContract.View view;
    private Context ctx;

    public MainActivityPresenter(MainActivityContract.View view, Context ctx) {
        this.view = view;
        this.ctx = ctx;
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
}
