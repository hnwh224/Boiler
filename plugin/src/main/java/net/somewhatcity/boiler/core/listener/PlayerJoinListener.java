/*
 * Copyright (c) 2024.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.boiler.core.listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.somewhatcity.boiler.core.BoilerPlugin;
import net.somewhatcity.boiler.core.net.FriendlyByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.UUID;

public class PlayerJoinListener implements Listener {

    public static HashMap<UUID, Long> playerTimeJoined = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        System.out.println("PLAYER JOIN");
        playerTimeJoined.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
        Bukkit.getScheduler().runTaskLater(BoilerPlugin.getPlugin(), () -> {
            e.getPlayer().setMetadata("boiler_mod", new FixedMetadataValue(BoilerPlugin.getPlugin(), false));


            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeBoolean(true);

            e.getPlayer().sendPluginMessage(BoilerPlugin.getPlugin(), "boiler:handshake_request", out.toByteArray());
            System.out.println("Send boiler handshake request to %s".formatted(e.getPlayer().getName()));



        }, 20 * 1);

    }

}
