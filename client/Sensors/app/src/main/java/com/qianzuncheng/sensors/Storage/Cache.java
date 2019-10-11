package com.qianzuncheng.sensors.Storage;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import com.qianzuncheng.sensors.MyApplication;
import com.qianzuncheng.sensors.R;
import com.qianzuncheng.sensors.Utils.PhotoUtils;
import com.qianzuncheng.sensors.Utils.VideoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Cache {
    public static final String TAG = "Cache";
    private volatile static Cache uniqueInstance;
    private Context context;
    // in class <Cache> methods to update upload queue file may be invoked in different threads
    // private Lock cacheUploadQueueFileLock;
    // however double check lock is thread-safe
    public static class CATEGORY {
        public static final String DATA_COLLECT_SERVICE  = "DataCollectUtils";
        public static final String AUDIO                 = "Audio";
        public static final String NETWORK_LOG           = "NetworkLog";
        public static final String PHOTO                 = "Photo";
        public static final String UPLOAD_QUEUE          = "UploadQueue";
        public static final String VIDEO                 = "Video";
        public static String[] all() {
            return (new String[] {
                    DATA_COLLECT_SERVICE,
                    AUDIO,
                    NETWORK_LOG,
                    PHOTO,
                    VIDEO,
                    UPLOAD_QUEUE,
            });
        }
        public static String[] allUploadCategory() {
            return (new String[] {
                    DATA_COLLECT_SERVICE,
                    AUDIO,
                    PHOTO,
                    VIDEO
            });
        }
    }
    public static class Size {
        private static double dataCollectServiceSize;
        private static double audioSize;
        private static double photoSize;
        private static double videoSize;
        public static void updateCacheSize(Context context) {
            dataCollectServiceSize  = 0;
            audioSize               = 0;
            photoSize               = 0;
            videoSize               = 0;
            File[] files = Cache.getInstance(context).getFiles(CATEGORY.DATA_COLLECT_SERVICE);
            if (files != null) {
                for (File f : files) {
                    dataCollectServiceSize += f.length() / 1024.0;
                }
            }

            files = Cache.getInstance(context).getFiles(CATEGORY.AUDIO);
            if (files != null) {
                for (File f : files) {
                    audioSize += f.length() / 1024.0;
                }
            }

            files = Cache.getInstance(context).getFiles(CATEGORY.PHOTO);
            if (files != null) {
                for (File f : files) {
                    photoSize += f.length() / 1024.0;
                }
            }
            files = Cache.getInstance(context).getFiles(CATEGORY.VIDEO);
            if (files != null) {
                for (File f : files) {
                    videoSize += f.length() / 1024.0;
                }
            }
        }
        public static SpannableString[] toSpannableStringArray() {
            String[] showStr = {
                    MyApplication.getContext().getString(R.string.main_list_sensor) + ": ", "", " KB\n",
                    MyApplication.getContext().getString(R.string.main_list_audio) + ": ", "", " KB\n",
                    MyApplication.getContext().getString(R.string.main_list_photo) + ": ", "", " KB\n",
                    MyApplication.getContext().getString(R.string.main_list_video) + ": ", "", " KB\n"
            };
            showStr[1] = String.format("%.2f", dataCollectServiceSize);
            showStr[4] = String.format("%.2f", audioSize);
            showStr[7] = String.format("%.2f", photoSize);
            showStr[10] = String.format("%.2f", videoSize);

            int LINE_NUM = 4;
            SpannableString spannableStringArray[] = new SpannableString[LINE_NUM];
            for (int i = 0; i < LINE_NUM; i++) {
                String finalString = "";
                for(int j = 0; j < 3; j++) {
                    finalString += showStr[i * 3 + j];
                }
                // bold
                spannableStringArray[i] = new SpannableString(finalString);
                spannableStringArray[i].setSpan(
                        new StyleSpan(android.graphics.Typeface.BOLD),
                        finalString.indexOf(showStr[i * 3]),
                        finalString.indexOf(showStr[i * 3]) + showStr[i * 3].length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            return spannableStringArray;
        }
    }
    public static class FilePack {
        public String filePath;
        public String category;
        //public int fileSize;
        //public int priority;
        public FilePack(String fp, String c) {
            filePath = fp;
            category = c;
        }
    }

    private Cache(Context context) {
        this.context = context;
        //cacheUploadQueueFileLock = new ReentrantLock();
    }
    // Double CheckLock(DCL)
    public static Cache getInstance(Context context) {
        if (uniqueInstance == null) {
            synchronized (Cache.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new Cache(context);
                }
            }
        }
        return uniqueInstance;
    }

    // for text data
    // when write, need to synchronize
    public synchronized void append(String category, String s) {
        try {
            String dirpath = Cache.getInstance(context).getFileDirpath(category);
            File folder = new File(dirpath);
            if (!folder.exists()) {
                folder.mkdir();
            }
            FileOutputStream fos = new FileOutputStream(dirpath + category, true);
            fos.write((s + "\n").getBytes());
            fos.close();
        } catch (Exception e) {
            // TODO: ...
            Log.e(TAG, "append: " + e.toString());
            return;
        }
    }

    public void append(String category, FilePack filePack) {
        append(category, filePack.filePath + "\t" + filePack.category);
    }

    // for text data
    public String read(String category) {
        String s = "";
        try {
            FileInputStream fis = new FileInputStream(getFilePaths(category)[0]);
            int len = 0;
            byte[] buf = new byte[1024];
            while((len = fis.read(buf)) != -1){
                s += new String(buf, 0, len);
            }
            fis.close();
        } catch (Exception e) {
            s = null;
            Log.e(TAG, "read: " + e.toString());
        }
        return s;
    }

    public String getFileDirpath(String category) {
        String dirpath = context.getFilesDir().getAbsolutePath();
        switch (category) {
            case CATEGORY.DATA_COLLECT_SERVICE:
                dirpath += "/data_collect_service/";
                break;
            case CATEGORY.AUDIO:
                dirpath += "/audio/";
                break;
            case CATEGORY.NETWORK_LOG:
                dirpath += "/network_log/";
                break;
            case CATEGORY.PHOTO:
                dirpath = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES) + "/" + PhotoUtils.dirName + "/";
                break;
            case CATEGORY.VIDEO:
                dirpath = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM) + "/" + VideoUtils.dirName + "/";
                break;
            case CATEGORY.UPLOAD_QUEUE:
                dirpath += "/upload_queue/";
                break;
            default: break;
        }
        return dirpath;
    }

    public File[] getFiles(String category) {
        File dir = new File(getFileDirpath(category));
        return dir.listFiles();
    }

    public String[] getFilePaths(String category) {
        File[] files = getFiles(category);
        if(files == null) return null;
        String[] names = new String[files.length];
        for(int i = 0; i < files.length; i++) {
            names[i] = files[i].getAbsolutePath();
        }
        return names;
    }

    public void clear(@Nullable String category) {
        if(category == null) {
            for(String e: CATEGORY.all()) {
                File[] files = getFiles(e);
                if(files == null) return;
                for(File file: files) {
                    file.delete();
                }
            }
        } else {
            File[] files = getFiles(category);
            if(files == null) return;
            for(File file: files) {
                file.delete();
            }
        }
    }

    // TODO: use lock
    public void clear(File f) {
        f.delete();
    }

}
