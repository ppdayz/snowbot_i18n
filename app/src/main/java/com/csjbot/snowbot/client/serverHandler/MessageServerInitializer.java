package com.csjbot.snowbot.client.serverHandler;


import com.csjbot.csjbase.log.Csjlogger;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;

/**
 * Created by aoe on 16/4/30.
 */
public class MessageServerInitializer extends ChannelInitializer<SocketChannel> {


    // 设置6秒检测chanel是否接受过心跳数据
    private static final int READ_WAIT_SECONDS = 6;


    private final SslContext sslCtx;

    public MessageServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));

        }
        Csjlogger.debug("initChannel");
        pipeline.addLast(new StringEncoder());
//        pipeline.addLast(new StringDecoder());
//        pipeline.addLast(new DelimiterBasedFrameDecoder(102400, delimiter));
        pipeline.addLast("Deframer", new DelimiterBasedFrameDecoder(256 * 1024, true, true, Delimiters.nulDelimiter()));
//        pipeline.addLast("StringDecoder", new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast(new MessageServerHandler());
    }
}
