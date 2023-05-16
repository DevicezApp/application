package de.devicez.agent.networking;

import de.devicez.agent.networking.packet.AbstractPacketHandler;
import de.devicez.common.packet.AbstractPacket;
import lombok.extern.slf4j.Slf4j;
import org.snf4j.core.SelectorLoop;
import org.snf4j.core.session.IStreamSession;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class NetworkingClient {

    private final Map<Class<? extends AbstractPacket>, AbstractPacketHandler<?>> packetHandlerMap = new HashMap<>();
    private final String hostname;
    private final int port;

    private SelectorLoop loop;
    private IStreamSession session;

    public NetworkingClient(final String hostname, final int port) {
        this.hostname = hostname;
        this.port = port;

        registerPacketHandler();
        new Thread(this::start).start();
    }

    private void registerPacketHandler() {

    }

    private void start() {
        try {
            loop = new SelectorLoop();
            loop.start();

            long lastAttempt = 0;
            while (loop.isOpen()) {
                if (session == null || !session.isOpen()) {
                    final long now = System.currentTimeMillis();
                    if (now - lastAttempt < 1000) {
                        continue;
                    }

                    lastAttempt = now;
                    attemptConnection();
                }
            }

            loop.join();
        } catch (final InterruptedException | IOException e) {
            log.error("Error while starting networking client", e);
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

        handler.handlePacket(session, packet);
    }

    private void attemptConnection() {
        log.info("Connecting to {}:{}", hostname, port);

        try {
            final SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(InetAddress.getByName(hostname), port));

            session = (IStreamSession) loop.register(channel, new ClientStreamHandler(this)).sync().getSession();
            session.getReadyFuture().sync();
            log.info("Connection established");
        } catch (final Exception e) {
            log.error("Error while connecting", e);
        }
    }

    public IStreamSession getSession() {
        return session;
    }
}
