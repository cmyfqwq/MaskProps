package com.oppo.spoof;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsActivity extends Activity {

    private LinearLayout cardContainer;
    private SharedPreferences prefs;
    private TextView tvPresetSummary;

    // ─── 预设机型 ───
    private static final Map<String, String[]> PRESETS = new LinkedHashMap<>();
    static {
        PRESETS.put("oneplus_ace6t",     new String[]{"OnePlus","OnePlus",    "ACE6T","ACE6T","ACE6T","qcom",     "OnePlus/ACE6T/ACE6T:15/AP3A.240905.015.A1/01092349:user/release-keys","15","35","AP3A.240905.015.A1"});
        PRESETS.put("oneplus_13",        new String[]{"OnePlus","OnePlus",    "PJZ110","PJZ110","PJZ110","qcom",  "OnePlus/PJZ110/PJZ110:15/AQ3A.240912.001/01092349:user/release-keys","15","35","AQ3A.240912.001"});
        PRESETS.put("oppo_find_x8",      new String[]{"OPPO",   "OPPO",       "PKC110","PKC110","PKC110","mt6897","OPPO/PKC110/PKC110:15/AP3A.240905.015.A1/01092349:user/release-keys","15","35","AP3A.240905.015.A1"});
        PRESETS.put("xiaomi_14",         new String[]{"Xiaomi", "Xiaomi",     "23127PN0CC","houji","houji","qcom","Xiaomi/houji/houji:14/UKQ1.230804.001/V816.0.8.0.UNCCNXM:user/release-keys","14","34","UKQ1.230804.001"});
        PRESETS.put("huawei_mate60pro",  new String[]{"HUAWEI", "HUAWEI",    "ALN-AL00","ALN-AL00","ALN-AL00","kirin9000s","HUAWEI/ALN-AL00/HWALN:12/HUAWEIALN-AL00/103.0.0.168:user/release-keys","12","31","103.0.0.168"});
        PRESETS.put("samsung_s24ultra",  new String[]{"samsung","samsung",    "SM-S9280","q5q","q5qzcx","qcom","samsung/q5qzcx/q5q:14/UP1A.231005.007/S9280ZCU1AXB7:user/release-keys","14","34","UP1A.231005.007"});
        PRESETS.put("vivo_x100pro",      new String[]{"vivo",   "vivo",       "V2309A","V2309A","V2309A","mt6989","vivo/V2309A/V2309A:14/UP1A.231005.007/compiler11232138:user/release-keys","14","34","UP1A.231005.007"});
    }

    // ─── 属性定义 ───
    private static class PropDef {
        String key, label, emoji, defaultVal;
        PropDef(String k, String l, String e, String d) {
            key = k; label = l; emoji = e; defaultVal = d;
        }
    }

    private static final PropDef[] PROPS = {
        new PropDef("brand",        "品牌",    "🏷️", "OnePlus"),
        new PropDef("manufacturer", "制造商",  "🏭", "OnePlus"),
        new PropDef("model",        "型号",    "📱", "ACE6T"),
        new PropDef("device",       "设备代号", "⚙️", "ACE6T"),
        new PropDef("product",      "产品名",  "📦", "ACE6T"),
        new PropDef("hardware",     "硬件",    "🔧", "qcom"),
        new PropDef("fingerprint",  "指纹",    "🖐️", "OnePlus/ACE6T/ACE6T:15/..."),
        new PropDef("release",      "版本号",  "🔢", "15"),
        new PropDef("sdk",          "SDK",     "💿", "35"),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("spoof_config", MODE_PRIVATE);
        cardContainer = findViewById(R.id.card_container);
        tvPresetSummary = findViewById(R.id.tv_preset_summary);

        // 预设选择按钮
        findViewById(R.id.btn_preset).setOnClickListener(v -> showPresetDialog());

        // 更新预设摘要
        updatePresetSummary();

        // 构建卡片列表
        for (PropDef prop : PROPS) {
            cardContainer.addView(buildCard(prop));
        }
    }

    // ─── 构建单张属性卡片 ───
    private View buildCard(PropDef prop) {
        // 卡片容器
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dp(8));
        card.setLayoutParams(cardParams);
        card.setBackgroundResource(R.drawable.card_bg);
        card.setPadding(dp(12), dp(10), dp(8), dp(10));

        // 第一行：图标 + 标签 + 开关
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 图标
        TextView icon = new TextView(this);
        icon.setText(prop.emoji);
        icon.setTextSize(20f);
        icon.setPadding(0, 0, dp(10), 0);
        topRow.addView(icon);

        // 标签
        TextView label = new TextView(this);
        label.setText(prop.label);
        label.setTextSize(15f);
        label.setTextColor(Color.parseColor("#212121"));
        label.setTypeface(null, Typeface.BOLD);
        topRow.addView(label);

        // 占位
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));
        topRow.addView(spacer);

        // 开关
        Switch sw = new Switch(this);
        sw.setChecked(prefs.getBoolean("enable_" + prop.key, false));
        sw.setOnCheckedChangeListener((v, checked) ->
                prefs.edit().putBoolean("enable_" + prop.key, checked).apply());
        topRow.addView(sw);

        card.addView(topRow);

        // 第二行：当前值
        String savedVal = prefs.getString("value_" + prop.key, null);
        String displayVal = (savedVal != null) ? savedVal : prop.defaultVal;
        TextView valueText = new TextView(this);
        valueText.setText(displayVal);
        valueText.setTextSize(13f);
        valueText.setTextColor(Color.parseColor("#757575"));
        valueText.setPadding(dp(30), dp(2), 0, 0);
        card.addView(valueText);

        // 给值文本绑定更新函数
        valueText.setTag(prop.key);
        valueText.setOnLongClickListener(v -> {
            showEditDialog(prop, valueText);
            return true;
        });

        return card;
    }

    // ─── 编辑对话框 ───
    private void showEditDialog(PropDef prop, TextView valueText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改 " + prop.label);

        final EditText input = new EditText(this);
        String current = prefs.getString("value_" + prop.key, "");
        input.setText(current.isEmpty() ? prop.defaultVal : current);
        input.setSelectAllOnFocus(true);
        builder.setView(input);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String val = input.getText().toString().trim();
            prefs.edit().putString("value_" + prop.key, val).apply();
            valueText.setText(val);
            Toast.makeText(this, prop.label + " 已更新", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    // ─── 预设选择对话框 ───
    private void showPresetDialog() {
        String[] names = PRESETS.keySet().toArray(new String[0]);
        String[] labels = {"一加 Ace 6T", "一加 13", "OPPO Find X8", "小米 14",
                           "华为 Mate 60 Pro", "三星 S24 Ultra", "vivo X100 Pro"};

        new AlertDialog.Builder(this)
            .setTitle("选择预设机型")
            .setItems(labels, (dialog, which) -> {
                String presetKey = names[which];
                prefs.edit().putString("preset", presetKey).apply();

                // 清空所有自定义值
                SharedPreferences.Editor ed = prefs.edit();
                for (PropDef p : PROPS) {
                    ed.remove("value_" + p.key);
                }
                ed.apply();

                // 更新显示
                cardContainer.removeAllViews();
                updatePresetSummary();
                for (PropDef p : PROPS) {
                    cardContainer.addView(buildCard(p));
                }
                Toast.makeText(this, "已切换为: " + labels[which], Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    private void updatePresetSummary() {
        String presetKey = prefs.getString("preset", "oneplus_ace6t");
        String[] vals = PRESETS.get(presetKey);
        if (vals != null) {
            // 更新各属性默认值
            int i = 0;
            for (PropDef p : PROPS) {
                if (i < vals.length) {
                    p.defaultVal = vals[i];
                }
                i++;
            }
            // 显示摘要
            String brand = vals.length > 0 ? vals[0] : "";
            String model = vals.length > 2 ? vals[2] : "";
            tvPresetSummary.setText(brand + " " + model);
        }
    }

    private int dp(int px) {
        return (int) (px * getResources().getDisplayMetrics().density);
    }
}
