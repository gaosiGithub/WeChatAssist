package com.standard.wechat.wechatassist;

/**
 * Created by gaosi on 2016/10/20.
 */

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.IOException;
import java.util.List;

public class AutoReplyService extends AccessibilityService {

    private final static String MM_PNAME = "com.tencent.mm";
    private final static int PARENT = 1;
    private final static int SELF = 0;
    boolean hasAction = false;
    boolean locked = false;
    boolean background = false;
    private String name;
    private String scontent;
    AccessibilityNodeInfo itemNodeinfo;
    private KeyguardManager.KeyguardLock kl;
    private Handler handler = new Handler();
    private String lastContent;
    private String currentWindow = "com.tencent.mm.ui.LauncherUI";
    private boolean isFromNotification = false;

    /**
     * 必须重写的方法，响应各种事件。
     *
     * @param event
     */
    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        int eventType = event.getEventType();
        android.util.Log.d("EVENT_TYPE", " eventType = " + eventType);
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:// 通知栏事件
                android.util.Log.d("NOTIFICATION_CHANGED", "得到通知栏事件");
                android.util.Log.d("NOTIFICATION_CHANGED", "event.className" + event.getClassName().toString());
                isFromNotification = true;
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        if (!TextUtils.isEmpty(content)) {
                            lastContent = content;
                            if (isScreenLocked()) {
                                locked = true;
                                wakeAndUnlock();
                                android.util.Log.d("maptrix", "the screen is locked");
                                if (isAppForeground(MM_PNAME)) {
                                    background = false;
                                    android.util.Log.d("maptrix", "is mm in foreground");
                                    sendNotifacationReply(event);
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            sendNotifacationReply(event);
                                            if (fill()) {
                                                send();
                                            }
                                        }
                                    }, 1000);
                                } else {
                                    background = true;
                                    android.util.Log.d("maptrix", "is mm in background");
                                    sendNotifacationReply(event);
                                    android.util.Log.d("maptrix", " currentWindow" + currentWindow);
                                    if (currentWindow.equals("com.tencent.mm.ui.LauncherUI")) {
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (fill()) {
                                                    send();
                                                }
                                            }
                                        }, 1000);
                                    }
                                }
                            } else {
                                locked = false;
                                android.util.Log.d("maptrix", "the screen is unlocked");
                                if (isAppForeground(MM_PNAME)) {
                                    background = false;
                                    android.util.Log.d("maptrix", "is mm in foreground");
                                    sendNotifacationReply(event);
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (fill()) {
                                                send();
                                            }
                                        }
                                    }, 1000);
                                } else {
                                    background = true;
                                    android.util.Log.d("maptrix", "is mm in background");
                                    sendNotifacationReply(event);
                                    android.util.Log.d("maptrix", " currentWindow" + currentWindow);
                                    if (currentWindow.equals("com.tencent.mm.ui.LauncherUI")) {
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (fill()) {
                                                    send();
                                                }
                                            }
                                        }, 1000);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:  //窗口变化监听事件
                String className = event.getClassName().toString();
