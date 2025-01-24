/*
 * Copyright (c) 2025.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.boiler.core.display;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.somewhatcity.boiler.api.display.IBoilerDisplay;
import net.somewhatcity.boiler.core.BoilerPlugin;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BoilerClientUtil {

    public static void sendCreateDisplay(Player player, IBoilerDisplay display, String stream) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(display.id());

        out.writeInt(6);
        out.writeInt(display.cornerA().getBlockX());
        out.writeInt(display.cornerA().getBlockY());
        out.writeInt(display.cornerA().getBlockZ());
        out.writeInt(display.cornerB().getBlockX());
        out.writeInt(display.cornerB().getBlockY());
        out.writeInt(display.cornerB().getBlockZ());

        out.writeInt(display.mapDisplay().width()); // display width
        out.writeInt(display.mapDisplay().height()); // display height
        out.writeInt(facingToInt(display.facing())); // display facing
        out.writeInt(0); // rotation



        if (display.speakers().isEmpty()) {
            out.writeInt(0);
        } else {
            out.writeInt(display.speakers().size() * 3);
            for (Location speaker : display.speakers()) {
                out.writeInt(speaker.getBlockX());
                out.writeInt(speaker.getBlockY());
                out.writeInt(speaker.getBlockZ());
            }
        }

        out.writeUTF(stream);


        player.sendPluginMessage(BoilerPlugin.getPlugin(), "boiler:display_create", out.toByteArray());
    }


    public static void sendRemoveDisplay(Player player, IBoilerDisplay display) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(display.id()); // root map

        player.sendPluginMessage(BoilerPlugin.getPlugin(), "boiler:display_remove", out.toByteArray());
    }

    public static int facingToInt(BlockFace face) {
        switch (face) {
            case NORTH -> {
                return 0;
            }
            case EAST -> {
                return 1;
            }
            case SOUTH -> {
                return 2;
            }
            case WEST -> {
                return 3;
            }
            case UP -> {
                return 4;
            }
            case DOWN -> {
                return 5;
            }
            default -> {
                return  0;
            }
        }
    }
}
