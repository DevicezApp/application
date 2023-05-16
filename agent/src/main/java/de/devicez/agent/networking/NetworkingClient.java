package de.devicez.agent.networking;

import de.devicez.agent.networking.packet.AbstractPacketHandler;
import de.devicez.common.application.Platform;
import de.devicez.common.packet.AbstractPacket;
import de.devicez.common.packet.client.LoginPacket;
import lombok.extern.slf4j.Slf4j;
import org.snf4j.core.SelectorLoop;
import org.snf4j.core.session.ISession;
import org.snf4j.core.session.IStreamSession;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
public class NetworkingClient {

    private final Map<Class<? extends AbstractPacket>, AbstractPacketHandler<?>> packetHandlerMap = new HashMap<>();

    private SelectorLoop loop;

    public NetworkingClient(final String hostname, final int port) {
        registerPacketHandler();
        new Thread(() -> start(hostname, port)).start();
    }

    private void registerPacketHandler() {

    }

    private void start(final String hostname, final int port) {
        try {
            loop = new SelectorLoop();
            loop.start();

            final SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(InetAddress.getByName(hostname), port));
            log.info("Connecting to {}:{}", hostname, port);

            final IStreamSession session = (IStreamSession) loop.register(channel, new ClientStreamHandler()).sync().getSession();
            session.getReadyFuture().sync();

            log.info("Connection established");

            session.write(new LoginPacket(UUID.randomUUID(), "test", Platform.WINDOWS));

            loop.join();
        } catch (final ExecutionException | InterruptedException | IOException e) {
            log.error("Error while starting networking client", e);
        } finally {
            loop.stop();
        }
    }
}
