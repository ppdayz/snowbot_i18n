package com.csjbot.snowbot.client.nettyHandler;

import com.alibaba.fastjson.JSON;
import com.android.core.util.SharedUtil;
import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.bean.CommandBean;
import com.csjbot.snowbot.bean.CommandDataBean;
import com.csjbot.snowbot.bean.LoginContentBean;
import com.csjbot.snowbot.client.NettyClientNew;
import com.csjbot.snowbot.utils.CommonTool;
import com.csjbot.snowbot.utils.SharedKey;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by huantingting on 16/6/16.
 */
public class ConnectHandler extends SimpleChannelInboundHandler<String> {

    private static ClientListener listener = null;
    private String heartBeatString = "PING";


    public ConnectHandler(ClientListener listener) {
        this.listener = listener;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if (listener != null) {
            listener.clientConnected();
        }
//        Csjlogger.debug("Netty Client Active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Csjlogger.error(getClass().getSimpleName(), "channelInactive!");
        if (listener != null) {
            listener.clientDisConnected();
        }
        offfLine();
        NettyClientNew.getInstence().doConnect();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        Csjlogger.debug("Netty Server" + body);

        if (listener != null) {
            listener.recMessage(body);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Csjlogger.debug("Netty Server " + msg);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // IdleStateHandler 所产生的 IdleStateEvent 的处理逻辑.
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
//                    handleAllIdle(ctx);
//                    handleReaderIdle(ctx);
                    break;
                case WRITER_IDLE:
//                    handleAllIdle(ctx);
//                    handleWriterIdle(ctx);
                    break;
                case ALL_IDLE:
                    handleAllIdle(ctx);
                    break;
                default:
                    break;
            }
        }
    }

    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        System.err.println("---READER_IDLE---");
    }

    protected void handleWriterIdle(ChannelHandlerContext ctx) {
        System.err.println("---WRITER_IDLE---");
    }

    protected void handleAllIdle(ChannelHandlerContext ctx) {
        System.err.println("---ALL_IDLE---");
        NettyClientNew.getInstence().sendMessage(heartBeatString);
    }

    private void offfLine() {
        Csjlogger.debug("Netty Client offline");
        CommandBean commandBean = new CommandBean();
        CommandDataBean<LoginContentBean> commandDataBean = new CommandDataBean<>();
        LoginContentBean loginContentBean = new LoginContentBean();
        commandDataBean.setServer("iot");
        commandDataBean.setService("ClientOffline");
        commandDataBean.setTimestamp(CommonTool.getCurrentTime());
        loginContentBean.setType("robot");
        loginContentBean.setId(SharedUtil.getPreferStr(SharedKey.DEVICEUUID));
        commandDataBean.setContent(loginContentBean);
        commandBean.setData(commandDataBean);
        String str = JSON.toJSONString(commandBean);
        NettyClientNew.getInstence().sendMessage(str);
    }
}
