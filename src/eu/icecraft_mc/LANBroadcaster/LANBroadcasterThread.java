package eu.icecraft_mc.LANBroadcaster;

import java.net.*;
import java.util.Enumeration;

public class LANBroadcasterThread extends Thread {
    private LANBroadcaster parent;
    private DatagramSocket socket;
    public boolean running = true;

    public LANBroadcasterThread(LANBroadcaster plugin) {
        super("LANBroadcaster");
        this.parent = plugin;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(3000);
        } catch (Exception e) {
            e.printStackTrace();
            parent.getLogger().severe("Host does not support datagram sockets; disabling.");
            parent.getPluginLoader().disablePlugin(parent);
            running = false;
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        running = false;
        socket.close();
    }

    @Override
    public void run() {
        try {
            String motd = parent.getServer().getMotd();
            String serverLanIP = getLanIP() + ":" + parent.getServer().getPort();
            byte[] ad = ("[MOTD]" + motd + "[/MOTD][AD]" + serverLanIP + "[/AD]").getBytes();
            DatagramPacket packet = new DatagramPacket(ad, ad.length, InetAddress.getByName("224.0.2.60"), 4445);
            parent.getLogger().info("Broadcasting " + serverLanIP + " over LAN.");
            while (!isInterrupted() && running) {
                try {
                    socket.send(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                try {
                    sleep(1500L);
                } catch (InterruptedException e) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getLanIP() throws Exception {
        if (!parent.getServer().getIp().equals("")) return parent.getServer().getIp();

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
            parent.getLogger().severe("Could not automatically detect LAN IP, please set server-ip in server.properties.");
            return InetAddress.getLocalHost().getHostAddress();
        }
    }
}
