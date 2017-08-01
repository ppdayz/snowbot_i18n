package com.csjbot.snowbot.client;


import com.alibaba.fastjson.JSON;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.bean.LoginBean;
import com.csjbot.snowbot.bean.PacketBean;
import com.csjbot.snowbot.client.nettyHandler.ClientListener;
import com.csjbot.snowbot.client.nettyHandler.ConnectInitializer;
import com.csjbot.snowbot.utils.UUIDGenerator;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by huantingting on 16/6/16.
 */

public class NettyClient {
    private static NettyClient ConnectWithNetty = new NettyClient();

    public static NettyClient getInstence() {
        return ConnectWithNetty;
    }

    private NettyClient() {
        singleThreadExecutor.execute(sendRunable);
    }

    ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    private static final String EOF = "&_";
    private Channel channel = null;
    private EventLoopGroup workgroup = null;

    public void connect(final String ip, final ClientListener listener) {
        if ("".equals(ip) && ip == null) {
            Csjlogger.debug("connect()  ip is null");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                int port = 7700;

                try {
                    workgroup = new NioEventLoopGroup();

                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(workgroup)
                            .channel(NioSocketChannel.class)
                            .handler(new ConnectInitializer(listener));

                    //设置TCP协议的属性
                    bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

                    ChannelFuture channelFuture = bootstrap.connect(ip, port).awaitUninterruptibly();
                    channelFuture.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if (channelFuture.isSuccess()) {
                                Csjlogger.debug("Connect success");
                                isStop = false;
                            } else {

                                Csjlogger.debug("Connect " + ip + " failed ");
                            }
                        }
                    });
                    channel = channelFuture.channel();
                    channel.closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (workgroup != null) {
                        workgroup.shutdownGracefully();
                        Csjlogger.debug("close connect");
                    }
                }
            }
        }).start();
    }

    public boolean isConnected() {
        if (channel != null) {
            return channel.isActive();
        }
        return false;
    }


    private LinkedList<String> cmdList = new LinkedList<>();
    private boolean isStop = false;

    private Runnable sendRunable = new Runnable() {
        @Override
        public void run() {
            while (!isStop) {
                if (!isStop) {
                    if (cmdList.isEmpty()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }
                final String cmd = cmdList.get(0);

                if (channel.isActive()) {
                    ByteBuf out = Unpooled.copiedBuffer(cmd.getBytes());
                    ChannelFuture channelFuture = channel.writeAndFlush(out);
                    channelFuture.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if (channelFuture.isSuccess()) {
                                Csjlogger.info("send msg : " + cmd + "_isSuccess");
                                cmdList.remove(0);
                            } else {
                                Csjlogger.debug("send failed");
                            }
                        }
                    });
                }
                if (!isStop) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    public void sendMsg(final String msg) {
        if (channel != null && channel.isActive()) {
            cmdList.add(msg + new String(new byte[]{0}));
        }
    }

    public void login() {
        LoginBean loginBean = new LoginBean();
        PacketBean packet = new PacketBean(0, "Login", UUIDGenerator.getInstance().getDeviceUUID(), loginBean);
        sendMsg(JSON.toJSONString(packet));
    }

    public void exitClient() {
        closeConnect();
        isStop = true;
        singleThreadExecutor.shutdown();
    }

    public void closeConnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (channel != null)
                    channel.close();
            }
        }).start();
    }

    public void disConnect() {
        if (workgroup != null) {
            workgroup.shutdownGracefully();
        }
    }
}
