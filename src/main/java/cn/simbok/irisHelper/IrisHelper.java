package cn.simbok.irisHelper;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Simbok Iris Model SDK
 *
 * @author Beijing Simbok Intelligent Technology Co., Ltd
 */
public class IrisHelper {
    // Delayed library loading flag
    private static boolean libraryLoaded = false;

    // Delayed load JNI library to avoid startup crash
    private static synchronized void loadLibrary() {
        if (!libraryLoaded) {
            System.loadLibrary("irisenginehelper");
            libraryLoaded = true;
        }
    }

    // Constructor automatically loads library
    public IrisHelper() {
        loadLibrary();
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

        int onIdentifyFinishedCallback(
                String name,
                int result,
                int whichEye,
                int finished
        );
    }

    static int cbInit(int event, int param) {
        if (mInvoker != null) {
            return mInvoker.onInitCallback(event, param);
        } else {
            return _cbInitFunc.invoke(event, param);
        }
    }

    static int cbEnroll(String name, int result, int eye, int finish) {
        if (mInvoker != null) {
            return mInvoker.onEnrollFinishedCallback(name, result, eye, finish);
        } else {
            return _cbEnrollFunc.invoke(name, result, eye, finish);
        }
    }

    static int cbIdentify(String name, int result, int eye, int finish) {
        if (mInvoker != null) {
            return mInvoker.onIdentifyFinishedCallback(name, result, eye, finish);
        } else {
            return _cbIdentifyFunc.invoke(name, result, eye, finish);
        }
    }

    static int cbPreview(byte[] frame, int width, int height) {
        if (mInvoker != null) {
            return mInvoker.onPreviewCallback(frame, width, height);
        } else {
            if (_cbPreviewFunc == null) {
                System.out.println("[IrisHelper] WARNING: _cbPreviewFunc is null!");
                return -1;
            }
            return _cbPreviewFunc.invoke(frame, width, height);
        }
    }

    static int cbPlaySound(int id) {
        if (_notification == null) return -1;
        return _notification.play(id) == 0 ? 0 : -1;
    }

    static int cbCheckHardware(@Nullable String name, int result, int eye, int finish) {
        return _cbCheckHardwareFunc.invoke(name, result, eye, finish);
    }

//    public static String getDefaultConfig() {
//         try {
//             InputStream ips = ctx.getAssets().open("config/irisengine.json");
//             ByteBuffer buf = ByteBuffer.allocate(ips.available());
//             ips.read(buf.array());
//             return new String(buf.array(), StandardCharsets.UTF_8);
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//
//        return "";
//    }

//     private JSONObject getSpecificConfig(String specific) {
//     try {
//         InputStream ips = ctx.getAssets().open("config/specific.json");
//         ByteBuffer buf = ByteBuffer.allocate(ips.available());
//         ips.read(buf.array());
//         String s = new String(buf.array(), StandardCharsets.UTF_8);
//         JSONObject j = new JSONObject(s);
//
//         return j.has(specific) ? j.getJSONObject(specific) : null;
//     } catch (IOException | JSONException e) {
//         e.printStackTrace();
//     }
//
//         return null;
//     }

    public static String PREVIEWMODE_WHOLE = "whole";
    public static String PREVIEWMODE_IRIS = "iris";
    public static String PREVIEWMODE_CUSTOM = "custom";

    public static String FMT_JPG = "jpg";
    public static String FMT_BMP = "bmp";

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

    // @Nullable
    // private JSONObject mEngineInfo = null;

    /**
     * init iris engine and get iris model ready to use.
     *
     * @param config       custom configuration pass to iris engine
     * @param notification notify when some action happen
     * @param iif          callback collections of specific iris operation like enroll/identify/preview
     * @return 0 success, other fail
     * @throws EmptyDeviceInfoException iris model information query failed
     */
    public int init(
            String config,
            @Nullable IrisNotificationInterface notification,
            @Nullable IrisInvokeInterface iif
    ) throws EmptyDeviceInfoException {
        loadLibrary(); // Load JNI library on first init call
        mInvoker = iif;
        _notification = notification;
        return IrisEngineInit(config);
    }

