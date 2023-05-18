package de.devicez.server.networking;

import de.devicez.common.packet.AbstractPacket;
import de.devicez.common.packet.client.LoginPacket;
import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.networking.packet.AbstractPacketHandler;
import de.devicez.server.networking.packet.LoginPacketHandler;
import lombok.extern.slf4j.Slf4j;
import org.snf4j.core.SelectorLoop;
import org.snf4j.core.factory.AbstractSessionFactory;
import org.snf4j.core.handler.IStreamHandler;
import org.snf4j.core.session.IStreamSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class NetworkingServer {

    private final DeviceZServerApplication application;
    private final Map<Class<? extends AbstractPacket>, AbstractPacketHandler<?>> packetHandlerMap = new HashMap<>();

    private SelectorLoop loop;

    public NetworkingServer(final DeviceZServerApplication application, final int port) {
        this.application = application;

        registerPacketHandler();
        new Thread(() -> start(port)).start();
    }

    private void registerPacketHandler() {
        packetHandlerMap.put(LoginPacket.class, new LoginPacketHandler(this));
    }

    private void start(final int port) {
        try {
            loop = new SelectorLoop();
            loop.start();

            // Initialize server socket
            final ServerSocketChannel channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(port));

            // Register with the selector loop
            loop.register(channel, new ServerSessionFactory(this));

            log.info("Started networking server on port " + port);

            // Wait for loop to end
            loop.join();
        } catch (final InterruptedException | IOException e) {
            log.error("Error while starting networking server", e);
            System.exit(1);
        } finally {
            loop.stop();
        }
    }

    public void close() {
        loop.stop();
    }

    <T extends AbstractPacket> void handlePacket(final IStreamSession session, final T packet) {
        final AbstractPacketHandler<T> handler = (AbstractPacketHandler<T>) packetHandlerMap.get(packet.getClass());
        if (handler == null) {
            log.warn("No handler found for packet {}", packet.getClass().getSimpleName());
            return;
        }

        try {
            handler.handlePacket(session, packet);
        } catch (final Exception e) {
            log.error("Error while handling {}", packet.getClass().getSimpleName(), e);
        }
    }

    public DeviceZServerApplication getApplication() {
        return application;
    }

    private static class ServerSessionFactory extends AbstractSessionFactory {

        private final NetworkingServer server;

        public ServerSessionFactory(final NetworkingServer server) {
            this.server = server;
        }

        @Override
        protected IStreamHandler createHandler(final SocketChannel socketChannel) {
            return new ServerStreamHandler(server);
        }
    }
}
