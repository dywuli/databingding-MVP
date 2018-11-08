package com.ticauto.accessory.media.tuner.util;


import android.content.Context;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.view.TouchDelegate;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobvoi.android.common.utils.LogUtil;
import com.squareup.okhttp.Response;
import com.ticauto.accessory.media.tuner.service.function.nettuner.NetFmParm;
import com.ticauto.common.tuner.TunerConstants;
import com.ticauto.common.tuner.internal.BaseChannelInfo;
import com.ticauto.common.utils.OkHttpHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by liwu on 18-3-31.
 */

public class TunerCommonUtil {

    public final static String TAG  = "TunerCommonUtil";

    private final static String KEY_TYPE = "type";
    private final static String KEY_FREQ = "frequency";
    private final static String KEY_PROVINCE = "province";
    private final static String KEY_CITY = "city";

    /**
     * 没有连接网络
     */
    public static final int NETWORK_NONE = -1;
    /**
     * 移动网络
     */
    public static final int NETWORK_MOBILE = 0;
    /**
     * 无线网络
     */
    public static final int NETWORK_WIFI = 1;

    public class broadcastResponse<T> {
        public int currentPage;
        public int totalPage;
        public T broadcastList;
    }
    public class BroadcastInfo {
        private String broadcastId;//网络BroadcastId
        private String playUrl;//音频流协议
        private String broadcastName;//频道名字
        private String programName;//当前节目名字
        private String img;//缓存图片的路径

    }
    public static class Area {
        public String areaId;
        public String areaName;
    }
    public static int getNetWorkState(Context context) {
        // 得到连接管理器对象
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {

            if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return NETWORK_WIFI;
            } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                return NETWORK_MOBILE;
            }
        } else {
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }

    /**
     * Request http data with url
     * @param strUrl
     * @return String
     */
    public static String requestHttpData (String strUrl) {
        LogUtil.d(TAG, ">>>>>requestHttpData() " + strUrl);
        String retStirng = null;
        try {
            Response response = OkHttpHelper.getInstance().request(OkHttpHelper.getInstance().buildGetRequest(strUrl));
            if (response.isSuccessful()) {
                retStirng =  response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return retStirng;
    }

    public static String getSystemTime() {

        LogUtil.d(TAG, ">>>>>getSystemTime()");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        String strTime = simpleDateFormat.format(date);

        return strTime;

    }



    public static List<BaseChannelInfo> loadCurrentCityInfo(String strCurrentCityUrl) {
        String strNet = requestHttpData(strCurrentCityUrl);
        List<BaseChannelInfo> baseChannelInfoList = new ArrayList<>();
        List<BroadcastInfo> broadcastInfoList = new ArrayList<>();
        if (!TextUtils.isEmpty(strNet)) {
            Gson gson = new Gson();
            Type userListType = new TypeToken<broadcastResponse<List<BroadcastInfo >>>(){}.getType();
            broadcastResponse<List<BroadcastInfo>> userListResult = gson.fromJson(strNet,userListType);
            broadcastInfoList.addAll(userListResult.broadcastList);
            for (BroadcastInfo broadcastInfo : broadcastInfoList) {
                BaseChannelInfo baseChannelInfo = new BaseChannelInfo();
                baseChannelInfo.setModelType(TunerConstants.ModelType.NET_FM);
                baseChannelInfo.setImgCachePath(broadcastInfo.img);
                baseChannelInfo.setChannelName(broadcastInfo.broadcastName);
                baseChannelInfo.setProgramName(broadcastInfo.programName);
                baseChannelInfo.setPlayUrl(broadcastInfo.playUrl);
                baseChannelInfo.setBroadcastId(broadcastInfo.broadcastId);
                baseChannelInfoList.add(baseChannelInfo);
            }
        }
        return baseChannelInfoList;
    }

    ////将频率转换成需要显示的格式 8920 -> 89.2
    public static String formatFrequency(int dividen, int divisor) {
        LogUtil.d(TAG, ">>>>>formatFrequency");
        BigDecimal bigDividen = new BigDecimal(Integer.toString(dividen));
        BigDecimal bigDivisor = new BigDecimal(Integer.toString(divisor));
        DecimalFormat decimalFormat = new DecimalFormat("#0.0");
        return decimalFormat.format(bigDividen.divide(bigDivisor));
    }

    public static String stringToMD5(String string) {

        byte[] hash;
        if (TextUtils.isEmpty(string)){
            return null;
        }
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            LogUtil.e(TAG, ">>>>>stringToMD5 " + e);
            return null;
        } catch (UnsupportedEncodingException e) {
            LogUtil.e(TAG, ">>>>>stringToMD5 " + e);
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        LogUtil.d(TAG, ">>>>> stringToMD5 " + hex.toString());
        return hex.toString();
    }
    //扩大控件点击范围
    public static void onTouchDelegate(View view, Rect boud) {
        int left = view.getLeft();
        int top = view.getTop();
        int right = view.getRight();
        int bottom = view.getBottom();
        Rect bouds = new Rect(left - boud.left, top - boud.top, right + boud.right, bottom + boud.bottom);
        TouchDelegate delegate = new TouchDelegate(bouds, view);
        ((View) view.getParent()).setTouchDelegate(delegate);
    }

    public static String getSysFormatTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date).toString();
    }

    public static String createChannelJson(BaseChannelInfo channelInfo) throws JSONException{
        if (channelInfo == null) {
            LogUtil.e(TAG, ">>>>>createJson args is null! ");
        }
        JSONObject jsonobject = new JSONObject();
        int modelType = channelInfo.getModelType();
        switch (modelType) {
            case TunerConstants.ModelType.LOCAL_AM:
            case TunerConstants.ModelType.LOCAL_FM:
                String name = channelInfo.getChannelName();
                if (name == null) {
                    break;
                }
                if(modelType == TunerConstants.ModelType.LOCAL_FM){
                    jsonobject.put(KEY_TYPE, "FM");
                    jsonobject.put(KEY_FREQ, name.substring(0, name.length()-2));
                }else {
                    jsonobject.put(KEY_TYPE, "AM");
                    jsonobject.put(KEY_FREQ, name.substring(0, name.length()-2));
                }
                jsonobject.put(KEY_PROVINCE, channelInfo.getProvince());
                jsonobject.put(KEY_CITY, channelInfo.getCity());

                break;
            case TunerConstants.ModelType.NET_FM:
                jsonobject.put(NetFmParm.BROADCAST_ID, channelInfo.getBroadcastId());

        }

        return String.valueOf(jsonobject);
    }

    public static boolean postData(String strUrl, String strJson) {

        boolean result = false;
        OutputStream outputStream = null;
        if (TextUtils.isEmpty(strUrl) || TextUtils.isEmpty(strJson)) {
            LogUtil.e(TAG, ">>>>>HttpURLConnection args is null! ");
            return result;
        }
        try {
            URL url = new URL(strUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            if (connection == null) {
                LogUtil.e(TAG, ">>>>>HttpURLConnection is null! ");
                return result;
            }
            connection.setConnectTimeout(5000);
            // 设置允许输出
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            // 设置contentType
            connection.setRequestProperty("Content-Type","application/json");
            outputStream = connection.getOutputStream();
            if (outputStream != null) {
                outputStream.write(strJson.getBytes());
            }
            if (connection.getResponseCode() == 200) {
                result = true;
            }
        } catch (IOException e) {
            LogUtil.e(TAG, ">>>>>postData " + e);
            result = false;
        } finally {
            try {
                if (outputStream != null){
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    }
}
