package voice.example.com.myapplication.model;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liwu on 18-11-21.
 */

public class FileOperation {

    private static final String TAG = "RecorderVoice";
    private static RandomAccessFile mFile;

    private static boolean openFile(String path) {
        boolean result = false;
        if (null == path || "".equals(path)) {
            return result;
        }
        File file = new File(path);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            mFile = new RandomAccessFile(file, "rw");
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }

    public static void close() {
        if (mFile != null) {
            try {
                mFile.getChannel().close();
                mFile.close();
                mFile = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String read(String fileName) {
        String strContent = "";
        try {
            if (openFile(fileName)) {
                int fileSize = (int) mFile.length();
                byte[] buff = new byte[fileSize];
                mFile.read(buff, 0, fileSize);
                strContent = new String(buff);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
            return strContent;
        }

    }


    public static HashMap readLine(String fileName) {
        String strContent = "";
        HashMap<Integer, String> mapContent = new HashMap<>();
        try {
            if (openFile(fileName)) {
                int key = 0;
                while ((strContent = mFile.readLine()) != null) {
                    mapContent.put(key++, strContent);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
            return mapContent;
        }
    }

    /**
     *
     * @param fileName
     * @param content
     */

    public static void writeFile(String fileName, ByteBuffer content){

        if (!content.hasRemaining()) return;

        try {
            if (openFile(fileName)) {
                long fileSize = mFile.length();
                mFile.seek(fileSize);
                mFile.getChannel().write(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }

    }
    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f.getAbsolutePath());
            }
            //file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }

    }

    public static List<RecordItem> readQueryFromFile(File file) {
        List<RecordItem> recordItems = new ArrayList<>();
        FileInputStream fis;
        BufferedReader bufferReader;
        String[] words;
        Log.d(TAG, "read file exites =  " + file.exists());
        if (file.exists()) {
            try {
                fis = new FileInputStream(file);
                bufferReader = new BufferedReader(new InputStreamReader(fis));
                String line = "";
                Log.d(TAG, "read bufferReader =  " + bufferReader);
                try {
                    while ((line = bufferReader.readLine()) != null) {
                        words = line.split("\t");
                        if (words.length > 1) {
                            RecordItem recordItem = new RecordItem();
                            recordItem.setFileName(words[0]);
                            recordItem.setQueryText(words[1]);
                            recordItems.add(recordItem);
                            //mRecordItemList.add(recordItem);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bufferReader.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return recordItems;
        } else {
            return null;
        }
    }
}
