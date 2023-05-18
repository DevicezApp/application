package de.devicez.agent.networking;

import de.devicez.agent.DeviceZAgentApplication;
import de.devicez.agent.networking.packet.AbstractPacketHandler;
import de.devicez.agent.networking.packet.ShutdownCancelPacketHandler;
import de.devicez.agent.networking.packet.ShutdownPacketHandler;
import de.devicez.common.packet.AbstractPacket;
import de.devicez.common.packet.client.HeartbeatPacket;
import de.devicez.common.packet.server.ShutdownCancelPacket;
import de.devicez.common.packet.server.ShutdownPacket;
import de.devicez.common.util.NetworkUtil;
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

    private static final int RETRY_INTERVAL = 1000;
    private static final int HEARTBEAT_INTERVAL = 5000;

    private final Map<Class<? extends AbstractPacket>, AbstractPacketHandler<?>> packetHandlerMap = new HashMap<>();

    private final DeviceZAgentApplication application;
    private final String hostname;
    private final int port;

    private SelectorLoop loop;
    private IStreamSession session;

    public NetworkingClient(final DeviceZAgentApplication application, final String hostname, final int port) {
        this.application = application;
        this.hostname = hostname;
        this.port = port;

        registerPacketHandler();
        new Thread(this::start).start();
    }

    private void registerPacketHandler() {
        packetHandlerMap.put(ShutdownPacket.class, new ShutdownPacketHandler(this));
        packetHandlerMap.put(ShutdownCancelPacket.class, new ShutdownCancelPacketHandler(this));
    }

    private void start() {
        try {
            loop = new SelectorLoop();
            loop.start();

            long lastAttempt = 0;
            while (loop.isOpen()) {
                final long now = System.currentTimeMillis();
                if (session == null || !session.isOpen()) {
                    if (now - lastAttempt < RETRY_INTERVAL) {
                        continue;
                    }

                    lastAttempt = now;
                    attemptConnection();
                } else {
                    if (now - lastAttempt < HEARTBEAT_INTERVAL) {
                        continue;
                    }

                    lastAttempt = now;
                    sendHeartbeat();
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

        try {
            handler.handlePacket(session, packet);
        } catch (final Exception e) {
            log.error("Error while handling {}", packet.getClass().getSimpleName(), e);
        }
    }

    private void sendHeartbeat() {
        try {
            session.writenf(new HeartbeatPacket(NetworkUtil.getHardwareAddress()));
        } catch (final IOException e) {
            log.error("Error while sending HeartbeatPacket", e);
        }
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

    public DeviceZAgentApplication getApplication() {
        return application;
    }

    public IStreamSession getSession() {
        return session;
    }
}
