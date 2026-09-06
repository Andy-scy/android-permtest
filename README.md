# 权限测试 (Android Permission Tester)

一个用于测试 Android 危险权限授权流程的轻量工具 App。逐项或一键申请运行时权限,实时显示每项权限的授权状态(已授权 / 未授权 / 被永久拒绝),适用于真机调试环境验证。

覆盖权限:

- 相机 (CAMERA)
- 麦克风/录音 (RECORD_AUDIO)
- 精确位置 / 粗略位置 (ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION)
- 通知 (POST_NOTIFICATIONS,Android 13+)
- 照片和视频 (READ_MEDIA_IMAGES,Android 13+)
- 通讯录 (READ_CONTACTS)
- 电话状态 (READ_PHONE_STATE)
- 附近设备/蓝牙 (BLUETOOTH_CONNECT,Android 12+)

## 环境

- 最低支持 Android 8.0 (minSdk 26),目标 Android 15 (targetSdk 35),已在 Android 16 真机验证
- 构建:Android SDK build-tools 35.0.0 + platform android-35 + JDK 21(命令行构建,无需 Gradle)

## 命令行构建

```bat
aapt2 link -o base.apk -I <sdk>\platforms\android-35\android.jar --manifest AndroidManifest.xml
javac -encoding UTF-8 -source 1.8 -target 1.8 -bootclasspath <sdk>\platforms\android-35\android.jar -d classes MainActivity.java
jar cf classes.jar -C classes .
d8 --release --lib <sdk>\platforms\android-35\android.jar --min-api 26 --output . classes.jar
aapt add base.apk classes.dex
zipalign -f 4 base.apk aligned.apk
apksigner sign --ks <keystore> --out permtest.apk aligned.apk
```

## 安装

```bat
adb install -r permtest.apk
```
