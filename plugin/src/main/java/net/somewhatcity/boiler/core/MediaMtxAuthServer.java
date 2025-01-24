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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.javalin.Javalin;

import java.util.HashMap;

public class MediaMtxAuthServer {

    private Javalin app;
    private static HashMap<String, String> credentials = new HashMap<>();

    public MediaMtxAuthServer() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(BoilerPlugin.class.getClassLoader());
        app = Javalin.create().start(7000);
        Thread.currentThread().setContextClassLoader(classLoader);

        app.exception(Exception.class, (e, context) -> {
            e.printStackTrace();
        });

        app.post("/auth", ctx -> {
            String body = ctx.body();
            System.out.println(body);

            try {
                JsonObject json = (JsonObject) JsonParser.parseString(body);
            } catch (JsonSyntaxException e) {
                System.out.println("Invalid JSON format: " + e.getMessage());
                ctx.status(400).result("Invalid JSON");
                return;
            }

            JsonObject json = (JsonObject) JsonParser.parseString(body);
            System.out.println("parsed");

            String ip = json.get("ip").getAsString();
            String user = json.get("user").getAsString();
            String password = json.get("password").getAsString();

            System.out.println("auth attempt with ip=%s user=%s password=%s".formatted(ip, user, password));

            if(ip.equals("::1")) {
                ctx.status(200);
            }
            else {
                if(credentials.containsKey(user) && credentials.get(user).equals(password)) {
                    System.out.println("auth success");
                    ctx.status(200);
                }
                else {
                    System.out.println("auth fail");
                    ctx.status(401);
                }
            }
        });

    }

    public void registerUser(String user, String password) {
        credentials.put(user, password);
    }

    public void stop() {
        app.stop();
    }
}
