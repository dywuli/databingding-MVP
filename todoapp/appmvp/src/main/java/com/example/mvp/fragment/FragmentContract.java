package com.example.mvp.fragment;

import com.example.mvplib.BasePresenter;
import com.example.mvplib.Model;
import com.example.mvplib.View;

/**
 * Created by liwu on 18-11-19.
 */

public interface FragmentContract {
    public interface BlackModel extends Model {
        String getDataFromHWW();

        void stopRequest();
    }
    public interface BlackView extends View {
        void setTextData(String str);
    }

    public abstract class abstractPresenter extends BasePresenter<BlackModel, BlackView> {

    }

}
