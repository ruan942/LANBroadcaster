package eu.icecraft_mc.LANBroadcaster;

import java.net.*;

public class LANBroadcasterThread extends Thread {
    private LANBroadcaster parent;
    private DatagramSocket socket;
    public boolean running = true;

    public LANBroadcasterThread(LANBroadcaster plugin) {
        super("LANBroadcaster");
        setDaemon(true);
        this.parent = plugin;
        try {
            socket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        running = false;
    }

    @Override
    public void run() {
        while (!parent.isEnabled())
            try {
                sleep(100L);
            } catch (InterruptedException e) {}
        String motd = parent.getServer().getMotd();
        String serverLanIP = parent.getServer().getIp();
        byte[] ad = ("[MOTD]" + motd + "[/MOTD][AD]" + serverLanIP + "[/AD]").getBytes();

        while (!isInterrupted() && running) {
            try {
                socket.send(new DatagramPacket(ad, ad.length, InetAddress.getByName("224.0.2.60"), 4445));
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
            try {
                sleep(1500L);
            } catch (InterruptedException e) {}
        }
    }
}
