package com.csjbot.snowbot.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.android.core.util.SharedUtil;
import com.android.core.util.StrUtil;
import com.csjbot.csjbase.log.Csjlogger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 文件工具类。
 *
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月23日 上午10:31:30
 */
public class FileUtil {
    public final static String SURFFIX_PCM = ".pcm";

    public final static String SURFFIX_TXT = ".txt";

    public final static String SURFFIX_CFG = ".cfg";

    /**
     * 数据文件类，用于读写文件。
     *
     * @author hj
     * @date 2016年5月11日 下午2:18:47
     */
    public static class DataFileHelper {
        private String FILE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Files/";

        private BufferedOutputStream mFos;

        private FileInputStream mFis;

        private BufferedWriter mWriter;

        public DataFileHelper(String fileDir) {
            FILE_DIR = fileDir;
        }

        public boolean openFile(String filePath) {
            return openFile(filePath, true);
        }

        public boolean openFile(String filePath, boolean inCurrentDir) {
            File file = null;
            if (inCurrentDir) {
                file = new File(FILE_DIR + filePath);
            } else {
                file = new File(filePath);
            }

            try {
                mFis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mFis = null;
                return false;
            }

            return true;
        }

        public int getAvailabe() {
            if (null != mFis) {
                try {
                    return mFis.available();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return -1;
        }

        public int read(byte[] buffer) {
            if (null != mFis) {
                try {
                    return mFis.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    closeReadFile();
                    return 0;
                }
            }

            return -1;
        }

        public void closeReadFile() {
            if (null != mFis) {
                try {
                    mFis.close();
                    mFis = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void createAppendableFile(String filename, String suffix) {
            File dir = new File(FILE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            if (null != mWriter) {
                return;
            }

            if (TextUtils.isEmpty(filename)) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
                filename = df.format(new Date());
            }

            String filePath = FILE_DIR + filename + suffix;
            File file = new File(filePath);

            try {
                if (!file.exists()) {
                    file.createNewFile();
                }

                mWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void append(String s) {
            if (null != mWriter) {
                try {
                    mWriter.write(s);
                    mWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void closeAppendableFile() {
            if (null != mWriter) {
                try {
                    mWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mWriter = null;
            }
        }

        public void createFile(String filename, String suffix, boolean append) {
            File dir = new File(FILE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            if (null != mFos) {
                return;
            }

            if (TextUtils.isEmpty(filename)) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
                filename = df.format(new Date());
            }

            String filePath = FILE_DIR + filename + suffix;

            File file = new File(filePath);
            try {
                mFos = new BufferedOutputStream(
                        new FileOutputStream(file, append));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void createPcmFile(String filename) {
            if (TextUtils.isEmpty(filename)) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
                filename = df.format(new Date());
            }

            createFile(filename, SURFFIX_PCM, false);
        }

        public void write(byte[] data, boolean flush) {
            synchronized (DataFileHelper.this) {
                if (null != mFos) {
                    try {
                        mFos.write(data);
                        if (flush) {
                            mFos.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void write(byte[] data, int offset, int len, boolean flush) {
            synchronized (DataFileHelper.this) {
                if (null != mFos) {
                    try {
                        mFos.write(data, offset, len);
                        if (flush) {
                            mFos.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void closeWriteFile() {
            synchronized (DataFileHelper.this) {
                if (null != mFos) {
                    try {
                        mFos.flush();
                        mFos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mFos = null;
                }
            }
        }
    }

    /**
     * 目录大小守护线程，当目录大小超出限制时，按时间先后顺序删除子目录，以保持目录大小。
     *
     * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
     * @date 2016年5月11日 下午2:17:16
     */
    public static class DirSizeDeamonThread extends Thread {
        private static final String TAG = "DirSizeDeamonThread";

        private File mDir;

        private long mDirSizeBytesLimit;

        private long mDelSizeBytes;

        private int mCheckIntervalSec = 10 * 60;

        private boolean mStopRun = false;

        public DirSizeDeamonThread(String dirPath, long dirSizeBytesLimit,
                                   long delSizeBytes, int checkIntervalSec) {
            mDir = new File(dirPath);
            mDirSizeBytesLimit = dirSizeBytesLimit;
            mDelSizeBytes = delSizeBytes;
            mCheckIntervalSec = checkIntervalSec;

            // 检查间隔至少为10分钟
            if (mCheckIntervalSec < 10 * 60) {
                mCheckIntervalSec = 10 * 60;
            }
        }

        public void setDirPath(String dirPath) {
            mDir = new File(dirPath);
        }

        // 比较器，将文件按修改时间排序
        private Comparator<File> mTimeComparator = new Comparator<File>() {

            @Override
            public int compare(File file1, File file2) {
                long time1 = file1.lastModified();
                long time2 = file2.lastModified();

                if (time1 < time2) {
                    return -1;
                } else if (time1 > time2) {
                    return 1;
                }

                return 0;
            }
        };

        public void stopRun() {
            mStopRun = true;
            interrupt();
        }

        // 从oriFiles中按顺序找到总和刚好大于totalSize的文件
        private List<File> getFilesBySize(List<File> oriFiles, Map<File, Long> details,
                                          double totalSize) {
            if (null == oriFiles || null == details || totalSize <= 0) {
                return null;
            }

            List<File> files = new ArrayList<File>();
            double curSize = 0;

            for (File oriFile : oriFiles) {
                curSize += details.get(oriFile);
                files.add(oriFile);

                if (curSize >= totalSize) {
                    break;
                }
            }

            return files.size() == 0 ? null : files;
        }

        // 删除文件
        private void delFlies(List<File> files) {
            if (null == files) {
                return;
            }

            for (File file : files) {
                try {
//                    DebugLog.LogD(TAG, "delFile:" + file.getName());
                    Csjlogger.info("delFile:" + file.getName());

                    FileUtil.delFile(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            super.run();

            while (!mStopRun) {
                Map<File, Long> details = new HashMap<File, Long>();

                long time = System.currentTimeMillis();

                double totalSizeBytes = FileUtil.getFileSizeBytes(mDir, details, true);

                long spent = System.currentTimeMillis() - time;

//                DebugLog.LogD(TAG, "getFileSize, spent=" + spent);
                Csjlogger.info("getFileSize, spent=" + spent);

                if (totalSizeBytes - mDirSizeBytesLimit > 0) {
                    List<File> subFileList = new ArrayList<File>(details.keySet());
                    Collections.sort(subFileList, mTimeComparator);

                    List<File> delFileList = getFilesBySize(subFileList, details, mDelSizeBytes);

                    long time2 = System.currentTimeMillis();

                    delFlies(delFileList);

                    long spent2 = System.currentTimeMillis() - time2;

//                    DebugLog.LogD(TAG, "delFlies, spent=" + spent2);
                    Csjlogger.info("delFlies, spent=" + spent2);
                }

                try {
                    sleep(mCheckIntervalSec * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 创建一个FileHelper
     *
     * @param fileDir 文件目录
     * @return
     */
    public static DataFileHelper createFileHelper(String fileDir) {
        return new DataFileHelper(fileDir);
    }

    /**
     * 检测文件是否存在
     *
     * @param path 文件全路径
     * @return 是否存在
     */
    public static boolean exist(String path) {
        File file = new File(path);

        return file.exists();
    }

    /**
     * 删除文件（夹）。
     *
     * @param file
     * @return 是否成功
     */
    public static boolean delFile(File file) {
        if (null == file) {
            return false;
        }

        if (file.isFile()) {
            return file.delete();
        }

        boolean deleted = true;

        File[] subFiles = file.listFiles();
        if (null != subFiles) {
            for (File subFile : subFiles) {
                deleted = delFile(subFile);

                if (!deleted) {
                    return deleted;
                }
            }
        }

        return deleted;
    }

    /**
     * 获取文件（夹）的大小，单位：字节。
     *
     * @param file          文件（夹）对象
     * @param details       用于存储文件夹下文件夹大小信息
     * @param onlyDirDetail 是否只需要子文件夹信息
     * @return
     */
    public static long getFileSizeBytes(File file, Map<File, Long> details,
                                        boolean onlyDirDetail) {
        if (!file.exists()) {
            return 0;
        }

        if (file.isFile()) {
            return file.length();
        }

        long totalSizeBytes = 0;

        File[] subFiles = file.listFiles();
        if (null != subFiles) {
            for (File subFile : subFiles) {
                long subFileSizeBytes = getFileSizeBytes(subFile, null, onlyDirDetail);

                if (null != details) {
                    if (onlyDirDetail) {
                        if (subFile.isDirectory()) {
                            details.put(subFile.getAbsoluteFile(), subFileSizeBytes);
                        }
                    } else {
                        details.put(subFile.getAbsoluteFile(), subFileSizeBytes);
                    }
                }

                totalSizeBytes += subFileSizeBytes;
            }
        }

        return totalSizeBytes;
    }

    /**
     * 从assets目录读取字符文件。
     *
     * @param filePath 文件路径
     * @return 文件内容
     */
    public static String readAssetsFile(Context context, String filePath) {
        String content = "";

        AssetManager assetManager = context.getResources().getAssets();
        try {
            InputStream ins = assetManager.open(filePath);
            byte[] buffer = new byte[ins.available()];

            ins.read(buffer);
            ins.close();

            content = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    /**
     * 按行读取文件——用户自定义
     *
     * @param filePath 文件路径
     * @param fileName 文件名称
     */
    public static void readText(String filePath, String fileName) {
        try {
            makeRootDirectory(filePath);
            Map<String, String> customData;
            if (SharedUtil.getMap(SharedKey.CUSTOMVOICE) != null) {
                customData = SharedUtil.getMap(SharedKey.CUSTOMVOICE);
            } else {
                customData = new HashMap<String, String>();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)),
                    "Unicode"));
            String lineTxt = null;
            while ((lineTxt = br.readLine()) != null) {
                String[] clientVoice = lineTxt.split("\\|");
                //限制每行问题+答案最少是2个，最多是5个
                if (clientVoice.length >= 2 && clientVoice.length <= 5) {
                    for (int i = 0; i < clientVoice.length - 1; i++) {
                        if (StrUtil.isNotBlank(clientVoice[clientVoice.length - 1])) {
                            customData.put(clientVoice[i], clientVoice[clientVoice.length - 1]);
                        }
                    }
                    SharedUtil.setMap(SharedKey.CUSTOMVOICE, customData);
                }
            }
            br.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建文件夹
     *
     * @param filePath
     */
    public static void makeRootDirectory(String filePath) {
        File file = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            try {
                file = new File(filePath);
                if (!file.exists()) {
                    file.mkdirs();
                }
            } catch (Exception e) {

            }
        }
    }

    /**
     * 拷贝指定文件
     *
     * @param fromFile
     * @param toFile
     * @param fileName
     * @return
     */
    public static int copy(String fromFile, String toFile, String fileName) {
        File root = new File(fromFile + fileName);
        //如同判断SD卡是否存在或者文件是否存在
        //如果不存在则 return出去
        if (!root.exists()) {
            return -1;
        }
        //目标目录
        File targetDir = new File(toFile);
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        copySdcardFile(fromFile + fileName, toFile + fileName);
        return 0;
    }

    /**
     * 文件拷贝
     *
     * @param fromFile
     * @param toFile
     * @return
     */
    public static int copySdcardFile(String fromFile, String toFile) {
        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return 0;
        } catch (Exception ex) {
            return -2;
        }
    }

    /**
     * 拷贝指定文件夹下所有文件
     *
     * @param fromFile
     * @param toFile
     * @return
     */
    public int copy(String fromFile, String toFile) {
        //要复制的文件目录
        File[] currentFiles;
        File root = new File(fromFile);
        //如同判断SD卡是否存在或者文件是否存在
        //如果不存在则 return出去
        if (!root.exists()) {
            return -1;
        }
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();

        //目标目录
        File targetDir = new File(toFile);
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        //遍历要复制该目录下的全部文件
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].isDirectory())//如果当前项为子目录 进行递归
            {
                copy(currentFiles[i].getPath() + "/", toFile + currentFiles[i].getName() + "/");

            } else//如果当前项为文件则进行文件拷贝
            {
                copySdcardFile(currentFiles[i].getPath(), toFile + currentFiles[i].getName());
            }
        }
        return 0;
    }

    /**
     * SD卡剩余空间
     *
     * @return
     */
    public static long getSDFreeSize() {
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocks();
        //返回SD卡空闲大小
        //return freeBlocks * blockSize;  //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
        return (freeBlocks * blockSize) / 1024 / 1024; //单位MB
    }

}
