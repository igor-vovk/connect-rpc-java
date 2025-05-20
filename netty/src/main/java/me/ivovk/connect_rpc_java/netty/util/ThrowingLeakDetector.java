package me.ivovk.connect_rpc_java.netty.util;

import io.netty.util.ResourceLeakDetector;

public class ThrowingLeakDetector<T> extends ResourceLeakDetector<T> {

  public ThrowingLeakDetector(Class<?> resourceType, int samplingInterval) {
    super(resourceType, samplingInterval);
  }

  @Override
  protected void reportTracedLeak(String resourceType, String records) {
    throw new RuntimeException("Resource leak detected: " + resourceType + "\n" + records);
  }

  @Override
  protected void reportUntracedLeak(String resourceType) {
    throw new RuntimeException("Resource leak detected: " + resourceType);
  }
}
