package cn.simbok.iris.helper;

import cn.simbok.iris.util.PlatformUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Simbok Iris Model SDK - Cross Platform Version
 *
 * @author Beijing Simbok Intelligent Technology Co., Ltd
 */
public class IrisHelper {
    
    // 跨平台加载JNI库
    static {
        try {
            System.loadLibrary(PlatformUtils.getLibraryName());
            System.out.println("Successfully loaded iris library on " + PlatformUtils.getOsName());
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load iris library: " + e.getMessage());
            throw e;
        }
    }

    private final String TAG = IrisHelper.class.getSimpleName();

    public static class EmptyDeviceInfoException extends Exception {
    }

    private static IrisInvokeInterface mInvoker = null;

    @Nullable
    private static IrisNotificationInterface _notification = null;

    private static Function2<Integer, Integer, Integer> _cbInitFunc;
    private static Function4<String, Integer, Integer, Integer, Integer> _cbEnrollFunc;
    private static Function4<String, Integer, Integer, Integer, Integer> _cbIdentifyFunc;
    private static Function4<String, Integer, Integer, Integer, Integer> _cbCheckHardwareFunc;
    private static Function3<byte[], Integer, Integer, Integer> _cbPreviewFunc;

    public interface IrisInvokeInterface {
        int onInitCallback(int event, int param);
        int onPreviewCallback(byte[] frame, int width, int height);
        int onEnrollFinishedCallback(String name, int result, int whichEye, int finished);
        int onIdentifyFinishedCallback(String name, int result, int whichEye, int finished);
    }

    static int cbInit(int event, int param) {
        if (mInvoker != null) {
            return mInvoker.onInitCallback(event, param);
        } else if (_cbInitFunc != null) {
            return _cbInitFunc.invoke(event, param);
        }
        return 0;
    }

    static int cbEnroll(String name, int result, int eye, int finish) {
        if (mInvoker != null) {
            return mInvoker.onEnrollFinishedCallback(name, result, eye, finish);
        } else if (_cbEnrollFunc != null) {
            return _cbEnrollFunc.invoke(name, result, eye, finish);
        }
        return 0;
    }

    static int cbIdentify(String name, int result, int eye, int finish) {
        if (mInvoker != null) {
            return mInvoker.onIdentifyFinishedCallback(name, result, eye, finish);
        } else if (_cbIdentifyFunc != null) {
            return _cbIdentifyFunc.invoke(name, result, eye, finish);
        }
        return 0;
    }

    static int cbPreview(byte[] frame, int width, int height) {
        if (mInvoker != null) {
            return mInvoker.onPreviewCallback(frame, width, height);
        } else if (_cbPreviewFunc != null) {
            return _cbPreviewFunc.invoke(frame, width, height);
        }
        return 0;
    }

    static int cbPlaySound(int id) {
        if (_notification == null) return -1;
        return _notification.play(id) == 0 ? 0 : -1;
    }

    static int cbCheckHardware(@Nullable String name, int result, int eye, int finish) {
        if (_cbCheckHardwareFunc != null) {
            return _cbCheckHardwareFunc.invoke(name, result, eye, finish);
        }
        return 0;
    }

    public static String PREVIEWMODE_WHOLE = "whole";
    public static String PREVIEWMODE_IRIS = "iris";
    public static String PREVIEWMODE_CUSTOM = "custom";

    public static String FMT_JPG = "jpg";
    public static String FMT_BMP = "bmp";

    // Native methods
    private native int IrisEngineInit(String config);
    private native int IrisEngineAddIrisUser(String uid, byte[] irisL, byte[] irisR);
    private native int IrisEngineStop();
    private native int IrisEngineEnroll(String name, int cover);
    private native int IrisEngineIdentify(@Nullable String name, int continuous);
    private native String IrisEngineGetUserList();
    private native int IrisEngineDeleteUser(String user);
    private native int IrisEngineDeleteAllUser();
    private native int IrisEngineSetPreview(String mode, int format, int width, int height, int marginX, int marginY);
    private native int IrisEngineStartPurePreview();
    private native int IrisEngineStopPurePreview();
    private native int IrisEngineStopPreview();
    private native int IrisEnginePauseAction();
    private native int IrisEngineResumeAction();
    private native int IrisEngineChangeConfigure(String config);
    private native int IrisEngineLoadDevParams(String devParams);
    private native void IrisEngineRelease();
    private native void IrisEngineRelease2(boolean r);
    @Nullable
    private native String IrisEngineGetDeviceInfo2();
    private native String IrisEngineGetEngineInfo();
    private native String IrisEngineGetRuntimeInfo();
    @Nullable
    private native String IrisEngineGetEnrollData(String uid);
    @Nullable
    private native String IrisEngineGetIdentifyData();
    private native String IrisEngineGetZBIOInfo(byte[] frame, int size, String format);
    private native byte[] IrisEngineGetIrisCode(byte[] frame, int size, String format);
    private native int IrisEngineSort(String l);
    private native int IrisEngineUpdateFirmware(byte[] fw);
    private native int IrisEngineCheckHardware();

