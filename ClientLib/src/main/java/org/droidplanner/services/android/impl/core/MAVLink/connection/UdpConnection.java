package org.droidplanner.services.android.impl.core.MAVLink.connection;

import android.content.Context;
import android.os.Bundle;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides support for mavlink connection via udp.
 */
public abstract class UdpConnection extends MavLinkConnection {

    private final AtomicReference<DatagramSocket> socketRef = new AtomicReference<>();
    private final AtomicReference<DatagramSocket> socketSendRef = new AtomicReference<>();
    private int serverPort;
    final private String espAdd = "0.0.0.0";
    private int hostPort;
    private InetAddress hostAdd;
    private DatagramPacket sendPacket;
    private DatagramPacket receivePacket;

    protected UdpConnection(Context context) {
        super(context);
    }

    private void getUdpStream(Bundle extras) throws IOException {
        final DatagramSocket socketReceive = new DatagramSocket(serverPort);
//        final DatagramSocket socketReceive = new DatagramSocket(serverPort, InetAddress.getByName(espAdd));
        socketReceive.setBroadcast(true);
        socketReceive.setReuseAddress(true);
//        socketReceive.connect(socketAdd);
//        NetworkUtils.bindSocketToNetwork(extras, socketReceive);
        socketRef.set(socketReceive);

        System.out.println(socketReceive.getLocalPort());
        System.out.println(socketReceive.getLocalAddress().getHostAddress());
        System.out.println(socketReceive.getLocalSocketAddress());
        System.out.println(socketReceive.getRemoteSocketAddress());
        System.out.println(socketReceive.getSoTimeout());
        System.out.println(socketReceive.isConnected());

        final DatagramSocket socketSend = new DatagramSocket(null);
        socketSend.setBroadcast(true);
        socketSend.setReuseAddress(true);

        socketSendRef.set(socketSend);
    }

    @Override
    public final void closeConnection() throws IOException {
        final DatagramSocket socketReceive = socketRef.get();
        final DatagramSocket socketSend = socketSendRef.get();
        if (socketReceive != null) {
            socketReceive.close();
            socketSend.close();
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
        InetAddress socketAdd = InetAddress.getByName(espAdd);

        try {
            if (hostAdd != null) { // We can't send to our sister until they
                // have connected to us
                if (sendPacket == null) {
                    sendPacket = new DatagramPacket(buffer, buffer.length, hostAdd, hostPort);
                } else {
//                    sendPacket = new DatagramPacket(buffer, 0, buffer.length, hostAdd, hostPort);
                    sendPacket.setData(buffer, 0, buffer.length);
                    sendPacket.setAddress(hostAdd);
                    sendPacket.setPort(hostPort);
                }
                socket.send(sendPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendBuffer(InetAddress targetAddr, int targetPort, byte[] buffer) throws IOException {
        final DatagramSocket socket = socketSendRef.get();
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
//        if (!socket.isConnected()) {
//            socket.connect(hostAdd, hostPort);
//        }
        return receivePacket.getLength();
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
