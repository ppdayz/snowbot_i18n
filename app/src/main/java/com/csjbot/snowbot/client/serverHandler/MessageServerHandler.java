package com.csjbot.snowbot.client.serverHandler;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.client.NettyServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


/**
 * Created by aoe on 16/4/30.
 */
public class MessageServerHandler extends SimpleChannelInboundHandler<String> {

    // 定义客户端没有收到服务端的ping消息的最大次数
    private static final int MAX_UN_REC_PING_TIMES = 3;

    // 失败计数器：未收到client端发送的ping请求
    private int unRecPingTimes = 0;

    private static MessageRecInterface messageRecInterface = null;

    public static void setMessageRecInterface(MessageRecInterface inf) {
        messageRecInterface = inf;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        Csjlogger.debug("Server : " + body);

        if (messageRecInterface != null) {
            messageRecInterface.messageRec(body);
        }
    }

    /**
     * <strong>Please keep in mind that this method will be renamed to
     * {@code messageReceived(ChannelHandlerContext, I)} in 5.0.</strong>
     * <p>
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *            belongs to
     * @param msg the message to handle
     * @throws Exception is thrown if an error occurred
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (msg != null) {
            Csjlogger.debug(msg.toString());
        } else {
            Csjlogger.error("msg = null");
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Csjlogger.debug("连接成功 " + ctx.channel().remoteAddress().toString());
        NettyServer.getOurInstance().addChannelHandlerContext(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Csjlogger.debug("管道断开" + ctx.channel().remoteAddress().toString());
        NettyServer.getOurInstance().removeChannelHandlerContext(ctx);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();


    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    }


}
