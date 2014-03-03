package lanbroadcaster;

import java.net.*;
import java.util.Enumeration;

public class LANBroadcaster implements Runnable {
    private LANBroadcasterPlugin plugin;
    private DatagramSocket socket;
    int failcount;

    public LANBroadcaster(LANBroadcasterPlugin parent) {
        this.plugin = parent;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(3000);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().severe("Host does not support UDP datagram sockets.");
        }
    }

    @Override
    public void run() {
        try {
            final byte[] ad = getAd();
            final DatagramSocket socket = this.socket;
            final DatagramPacket packet = new DatagramPacket(ad, ad.length, InetAddress.getByName("224.0.2.60"), 4445);
            while (true) {
                try {
                    try {
                        socket.send(packet);
                        failcount = 0;
                    } catch (Throwable ex) {
                        if (failcount++ == 0) {
                            ex.printStackTrace();
                        }
                        if (failcount < 5) {
                            plugin.getLogger().warning("Failed to broadcast, trying again in 10 seconds...");
                        } else {
                            plugin.getLogger().severe("Broadcasting will not work until the network is fixed. Warnings disabled.");
                        }
                        Thread.sleep(8500);
                    }
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        socket.close();
    }

    private byte[] getAd() {
        int port = plugin.getServer().getPort();
        String motd = plugin.getServer().getMotd(), ip = getLanIP(), ad = ip + ":" + port;
        if (isBukkit1_6()) {
            ad = String.valueOf(port);
            plugin.getLogger().info("Broadcasting server with port " + ad + " over LAN.");
        } else {
            plugin.getLogger().info("Broadcasting " + ip + " over LAN.");
        }
        byte[] adBytes = ("[MOTD]" + motd + "[/MOTD][AD]" + ad + "[/AD]").getBytes();
        return adBytes;
    }

    private String getLanIP() {
        String configuredIP = plugin.getServer().getIp();
        if (!configuredIP.equals("")) return configuredIP;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) return address.getHostAddress();
                }
            }
            throw new Exception("No usable IPv4 non-loopback address found");
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().severe("Could not automatically detect LAN IP, please set server-ip in server.properties.");
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
                plugin.getLogger().severe("No network interfaces found!");
                return "End of the world";
            }
        }
    }

    private boolean isBukkit1_6() {
        try {
            Class.forName("org.bukkit.entity.Horse"); // avoiding CB version parsing hell
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }
}