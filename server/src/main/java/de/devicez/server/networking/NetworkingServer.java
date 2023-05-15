package de.devicez.server.networking;

import lombok.extern.slf4j.Slf4j;
import org.snf4j.core.SelectorLoop;
import org.snf4j.core.factory.AbstractSessionFactory;
import org.snf4j.core.handler.IStreamHandler;
import org.snf4j.core.session.IStreamSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

@Slf4j
public class NetworkingServer {

    private SelectorLoop loop;

    public NetworkingServer(final int port) {
        new Thread(() -> start(port)).start();
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
            log.info("Error while starting networking server", e);
            System.exit(1);
        } finally {
            loop.stop();
        }
    }

    public void close() {
        loop.stop();
    }

    void addClient(final IStreamSession session) {

    }

    void removeClient(final long sessionId) {

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
