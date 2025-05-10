package me.ivovk.connect_rpc_java.netty.util;

import static org.junit.jupiter.api.Assertions.*;

import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class FutureUtilsTest {

  private EventExecutor executor;

  @BeforeEach
  void setUp() {
    executor = new DefaultEventExecutor();
  }

  @AfterEach
  void tearDown() {
    executor.shutdownGracefully();
  }

  @Test
  void toCompletableFuture_Success() throws ExecutionException, InterruptedException {
    // Given
    Promise<String> nettyPromise = executor.newPromise();

    // When
    CompletableFuture<String> completableFuture = FutureUtils.toCompletableFuture(nettyPromise);
    nettyPromise.setSuccess("success");

    // Then
    assertEquals("success", completableFuture.get());
  }

  @Test
  void toCompletableFuture_Failure() {
    // Given
    Promise<String> nettyPromise = executor.newPromise();
    Exception expectedException = new RuntimeException("test exception");

    // When
    CompletableFuture<String> completableFuture = FutureUtils.toCompletableFuture(nettyPromise);
    nettyPromise.setFailure(expectedException);

    // Then
    ExecutionException exception = assertThrows(ExecutionException.class, completableFuture::get);
    assertEquals(expectedException, exception.getCause());
  }

  @Test
  void toNettyFuture_Success() throws ExecutionException, InterruptedException {
    // Given
    CompletableFuture<String> completableFuture = new CompletableFuture<>();

    // When
    Future<String> nettyFuture = FutureUtils.toNettyFuture(completableFuture, executor);
    completableFuture.complete("success");

    // Then
    assertTrue(nettyFuture.isSuccess());
    assertEquals("success", nettyFuture.get());
  }

  @Test
  void toNettyFuture_Failure() {
    // Given
    CompletableFuture<String> completableFuture = new CompletableFuture<>();
    Exception expectedException = new RuntimeException("test exception");

    // When
    Future<String> nettyFuture = FutureUtils.toNettyFuture(completableFuture, executor);
    completableFuture.completeExceptionally(expectedException);

    // Then
    assertTrue(nettyFuture.isDone());
    assertFalse(nettyFuture.isSuccess());
    assertEquals(expectedException, nettyFuture.cause());
  }
}
