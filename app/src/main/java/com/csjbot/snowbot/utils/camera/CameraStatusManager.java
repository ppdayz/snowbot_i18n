package com.csjbot.snowbot.utils.camera;

import android.hardware.Camera;

import com.csjbot.csjbase.log.Csjlogger;

/**
 * Created by xiasuhuei321 on 2017/8/7.
 * author:luo
 * e-mail:xiasuhuei321@163.com
 * <p>
 * 主要用来管理相机状态！程序一直被相机的问题引发crash！
 * 目前用来管理LauncherActivity和TourGuideActivity的相机状态
 */

public class CameraStatusManager {
    // 相机是否是开启状态
    private boolean isOpen = false;
    // 相机正在释放资源
    private boolean isRelease = false;
    // 相机正在打开，但是还未真正打开
    private boolean openFuture = false;

    private static class CameraManageHolder {
        private static final CameraStatusManager INSTANCE = new CameraStatusManager();
    }

    private CameraStatusManager() {

    }

    public static CameraStatusManager getInstance() {
        return CameraManageHolder.INSTANCE;
    }

    /**
     * 检查是否有相机
     *
     * @return 如果有返回true，没有false
     */
    public boolean checkHasCamera() {
        boolean ret = false;
        Camera camera = null;
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            ret = true;
        } catch (RuntimeException e) {
            Csjlogger.error(e);
        } finally {
            if (camera != null) {
                camera.release();
            }
        }

        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            ret = true;
        } catch (RuntimeException e) {
            Csjlogger.error(e);
        } finally {
            if (camera != null) {
                camera.release();
            }
        }

        return ret;
    }

    public boolean canOpen() {
        if (!isOpen && openFuture) {
            // 虽然未打开，但是正在尝试打开相机，此时不可再打开
            return false;
        }
        if (isOpen) {
            // 如果已经打开，不可再次打开
            return false;
        }

        return true;
    }

    public void clear() {
        isOpen = false;
        isRelease = false;
        openFuture = false;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean isRelease() {
        return isRelease;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public void setRelease(boolean release) {
        isRelease = release;
    }

    public boolean isOpenFuture() {
        return openFuture;
    }

    public void setOpenFuture(boolean openFuture) {
        this.openFuture = openFuture;
    }


}
