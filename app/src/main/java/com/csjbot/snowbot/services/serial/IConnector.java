package com.csjbot.snowbot.services.serial;


/**
 * Copyright (c) 2016, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2016/11/17 0017-14:53.
 * Email: puyz@csjbot.com
 */

public interface IConnector {
    /**
     * 连接服务器
     *
     * @param hostName ip地址
     * @param port     端口
     */
    int connect(String hostName, int port);

    /**
     * 发送数据
     *
     * @param data 要发送的二进制数据
     */
    int sendData(byte[] data);

    /**
     * 接受数据的回调设置
     *
     * @param receive 接收数据的回调
     * @see DataReceive
     */
    void setDataReceive(DataReceive receive);

    /**
     * IConnector 是否连接
     *
     * @return 返回true则已经连接，返回false则没有连接
     */
    boolean isRunning();

    /**
     * 销毁 IConnector， 参见各个实现，保证所有的都设置为空
     */
    void destroy();
}
