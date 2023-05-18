package de.devicez.common.util;

import java.io.IOException;
import java.net.*;
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
        final InetAddress address = InetAddress.getLocalHost();
        final NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
        return networkInterface.getHardwareAddress();
    }

    public static InetAddress getBroadcastAddress() throws IOException {
        final InetAddress address = InetAddress.getLocalHost();
        final NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
        final List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
        for (final InterfaceAddress interfaceAddress : interfaceAddresses) {
            final InetAddress broadcast = interfaceAddress.getBroadcast();
            if (broadcast == null)
                continue;

            return broadcast;
        }

        return null;
    }

    public static String formatHardwareAddress(final byte[] hardwareAddress) {
        final String[] hexadecimal = new String[hardwareAddress.length];
        for (int i = 0; i < hardwareAddress.length; i++) {
            hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
        }
        return String.join("-", hexadecimal);
    }
}