    /**
     * init iris engine and get iris model ready to use.
     *
     * @param config       custom configuration pass to iris engine
     * @param notification notify when some action happen
     * @param cbfunc       callback function of init
     * @return 0 success, other fail
     * @throws EmptyDeviceInfoException iris model information query failed
     */
    public int init(
            String config,
            @Nullable IrisNotificationInterface notification,
            @Nullable Function2<Integer, Integer, Integer> cbfunc
    ) throws EmptyDeviceInfoException {
        loadLibrary(); // Load JNI library on first init call
        _cbInitFunc = cbfunc;
        _notification = notification;
        return IrisEngineInit(config);
    }

    /**
     * add user to iris engine with iris features.
     *
     * @param uid        user id
     * @param iris_left  left iris feature
     * @param iris_right right iris feature
     * @return 0 success, other fail.
     */
    public int addIrisUser(String uid, byte[] iris_left, byte[] iris_right) {
        if (uid.isEmpty()) {
            // Log.w(TAG, "empty uid while add iris user");
            return -1;
        }
        if (iris_left == null && iris_right == null) return -1;

        if (iris_left != null && iris_left.length != 1500) return -2;
        if (iris_right != null && iris_right.length != 1500) return -2;

        return IrisEngineAddIrisUser(uid, iris_left, iris_right);
    }

    /**
     * change iris engine configuration
     *
     * @param config the change config
     * @return 0 success, other fail
     */
    public int changeConfigure(String config) {
        return IrisEngineChangeConfigure(config);
    }

    /**
     * load device specific parameters
     *
     * @param devParams
     * @return 0 success, other fail
     */
    public int loadDevParams(String devParams) {
        return IrisEngineLoadDevParams(devParams);
    }

    /**
     * stop iris engine.
     *
     * @return 0 success, other fail.
     */
    public int stop() {
        return IrisEngineStop();
    }

    /**
     * start model to enroll a user.
     *
     * @param uid    user id.
     * @param cover  cover if uid exists.
     * @param cbFunc callback function if init engine without IrisInvokeInterface
     * @return 0 success, other fail.
     */
    public int enroll(String uid, Boolean cover, @Nullable Function4<String, Integer, Integer, Integer, Integer> cbFunc) {
        if (mInvoker == null) _cbEnrollFunc = cbFunc;

        List<String> existList = getUserList();
        if (!cover && existList.contains(uid)) return 25;

        return IrisEngineEnroll(uid, cover ? 1 : 0);
    }


    /**
     * start iris model to identify
     *
     * @param uid        specific to identify a user, or null for detection
     * @param continuous false for one-shot identify, true for continuous identify
     * @param cbFunc     callback when detected or one-shot identify timeout
     * @return 0 success, other fail
     */
    public int identify(
            @Nullable String uid,
            Boolean continuous,
            @Nullable Function4<String, Integer, Integer, Integer, Integer> cbFunc
    ) {
        if (mInvoker == null) _cbIdentifyFunc = cbFunc;
        return IrisEngineIdentify(uid, continuous ? 1 : 0);
    }

    /**
     * get user list of iris engine
     *
     * @return list of uid
     */
    public List<String> getUserList() {
        ArrayList<String> userList = new ArrayList<>();
        String[] sList = IrisEngineGetUserList().split(",");
        for (String s : sList) {
            if (!Objects.equals(s, "")) userList.add(s);
        }

        return userList;
    }

    /**
     * delete user
     *
     * @param uid uid to delete
     * @return 0 success, other fail
     */
    public int deleteUser(String uid) {
        if (Objects.equals(uid, "")) return 0;
        return IrisEngineDeleteUser(uid);
    }

    /**
     * delete all user
     *
     * @return 0 success, other fail
     */
    public int deleteAllUser() {
        return IrisEngineDeleteAllUser();
    }

    @Deprecated
    public int setPreview(
            String mode,
            int width,
            int height,
            int marginX,
            int marginY,
            @Nullable Function3<byte[], Integer, Integer, Integer> cbFunc
    ) {
        if (mInvoker == null) _cbPreviewFunc = cbFunc;
        return IrisEngineSetPreview(mode, 2, width, height, marginX, marginY);
    }

