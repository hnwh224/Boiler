/*
 * Copyright (c) 2025.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.boiler.core.commands.display;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import net.somewhatcity.boiler.api.display.IBoilerDisplay;
import net.somewhatcity.boiler.core.commands.BoilerArguments;
import org.bukkit.Location;

public class DisplaySpeakerCommand extends CommandAPICommand {
    public DisplaySpeakerCommand() {
        super("speaker");
        withArguments(BoilerArguments.displayArgument("display"));
        withSubcommand(new CommandAPICommand("add")
                .withArguments(new LocationArgument("location", LocationType.BLOCK_POSITION))
                .executesPlayer((player, args) -> {
                    IBoilerDisplay display = (IBoilerDisplay) args.get("display");
                    Location location = (Location) args.get("location");

                    display.addSpeaker(location);
                    display.save();
                })
        );
        withSubcommand(new CommandAPICommand("clear")
                .executesPlayer((player, args) -> {
                    IBoilerDisplay display = (IBoilerDisplay) args.get("display");

                    display.clearSpeakers();
                    display.save();
                })
        );
        executes((sender, args) -> {
            sender.sendMessage("please specify an action!");
        });
    }
}
