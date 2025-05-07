package org.example;


import me.ivovk.connect_rpc_java.netty.NettyServerBuilder;

import java.util.List;

public class Main {
  public static void main(String[] args) {
    var server = NettyServerBuilder.forServices(List.of());
  }
}