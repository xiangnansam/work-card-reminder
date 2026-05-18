# 工卡提醒

一个轻量的 Android 工卡提醒 App。它会在你设定的时间提醒确认“今天带工卡了吗？”，并结合中国法定节假日和调休数据，只在法定工作日提醒。

## 功能

- 自主选择每日提醒时间
- 同步中国节假日和调休数据
- 仅在法定工作日提醒，跳过周末和节假日
- 到点弹出确认窗口：`今天带工卡了吗？`
- 点击 `带了` 后关闭提醒
- 支持手机重启后重新安排提醒
- 提供精确定时、后台提醒、应用权限入口，便于在 vivo/OPPO/小米等系统上放开提醒限制

## 技术栈

- Java
- Android SDK
- Gradle
- AlarmManager
- BroadcastReceiver
- NotificationManager
- SharedPreferences

## 项目结构

```text
app/src/main/java/com/example/workcardreminder/
├── AlarmScheduler.java      # 安排下一次工作日提醒
├── BootReceiver.java        # 手机重启后恢复提醒
├── HolidayCalendar.java     # 本地节假日缓存和工作日判断
├── HolidaySyncer.java       # 同步中国节假日数据
├── MainActivity.java        # 首页设置界面
├── ReminderActivity.java    # 弹窗提醒界面
├── ReminderNotifier.java    # 通知和震动提醒
└── ReminderReceiver.java    # 定时触发入口
```

## 构建

先确认本机已安装 Android SDK，并配置好 `ANDROID_HOME` 或 `local.properties`。

```powershell
gradle assembleDebug
```

Debug APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 真机安装

手机开启 USB 调试后连接电脑：

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

首次打开 App 后，请允许通知权限。部分国产 Android 系统还需要手动允许后台运行、自启动、后台弹出或全屏提醒权限。

## 节假日数据

节假日同步使用公开的中国节假日接口，并将结果缓存到本地。同步失败时 App 会保留本地缓存；若没有缓存，则按普通周一到周五作为兜底判断。
