package me.ivovk.connect_rpc_java.netty.util;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;

public class NettyLeakDetectorFactory extends ResourceLeakDetectorFactory {

  @Override
  public <T> ResourceLeakDetector<T> newResourceLeakDetector(
      Class<T> resource, int samplingInterval, long maxActive) {
    return new ThrowingLeakDetector<>(resource, samplingInterval);
  }

  public static void use() {
    ResourceLeakDetectorFactory.setResourceLeakDetectorFactory(new NettyLeakDetectorFactory());
    ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
  }
}
