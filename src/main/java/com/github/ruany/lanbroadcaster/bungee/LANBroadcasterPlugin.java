package com.github.ruany.lanbroadcaster.bungee;

import com.github.ruany.lanbroadcaster.LANBroadcaster;
import lombok.val;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.ChatColor;

import java.util.*;

/**
 * Created by ruan on 2015-03-28 at 05:24.
 */
public class LANBroadcasterPlugin extends Plugin {
    private List<LANBroadcaster> broadcasters = new ArrayList<LANBroadcaster>();

    @Override
    public void onDisable() {
        for (LANBroadcaster broadcaster : broadcasters) {
            broadcaster.setRunning(false);
        }
        broadcasters.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEnable() {
        ProxyServer proxy = getProxy();
        Collection<?> listeners = proxy.getConfigurationAdapter().getList("listeners", null);
        for (Object obj : listeners) {
            Map<String, Object> map = (Map<String, Object>) obj;

            String host = (String) map.get("host");
            String[] split = host.split(":", 2);

            String addr = split[0];
            int port = Integer.parseInt(split[1]);

            if (addr.equals("0.0.0.0") || addr.equals("127.0.0.1")) addr = "";
            val broadcaster = new LANBroadcaster(LANBroadcaster.createSocket(),
                    port,
                    translateColorCodes((String) map.get("motd")),
                    addr, getLogger());
            broadcasters.add(broadcaster);
        }
        for (LANBroadcaster broadcaster : broadcasters) {
            proxy.getScheduler().runAsync(this, broadcaster);
        }
    }

    private static String translateColorCodes(String text) {
        char[] b = text.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = ChatColor.COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }
}
