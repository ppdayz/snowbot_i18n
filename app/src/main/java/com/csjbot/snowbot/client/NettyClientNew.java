package com.csjbot.snowbot.client;


import com.alibaba.fastjson.JSON;
import com.android.core.util.SharedUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.bean.CommandBean;
import com.csjbot.snowbot.bean.CommandDataBean;
import com.csjbot.snowbot.bean.LoginContentBean;
import com.csjbot.snowbot.client.nettyHandler.ClientListener;
import com.csjbot.snowbot.client.nettyHandler.ConnectInitializer;
import com.csjbot.snowbot.utils.CommonTool;
import com.csjbot.snowbot.utils.SharedKey;

import java.util.concurrent.TimeUnit;

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

public class NettyClientNew {
    private static NettyClientNew ConnectWithNetty = new NettyClientNew();

    public static NettyClientNew getInstence() {
        return ConnectWithNetty;
    }


    private NettyClientNew() {
    }

    private Channel channel = null;
    private EventLoopGroup workgroup = new NioEventLoopGroup();
    private Bootstrap bootstrap;
    private int port = 7700;
    private String ip;
    private boolean isRegisted = true;

    public void connect(final String ip, final ClientListener listener) {
        Csjlogger.debug("connect time");
        if (ip == null ||"".equals(ip) ) {
            Csjlogger.debug("connect()  ip is null");
            return;
        }
        this.ip = ip;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bootstrap = new Bootstrap();
                    bootstrap.group(workgroup)
                            .channel(NioSocketChannel.class)
                            .handler(new ConnectInitializer(listener));

                    //设置TCP协议的属性
                    bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

                    doConnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                finally {
//                    if (workgroup != null) {
//                        workgroup.shutdownGracefully();
//                    }
//                }
            }
        }).start();

    }


    public void doConnect() {
        if (channel != null ) {
            channel.closeFuture();
            channel.close();
            channel = null;
        }
        if (!isRegisted)
            return;
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.connect(ip, port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    channel = channelFuture.channel();
//                    channel.closeFuture().sync();
                    onLine();
                    SharedUtil.setPreferBool(SharedKey.NETWORKSTATUS,true);
                    Csjlogger.debug("Netty Client " + ip + " connect success ");
                } else {
                    channelFuture.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            doConnect();
                        }
                    }, 10, TimeUnit.SECONDS);
                    SharedUtil.setPreferBool(SharedKey.NETWORKSTATUS, false);
                    Csjlogger.debug("Netty Client " + ip + " failed ");
                }
            }
        });
    }

    public void closeChannle() {
        if (null != channel) {
            channel.close();
        }
    }


    public void sendMessage(String data) {
        try {
            String cmd = data + new String(new byte[]{0});
            if (null != channel && channel.isActive()) {
                ByteBuf out = Unpooled.copiedBuffer(cmd.getBytes());
                ChannelFuture channelFuture = channel.writeAndFlush(out);
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            Csjlogger.info("Netty Client send " + cmd + "_isSuccess");
                        } else {
                            Csjlogger.debug("Netty Client send failed");
                        }
                    }
                });
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void onLine() {
        CommandBean commandBean = new CommandBean();
        CommandDataBean<LoginContentBean> commandDataBean = new CommandDataBean<>();
        LoginContentBean loginContentBean = new LoginContentBean();
        commandDataBean.setServer("iot");
        commandDataBean.setService("ClientOnline");
        commandDataBean.setTimestamp(CommonTool.getCurrentTime());
        loginContentBean.setType("robot");
        loginContentBean.setId(SharedUtil.getPreferStr(SharedKey.DEVICEUUID));
        commandDataBean.setContent(loginContentBean);
        commandBean.setData(commandDataBean);
        String str = JSON.toJSONString(commandBean);
        sendMessage(str);
    }

    public void setisRegisted(boolean isRegisted) {
        this.isRegisted = isRegisted;
    }
}
