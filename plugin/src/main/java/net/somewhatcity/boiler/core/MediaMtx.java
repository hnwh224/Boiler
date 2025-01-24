/*
 * Copyright (c) 2025.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.boiler.core;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class MediaMtx {

    private Process process;


    public void start() {
        Thread thread = new Thread(() -> {
            try {
                ProcessBuilder pb = null;
                String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
                File installDir = BoilerPlugin.getPlugin().getDataFolder();

                if(os.contains("win")) {
                    pb = new ProcessBuilder(new File(installDir, "mediamtx.exe").getAbsolutePath());
                }
                else if(os.contains("nux")) {
                    pb = new ProcessBuilder(new File(installDir, "mediamtx").getAbsolutePath());
                }

                pb.directory(installDir);
                File dir = pb.directory();
                System.out.println("DIR: " + dir.getAbsolutePath());
                pb.inheritIO();
                process = pb.start();
                process.waitFor();
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }

        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        process.children().forEach(ProcessHandle::destroy);
        process.destroy();
    }

    private final OkHttpClient httpClient = new OkHttpClient();

    public StreamState getStreamState(String path) {
        Request request = new Request.Builder()
                .url("http://127.0.0.1:9997/v3/paths/get/" + path)
                .build();

        try(Response response = httpClient.newCall(request).execute()) {
            if(response.code() == 200) return StreamState.ONLINE;
            if(response.code() == 404) return StreamState.OFFLINE;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return StreamState.UNKNOWN;
    }

    public static enum StreamState {
        ONLINE, OFFLINE, UNKNOWN
    }

}
