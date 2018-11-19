package com.example.mvp;

import com.example.mvplib.BasePresenter;
import com.example.mvplib.Model;
import com.example.mvplib.View;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by liwu on 18-11-19.
 */

public class IActivityContract {
    public interface MainView extends View {
        /**
         * 设置数据
         *
         * @param str
         */
        void setData(String str);
    }
    public interface MainModel extends Model {
        /**
         * 从网络获取数据
         *
         * @return
         */
        Observable<List<User>> getDataFromNet();

        String getDataFromString();

        /**
         * 停止请求
         */
        void stopRequest();
    }
    public abstract class MainPresenter extends BasePresenter<MainModel, MainView> {

    }
}
