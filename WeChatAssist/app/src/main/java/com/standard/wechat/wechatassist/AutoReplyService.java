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
import java.util.Random;

public class AutoReplyService extends AccessibilityService {

    private final static String MM_PNAME = "com.tencent.mm";
    boolean hasAction = false;
    boolean locked = false;
    boolean background = false;
    private String name;
    private String scontent;
    AccessibilityNodeInfo itemNodeinfo;
    private KeyguardManager.KeyguardLock kl;
    private Handler handler = new Handler();
    private String lastContent;

    private String currentWindow = "";

    /**
     * 必须重写的方法，响应各种事件。
     *
     * @param event
     */
    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        int eventType = event.getEventType();
        android.util.Log.d("maptrix", "get event = " + eventType);
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:// 通知栏事件
                android.util.Log.d("maptrix", "get notification event");
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
                                }
                            }
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:  //窗口变化监听事件
                android.util.Log.d("maptrix", "get type window down event TYPE_WINDOW_STATE_CHANGED");
                android.util.Log.d("WINDOW_STATE_CHANGED", "" + event.getClassName().toString());
//                if (!hasAction) break;
                itemNodeinfo = null;
                String className = event.getClassName().toString();
                android.util.Log.d("WINDOW_STATE_CHANGED", "" + event.getClassName().toString());
//                if (className.equals("com.tencent.mm.ui.LauncherUI")) {
//                    if (fill()) {
//                        send();
//                    } else {
//                        if (itemNodeinfo != null) {
//                            itemNodeinfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                            handler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (fill()) {
//                                        send();
//                                    }
//                                    back2Home();
//                                    release();
//                                    hasAction = false;
//                                }
//                            }, 1000);
//                            break;
//                        }
//                    }
//                }

                if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                    currentWindow = "com.tencent.mm.ui.LauncherUI";
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    straight();
                }
                if (className.equals("com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI")) {
                    currentWindow = "com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI";
                    addFreinds(getRootInActiveWindow());
                }
                if (className.equals("com.tencent.mm.ui.contact.SelectContactUI")) {
                    currentWindow = "com.tencent.mm.ui.contact.SelectContactUI";
                    chooseFriend(getRootInActiveWindow());
                }


//                back2Home();
//                release();
//                hasAction = false;
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                android.util.Log.d("CONTENT_CHANGE_TEXT", "" + "tetetetetetetetetetetet");
                android.util.Log.d("CONTENT_CHANGE_TEXT", "" + event.getClassName().toString());
                if (event.getClassName().equals("android.widget.EditText") &&
                        currentWindow.equals("com.tencent.mm.ui.contact.SelectContactUI")) {
                    pressFreind();
                    pressOKBtn();
                }
                if (event.getClassName().equals("android.widget.EditText") &&
                        currentWindow.equals("com.tencent.mm.ui.LauncherUI")) {
                    chatInfo(getRootInActiveWindow());
                }
                break;
        }
    }

    private void straight() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/conversation_item_ll");
            android.util.Log.i("straight", "list count" + list.size());
            if (list != null && list.size() > 0) {
                for (AccessibilityNodeInfo n : list) {
                    if (n.getContentDescription() != null) {
                        android.util.Log.i("straight", "des:" + n.getContentDescription().toString());
                        if (n.getClassName().toString().equals("android.widget.LinearLayout") &&
                                n.getContentDescription().toString().contains("郑州")) {
                            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }
        }
        addText(nodeInfo);
    }

    private void chatInfo(AccessibilityNodeInfo rootNode) {
        int count = rootNode.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);

            if (nodeInfo == null) {
                continue;
            }

            if (nodeInfo.getContentDescription() != null) {
                android.util.Log.i("chatInfo", "des:" + nodeInfo.getContentDescription().toString());
                if (nodeInfo.getClassName().toString().equals("android.widget.TextView") &&
                        nodeInfo.getContentDescription().toString().equals("聊天信息")) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
            chatInfo(nodeInfo);
        }
    }

    private void addFreinds(AccessibilityNodeInfo rootNode) {
        int count = rootNode.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);

            if (nodeInfo == null) {
                continue;
            }

            if (nodeInfo.getContentDescription() != null) {
                android.util.Log.i("addFreinds", "des:" + nodeInfo.getContentDescription().toString());
                if (nodeInfo.getClassName().toString().equals("android.widget.ImageView") &&
                        nodeInfo.getContentDescription().toString().equals("添加成员")) {
                    android.util.Log.i("addFreinds", "parent:" + nodeInfo.getParent().getClassName().toString());
                    nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
            addFreinds(nodeInfo);
        }
    }

    private void chooseFriend(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/mutiselectcontact_edittext");
            android.util.Log.i("chooseFriend", "list" + list.size() + "");
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
                        ClipData clip = ClipData.newPlainText("label", name);
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setPrimaryClip(clip);
                        n.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                    }
                }
            }

        }
    }

    private void addText(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/chatting_content_et");
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
    }

    private void pressFreind() {
        List<AccessibilityNodeInfo> list = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/title_tv");
        android.util.Log.i("chooseFriend", "list" + list.size() + "");
        if (list != null && list.size() > 0) {
            for (AccessibilityNodeInfo n : list) {
                if (n.getClassName().equals("android.widget.TextView") && n.isEnabled() &&
                        n.getText().toString().equals("健康养生")) {
                    android.util.Log.i("chooseFriend", "list" + n.getParent().getClassName());
                    n.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    private void pressOKBtn(){
        List<AccessibilityNodeInfo> list = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/action_option_style_button");
        android.util.Log.i("pressOKBtn", "list" + list.size() + "");
        if (list != null && list.size() > 0) {
            for (AccessibilityNodeInfo n : list) {
                if (n.getClassName().equals("android.widget.TextView") && n.isEnabled() &&
                        n.getText().toString().contains("确定")) {
                    android.util.Log.i("pressOKBtn", "list" + n.getParent().getClassName());
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    private void pressAddBtn() {
        android.util.Log.i("maptrix", "press add Button");
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/icon_tv");
            android.util.Log.i("maptrix", "list count" + list.size());
            if (list != null && list.size() > 0) {
                for (AccessibilityNodeInfo n : list) {
                    if (n.getClassName().equals("android.widget.TextView") && n.isEnabled()
                            && n.getText().toString().equals("通讯录")) {
                        android.util.Log.i("maptrix", "click true");
                        android.util.Log.i("maptrix", n.getParent().getClassName().toString());
                        n.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
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
