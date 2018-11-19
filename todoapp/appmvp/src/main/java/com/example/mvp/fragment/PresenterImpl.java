package com.example.mvp.fragment;


import android.util.Log;

public class PresenterImpl extends FragmentContract.abstractPresenter {
    public void getData() {
        String dataFromNet = null;
        if (model != null) {
            dataFromNet = model.getDataFromHWW();
        }
        if (getView() != null) {
            getView().setTextData(dataFromNet);
        }
    }

    @Override
    protected void onViewDestroy() {
        Log.i("view-uninstall", "SecondActivity finished");
        if (model != null) {
            model.stopRequest();
        }
    }

}
