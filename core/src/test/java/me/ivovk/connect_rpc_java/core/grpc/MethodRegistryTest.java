package me.ivovk.connect_rpc_java.core.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import test.MethodRegistryTestServiceGrpc;

import java.util.List;

class MethodRegistryTest {

  static class TestGrpcServiceImpl
      extends MethodRegistryTestServiceGrpc.MethodRegistryTestServiceImplBase {}

  @Test
  void testMethodRegistry() {
    var service = new TestGrpcServiceImpl();
    var registry = MethodRegistry.create(List.of(service.bindService()));

    var entry = registry.get("test.MethodRegistryTestService", "SimpleMethod");
    assertTrue(entry.isPresent());

    assertEquals("test.MethodRegistryTestService", entry.get().methodName().service());
    assertEquals("SimpleMethod", entry.get().methodName().method());
  }

  @Test
  void testHttpAnnotationParsing() {
    var service = new TestGrpcServiceImpl();
    var registry = MethodRegistry.create(List.of(service.bindService()));

    var entry = registry.get("test.MethodRegistryTestService", "HttpAnnotationMethod");
    assertTrue(entry.isPresent());
    assertTrue(entry.get().httpRule().isPresent());

    var httpRule = entry.get().httpRule().get();
    assertEquals("/v1/test/http_annotation_method", httpRule.getPost());
    assertEquals("*", httpRule.getBody());
  }
}
