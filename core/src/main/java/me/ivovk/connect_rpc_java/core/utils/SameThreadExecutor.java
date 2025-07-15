package me.ivovk.connect_rpc_java.core.utils;

import java.util.concurrent.Executor;

public class SameThreadExecutor implements Executor {
  public static final SameThreadExecutor INSTANCE = new SameThreadExecutor();

  private SameThreadExecutor() {}

  @Override
  public void execute(Runnable command) {
    command.run();
  }
}