//                String className = event.getClassName().toString();
                android.util.Log.d("WINDOW_STATE_CHANGED", " 得到窗口变化事件");
                android.util.Log.d("WINDOW_STATE_CHANGED", " event.getClassName：" + event.getClassName().toString());

                if (className.equals("com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI")) {
                    currentWindow = "com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI";
                }
                if(className.equals("com.tencent.mm.ui.LauncherUI")){
                    currentWindow = "com.tencent.mm.ui.LauncherUI";
                }
                if (className.equals("com.tencent.mm.ui.contact.SelectContactUI")) {
                    currentWindow = "com.tencent.mm.ui.contact.SelectContactUI";
                }

                if (className.equals("com.tencent.mm.ui.LauncherUI") && scontent != null
                        && isFromNotification) {
                    isFromNotification = false;
                    if (scontent.contains("郑州")) {
                        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                        pressBtnByViewId(1, 1, "com.tencent.mm:id/conversation_item_ll", "android.widget.LinearLayout", "", "郑州", SELF);
                        addText();
                        chatInfo(1, 2, getRootInActiveWindow());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                pressBtnByViewId(1, 3, "com.tencent.mm:id/roominfo_img", "android.widget.ImageView", "", "添加成员", PARENT);
                            }
                        }, 1000);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                chooseFriend(1, 4, "古法养生", getRootInActiveWindow());
                            }
                        }, 2000);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                pressBtnByViewId(1, 5, "com.tencent.mm:id/title_tv", "android.widget.TextView", "古法养生", "", PARENT);
                                pressBtnByViewId(1, 6, "com.tencent.mm:id/action_option_style_button", "android.widget.TextView", "确定", "", SELF);
                            }
                        }, 3000);
                    } else {
                        if (fill()) {
                            send();
                        }
                        back2Home();
                        release();
                    }
                }
                break;
        }
    }

    //判断当前聊天界面是否是群聊天
    private boolean isGroupChat(AccessibilityEvent event) {
        android.util.Log.i("isGroupChat", "nodeInfoList:" + event);
        if (event.getContentDescription() != null) {
            if (event.getContentDescription().toString().contains("(") &&
                    event.getContentDescription().toString().contains(")")) {
                return true;
            }
        }
        return false;
    }

    private void chatInfo(int type, int id, AccessibilityNodeInfo rootNode) {
        int count = rootNode.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);

            if (nodeInfo == null) {
                continue;
            }

            if (nodeInfo.getContentDescription() != null) {
                android.util.Log.i("pressBtnByViewId", "type:" + type + " id:" + id + " des:" + nodeInfo.getContentDescription().toString());
                if (nodeInfo.getClassName().toString().equals("android.widget.TextView") &&
                        nodeInfo.getContentDescription().toString().equals("聊天信息")) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
            chatInfo(1, 2, nodeInfo);
        }
    }

    //用于引起当前activity发生变化，然后可获取当前activity节点
    private void addText() {
        List<AccessibilityNodeInfo> list = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/chatting_content_et");
        android.util.Log.i("addText", "list" + list.size() + "");
        if (list != null && list.size() > 0) {
            for (AccessibilityNodeInfo n : list) {
                if (n.getClassName().equals("android.widget.EditText") && n.isEnabled()) {
                    Bundle arguments = new Bundle();
                    arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                            AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
                    arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                            true);
                    n.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                            arguments);
                    n.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                    ClipData clip = ClipData.newPlainText("label", "" + Math.random());
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(clip);
                    n.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                }
            }
        }
    }

    private void chooseFriend(int type, int id, String friend, AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/mutiselectcontact_edittext");
            android.util.Log.i("pressBtnByViewId", "type:" + type + " id:" + id + "list" + list.size() + "");
            if (list != null && list.size() > 0) {
                for (AccessibilityNodeInfo n : list) {
                    if (n.getClassName().equals("android.widget.EditText") && n.isEnabled() &&
                            n.getText().toString().equals("搜索")) {
                        android.util.Log.i("chooseFriend", "list" + n.getParent().getClassName());
                        Bundle arguments = new Bundle();
                        arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                                AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
                        arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                                true);
                        n.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                                arguments);
                        n.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                        ClipData clip = ClipData.newPlainText("label", friend);
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setPrimaryClip(clip);
                        n.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                    }
                }
            }

        }
    }

    //通用获取节点信息方法
    private void pressBtnByViewId(int type, int id, String viewId, String className, String text, String des, int whichNode) {
        List<AccessibilityNodeInfo> nodeInfoList = getRootInActiveWindow().findAccessibilityNodeInfosByViewId(viewId);
        android.util.Log.i("pressBtnByViewId", "type:" + type + " id:" + id + " nodeInfoList:" + nodeInfoList.size());
        if (nodeInfoList != null && nodeInfoList.size() > 0) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (des.equals("")) {
                    if (nodeInfo.getClassName() != null && nodeInfo.getText() != null) {
                        if (nodeInfo.getClassName().equals(className) && nodeInfo.isEnabled() &&
                                nodeInfo.getText().toString().contains(text)) {
                            switch (whichNode) {
                                case PARENT:
                                    nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    android.util.Log.i("pressBtnByViewId", "type:" + type + " id:" + id + " n.parent.class:" + nodeInfo.getParent().getClassName());
                                    break;
                                default:
                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    android.util.Log.i("pressBtnByViewId", "type:" + type + " id:" + id + " n.class:" + nodeInfo.getClassName());
                                    break;
                            }
                        }
                    }
                } else {
                    if (nodeInfo.getClassName() != null && nodeInfo.getContentDescription() != null) {
                        if (nodeInfo.getClassName().equals(className) && nodeInfo.isEnabled() &&
                                nodeInfo.getContentDescription().toString().contains(des)) {


                            switch (whichNode) {
                                case PARENT:
                                    nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    android.util.Log.i("pressBtnByViewId", "type:" + type + " id:" + id + " n.parent.class:" + nodeInfo.getParent().getClassName());
                                    break;
                                default:
                                    android.util.Log.i("pressBtnByViewId", "type:" + type + " id:" + id + " n.class:" + nodeInfo.getClassName());
                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 寻找窗体中的“发送”按钮，并且点击。
     */
    @SuppressLint("NewApi")
    private void send() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo
                    .findAccessibilityNodeInfosByText("发送");
            if (list != null && list.size() > 0) {
                for (AccessibilityNodeInfo n : list) {
                    if (n.getClassName().equals("android.widget.Button") && n.isEnabled()) {
                        n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }

            } else {
                List<AccessibilityNodeInfo> liste = nodeInfo
                        .findAccessibilityNodeInfosByText("Send");
                if (liste != null && liste.size() > 0) {
                    for (AccessibilityNodeInfo n : liste) {
                        if (n.getClassName().equals("android.widget.Button") && n.isEnabled()) {
                            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }
        }
        pressBackButton();
    }

    /**
     * 模拟back按键
     */
    private void pressBackButton() {
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param event
     */
    private void sendNotifacationReply(AccessibilityEvent event) {
        hasAction = true;
        if (event.getParcelableData() != null
                && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event
                    .getParcelableData();
            String content = notification.tickerText.toString();
            String[] cc = content.split(":");
            name = cc[0].trim();
            scontent = cc[1].trim();

            android.util.Log.i("maptrix", "sender name =" + name);
            android.util.Log.i("maptrix", "sender content =" + scontent);


            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("NewApi")
    private boolean fill() {
        String content = "你好，你是哪里的？";
        if (lastContent != null) {
            if (lastContent.contains("河南")) {
                content = "你好，你是河南哪里的？";
            } else if (lastContent.contains("郑州")) {
                content = "请加入郑州群34531233";
            } else {
                content = "你好，你是哪里的？";
            }
        }
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            return findEditText(rootNode, content);
        }
        return false;
    }


    private boolean findEditText(AccessibilityNodeInfo rootNode, String content) {
        int count = rootNode.getChildCount();
        android.util.Log.d("maptrix", "root class=" + rootNode.getClassName() + "," + rootNode.getText() + "," + count);
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
            if (nodeInfo == null) {
                android.util.Log.d("maptrix", "nodeinfo = null");
                continue;
            }

            android.util.Log.d("maptrix", "class=" + nodeInfo.getClassName());
            android.util.Log.e("maptrix", "ds=" + nodeInfo.getContentDescription());
            if (nodeInfo.getContentDescription() != null) {
                int nindex = nodeInfo.getContentDescription().toString().indexOf(name);
                int cindex = nodeInfo.getContentDescription().toString().indexOf(scontent);
                android.util.Log.e("maptrix", "nindex=" + nindex + " cindex=" + cindex);
                if (nindex != -1) {
                    itemNodeinfo = nodeInfo;
                    android.util.Log.i("maptrix", "find node info");
                }
            }
            if ("android.widget.EditText".equals(nodeInfo.getClassName())) {
                android.util.Log.i("maptrix", "==================");
                Bundle arguments = new Bundle();
                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                        AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
                arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                        true);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                        arguments);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                ClipData clip = ClipData.newPlainText("label", content);
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clip);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                return true;
            }

            if (findEditText(nodeInfo, content)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onInterrupt() {

    }

    /**
     * 判断指定的应用是否在前台运行
     *
     * @param packageName
     * @return
     */
    private boolean isAppForeground(String packageName) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(packageName)) {
            return true;
        }

        return false;
    }


    /**
     * 将当前应用运行到前台
     */
    private void bring2Front() {
        ActivityManager activtyManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activtyManager.getRunningTasks(3);
        for (ActivityManager.RunningTaskInfo runningTaskInfo : runningTaskInfos) {
            if (this.getPackageName().equals(runningTaskInfo.topActivity.getPackageName())) {
                activtyManager.moveTaskToFront(runningTaskInfo.id, ActivityManager.MOVE_TASK_WITH_HOME);
                return;
            }
        }
    }

    /**
     * 回到系统桌面
     */
    private void back2Home() {
        Intent home = new Intent(Intent.ACTION_MAIN);

        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addCategory(Intent.CATEGORY_HOME);

        startActivity(home);
    }


    /**
     * 系统是否在锁屏状态
     *
     * @return
     */
    private boolean isScreenLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.inKeyguardRestrictedInputMode();
    }

    private void wakeAndUnlock() {
        //获取电源管理器对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");

        //点亮屏幕
        wl.acquire(1000);

        //得到键盘锁管理器对象
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        kl = km.newKeyguardLock("unLock");

        //解锁
        kl.disableKeyguard();

    }

    private void release() {
        if (locked && kl != null) {
            android.util.Log.d("maptrix", "release the lock");
            //得到键盘锁管理器对象
            kl.reenableKeyguard();
            locked = false;
        }
    }
}
