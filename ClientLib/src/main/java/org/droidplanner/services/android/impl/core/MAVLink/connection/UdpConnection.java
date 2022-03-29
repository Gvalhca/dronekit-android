package org.droidplanner.services.android.impl.core.MAVLink.connection;

import android.content.Context;
import android.os.Bundle;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides support for mavlink connection via udp.
 */
public abstract class UdpConnection extends MavLinkConnection {

    private final AtomicReference<DatagramSocket> socketRef = new AtomicReference<>();
    private int serverPort;
    private int hostPort;
    private InetAddress hostAdd;
    private DatagramPacket sendPacket;
    private DatagramPacket receivePacket;

    protected UdpConnection(Context context) {
        super(context);
    }

    private void getUdpStream(Bundle extras) throws IOException {
        final DatagramSocket socketReceive = new DatagramSocket(serverPort);
        socketReceive.setBroadcast(true);
        socketReceive.setReuseAddress(true);
//        NetworkUtils.bindSocketToNetwork(extras, socketReceive);
        socketRef.set(socketReceive);
    }

    @Override
    public final void closeConnection() throws IOException {
        final DatagramSocket socketReceive = socketRef.get();
        if (socketReceive != null) {
            socketReceive.close();
        }
    }

    @Override
    public final void openConnection(Bundle connectionExtras) throws IOException {
        getUdpStream(connectionExtras);
        onConnectionOpened(connectionExtras);
    }

    @Override
    public final void sendBuffer(byte[] buffer) throws IOException {
        final DatagramSocket socket = socketRef.get();
        if (socket == null) {
            return;
        }
        try {
            if (hostAdd != null) { // We can't send to our sister until they
                // have connected to us
                if (sendPacket == null) {
                    sendPacket = new DatagramPacket(buffer, buffer.length, hostAdd, hostPort);
                } else {
                    sendPacket.setData(buffer, 0, buffer.length);
                    sendPacket.setAddress(hostAdd);
                    sendPacket.setPort(hostPort);
                }
                socket.send(sendPacket);
//                System.out.println("Sent packet: " + Arrays.toString(sendPacket.getData()) + bytesToHex(sendPacket.getData()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendBuffer(InetAddress targetAddr, int targetPort, byte[] buffer) throws IOException {
        final DatagramSocket socket = socketRef.get();
        if (socket == null || targetAddr == null || buffer == null) {
            return;
        }

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, targetAddr, targetPort);
        socket.send(packet);
    }

    @Override
    public final int readDataBlock(byte[] readData) throws IOException {
        final DatagramSocket socket = socketRef.get();
        if (socket == null) {
            return 0;
        }

        if (receivePacket == null) {
            receivePacket = new DatagramPacket(readData, readData.length);
        } else {
            receivePacket.setData(readData);
        }

        socket.receive(receivePacket);
        hostAdd = receivePacket.getAddress();
        hostPort = receivePacket.getPort();

//        System.out.println("Received packet: " + bytesToHex(receivePacket.getData()));
        return receivePacket.getLength();
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public final void loadPreferences() {
        serverPort = loadServerPort();
    }

    @Override
    public final int getConnectionType() {
        return MavLinkConnectionTypes.MAVLINK_CONNECTION_UDP;
    }

    protected abstract int loadServerPort();
}
