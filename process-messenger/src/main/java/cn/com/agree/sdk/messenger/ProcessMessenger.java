package cn.com.agree.sdk.messenger;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import cn.com.agree.sdk.messenger.utils.ProcessUtil;

public class ProcessMessenger {
    private static BinderNode client;
    private static BinderNode otherAppClient;
    private static Application context;

    private ProcessMessenger() {
    }

    /**
     * 初始化框架,在主进程初始化
     */
    public static void init(Application application) {
        if (ProcessUtil.isMainProcess(application)) {
            context = application;
            Intent intent = new Intent(application, BinderPool.class);
            application.startService(intent);
        } else {
            client = new BinderNode();
            client.bind(application);
        }
    }

    /**
     * 绑定跨远程app
     *
     * @param application application
     * @param packageName 远程app包名
     */
    public static void bindOtherAPP(Application application, String packageName) {
        if (context == null) {
            context = application;
        }
        otherAppClient = new BinderNode();
        otherAppClient.bind(application, packageName);
    }

    /**
     * 订阅消息
     *
     * @param key      消息唯一值
     * @param callback 消息回调
     */
    public static void subscribe(@NonNull String key, @NonNull MessageCallback callback) {
        ProcessMessageCenter.subscribe(key, callback);
    }

    /**
     * 取消订阅
     *
     * @param key 消息唯一值
     */
    public static void unsubscribe(@NonNull String key) {
        ProcessMessageCenter.unsubscribe(key);
    }

    /**
     * 发送消息
     *
     * @param key  接收消息唯一值
     * @param data data
     */
    public static void sendMessage(@NonNull String key, Bundle data) {
        data.putString(Constant.KEY_STRING, key);
        if (client != null) {
            client.sendMessage(data);
        } else {
            Intent intent = new Intent(context, BinderPool.class);
            intent.putExtras(data);
            context.startService(intent);
        }
        if (otherAppClient != null) {
            otherAppClient.sendMessage(data);
        }
    }
}
