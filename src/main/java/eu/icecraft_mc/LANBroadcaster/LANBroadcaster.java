package eu.icecraft_mc.LANBroadcaster;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class LANBroadcaster extends JavaPlugin {
    final String payload = "[MOTD]%s[/MOTD][AD]%s[/AD]";
    final String multicastAddress = "224.0.2.60";
    final int PORT = 4445;

    private InetAddress group;
    private MulticastSocket socket;

    @Override
    public void onEnable() {
        try {
            this.group = InetAddress.getByName("224.0.2.60");
            this.socket = new MulticastSocket(this.PORT);

            this.socket.setTimeToLive(3);
            this.socket.joinGroup(this.group);

            final String actualIp = Bukkit.getIp().isEmpty() ? InetAddress.getLocalHost().getHostAddress() : Bukkit.getIp();
            final String formattedPayload = String.format(this.payload, Bukkit.getMotd(), actualIp);
            this.getLogger().info("Multicast packet payload: " + formattedPayload);

            final byte[] b = formattedPayload.getBytes();
            final DatagramPacket d = new DatagramPacket(b, b.length, this.group, this.PORT);

            Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new BukkitRunnable() {
                
                AtomicBoolean running = new AtomicBoolean();
                
                @Override
                public void run() {                    
                    try {
                        if (!running.compareAndSet(false, true)) return;
                        LANBroadcaster.this.socket.send(d);
                        running.set(false);
                    } catch (final IOException e) {
                        throw new RuntimeException(LANBroadcaster.this.toString() + " failed to send broadcast packet", e);
                    }
                }
            }, 30l, 30l);
        } catch (final IOException e) {
            this.getLogger().log(Level.SEVERE, e.getMessage(), e);
            this.getPluginLoader().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        this.socket.close();
    }
}
