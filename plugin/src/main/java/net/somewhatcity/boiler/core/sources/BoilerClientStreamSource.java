/*
 * Copyright (c) 2025.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.boiler.core.sources;

import com.google.gson.JsonObject;
import net.somewhatcity.boiler.api.IBoilerSource;
import net.somewhatcity.boiler.api.SourceConfig;
import net.somewhatcity.boiler.api.display.IBoilerDisplay;
import net.somewhatcity.boiler.api.util.SourceType;
import net.somewhatcity.boiler.core.BoilerConfig;
import net.somewhatcity.boiler.core.BoilerPlugin;
import net.somewhatcity.boiler.core.MediaMtx;
import net.somewhatcity.boiler.core.display.BoilerClientUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@SourceConfig(
        sourceType = SourceType.CLIENT
)
public class BoilerClientStreamSource implements IBoilerSource {

    private IBoilerDisplay display;
    private String stream;
    private Set<Player> waitingForStreamInit = new HashSet<>();
    private boolean streamInit = true;
    private boolean running = true;

    @Override
    public void load(IBoilerDisplay display, JsonObject data) {
        this.display = display;

        String host = BoilerConfig.rtmpHost;
        String bind = BoilerConfig.rtmpBind;
        int port = BoilerConfig.rtmpPort;

        this.stream = String.format("rtsp://{user}:{password}@%s:%s/live/display_%s", host, port, display.id());

    }

    @Override
    public void unload() {
        running = false;
    }
}
