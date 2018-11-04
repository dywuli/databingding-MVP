package radio.media.eva.auto.vm.myapplication.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import radio.media.eva.auto.vm.myapplication.BR;

/**
 * Created by liwu on 18-11-2.
 */

public class TemperatureData extends BaseObservable {
    private String celsius;
    @Bindable
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    private String location;

    public TemperatureData(String celsius, String location) {
        this.celsius = celsius;
        this.location = location;
    }

    @Bindable
    public String getCelsius() {
        return celsius;
    }

    public void setCelsius(String celsius) {
        this.celsius = celsius;
        notifyPropertyChanged(BR.celsius);

    }
}
