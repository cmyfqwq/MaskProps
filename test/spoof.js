/*
 * 伪装为OPPO - Frida 测试脚本
 * 用法：frida -U -f <目标包名> -l spoof.js
 *
 * 示例：
 *   frida -U -f com.finshell.wallet -l spoof.js
 *   frida -U -f com.tencent.mm -l spoof.js
 */

// ─── 伪装目标设备：一加 Ace 6T ───
var FAKE = {
    MANUFACTURER: "OnePlus",
    BRAND:        "OnePlus",
    MODEL:        "PJF110",
    DEVICE:       "PJF110",
    PRODUCT:      "PJF110",
    HARDWARE:     "qcom",
    FINGERPRINT:  "OnePlus/PJF110/PJF110:15/AP3A.240905.015.A1/01092349:user/release-keys",
    DISPLAY:      "AP3A.240905.015.A1",
    SDK_INT:      35,
    RELEASE:      "15",
};

function main() {
    Java.perform(function () {

        // ─── 1. Hook Build 静态字段 ───
        var Build = Java.use("android.os.Build");
        var Build_VERSION = Java.use("android.os.Build$VERSION");

        Build.MANUFACTURER.value = FAKE.MANUFACTURER;
        Build.BRAND.value        = FAKE.BRAND;
        Build.MODEL.value        = FAKE.MODEL;
        Build.DEVICE.value       = FAKE.DEVICE;
        Build.PRODUCT.value      = FAKE.PRODUCT;
        Build.HARDWARE.value     = FAKE.HARDWARE;
        Build.FINGERPRINT.value  = FAKE.FINGERPRINT;
        Build.DISPLAY.value      = FAKE.DISPLAY;

        Build_VERSION.RELEASE.value  = FAKE.RELEASE;
        Build_VERSION.SDK_INT.value = FAKE.SDK_INT;

        console.log("[OPPO-Spoof] Build 字段已伪装");

        // ─── 2. Hook SystemProperties（关键！） ───
        try {
            var SystemProperties = Java.use("android.os.SystemProperties");

            SystemProperties.get.overload('java.lang.String').implementation = function (key) {
                var result = spoofProperty(key);
                if (result !== null) {
                    console.log("[OPPO-Spoof] get(" + key + ") → " + result);
                    return result;
                }
                return this.get(key);
            };

            SystemProperties.get.overload('java.lang.String', 'java.lang.String').implementation = function (key, def) {
                var result = spoofProperty(key);
                if (result !== null) {
                    return result;
                }
                return this.get(key, def);
            };

            SystemProperties.getInt.overload('java.lang.String', 'int').implementation = function (key, def) {
                if (key === "ro.build.version.sdk") {
                    return FAKE.SDK_INT;
                }
                return this.getInt(key, def);
            };

            console.log("[OPPO-Spoof] SystemProperties hook 成功");
        } catch (e) {
            console.log("[OPPO-Spoof] SystemProperties hook 失败: " + e);
        }

        console.log("[OPPO-Spoof] 伪装生效，包名: " + Java.use("android.app.ActivityThread").currentApplication().getPackageName());
    });
}

function spoofProperty(key) {
    switch (key) {
        case "ro.product.manufacturer": return FAKE.MANUFACTURER;
        case "ro.product.brand":        return FAKE.BRAND;
        case "ro.product.model":        return FAKE.MODEL;
        case "ro.product.device":       return FAKE.DEVICE;
        case "ro.product.product":      return FAKE.PRODUCT;
        case "ro.hardware":             return FAKE.HARDWARE;
        case "ro.build.fingerprint":     return FAKE.FINGERPRINT;
        case "ro.build.display.id":      return FAKE.DISPLAY;
        case "ro.build.version.sdk":     return String(FAKE.SDK_INT);
        case "ro.build.version.release": return FAKE.RELEASE;
        default: return null;
    }
}

// ─── 启动 ───
setTimeout(main, 100);
