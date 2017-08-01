package com.csjbot.snowbot.client.nettyHandler;

/**
 * Created by Administrator on 2016/7/30 0030.
 */
public interface ClientListener {
    void recMessage(String msg);

    void clientConnected();

    void clientDisConnected();
}
