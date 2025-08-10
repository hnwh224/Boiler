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
import net.somewhatcity.boiler.api.display.IBoilerDisplay;
import net.somewhatcity.boiler.core.Util;
import net.somewhatcity.boiler.core.commands.BoilerArguments;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DisplayInfoCommand extends CommandAPICommand {
    public DisplayInfoCommand() {
        super("info");
        withArguments(BoilerArguments.displayArgument("display"));
        executes((sender, args) -> {
            IBoilerDisplay display = (IBoilerDisplay) args.get(0);
            String viewers = Arrays.toString(display.viewers().stream().map(Player::getName).toArray());
            Util.sendMsg(sender, "Viewers: %s".formatted(viewers));
        });
    }
}
