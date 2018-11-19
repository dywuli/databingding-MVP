package com.example.mvp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mvplib.BaseMvpActivity;

public class MainActivity extends BaseMvpActivity<IActivityContract.MainModel, IActivityContract.MainView, MainPresenterImpl> implements IActivityContract.MainView {
    private TextView tvData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvData = findViewById(R.id.tv_data);
        Button btnFirst = findViewById(R.id.btn_first);
        btnFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        });
        init();
    }


    @Override
    public IActivityContract.MainModel createModel() {
        return new MainModelImpl();
    }

    @Override
    public IActivityContract.MainView createView() {
        return this;
    }

    @Override
    public MainPresenterImpl createPresenter() {
        return new MainPresenterImpl();
    }

    private void init() {
        if (presenter != null) {
            presenter.getData();
        }
    }

    @Override
    public void setData(String str) {
        tvData.setText(str);
    }

    @Override
    public void showToast(String info) {

    }

    @Override
    public void showProgress() {

    }
}
