package com.example.permtest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    // 显示名 → 运行时权限
    private static final String[][] PERMS = {
        {"相机",          "android.permission.CAMERA"},
        {"麦克风(录音)",  "android.permission.RECORD_AUDIO"},
        {"精确位置",      "android.permission.ACCESS_FINE_LOCATION"},
        {"粗略位置",      "android.permission.ACCESS_COARSE_LOCATION"},
        {"通知",          "android.permission.POST_NOTIFICATIONS"},
        {"照片和视频",    "android.permission.READ_MEDIA_IMAGES"},
        {"通讯录",        "android.permission.READ_CONTACTS"},
        {"电话状态",      "android.permission.READ_PHONE_STATE"},
        {"附近设备(蓝牙)", "android.permission.BLUETOOTH_CONNECT"},
    };

    private static final int REQ_ALL = 100;
    private static final int REQ_ONE = 200;

    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = dp(16);
        int statusBar = 0;
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) statusBar = getResources().getDimensionPixelSize(resId);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF14181D);
        root.setPadding(pad, pad + statusBar, pad, pad);

        TextView title = new TextView(this);
        title.setText("权限测试  ·  " + Build.MANUFACTURER + " " + Build.MODEL
                + "  Android " + Build.VERSION.RELEASE);
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(17);
        root.addView(title);

        statusView = new TextView(this);
        statusView.setTextColor(0xFF8FD18F);
        statusView.setTextSize(14);
        statusView.setPadding(0, dp(12), 0, dp(4));
        root.addView(statusView);

        root.addView(makeButton("刷新全部状态", 0xFF39424E, new View.OnClickListener() {
            public void onClick(View v) { refreshStatus(); }
        }));

        root.addView(makeButton("一键申请全部权限", 0xFF7A3B1D, new View.OnClickListener() {
            public void onClick(View v) { requestAll(); }
        }));

        for (int i = 0; i < PERMS.length; i++) {
            final int idx = i;
            root.addView(makeButton("申请:" + PERMS[i][0], 0xFF1F3A52, new View.OnClickListener() {
                public void onClick(View v) { requestOne(idx); }
            }));
        }

        root.addView(makeButton("打开系统应用设置页(被永久拒绝时用)", 0xFF4A2A4A, new View.OnClickListener() {
            public void onClick(View v) {
                Intent it = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getPackageName(), null));
                startActivity(it);
            }
        }));

        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        setContentView(sv);

        refreshStatus();
    }

    private Button makeButton(String text, int bg, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(0xFFFFFFFF);
        b.setBackgroundColor(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(8);
        b.setLayoutParams(lp);
        b.setOnClickListener(l);
        return b;
    }

    private void requestOne(int idx) {
        String p = PERMS[idx][1];
        if (checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED) {
            statusView.setText(PERMS[idx][0] + " 此前已授权,无需再申请");
            return;
        }
        requestPermissions(new String[]{p}, REQ_ONE + idx);
    }

    private void requestAll() {
        java.util.List<String> need = new java.util.ArrayList<String>();
        for (String[] entry : PERMS) {
            if (checkSelfPermission(entry[1]) != PackageManager.PERMISSION_GRANTED) {
                need.add(entry[1]);
            }
        }
        if (need.isEmpty()) {
            statusView.setText("全部权限均已授权");
            return;
        }
        requestPermissions(need.toArray(new String[0]), REQ_ALL);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        StringBuilder sb = new StringBuilder("本次申请结果:\n");
        for (int i = 0; i < permissions.length; i++) {
            String name = displayName(permissions[i]);
            boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            sb.append(name).append(": ").append(granted ? "√ 已授权" : "× 被拒绝");
            if (!granted && !shouldShowRequestPermissionRationale(permissions[i])) {
                sb.append("(可能被设为'一律不允许',去系统设置可恢复)");
            }
            sb.append("\n");
        }
        statusView.setText(sb.toString());
    }

    private void refreshStatus() {
        StringBuilder sb = new StringBuilder("当前权限状态:\n");
        for (String[] entry : PERMS) {
            boolean granted = false;
            try {
                granted = checkSelfPermission(entry[1]) == PackageManager.PERMISSION_GRANTED;
            } catch (Exception e) {
                sb.append(entry[0]).append(": 此系统版本不支持\n");
                continue;
            }
            sb.append(entry[0]).append(": ").append(granted ? "√ 已授权" : "× 未授权").append("\n");
        }
        statusView.setText(sb.toString());
    }

    private String displayName(String permission) {
        for (String[] entry : PERMS) {
            if (entry[1].equals(permission)) return entry[0];
        }
        return permission;
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
