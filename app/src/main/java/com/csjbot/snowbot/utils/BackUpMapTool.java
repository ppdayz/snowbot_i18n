package com.csjbot.snowbot.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Base64;

import com.alibaba.fastjson.JSON;
import com.android.core.entry.Static;
import com.android.core.util.FileUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot_rogue.bean.MapDataBean;
import com.csjbot.snowbot_rogue.servers.slams.SnowBotMoveServer;
import com.slamtec.slamware.geometry.Line;
import com.slamtec.slamware.robot.Map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author: jl
 * @Time: 2017/1/9
 * @Desc:
 */

public class BackUpMapTool {
    private static final String lasetSaveDirPath = "lasetSaveDir";
    private static final String saveDirPath = "saveDir";
    private static final String lastsavefile = "lastsavefile";
    public static BackUpMapTool backUpMap = new BackUpMapTool();
    private static final int RECT_LEN = 15;
    public static int MAXSIZE = 20 * 1024 * 1024;
    private Handler handler;


    public static BackUpMapTool getInstance() {
        return backUpMap;
    }

    private BackUpMapTool() {
        createFileDir();
    }

    private void createFileDir() {
        File lasetSaveDir = FileUtil.getCacheFileDir(lasetSaveDirPath);
        File saveDir = FileUtil.getCacheFileDir(saveDirPath);
    }

    public static void saveMapToSD(MapDataBean map) {
        if (null == map) {
            Csjlogger.debug("trying to save null Map");
            return;
        }
        File fileDir = FileUtil.getCacheFileDir(saveDirPath);
        removeCache(fileDir);
        String jsonTemp = JSON.toJSONString(map);
        FileWriter writer = null;
        String fileName = CommonTool.getSavaName();
        try {
            File file = new File(FileUtil.getCacheFileDir(saveDirPath), fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            writer = new FileWriter(file);
            writer.write(jsonTemp);
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != writer) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }
    }


