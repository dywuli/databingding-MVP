package com.example.utils;

import android.os.Handler;
import android.os.Looper;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpHelper {
    private static OkHttpHelper mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mDelivery;

    private static final String TAG = "OkHttpHelper";

    private OkHttpHelper() {
        mOkHttpClient = new OkHttpClient();
        mDelivery = new Handler(Looper.getMainLooper());
    }

    public synchronized static OkHttpHelper getInstance() {
        if (mInstance == null) {
            mInstance = new OkHttpHelper();
        }
        return mInstance;
    }

    /**
     * 开启同步线程访问网络
     * @param quest
     * @return
     * @throws IOException
     */
    public Response request(Request quest) throws IOException{
        return mOkHttpClient.newCall(quest).execute();
    }
    /**
     * 开启异步线程访问网络
     *
     * @param quest quest
     */
    public void request(final Request quest, final ResultCallback callback) {
        mOkHttpClient.newCall(quest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.failed(quest, e);
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, final okhttp3.Response response) throws IOException {
                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.success(response);
                        }
                    }
                });
            }

        });
    }

    public Request buildGetRequest(String url) {
        return new Request.Builder()
                .url(url)
                .get()
                .build();
    }



    public interface ResultCallback {
        void success(Response response);

        void failed(Request request, IOException e);

    }

}
