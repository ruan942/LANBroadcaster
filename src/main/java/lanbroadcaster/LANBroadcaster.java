package lanbroadcaster;

import lombok.*;

import java.net.*;
import java.util.Enumeration;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class LANBroadcaster implements Runnable {
    public static final String BROADCAST_HOST = "224.0.2.60";
    public static final int BROADCAST_PORT = 4445;
    private int failcount = 0;
    private final DatagramSocket socket;
    private final int port;
    private final String motd;
    private final String configuredIP;
    private final Logger logger;
    @Setter
    private boolean running = true;

    public static DatagramSocket createSocket() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return socket;
    }

    @Override
    public void run() {
        try {
            final byte[] ad = getAd();
            final DatagramPacket packet = new DatagramPacket(ad, ad.length, InetAddress.getByName(BROADCAST_HOST), BROADCAST_PORT);
            broadcast(socket, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        socket.close();
    }

    private void broadcast(final DatagramSocket socket, final DatagramPacket packet) {
        try {
            while (running) {
                try {
                    socket.send(packet);
                    failcount = 0;
                } catch (Throwable ex) {
                    fail(ex);
                }
                Thread.sleep(1500);
            }
        } catch (InterruptedException ignored) {}
    }

    private void fail(Throwable ex) throws InterruptedException {
        if (failcount++ == 0) {
            ex.printStackTrace();
        }
        if (failcount < 5) {
            logger.warning("Failed to broadcast, trying again in 10 seconds...");
        } else if (failcount == 5) {
            logger.severe("Broadcasting will not work until the network is fixed. Warnings disabled.");
        }
        Thread.sleep(8500);
    }

    @SneakyThrows
    private byte[] getAd() {
        String ip = getLanIP(), ad = ip + ':' + port;
        if (isBukkit1_6() || isBungee()) {
            ad = String.valueOf(port);
            logger.info("Broadcasting server with port " + ad + " over LAN.");
        } else {
            logger.info("Broadcasting " + ip + " over LAN.");
        }
        String str = "[MOTD]" + motd + "[/MOTD][AD]" + ad + "[/AD]";
        return str.getBytes("UTF-8");
    }

    private String getLanIP() {
        if (!configuredIP.equals("")) return configuredIP;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
            throw new Exception("No usable IPv4 non-loopback address found");
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Could not automatically detect LAN IP, please set server-ip in server.properties.");
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
                logger.severe("No network interfaces found!");
                return "End of the world";
            }
        }
    }

    private boolean isBukkit1_6() {
        try {
            Class.forName("org.bukkit.entity.Horse"); // avoiding CB version parsing hell
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    private boolean isBungee() {
        try {
            Class.forName("net.md_5.bungee.api.plugin.Plugin");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
