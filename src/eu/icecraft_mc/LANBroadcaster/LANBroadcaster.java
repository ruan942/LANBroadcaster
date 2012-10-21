package eu.icecraft_mc.LANBroadcaster;

import org.bukkit.plugin.java.JavaPlugin;

public class LANBroadcaster extends JavaPlugin {
    public final Thread broadcastThread = new LANBroadcasterThread(this);

    @Override
    public void onDisable() {
        broadcastThread.interrupt();
    }

    @Override
    public void onEnable() {
        broadcastThread.start();
    }
}
