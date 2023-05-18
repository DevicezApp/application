package de.devicez.common.util;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

public class NetworkUtil {

    public static void sendMagicPaket(final InetAddress broadcastAddress, final byte[] hardwareAddress) throws IOException {
        final byte[] bytes = new byte[6 + 16 * hardwareAddress.length];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }
        for (int i = 6; i < bytes.length; i += hardwareAddress.length) {
            System.arraycopy(hardwareAddress, 0, bytes, i, hardwareAddress.length);
        }

        final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcastAddress, 9);
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.send(packet);
        }
    }

    public static byte[] getHardwareAddress() throws IOException {
        final NetworkInterface networkInterface = getCurrentInterface();
        if (networkInterface == null) {
            throw new IllegalStateException("network unavailable");
        }

        byte[] hardwareAddress = networkInterface.getHardwareAddress();
        if (hardwareAddress == null) {
            throw new IllegalStateException("network unavailable");
        }

        return hardwareAddress;
    }

    public static InetAddress getBroadcastAddress() throws IOException {
        final NetworkInterface networkInterface = getCurrentInterface();
        final List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
        for (final InterfaceAddress interfaceAddress : interfaceAddresses) {
            final InetAddress broadcast = interfaceAddress.getBroadcast();
            if (broadcast == null)
                continue;

            return broadcast;
        }

        return null;
    }

    public static NetworkInterface getCurrentInterface() throws IOException {
        final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterface.isUp()) {
                final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    final InetAddress address = inetAddresses.nextElement();
                    if (address.isSiteLocalAddress()) return networkInterface;
                }
            }
        }

        throw new IllegalStateException("unable to obtain network interface");
    }

    public static String formatHardwareAddress(final byte[] hardwareAddress) {
        final String[] hexadecimal = new String[hardwareAddress.length];
        for (int i = 0; i < hardwareAddress.length; i++) {
            hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
        }
        return String.join("-", hexadecimal);
    }
}
