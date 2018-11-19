package com.example.mvplib;

public interface BaseMvp<M extends Model, V extends View, P extends BasePresenter> {
    M createModel();

    V createView();

    P createPresenter();
}
