package com.oppo.spoof;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainHook implements IXposedHookLoadPackage {

    private static final String PKG = "com.cmyf.oppo.spoof";
    private static final String PREFS_NAME = "spoof_config";

    // ─── 预设机型库 ───
    private static final Map<String, String[]> PRESETS = new LinkedHashMap<>();
    static {
        PRESETS.put("oneplus_ace6t",     arr("OnePlus","OnePlus",    "ACE6T","ACE6T","ACE6T","qcom",     "OnePlus/ACE6T/ACE6T:15/A3.240905.015.A1/01092349:user/release-keys","15","35","A3.240905.015.A1"));
        PRESETS.put("oneplus_13",        arr("OnePlus","OnePlus",    "PJZ110","PJZ110","PJZ110","qcom",  "OnePlus/PJZ110/PJZ110:15/AQ3A.240912.001/01092349:user/release-keys","15","35","AQ3A.240912.001"));
        PRESETS.put("oppo_find_x8",      arr("OPPO",   "OPPO",       "PKC110","PKC110","PKC110","mt6897","OPPO/PKC110/PKC110:15/AP3A.240905.015.A1/01092349:user/release-keys","15","35","AP3A.240905.015.A1"));
        PRESETS.put("xiaomi_14",         arr("Xiaomi", "Xiaomi",     "23127PN0CC","houji","houji","qcom","Xiaomi/houji/houji:14/UKQ1.230804.001/V816.0.8.0.UNCCNXM:user/release-keys","14","34","UKQ1.230804.001"));
        PRESETS.put("huawei_mate60pro",  arr("HUAWEI", "HUAWEI",    "ALN-AL00","ALN-AL00","ALN-AL00","kirin9000s","HUAWEI/ALN-AL00/HWALN:12/HUAWEIALN-AL00/103.0.0.168:user/release-keys","12","31","103.0.0.168"));
        PRESETS.put("samsung_s24ultra",  arr("samsung","samsung",    "SM-S9280","q5q","q5qzcx","qcom","samsung/q5qzcx/q5q:14/UP1A.231005.007/S9280ZCU1AXB7:user/release-keys","14","34","UP1A.231005.007"));
        PRESETS.put("vivo_x100pro",      arr("vivo",   "vivo",       "V2309A","V2309A","V2309A","mt6989","vivo/V2309A/V2309A:14/UP1A.231005.007/compiler11232138:user/release-keys","14","34","UP1A.231005.007"));
    }
    private static String[] arr(String... vs) { return vs; }

    private XSharedPreferences prefs;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        // 每次目标进程启动时重新加载配置
        prefs = new XSharedPreferences(PKG, PREFS_NAME);
        prefs.makeWorldReadable();
        prefs.reload();

        // 读取预设
        String presetKey = prefs.getString("preset", "oneplus_ace6t");
        int presetIdx = getPresetIndex(presetKey);

        XposedBridge.log("[OPPO-Spoof] Hook -> " + lpparam.packageName
                + " | preset=" + presetKey + " | idx=" + presetIdx);

        hookSystemProperties(lpparam, presetIdx);
    }

    // ─── 获取预设索引 ───
    private int getPresetIndex(String key) {
        int i = 0;
        for (String k : PRESETS.keySet()) {
            if (k.equals(key)) return i;
            i++;
        }
        return 0; // 默认 oneplus_ace6t
    }

    // ─── Hook SystemProperties ───
    private void hookSystemProperties(XC_LoadPackage.LoadPackageParam lpparam, int presetIdx) {
        try {
            Class<?> sp = XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader);

            // get(String)
            XposedHelpers.findAndHookMethod(sp, "get", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    handleProp((String) param.args[0], param, presetIdx);
                }
            });

            // get(String, String)
            XposedHelpers.findAndHookMethod(sp, "get", String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    handleProp((String) param.args[0], param, presetIdx);
                }
            });

            // getInt(String, int)
            XposedHelpers.findAndHookMethod(sp, "getInt", String.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if ("ro.build.version.sdk".equals(param.args[0])) {
                        if (prefs.getBoolean("enable_sdk", false)) {
                            String cv = prefs.getString("value_sdk", null);
                            String[] vals = getPresetVals(presetIdx);
                            param.setResult(cv != null ? Integer.parseInt(cv) : Integer.parseInt(vals[8]));
                        }
                    }
                }
            });

            XposedBridge.log("[OPPO-Spoof] SystemProperties hook OK -> " + lpparam.packageName);
        } catch (Throwable e) {
            XposedBridge.log("[OPPO-Spoof] SystemProperties hook FAIL: " + e.getMessage());
        }
    }

    private void handleProp(String key, XC_MethodHook.MethodHookParam param, int presetIdx) {
        if (key == null) return;
        String[] vals = getPresetVals(presetIdx);
        // vals: [0]brand [1]manufacturer [2]model [3]device [4]product [5]hardware [6]fingerprint [7]release [8]sdk [9]display

        switch (key) {
            case "ro.product.brand":
                if (prefs.getBoolean("enable_brand", false)) {
                    param.setResult(or(prefs.getString("value_brand", null), vals[0]));
                }
                break;
            case "ro.product.manufacturer":
                if (prefs.getBoolean("enable_manufacturer", false)) {
                    param.setResult(or(prefs.getString("value_manufacturer", null), vals[1]));
                }
                break;
            case "ro.product.model":
                if (prefs.getBoolean("enable_model", false)) {
                    param.setResult(or(prefs.getString("value_model", null), vals[2]));
                }
                break;
            case "ro.product.device":
                if (prefs.getBoolean("enable_device", false)) {
                    param.setResult(or(prefs.getString("value_device", null), vals[3]));
                }
                break;
            case "ro.product.name":
            case "ro.product.board":
            case "ro.build.product":
                if (prefs.getBoolean("enable_product", false)) {
                    param.setResult(or(prefs.getString("value_product", null), vals[4]));
                }
                break;
            case "ro.hardware":
                if (prefs.getBoolean("enable_hardware", false)) {
                    param.setResult(or(prefs.getString("value_hardware", null), vals[5]));
                }
                break;
            case "ro.build.fingerprint":
                if (prefs.getBoolean("enable_fingerprint", false)) {
                    param.setResult(or(prefs.getString("value_fingerprint", null), vals[6]));
                }
                break;
            case "ro.build.version.release":
                if (prefs.getBoolean("enable_release", false)) {
                    param.setResult(or(prefs.getString("value_release", null), vals[7]));
                }
                break;
            case "ro.build.version.sdk":
                if (prefs.getBoolean("enable_sdk", false)) {
                    String cv = prefs.getString("value_sdk", null);
                    param.setResult(String.valueOf(cv != null ? Integer.parseInt(cv) : Integer.parseInt(vals[8])));
                }
                break;
            case "ro.build.display.id":
                if (prefs.getBoolean("enable_fingerprint", false)) {
                    param.setResult(or(prefs.getString("value_fingerprint", null), vals[9]));
                }
                break;
        }
    }

    private String or(String custom, String preset) {
        return (custom != null && !custom.isEmpty()) ? custom : preset;
    }

    private String[] getPresetVals(int idx) {
        int i = 0;
        for (String[] vals : PRESETS.values()) {
            if (i++ == idx) return vals;
        }
        return PRESETS.get("oneplus_ace6t");
    }
}
