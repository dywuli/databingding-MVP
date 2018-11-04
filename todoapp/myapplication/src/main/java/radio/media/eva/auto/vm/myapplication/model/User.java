package radio.media.eva.auto.vm.myapplication.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import radio.media.eva.auto.vm.myapplication.BR;

/**
 * Created by liwu on 18-10-18.
 */

public class User extends BaseObservable{
    private String textName;
    private String btnName;
    @Bindable
    public String getTextName() {
        return textName;
    }

    public void setTextName(String textName) {
        this.textName = textName;
        notifyPropertyChanged(BR.textName);
    }
    @Bindable
    public String getBtnName() {
        return btnName;
    }

    public void setBtnName(String btnName) {
        this.btnName = btnName;
        notifyPropertyChanged(BR.btnName);
    }
}
