/*
 * Copyright (c) 2024.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.boiler.core.sources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.somewhatcity.boiler.api.CreateArgument;
import net.somewhatcity.boiler.api.CreateCommandArguments;
import net.somewhatcity.boiler.api.IBoilerSource;
import net.somewhatcity.boiler.api.display.IBoilerDisplay;
import net.somewhatcity.boiler.api.util.CommandArgumentType;
import net.somewhatcity.boiler.core.Util;
import net.somewhatcity.boiler.core.audio.BAudioPlayer;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

@CreateCommandArguments(arguments = {
        @CreateArgument(name = "buffer", type = CommandArgumentType.INTEGER),
        @CreateArgument(name = "url", type = CommandArgumentType.GREEDY_STRING)
})
public class BufferedFFMPEGSource implements IBoilerSource {
    private boolean running;
    private Queue<Short> audioQueue = new ArrayDeque<>();
    private BufferedImage image;
    private AudioFormat SOURCE_FORMAT = new AudioFormat(48000, 16, 1, true, true);
    //private final AudioFormat TARGET_FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000F, 16, 1, 2, 48000F, false);

    private final AudioFormat TARGET_FORMAT = new AudioFormat(48000, 16, 1, true, false);
    private Queue<BoilerFrame> buffer;
    private int bufferSize = 100;
    private BAudioPlayer bap;
    IBoilerDisplay display;
    private Java2DFrameConverter jconverter;
    private int imagesWithoutAudio;
    @Override
    public void load(IBoilerDisplay display, JsonObject data) {
        this.display = display;
        String streamUrl = data.get("url").getAsString();
        bufferSize = data.get("buffer").getAsInt();
        buffer = new ConcurrentLinkedDeque<>();
        bap = new BAudioPlayer(display);
        running = true;

        new Thread(() -> {
            try {

                jconverter = new Java2DFrameConverter();

                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(streamUrl);

                if(data.has("options")) {
                    for(JsonElement opt : data.get("options").getAsJsonArray()) {
                        grabber.setOption(
                                opt.getAsJsonObject().get("key").getAsString(),
                                opt.getAsJsonObject().get("value").getAsString()
                        );
                    }
                }

                grabber.start();
                SOURCE_FORMAT = new AudioFormat(grabber.getSampleRate(), 16, grabber.getAudioChannels(), true, true);

                boolean firstImageReceived = false;
                List<byte[]> audioDataSinceLastImage = new ArrayList<>();
                List<BufferedImage> imagesSinceLastAudio = new ArrayList<>();

                imagesWithoutAudio = 0;

                while (running) {
                    if(buffer.size() <= bufferSize) {
                        Frame frame = grabber.grabFrame();
                        if(frame != null) {
                            if(frame.image != null) {
                                firstImageReceived = true;
                                if(audioDataSinceLastImage.isEmpty()) {
                                    imagesWithoutAudio++;
                                    BufferedImage out = new BufferedImage(display.width(), display.height(), BufferedImage.TYPE_INT_ARGB);
                                    Graphics2D g2 = out.createGraphics();
                                    g2.drawImage(jconverter.convert(frame), 0, 0, display.width(), display.height(), null);
                                    g2.dispose();
                                    imagesSinceLastAudio.add(out);

                                } else if (imagesWithoutAudio > 1) {
                                    int audioSize = audioDataSinceLastImage.size();
                                    int imageSize = imagesSinceLastAudio.size();

                                    List<byte[]> audioToJoin = new ArrayList<>();
                                    int lastImg = 0;
                                    for(int i = 0; i < audioDataSinceLastImage.size(); i++) {
                                        int image = (int) Util.map(i, 0, audioSize, 0, imageSize);

                                        audioToJoin.add(audioDataSinceLastImage.get(i));
                                        if(image != lastImg) {
                                            byte[] audioData = joinByteArrays(audioToJoin);
                                            AudioInputStream source = new AudioInputStream(new ByteArrayInputStream(audioData), SOURCE_FORMAT, audioData.length);
                                            AudioInputStream converted = AudioSystem.getAudioInputStream(TARGET_FORMAT, source);

                                            byte[] convertedAudioData = converted.readAllBytes();

                                            buffer.add(new BoilerFrame(imagesSinceLastAudio.get(image), convertedAudioData));
                                            audioToJoin.clear();
                                        } else {
                                            lastImg = image;
                                        }
                                    }


                                    imagesWithoutAudio = 0;
                                    imagesSinceLastAudio.clear();
                                } else {
                                    BufferedImage out = new BufferedImage(display.width(), display.height(), BufferedImage.TYPE_INT_ARGB);
                                    Graphics2D g2 = out.createGraphics();
                                    g2.drawImage(jconverter.convert(frame), 0, 0, display.width(), display.height(), null);
                                    g2.dispose();

                                    byte[] audioData = joinByteArrays(audioDataSinceLastImage);
                                    AudioInputStream source = new AudioInputStream(new ByteArrayInputStream(audioData), SOURCE_FORMAT, audioData.length);
                                    AudioInputStream converted = AudioSystem.getAudioInputStream(TARGET_FORMAT, source);
                                    byte[] convertedAudioData = converted.readAllBytes();

                                    buffer.add(new BoilerFrame(out, convertedAudioData));
                                }
                                audioDataSinceLastImage.clear();
                            }
                            else if(frame.samples != null && firstImageReceived) {
                                ShortBuffer channelSamplesShortBuffer = (ShortBuffer) frame.samples[0];
                                channelSamplesShortBuffer.rewind();
                                ByteBuffer outBuffer = ByteBuffer.allocate(channelSamplesShortBuffer.capacity() * 2);
                                for (int i = 0; i < channelSamplesShortBuffer.capacity(); i++) {
                                    short val = channelSamplesShortBuffer.get(i);
                                    outBuffer.putShort(val);
                                }
                                audioDataSinceLastImage.add(outBuffer.array());


                            }
                        }
                    }
                }

            } catch (FrameGrabber.Exception e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            while (running) {
                if(!buffer.isEmpty()) {
                    if(bap.getAudioQueueSize() <= 960) {
                        BoilerFrame frame = buffer.poll();

                        image = frame.getImage();
                        bap.play(frame.getAudio());
                        //bap.queue(frame.getAudio());
                    }
                }
            }
        }).start();
    }

    public static byte[] joinByteArrays(List<byte[]> byteArrayList) {
        int totalLength = 0;
        for (byte[] byteArray : byteArrayList) {
            totalLength += byteArray.length;
        }

        byte[] result = new byte[totalLength];
        int currentPos = 0;

        for (byte[] byteArray : byteArrayList) {
            System.arraycopy(byteArray, 0, result, currentPos, byteArray.length);
            currentPos += byteArray.length;
        }

        return result;
    }

    @Override
    public void unload() {
        running = false;
        audioQueue.clear();
        bap.stop();
    }


    @Override
    public void draw(Graphics2D g2, Rectangle viewport) {
        g2.drawImage(image, 0, 0, viewport.width, viewport.height, null);
    }
    private static class BoilerFrame {
        private BufferedImage image;
        private byte[] audio;
        public BoilerFrame(BufferedImage image, byte[] audio) {
            this.image = image;
            this.audio = audio;
        }
        public BufferedImage getImage() {
            return image;
        }

        public byte[] getAudio() {
            return audio;
        }
    }
}
