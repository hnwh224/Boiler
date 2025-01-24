/*
 * Copyright (c) 2025.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.boiler.core.commands;

import dev.jorel.commandapi.CommandAPICommand;
import net.somewhatcity.boiler.core.BoilerPlugin;
import net.somewhatcity.boiler.core.Util;

import java.util.Locale;
import java.util.UUID;

public class BoilerClientCommand extends CommandAPICommand {
    public BoilerClientCommand() {
        super("client");
        withSubcommand(new CommandAPICommand("credentials")
                .executesPlayer((player, args) -> {
                    String username = "bsc_" + player.getName().replace("_", "").toLowerCase(Locale.ROOT);
                    String password = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

                    BoilerPlugin.getPlugin().mediaMtxAuthServer().registerUser(username, password);

                    Util.sendMsg(player, "Your credentials for streaming to the server:");
                    Util.sendMsg(player, "Username: %s", username);
                    Util.sendMsg(player, "Password: %s", password);
                })
        );
    }
}
