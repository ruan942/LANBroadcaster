package eu.icecraft_mc.LANBroadcaster;

import org.bukkit.plugin.java.JavaPlugin;

public class LANBroadcaster extends JavaPlugin {
    public Thread broadcastThread;

    @Override
    public void onDisable() {
        broadcastThread.interrupt();
    }

    @Override
    public void onEnable() {
        broadcastThread = new LANBroadcasterThread(this);
        broadcastThread.start();
    }
}