    public int init(String config, @Nullable IrisNotificationInterface notification,
                    @Nullable IrisInvokeInterface iif) throws EmptyDeviceInfoException {
        mInvoker = iif;
        _notification = notification;
        return IrisEngineInit(config);
    }

    public int init(String config, @Nullable IrisNotificationInterface notification,
                    @Nullable Function2<Integer, Integer, Integer> cbfunc) throws EmptyDeviceInfoException {
        _cbInitFunc = cbfunc;
        _notification = notification;
        return IrisEngineInit(config);
    }

    public int addIrisUser(String uid, byte[] iris_left, byte[] iris_right) {
        if (uid.isEmpty()) return -1;
        if (iris_left == null && iris_right == null) return -1;
        if (iris_left != null && iris_left.length != 1500) return -2;
        if (iris_right != null && iris_right.length != 1500) return -2;
        return IrisEngineAddIrisUser(uid, iris_left, iris_right);
    }

    public int changeConfigure(String config) {
        return IrisEngineChangeConfigure(config);
    }

    public int loadDevParams(String devParams) {
        return IrisEngineLoadDevParams(devParams);
    }

    public int stop() {
        return IrisEngineStop();
    }

    public int enroll(String uid, Boolean cover, @Nullable Function4<String, Integer, Integer, Integer, Integer> cbFunc) {
        if (mInvoker == null) _cbEnrollFunc = cbFunc;
        List<String> existList = getUserList();
        if (!cover && existList.contains(uid)) return 25;
        return IrisEngineEnroll(uid, cover ? 1 : 0);
    }

    public int identify(@Nullable String uid, Boolean continuous,
                        @Nullable Function4<String, Integer, Integer, Integer, Integer> cbFunc) {
        if (mInvoker == null) _cbIdentifyFunc = cbFunc;
        return IrisEngineIdentify(uid, continuous ? 1 : 0);
    }

    public List<String> getUserList() {
        ArrayList<String> userList = new ArrayList<>();
        String result = IrisEngineGetUserList();
        if (result != null && !result.isEmpty()) {
            String[] sList = result.split(",");
            for (String s : sList) {
                if (!Objects.equals(s, "")) userList.add(s);
            }
        }
        return userList;
    }

    public int deleteUser(String uid) {
        if (Objects.equals(uid, "")) return 0;
        return IrisEngineDeleteUser(uid);
    }

    public int deleteAllUser() {
        return IrisEngineDeleteAllUser();
    }

    public int setPreview(@Nullable Function3<byte[], Integer, Integer, Integer> cbFunc) {
        if (mInvoker == null) _cbPreviewFunc = cbFunc;
        return IrisEngineSetPreview(PREVIEWMODE_WHOLE, 2, 0, 0, 0, 0);
    }

    public int stopPreview() {
        return IrisEngineStopPreview();
    }

    public void release() {
        IrisEngineRelease();
    }

    public void release2(boolean releaseData) {
        IrisEngineRelease2(releaseData);
    }

    public String getDeviceInfo() throws EmptyDeviceInfoException {
        String s = IrisEngineGetDeviceInfo2();
        if (s == null) throw new EmptyDeviceInfoException();
        return s;
    }

    public String getRuntimeInfo() {
        return IrisEngineGetRuntimeInfo();
    }

    public String getEngineInfo() {
        return IrisEngineGetEngineInfo();
    }

    @Nullable
    public String getEnrollData(String uid) {
        return IrisEngineGetEnrollData(uid);
    }

    @Nullable
    public String getIdentifyData() {
        return IrisEngineGetIdentifyData();
    }

    public int checkHardware(Function4<String, Integer, Integer, Integer, Integer> cbFunc) {
        _cbCheckHardwareFunc = cbFunc;
        return IrisEngineCheckHardware();
    }

    private final Map<Integer, String> errCodeStr = new HashMap<Integer, String>() {{
        put(0, "OK");
        put(1, "未发现虹膜设备");
        put(2, "DEV_CONNECT");
        put(3, "非法虹膜设备");
        put(4, "DEV_CAPTURE");
        put(6, "DEV_HOTPLUG_ENABLE");
        put(7, "DEV_NOT_INIT");
        put(8, "DEV_ALREADY_INIT");
        put(9, "DEV_CONFIG");
        put(10, "DEV_LOG");
        put(11, "DEV_EU_NOT_REG");
        put(12, "DEV_EU_CTRL");
        put(15, "虹膜设备已过授权使用期");
        put(20, "NOT_INIT");
        put(21, "ALREADY_INIT");
        put(22, "WRONG_CONFIG");
        put(23, "MAXIMUM_USER_NUM");
        put(24, "USER_ID_ILLEGAL");
        put(25, "USER_ID_EXIST");
        put(26, "NO_USER");
        put(27, "ALREADY_RUN");
        put(28, "IRIS_ENG_ERR_USER_NOT_EXIST");
        put(30, "IRIS_ENG_ERR_SOUND_PLAY");
        put(50, "SYSTEM");
    }};

    public String err2str(int err) {
        return errCodeStr.getOrDefault(err, "unknown error code: " + err);
    }

    public String version() {
        return "20230608";
    }
}

