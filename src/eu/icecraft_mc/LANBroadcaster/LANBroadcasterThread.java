package eu.icecraft_mc.LANBroadcaster;

import java.net.*;
import java.util.Enumeration;

public class LANBroadcasterThread extends Thread {
    private LANBroadcaster plugin;
    private DatagramSocket socket;

    public LANBroadcasterThread(LANBroadcaster parent) {
        super("LANBroadcaster");
        this.plugin = parent;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(3000);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().severe("Host does not support datagram sockets; disabling.");
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    @Override
    public void run() {
        try {
            byte[] ad = getAd();
            DatagramPacket packet = new DatagramPacket(ad, ad.length, InetAddress.getByName("224.0.2.60"), 4445);
            while (!isInterrupted()) {
                socket.send(packet);
                try {
                    sleep(1500L);
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
        String motd = plugin.getServer().getMotd();
        String ip = getLanIP();
        int port = plugin.getServer().getPort();
        String ad = ip + ":" + port;
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
        if (!plugin.getServer().getIp().equals("")) return plugin.getServer().getIp();
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
                return "End of the world";
            }
        }
    }

    private boolean isBukkit1_6() {
        try {
            Class.forName("org.bukkit.entity.Horse"); // just another class
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }
}
