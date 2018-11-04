package radio.media.eva.auto.vm.myapplication.common;

import radio.media.eva.auto.vm.myapplication.model.TemperatureData;

/**
 * Created by liwu on 18-11-2.
 */

public interface MainActivityContract {

    public interface Presenter {
        void onShowData(TemperatureData temperatureData);
        void showList();
    }

    public interface View {
        void showData(TemperatureData temperatureData);
    }
}
