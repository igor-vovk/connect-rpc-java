package me.ivovk.connect_rpc_java.netty.client;

import static io.netty.util.CharsetUtil.UTF_8;

import com.google.protobuf.Message;
import io.grpc.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

class ClientCallImpl<Req, Resp extends Message> extends ClientCall<Req, Resp> {
  private static final Logger logger = LoggerFactory.getLogger(ClientCallImpl.class);

  private final MethodDescriptor<Req, Resp> methodDescriptor;
  private final CallOptions callOptions;
  private final Executor callExecutor;
  private final ClientCallParams params;

  private Metadata metadata;
  private Listener<Resp> responseListener;
  private volatile io.netty.channel.Channel nettyChannel;
  private Req messageToSend;

  public ClientCallImpl(
      ClientCallParams params,
      MethodDescriptor<Req, Resp> methodDescriptor,
      CallOptions callOptions,
      Executor callExecutor) {
    this.params = params;
    this.methodDescriptor = methodDescriptor;
    this.callOptions = callOptions;
    this.callExecutor = callExecutor;
  }

  @Override
  public void start(Listener<Resp> responseListener, Metadata metadata) {
    this.metadata = metadata;
    this.responseListener = responseListener;

    Deadline deadline = callOptions.getDeadline();

    long timeoutMillis;
    if (deadline != null) {
      timeoutMillis = deadline.timeRemaining(TimeUnit.MILLISECONDS);
    } else {
      timeoutMillis = params.timeout;
    }
    if (timeoutMillis < 0) {
      responseListener.onClose(
          Status.DEADLINE_EXCEEDED.withDescription("Deadline exceeded before call started."),
          new Metadata());
      return;
    }

    var bootstrap = new Bootstrap();
    bootstrap
        .group(params.workerGroup)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY, true)
        .option(
            ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) Math.min(timeoutMillis, Integer.MAX_VALUE))
        .handler(
            new ChannelInitializer<SocketChannel>() {
              @Override
              public void initChannel(SocketChannel ch) {
                ChannelPipeline p = ch.pipeline();
                p.addLast(new ReadTimeoutHandler(timeoutMillis, TimeUnit.MILLISECONDS));
                p.addLast(new HttpClientCodec());
                p.addLast(new HttpObjectAggregator(1048576));
                p.addLast(
                    new ConnectClientHandler<>(
                        methodDescriptor,
                        params.headerMapping,
                        params.jsonMarshallerFactory,
                        responseListener,
                        callExecutor));
              }
            });

    var connectFuture = bootstrap.connect(params.host, params.port);
    nettyChannel = connectFuture.channel();
    connectFuture.addListener(
        f -> {
          if (f.isSuccess()) {
            maybeSendRequest();
          } else {
            responseListener.onClose(Status.fromThrowable(f.cause()), new Metadata());
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

    responseListener.onClose(
        Status.CANCELLED.withDescription(message).withCause(cause), new Metadata());
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
    if (nettyChannel == null || !nettyChannel.isActive() || messageToSend == null) {
      // TODO: introduce a countdown latch or similar mechanism to throw an error if the channel
      // is not ready
      return; // Channel not ready or no message to send
    }

    try {
      ByteBuf content;
      if (messageToSend instanceof Message message1) {
        byte[] bytes =
            params.jsonMarshallerFactory.jsonMarshaller(message1).stream(message1).readAllBytes();
        content = Unpooled.wrappedBuffer(bytes);
      } else {
        throw new IllegalArgumentException(
            "Unsupported message type: " + messageToSend.getClass().getName());
      }

      FullHttpRequest httpRequest =
          new DefaultFullHttpRequest(
              HttpVersion.HTTP_1_1,
              HttpMethod.POST,
              "/" + methodDescriptor.getFullMethodName(),
              content);

      var headers = httpRequest.headers();

      headers
          .add(HttpHeaderNames.HOST, params.hostname)
          .add(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
          .add(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes())
          .add(HttpHeaderNames.CONTENT_TYPE, "application/json")
          .add(params.headerMapping.toHeaders(this.metadata));

      var deadline = callOptions.getDeadline();
      if (deadline != null) {
        headers.add("connect-timeout-ms", deadline.timeRemaining(TimeUnit.MILLISECONDS));
      }

      if (logger.isTraceEnabled()) {
        logger.trace(">>> Request headers: {}", headers);
        logger.trace(">>> Request content: {}", httpRequest.content().toString(UTF_8));
      }

      nettyChannel.writeAndFlush(httpRequest);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send message: " + e.getMessage(), e);
    }
  }
}