    /**
     * set iris preview callback function
     *
     * @param cbFunc callback function of preview
     * @return 0 success, other fail
     */
    public int setPreview(@Nullable Function3<byte[], Integer, Integer, Integer> cbFunc) {
        System.out.println("[IrisHelper] setPreview called. mInvoker: " + (mInvoker == null ? "null" : "not null") + ", cbFunc: " + (cbFunc == null ? "null" : "not null"));
        if (mInvoker == null) {
            _cbPreviewFunc = cbFunc;
            System.out.println("[IrisHelper] _cbPreviewFunc set to cbFunc");
        }
        int result = IrisEngineSetPreview(PREVIEWMODE_WHOLE, 2, 0, 0, 0, 0);
        System.out.println("[IrisHelper] IrisEngineSetPreview result: " + result);
        return result;
    }

    /**
     * stop preview
     *
     * @return 0 success, other fail
     */
    public int stopPreview() {
        return IrisEngineStopPreview();
    }

    @Deprecated
    public int pauseAction() {
        return IrisEnginePauseAction();
    }

    @Deprecated
    public int resumeAction() {
        return IrisEngineResumeAction();
    }

    /**
     * release iris engine and iris model
     */
    public void release() {
        IrisEngineRelease();
    }

    /**
     * release iris engine and iris model
     *
     * @param releaseData keep or not of iris data after release
     */
    public void release2(boolean releaseData) {
        IrisEngineRelease2(releaseData);
    }

    @Deprecated
    public int sort(String l) {
        return IrisEngineSort(l);
    }

    /**
     * get device information
     *
     * @return device information
     * @throws EmptyDeviceInfoException empty info
     */
    public String getDeviceInfo() throws EmptyDeviceInfoException {
        String s = IrisEngineGetDeviceInfo2();
        if (s == null) throw new EmptyDeviceInfoException();
        return s;
    }

    /**
     * get iris engine runtime information
     *
     * @return JSON string of runtime information
     */
    public String getRuntimeInfo() {
        return IrisEngineGetRuntimeInfo();
    }

    /**
     * get iris engine information
     *
     * @return JSON string of engine information
     */
    public String getEngineInfo() {
        return IrisEngineGetEngineInfo();
    }

    /**
     * get iris feature of image
     *
     * @param frame  iris bitmap
     * @param format format
     * @return iris feature
     */
    public byte[] getIrisCode(byte[] frame, String format) {
        return IrisEngineGetIrisCode(frame, frame.length, format);
    }

    @Nullable
    public String getZBIOInfo(byte[] frame, int size, String format) {
        return IrisEngineGetZBIOInfo(frame, size, format);
    }

    /**
     * start preview
     *
     * @return 0 success, other fail
     */
    public int startPurePreview() {
        return IrisEngineStartPurePreview();
    }

    /**
     * stop preview
     *
     * @return 0 success, other fail
     */
    public int stopPurePreview() {
        return IrisEngineStopPurePreview();
    }

    /**
     * get last enroll user's data
     *
     * @param uid last enroll user
     * @return Json struct contains last enroll data
     */
    @Nullable
    public String getEnrollData(String uid) {
        return IrisEngineGetEnrollData(uid);
    }

    /**
     * get last identify user's data
     *
     * @return Json struct contains lasat identify data
     */
    @Nullable
    public String getIdentifyData() {
        return IrisEngineGetIdentifyData();
    }

    /**
     * update connected iris model with firmware
     *
     * @param firmware update firmware
     * @return 0 success, other fail
     */
    public int updateFirmware(byte[] firmware) {
        return IrisEngineUpdateFirmware(firmware);
    }

    /**
     * test iris model hardware
     *
     * @param cbFunc callback function after check
     * @return 0 success, other fail
     */
    public int checkHardware(Function4<String, Integer, Integer, Integer, Integer> cbFunc) {
        _cbCheckHardwareFunc = cbFunc;
        return IrisEngineCheckHardware();
    }

    private final Map<Integer, String> errCodeStr = new HashMap<Integer, String>() {
        {
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
        }
    };

    public String err2str(int err) {
        return errCodeStr.getOrDefault(err, "code $err not found");
    }

    public Boolean isTemplateUpToDate(byte[] tpl) {
        if (tpl.length != 1500) return false;

        byte major = tpl[tpl.length - 13];
        byte minor = tpl[tpl.length - 12];
        byte micro = tpl[tpl.length - 11];
        String version = String.format("%d.%d%d", major, minor, micro);

        return false;
        // return version.equals(mEngineInfo.getString("algorithm"));
    }

    /**
     * get Sdk's version
     *
     * @return version of sdk
     */
    public String version() {
        return "20230608";
    }
}
