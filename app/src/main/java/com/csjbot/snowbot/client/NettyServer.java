package com.csjbot.snowbot.client;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.client.serverHandler.MessageServerInitializer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;


/**
 * Created by aoe on 16/4/29.
 */
public class NettyServer {

    static final int PORT = 6666;

    private static NettyServer ourInstance = new NettyServer();

    public static NettyServer getOurInstance() {
        return ourInstance;
    }

    private ArrayList<ChannelHandlerContext> channels = new ArrayList<>();

    public void addChannelHandlerContext(ChannelHandlerContext channel) {
        channels.add(channel);
    }

    public void removeChannelHandlerContext(ChannelHandlerContext channel) {
        Iterator<ChannelHandlerContext> channelIterator = channels.iterator();

        while (channelIterator.hasNext()) {
            if (channel == channelIterator.next()) {
                channelIterator.remove();
                break;
            }
        }
    }

    private NettyServer() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                final SslContext sslCtx = null;
                Csjlogger.debug("operationComplete");

                EventLoopGroup bossGroup = new NioEventLoopGroup(1);
                EventLoopGroup workGroup = new NioEventLoopGroup();

                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                            .handler(new LoggingHandler(LogLevel.INFO))
                            .option(ChannelOption.TCP_NODELAY, true)
                            .option(ChannelOption.SO_REUSEADDR, true)
                            .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                            .childHandler(new MessageServerInitializer(sslCtx));


                    final ChannelFuture f = b.bind(PORT).sync();
                    f.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if (f.isSuccess()) {
                                Csjlogger.debug("NettyServer operationComplete");
                            } else {
                                Csjlogger.debug("NettyServer not operationComplete");
                            }
                        }
                    });

                    f.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Csjlogger.debug(e.toString());

                } finally {
                    workGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                    Csjlogger.debug("workGroup shutdownGracefully");

                }
            }
        }).start();

        SendThread.start();
    }

    private LinkedList<String> cmdList = new LinkedList<>();
    private boolean isStop = false;

    private Thread SendThread = new Thread(new Runnable() {
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
//                Csjlogger.debug("sendmsg() : " + msg);
                final String cmd = cmdList.get(0);

                nettySendMsg(cmd);
//                if (channel.isActive()) {
//                    ChannelFuture channelFuture = channel.writeAndFlush(cmd);
//                    channelFuture.addListener(new ChannelFutureListener() {
//                        @Override
//                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
//                            if (channelFuture.isSuccess()) {
//                                Csjlogger.debug("send msg : " + cmd + " isSuccess");
//                                cmdList.remove(0);
//                            } else {
//                                Csjlogger.debug("send failed");
//                            }
//                        }
//                    });
//                }
                if (!isStop) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    });

    private void nettySendMsg(final String cmd) {
        final boolean ok = true;
        for (final ChannelHandlerContext ctx : channels) {
            ByteBuf buf = Unpooled.buffer(cmd.getBytes().length);
            buf.writeBytes((cmd).getBytes());
            ChannelFuture channelFuture = ctx.writeAndFlush(buf);

            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
//                        Csjlogger.debug("send msg : " + cmd + " isSuccess");
//                        cmdList.remove(0);
                        Csjlogger.debug("send to " + ctx.channel().remoteAddress());
                    } else {
                        Csjlogger.debug("send failed");
                    }
                }
            });
        }

        if (ok && channels.size() > 0) {
            Csjlogger.debug("send msg : " + cmd);
            cmdList.remove(0);
        }
    }

    public void sendMsg(String msg) {
        cmdList.add(msg + new String(new byte[]{0}));
    }

//    public void sendMsg(String msg) {
//        Csjlogger.debug(msg);
//        for (ChannelHandlerContext ctx : channels) {
////            Csjlogger.debug(msg);
//            ByteBuf buf = Unpooled.buffer(msg.getBytes().length);
//            buf.writeBytes((msg + "\n").getBytes());
//            ctx.writeAndFlush(buf);
//        }
//    }

}
