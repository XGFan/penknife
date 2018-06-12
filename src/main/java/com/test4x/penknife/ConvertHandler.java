package com.test4x.penknife;

import com.test4x.penknife.converter.HttpMessageConverter;
import com.test4x.penknife.message.Response;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@ChannelHandler.Sharable
public class ConvertHandler extends MessageToMessageEncoder<Response> {
    private PenKnife penKnife;


    public ConvertHandler(PenKnife penKnife) {
        this.penKnife = penKnife;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Response msg, List<Object> out) throws Exception {
        if(msg.body() == null){
            out.add(msg.build(null, null));
            return;
        }
        for (HttpMessageConverter httpMessageConverter : penKnife.converters()) {
            if (httpMessageConverter.accept(msg.body())) {
                final byte[] bytes = httpMessageConverter.convert(msg.body());
                out.add(msg.build(httpMessageConverter.contentType(), bytes));
                return;
            }
        }
    }
}
