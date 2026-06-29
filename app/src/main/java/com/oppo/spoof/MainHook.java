package com.oppo.spoof;

import android.os.Build;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainHook implements IXposedHookLoadPackage {

    private static final String CONFIG_PATH = "/sdcard/oppo-spoof/config.txt";

    // ─── 预设机型库 ───
    private static final Map<String, DeviceProfile> PRESETS = new LinkedHashMap<>();
    static {
        PRESETS.put("oneplus_ace6t", new DeviceProfile(
                "OnePlus", "OnePlus", "ACE6T", "ACE6T", "ACE6T", "qcom",
                "OnePlus/ACE6T/ACE6T:15/AP3A.240905.015.A1/01092349:user/release-keys",
                "15", 35, "AP3A.240905.015.A1"
        ));
        PRESETS.put("oneplus_13", new DeviceProfile(
                "OnePlus", "OnePlus", "PJZ110", "PJZ110", "PJZ110", "qcom",
                "OnePlus/PJZ110/PJZ110:15/AQ3A.240912.001/01092349:user/release-keys",
                "15", 35, "AQ3A.240912.001"
        ));
        PRESETS.put("oppo_find_x8", new DeviceProfile(
                "OPPO", "OPPO", "PKC110", "PKC110", "PKC110", "mt6897",
                "OPPO/PKC110/PKC110:15/AP3A.240905.015.A1/01092349:user/release-keys",
                "15", 35, "AP3A.240905.015.A1"
        ));
        PRESETS.put("xiaomi_14", new DeviceProfile(
                "Xiaomi", "Xiaomi", "23127PN0CC", "houji", "houji", "qcom",
                "Xiaomi/houji/houji:14/UKQ1.230804.001/V816.0.8.0.UNCCNXM:user/release-keys",
                "14", 34, "UKQ1.230804.001"
        ));
        PRESETS.put("huawei_mate60pro", new DeviceProfile(
                "HUAWEI", "HUAWEI", "ALN-AL00", "ALN-AL00", "ALN-AL00", "kirin9000s",
                "HUAWEI/ALN-AL00/HWALN:12/HUAWEIALN-AL00/103.0.0.168:user/release-keys",
                "12", 31, "103.0.0.168"
        ));
        PRESETS.put("samsung_s24ultra", new DeviceProfile(
                "samsung", "samsung", "SM-S9280", "q5q", "q5qzcx", "qcom",
                "samsung/q5qzcx/q5q:14/UP1A.231005.007/S9280ZCU1AXB7:user/release-keys",
                "14", 34, "UP1A.231005.007"
        ));
        PRESETS.put("vivo_x100pro", new DeviceProfile(
                "vivo", "vivo", "V2309A", "V2309A", "V2309A", "mt6989",
                "vivo/V2309A/V2309A:14/UP1A.231005.007/compiler11232138:user/release-keys",
                "14", 34, "UP1A.231005.007"
        ));
    }

    private static DeviceProfile profile;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (profile == null) {
            profile = loadProfile();
            XposedBridge.log("[OPPO-Spoof] 模块已加载，当前预设: " +
                    profile.brand + " " + profile.model + " | Android SDK: " + Build.VERSION.SDK_INT);
        }

        XposedBridge.log("[OPPO-Spoof] Hook -> " + lpparam.packageName);

        // 注意：Android 14+ 反射修改 Build final 字段会被 ART 拦截
        // 所以我们主要依赖 SystemProperties hook
        hookSystemProperties(lpparam);
    }

    // ─── 读取配置 ───
    private DeviceProfile loadProfile() {
        File configFile = new File(CONFIG_PATH);
        String presetName = null;
        Map<String, String> overrides = new LinkedHashMap<>();

        if (configFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eq = line.indexOf('=');
                    if (eq <= 0) continue;
                    String key = line.substring(0, eq).trim();
                    String val = line.substring(eq + 1).trim();
                    if ("preset".equals(key)) {
                        presetName = val;
                    } else {
                        overrides.put(key, val);
                    }
                }
            } catch (Exception e) {
                XposedBridge.log("[OPPO-Spoof] 读取配置失败: " + e.getMessage());
            }
        }

        DeviceProfile selected;
        if (presetName != null && PRESETS.containsKey(presetName)) {
            selected = PRESETS.get(presetName).clone();
        } else {
            selected = PRESETS.get("oneplus_ace6t").clone();
        }

        for (Map.Entry<String, String> e : overrides.entrySet()) {
            applyOverride(selected, e.getKey(), e.getValue());
        }

        return selected;
    }

    private void applyOverride(DeviceProfile p, String key, String value) {
        switch (key) {
            case "brand":        p.brand        = value; break;
            case "manufacturer": p.manufacturer = value; break;
            case "model":        p.model        = value; break;
            case "device":       p.device       = value; break;
            case "product":      p.product      = value; break;
            case "hardware":     p.hardware     = value; break;
            case "fingerprint":  p.fingerprint  = value; break;
            case "release":      p.release      = value; break;
            case "display":      p.display      = value; break;
            case "sdk":          try { p.sdk = Integer.parseInt(value); } catch (Exception ignored) {} break;
        }
    }

    // ─── Hook SystemProperties（主 Hook 路径，兼容 Android 10-16） ───
    private void hookSystemProperties(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> sp = XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader);

            // get(String)
            XposedHelpers.findAndHookMethod(sp, "get", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    handleProp((String) param.args[0], param);
                }
            });

            // get(String, String)
            XposedHelpers.findAndHookMethod(sp, "get", String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    handleProp((String) param.args[0], param);
                }
            });

            // getInt(String, int)
            XposedHelpers.findAndHookMethod(sp, "getInt", String.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if ("ro.build.version.sdk".equals(param.args[0])) {
                        param.setResult(profile.sdk);
                    }
                }
            });

            XposedBridge.log("[OPPO-Spoof] " + lpparam.packageName + " SystemProperties hook OK");
        } catch (Throwable e) {
            XposedBridge.log("[OPPO-Spoof] SystemProperties hook FAIL: " + e.getClass().getName() + " " + e.getMessage());
        }
    }

    private void handleProp(String key, XC_MethodHook.MethodHookParam param) {
        if (key == null) return;
        switch (key) {
            case "ro.product.manufacturer":   param.setResult(profile.manufacturer); break;
            case "ro.product.brand":          param.setResult(profile.brand);        break;
            case "ro.product.model":          param.setResult(profile.model);        break;
            case "ro.product.device":         param.setResult(profile.device);       break;
            case "ro.product.name":           param.setResult(profile.product);      break;
            case "ro.product.board":          param.setResult(profile.product);      break;
            case "ro.hardware":               param.setResult(profile.hardware);     break;
            case "ro.build.version.sdk":      param.setResult(String.valueOf(profile.sdk)); break;
            case "ro.build.version.release":  param.setResult(profile.release);      break;
            case "ro.build.fingerprint":      param.setResult(profile.fingerprint);  break;
            case "ro.build.display.id":       param.setResult(profile.display);      break;
            case "ro.build.product":          param.setResult(profile.product);      break;
        }
    }

    // ─── 数据类 ───
    private static class DeviceProfile implements Cloneable {
        String brand, manufacturer, model, device, product, hardware;
        String fingerprint, release, display;
        int sdk;

        DeviceProfile(String b, String mf, String mo, String dv, String pr,
                      String hw, String fp, String rl, int sk, String dp) {
            brand = b; manufacturer = mf; model = mo; device = dv;
            product = pr; hardware = hw; fingerprint = fp;
            release = rl; sdk = sk; display = dp;
        }

        @Override
        protected DeviceProfile clone() {
            try { return (DeviceProfile) super.clone(); } catch (Exception e) { return null; }
        }
    }
}
