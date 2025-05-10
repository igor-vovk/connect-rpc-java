package me.ivovk.connect_rpc_java.netty.util;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.CompletableFuture;

/**
 * Utility class for converting between Netty's {@link Future} and Java's {@link CompletableFuture}.
 */
public final class FutureUtils {

  private FutureUtils() {}

  /**
   * Converts a Netty {@link Future} to a Java {@link CompletableFuture}.
   *
   * @param nettyFuture the Netty future to convert
   * @param <T> the type of the result
   * @return a CompletableFuture that completes when the Netty Future completes
   */
  public static <T> CompletableFuture<T> toCompletableFuture(Future<T> nettyFuture) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();

    nettyFuture.addListener(
        future -> {
          if (future.isSuccess()) {
            @SuppressWarnings("unchecked")
            T result = (T) future.getNow();
            completableFuture.complete(result);
          } else {
            completableFuture.completeExceptionally(future.cause());
          }
        });

    return completableFuture;
  }

  /**
   * Converts a Java {@link CompletableFuture} to a Netty {@link Future}.
   *
   * @param completableFuture the CompletableFuture to convert
   * @param executor the Netty EventExecutor to use for the Promise
   * @param <T> the type of the result
   * @return a Netty Future that completes when the CompletableFuture completes
   */
  public static <T> Future<T> toNettyFuture(
      CompletableFuture<T> completableFuture, EventExecutor executor) {

    Promise<T> promise = executor.newPromise();

    completableFuture.whenComplete(
        (result, exception) -> {
          if (exception != null) {
            promise.setFailure(exception);
          } else {
            promise.setSuccess(result);
          }
        });

    return promise;
  }
}
