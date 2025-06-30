package me.ivovk.connect_rpc_java.netty.client;

import static io.netty.util.CharsetUtil.UTF_8;

import com.google.protobuf.Message;
import io.grpc.*;
import io.grpc.Channel;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import me.ivovk.connect_rpc_java.core.http.HeaderMapping;
import me.ivovk.connect_rpc_java.core.http.json.JsonMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class ConnectNettyChannel extends Channel implements AutoCloseable {

  private static final IoHandlerFactory ioHandlerFactory = NioIoHandler.newFactory();

  private final String host;

  private final int port;

  private final HeaderMapping<HttpHeaders> headerMapping;

  private final JsonMarshallerFactory jsonMarshallerFactory;

  private final EventLoopGroup workerGroup;

  public ConnectNettyChannel(
      String host,
      int port,
      HeaderMapping<HttpHeaders> headerMapping,
      JsonMarshallerFactory jsonMarshallerFactory) {
    this.host = host;
    this.port = port;
    this.headerMapping = headerMapping;
    this.jsonMarshallerFactory = jsonMarshallerFactory;
    this.workerGroup = new MultiThreadIoEventLoopGroup(1, ioHandlerFactory);
  }

  private class ClientCallImpl<Req, Resp extends Message> extends ClientCall<Req, Resp> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MethodDescriptor<Req, Resp> methodDescriptor;
    private final CallOptions callOptions;
    private Metadata metadata;
    private io.netty.channel.Channel nettyChannel;
    private Req messageToSend;

    public ClientCallImpl(MethodDescriptor<Req, Resp> methodDescriptor, CallOptions callOptions) {
      this.methodDescriptor = methodDescriptor;
      this.callOptions = callOptions;
    }

    @Override
    public void start(Listener<Resp> responseListener, Metadata metadata) {
      this.metadata = metadata;

      Bootstrap b = new Bootstrap();
      b.group(workerGroup)
          .channel(NioSocketChannel.class)
          .option(ChannelOption.TCP_NODELAY, true)
          .handler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                  ch.pipeline()
                      .addLast(new HttpClientCodec())
                      .addLast(new HttpObjectAggregator(1048576))
                      .addLast(
                          new ConnectRpcClientHandler<>(
                              methodDescriptor,
                              headerMapping,
                              jsonMarshallerFactory,
                              responseListener));
                }
              });

      ChannelFuture f = b.connect(host, port);
      nettyChannel = f.channel();
      f.addListener(
          future -> {
            if (future.isSuccess()) {
              maybeSendRequest();
            } else {
              responseListener.onClose(Status.fromThrowable(future.cause()), new Metadata());
            }
          });
    }

    @Override
    public void request(int numMessages) {
      // Not used for unary calls
    }

    @Override
    public void cancel(@Nullable String message, @Nullable Throwable cause) {
      if (nettyChannel != null) {
        nettyChannel.close();
      }
    }

    @Override
    public void halfClose() {
      // Not used for unary calls
    }

    @Override
    public void sendMessage(Req message) {
      this.messageToSend = message;

      maybeSendRequest();
    }

    private void maybeSendRequest() {
      if (!nettyChannel.isActive() || messageToSend == null) {
        return; // Channel not ready or no message to send
      }

      try {
        ByteBuf content;
        if (messageToSend instanceof Message message1) {
          byte[] bytes =
              jsonMarshallerFactory.jsonMarshaller(message1).stream(message1).readAllBytes();
          content = Unpooled.wrappedBuffer(bytes);
        } else {
          throw new IllegalArgumentException(
              "Unsupported message type: " + messageToSend.getClass().getName());
        }

        FullHttpRequest httpRequest =
            new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.POST,
                "http://" + host + ":" + port + "/" + methodDescriptor.getFullMethodName(),
                content);
        var headers = httpRequest.headers();
        headers.add(headerMapping.toHeaders(this.metadata));
        headers.add(HttpHeaderNames.HOST, host);
        headers.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        headers.add(HttpHeaderNames.CONTENT_LENGTH, httpRequest.content().readableBytes());
        headers.add(HttpHeaderNames.CONTENT_TYPE, "application/json");

        if (logger.isDebugEnabled()) {
          logger.trace(">>> Request headers: {}", headers);
          logger.debug(">>> Request content: {}", httpRequest.content().toString(UTF_8));
        }

        nettyChannel.writeAndFlush(httpRequest);
      } catch (Exception e) {
        throw new RuntimeException("Failed to send message: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public <Req, Resp> ClientCall<Req, Resp> newCall(
      MethodDescriptor<Req, Resp> methodDescriptor, CallOptions callOptions) {
    return new ClientCallImpl(methodDescriptor, callOptions);
  }

  @Override
  public String authority() {
    return host + ":" + port;
  }

  public void shutdown() {
    workerGroup.shutdownGracefully();
  }

  @Override
  public void close() throws Exception {
    shutdown();
  }
}