    public static void saveMapToLastDir(MapDataBean map) {
        if (null == map) {
            Csjlogger.debug("trying to save null Map");
            return;
        }
        String jsonTemp = JSON.toJSONString(map);
        FileWriter writer = null;
        try {
            File file = new File(FileUtil.getCacheFileDir(lasetSaveDirPath), lastsavefile);
            if (!file.exists()) {
                file.createNewFile();
            }
            writer = new FileWriter(file);
            writer.write(jsonTemp);
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != writer) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public static Bitmap getMapPic(MapDataBean mapDataBean) {
        Map map = new Map(mapDataBean.getOrigin(), mapDataBean.getDimension(), mapDataBean.getResolution(), mapDataBean.getTimestamp(), Base64.decode(mapDataBean.getData(), Base64.NO_WRAP));
        boolean deNoise = true;
        int showMapW, showMapH;
        int[] newData = new int[map.getData().length]; // 获取地图内容
        int width = map.getDimension().getWidth();
        int height = map.getDimension().getHeight();
        int dataLen = newData.length;
        int pos = 0;
        byte[] oldDate = map.getData(); // 获取原始数据
        // 第一步，坐标系转换，从右手坐标系转为左手坐标系
        // 机器坐标系和显示屏坐标系关于副对角线对称 故而从右下角开始取值
        for (int i = width - 1; i >= 0; --i) {
            for (int j = height - 1; j >= 0; --j) {
                newData[pos++] = (int) oldDate[i + j * width];
            }
        }
        reColor(newData);
        Bitmap bm = Bitmap.createBitmap(newData, height, width, Bitmap.Config.RGB_565); // 获取原始地图
        return bm;
    }

    public static MapDataBean getLastFile() {
        File file = new File(FileUtil.getCacheFileDir(lasetSaveDirPath), lastsavefile);
        MapDataBean mapDataBean = null;
        if (file.exists()) {
            try {
                String str = "";
                FileInputStream inputStream = new FileInputStream(file);
                int size = inputStream.available();
                byte[] buffer = new byte[size];
                inputStream.read(buffer);
                inputStream.close();
                str = new String(buffer);
                mapDataBean = JSON.parseObject(str, MapDataBean.class);
            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
        }
        return mapDataBean;

    }

    public static List<MapDataBean> getMapDataBean() {
        File saveDir = FileUtil.getCacheFileDir(saveDirPath);
        List<MapDataBean> mapDataBeens = new ArrayList<>();
        List<File> files = new ArrayList<>();
        String filePath = saveDir.getAbsolutePath();
        files = getFileSort1(filePath);
        if (null != files && files.size() > 0) {
            for (File file : files) {
                MapDataBean mapDataBean = null;
                if (file.exists()) {
                    try {
                        String str = "";
                        FileInputStream inputStream = new FileInputStream(file);
                        int size = inputStream.available();
                        byte[] buffer = new byte[size];
                        inputStream.read(buffer);
                        inputStream.close();
                        str = new String(buffer);
                        mapDataBean = JSON.parseObject(str, MapDataBean.class);
                        String[] sourceStrArray = file.getName().split("_");
                        String data = sourceStrArray[0] + "年" + sourceStrArray[1] + "月" + sourceStrArray[2] + "日" + sourceStrArray[3] + ":" + sourceStrArray[4] + ":" + sourceStrArray[5];
                        mapDataBean.setCreateTime(data);
                        mapDataBeens.add(mapDataBean);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {

                    }
                }
            }
        }
        return mapDataBeens;
    }

    public static void removeCache(File file) {
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }

        int dirSize = 0;
        for (int i = 0; i < files.length; i++) {
            dirSize += files[i].length();
        }

        while (MAXSIZE < dirSize) {
            List<File> tempFiles = getFileSort(file.getAbsolutePath());
            tempFiles.get(tempFiles.size() - 1).delete();
            File[] filesTemp = file.listFiles();
            int tempSize = 0;
            for (int i = 0; i < filesTemp.length; i++) {
                tempSize += files[i].length();
            }
            dirSize = tempSize;

        }
    }


    public static void updateFileTime(String dir, String fileName) {
        File file = new File(dir, fileName);
        long newModifiedTime = System.currentTimeMillis();
        file.setLastModified(newModifiedTime);
    }


    public static List<File> getFileSort(String path) {
        List<File> list = getFiles(path, new ArrayList<File>());
        if (list != null && list.size() > 0) {
            Collections.sort(list, new Comparator<File>() {
                public int compare(File file, File newFile) {
                    if (file.lastModified() < newFile.lastModified()) {
                        return 1;
                    } else if (file.lastModified() == newFile.lastModified()) {
                        return 0;
                    } else {
                        return -1;
                    }

                }
            });

        }
        return list;
    }

    public static List<File> getFileSort1(String path) {
        List<File> list = getFiles(path, new ArrayList<File>());
        if (list != null && list.size() > 0) {
            Collections.sort(list, new Comparator<File>() {
                public int compare(File file, File newFile) {
                    long fileTime = CommonTool.getSavaTime(file.getName());
                    long newFileTime = CommonTool.getSavaTime(newFile.getName());
                    if (fileTime < newFileTime) {
                        return 1;
                    } else if (fileTime == newFileTime) {
                        return 0;
                    } else {
                        return -1;
                    }

                }
            });

        }
        return list;
    }

    public static List<File> getFiles(String realpath, List<File> files) {
        File realFile = new File(realpath);
        if (realFile.isDirectory()) {
            File[] subfiles = realFile.listFiles();
            for (File file : subfiles) {
                if (file.isDirectory()) {
                    getFiles(file.getAbsolutePath(), files);
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }


    private static void reColor(int[] data) {
        for (int i = 0; i < data.length; ++i) {
            if (data[i] == 0) {
                data[i] = 0xaaaaaa;
            } else if (data[i] == -127) { // 是墙体
                data[i] = 0x71174D;
            } else {
                data[i] = 0xFFFFFF;
            }
        }
    }

    public interface RecoveryMapDataInterface {
        void recoveryMapDataSucceed();

        void recoveryMapDataFailed();
    }

    /**
     * 恢复地图
     */
    public void recoveryMapData(Context context, MapDataBean mapDataBean, RecoveryMapDataInterface mapDataInterfacen) {
        Map map = new Map(mapDataBean.getOrigin(), mapDataBean.getDimension(), mapDataBean.getResolution(), mapDataBean.getTimestamp(), Base64.decode(mapDataBean.getData(), Base64.NO_WRAP));

        List<Line> wallsList = new ArrayList<>();
        List<MapDataBean.MapWalls> walls = mapDataBean.getWallsData();
        if (walls != null) {
            for (int i = 0; i < walls.size(); i++) {
                Line line = new Line(mapDataBean.getWallsData().get(i).getSegmentId(), mapDataBean.getWallsData().get(i).getLine_startPoint_x(), mapDataBean.getWallsData().get(i).getLine_startPoint_y(),
                        mapDataBean.getWallsData().get(i).getLine_endPoint_x(), mapDataBean.getWallsData().get(i).getLine_endPoint_y());
                wallsList.add(line);
            }
        }

        if (null != mapDataBean && null != map) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(Static.CONTEXT.getResources().getString(R.string.enture_restore_map));
            builder.setNegativeButton(Static.CONTEXT.getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mapDataInterfacen != null) {
                        mapDataInterfacen.recoveryMapDataFailed();
                    }
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(Static.CONTEXT.getResources().getString(R.string.restore), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (null != map) {
                        SnowBotMoveServer.getInstance().recoveryMapData(map, wallsList);
                        if (mapDataInterfacen != null) {
                            mapDataInterfacen.recoveryMapDataSucceed();
                        }
//                        CSJToast.showToast(context, Static.CONTEXT.getResources().getString(R.string.restore_map_succeed));
                    }
                    dialog.dismiss();
                }
            });
            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    /**
     * 保存map
     */
    public static void saveMap() {
        //虚拟墙
        List<Line> wallsList = SnowBotMoveServer.getInstance().getBackUpWalls();
        List<MapDataBean.MapWalls> mapWalls = new ArrayList<>();

        if (wallsList == null) {
            return;
        }

        //实例化当前需要存储的数据
        for (int w = 0; w < wallsList.size(); w++) {
            MapDataBean.MapWalls mapWall = new MapDataBean.MapWalls();
            mapWall.setSegmentId(wallsList.get(w).getSegmentId());
            mapWall.setLine_startPoint_x(wallsList.get(w).getStartX());
            mapWall.setLine_startPoint_y(wallsList.get(w).getStartY());
            mapWall.setLine_endPoint_x(wallsList.get(w).getEndX());
            mapWall.setLine_endPoint_y(wallsList.get(w).getEndY());
            mapWalls.add(mapWall);
        }
        //地图
        Map map = SnowBotMoveServer.getInstance().getBackUpMap();
        if (null != map) {
            MapDataBean mapDataBean = new MapDataBean();
            mapDataBean.setDimension(map.getDimension());
//            mapDataBean.setCreateTime(System.currentTimeMillis());
            mapDataBean.setOrigin(map.getOrigin());
            mapDataBean.setResolution(map.getResolution());
            mapDataBean.setTimestamp(map.getTimestamp());
            mapDataBean.setData(Base64.encodeToString(map.getData(), Base64.NO_WRAP));
            mapDataBean.setWallsData(mapWalls);
            saveMapToSD(mapDataBean);
            saveMapToLastDir(mapDataBean);
        }
    }
}
