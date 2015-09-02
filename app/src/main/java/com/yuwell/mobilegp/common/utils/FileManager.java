package com.yuwell.mobilegp.common.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.yuwell.mobilegp.common.GlobalContext;

import junit.framework.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Date;

public class FileManager {

	private static final String LOG = "log";
    private static final String BACKUP_DB = "DbBackup";
    private static final String BACKUP_PREF = "PrefBackup";
	private static final String APP = "UHEALTH";
	private static final String TEMP = "tmp";
    private static final String IMAGE = "image";

    private static final String DATABASES = "databases";
    private static final String PREFERENCES = "shared_prefs";

	private static volatile boolean cantReadBecauseOfAndroidBugPermissionProblem = false;

	public static boolean isExternalStorageMounted() {
        boolean canRead = Environment.getExternalStorageDirectory().canRead();
        boolean onlyRead = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean unMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED);

        return !(!canRead || onlyRead || unMounted);
    }

    public static void copyAssets(Context context) {
        String path = Environment.getExternalStorageDirectory() + "/wltlib";
        File file = new File(path);
        if (!file.exists()) {
            mkdirsIfNotExist(path);

            AssetManager assetManager = context.getAssets();
            String[] files = null;
            try {
                files = assetManager.list("wltlib");
            } catch (IOException e) {
                Log.e("tag", "Failed to get asset file list.", e);
            }

            for (String filename : files) {
                InputStream in = null;
                OutputStream out = null;
                byte[] buffer = new byte[1024];
                try {
                    in = assetManager.open("wltlib/" + filename);
                    File outFile = new File(path, filename);
                    out = new FileOutputStream(outFile);

                    int read;
                    while((read = in.read(buffer)) != -1){
                        out.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    Log.e("tag", "Failed to copy asset file: " + filename, e);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                }
            }
        }
    }

    public static String getImageDir() {
        String path = getCacheDir() + File.separator + "image";
        return mkdirsIfNotExist(path);
    }

    public static boolean backupDatabase() {
        String dbDir = getDataDir(DATABASES);
        String toDir = getBackupDir(BACKUP_DB);

        boolean flag = false;
        if (!TextUtils.isEmpty(toDir)) {
            flag = copyFileUsingFileChannel(dbDir + File.separator + "mobilegp.db",
                    toDir + File.separator + "mobilegp.db");
        }

        return flag;
    }

    private static String getDataDir(String type) {
        return GlobalContext.getInstance().getApplicationInfo().dataDir + File.separator + type;
    }

    private static String getBackupDir(String folderName) {
        if (!isExternalStorageMounted()) {
            return "";
        } else {
            String path = getCacheDir() + File.separator + folderName + File.separator +  new Date().getTime();
            return mkdirsIfNotExist(path);
        }
    }

    private static String getCacheDir() {
        if (isExternalStorageMounted()) {
            File path = GlobalContext.getInstance().getExternalCacheDir();
            if (path != null) {
                return path.getAbsolutePath();
            }
        } else {
            File path = GlobalContext.getInstance().getCacheDir();
            if (path != null) {
                return path.getAbsolutePath();
            }
        }

        return "";
    }

    private static String mkdirsIfNotExist(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
             if (dir.mkdirs()) {
                 return path;
             } else {
                 return "";
             }
        }
        return path;
    }

    public static boolean copyDir(String fromDir, String toDir, String suffix) {
        boolean flag = false;

        File from = new File(fromDir);
        Assert.assertTrue(from.isDirectory());

        File[] files = from.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                String path = toDir + File.separator + file.getName();
                mkdirsIfNotExist(path);
                flag = copyDir(file.getAbsolutePath(), path, suffix);
            } else {
                flag = copyFileUsingFileChannel(file.getAbsolutePath(), toDir + File.separator + file.getName() + suffix);
            }
        }

        return flag;
    }

    public static boolean copyFile(String from, String to) {
        File fromFile = new File(from);
        File toFile = new File(to);

        Assert.assertTrue(fromFile.exists());
        boolean flag = false;

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(fromFile);
            fos = new FileOutputStream(toFile);

            byte[] buffer = new byte[1024 * 4];
            int length;

            while ((length = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
            }
            flag = true;
        } catch (FileNotFoundException e) {
            Log.e(e.getMessage(), e.toString());
        } catch (IOException e) {
            Log.e(e.getMessage(), e.toString());
        } finally {
            try {
                fis.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    private static boolean copyFileUsingFileChannel(String from, String to) {
        boolean flag = false;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel fcIn = null;
        FileChannel fcOut = null;

        try {
            fis = new FileInputStream(from);
            fos = new FileOutputStream(to);
            fcIn = fis.getChannel();
            fcOut = fos.getChannel();

            fcIn.transferTo(0, fcIn.size(), fcOut);

            flag = true;
        } catch (FileNotFoundException e) {
            Log.e(e.getMessage(), e.toString());
        } catch (IOException e) {
            Log.e(e.getMessage(), e.toString());
        } finally {
            try {
                fcIn.close();
                fis.close();
                fcOut.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

}
