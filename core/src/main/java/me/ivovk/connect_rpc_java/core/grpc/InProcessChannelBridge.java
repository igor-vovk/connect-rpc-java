package me.ivovk.connect_rpc_java.core.grpc;

import io.grpc.*;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class InProcessChannelBridge {

    static Channel create(
            List<ServerServiceDefinition> services,
            Function<ServerBuilder<?>, ServerBuilder<?>> serverConfigurator,
            Executor executor
    ) {
        String name = InProcessServerBuilder.generateName();

        Server server = createServer(name, services, serverConfigurator, executor);
        ManagedChannel channel = createChannel(name, Function.identity());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
            channel.shutdown();
        }));

        return channel;
    }

    static Server createServer(
            String name,
            List<ServerServiceDefinition> services,
            Function<ServerBuilder<?>, ServerBuilder<?>> serverConfigurator,
            Executor executor
    ) {
        ServerBuilder<?> builder = InProcessServerBuilder.forName(name)
                .addServices(services)
                .executor(executor);

        builder = serverConfigurator.apply(builder);

        return builder.build();
    }

    static ManagedChannel createChannel(
            String name,
            Function<ManagedChannelBuilder<?>, ManagedChannelBuilder<?>> channelConfigurator
    ) {
        ManagedChannelBuilder<?> builder = InProcessChannelBuilder.forName(name);

        builder = channelConfigurator.apply(builder);

        return builder.build();
    }

}
