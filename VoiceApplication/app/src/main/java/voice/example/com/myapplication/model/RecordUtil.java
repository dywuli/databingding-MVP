package voice.example.com.myapplication.model;

import android.app.Activity;
import android.content.Context;
import android.os.storage.StorageManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordUtil {
    private static final String TAG = "VoiceUtil";
    private Context mContext;
    private Method mMethodGetPaths;
    private Method mMethodGetPathsState;
    private StorageManager mStorageManager;


    public RecordUtil(Context context) {
        mContext = context;
        mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
        try {
            mMethodGetPaths = mStorageManager.getClass().getMethod("getVolumePaths");
            mMethodGetPathsState = mStorageManager.getClass().getMethod("getVolumeState", String.class);//String.class形参列表
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public String[] getVolumePaths() {
        String[] paths = null;
        try {
            paths = (String[]) mMethodGetPaths.invoke(mStorageManager);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return paths;
    }


    public String getVolumeState(String mountPoint) {
        //mountPoint是挂载点名Storage'paths[1]:/mnt/extSdCard不是/mnt/extSdCard/
        //不同手机外接存储卡名字不一样。/mnt/sdcard
        String status = null;
        try {
            status = (String) mMethodGetPathsState.invoke(mStorageManager, mountPoint);
            //调用该方法，mStorageManager是主调，mountPoint是实参数
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
        Log.d(TAG, "VolumnState:" + status);
        return status;
    }


    public String getCurrentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

}
