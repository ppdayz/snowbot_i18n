package com.csjbot.snowbot.client.nettyHandler;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by huantingting on 16/6/16.
 */
public class ConnectInitializer extends ChannelInitializer<SocketChannel> {
    private ClientListener listener;

    public ConnectInitializer(ClientListener listener) {
        this.listener = listener;
    }


    @Override
    public void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new IdleStateHandler(0, 0, 10));
        socketChannel.pipeline().addLast(new StringEncoder());
        socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024000, Delimiters.nulDelimiter()));
        socketChannel.pipeline().addLast(new ConnectHandler(listener));


    }
}
