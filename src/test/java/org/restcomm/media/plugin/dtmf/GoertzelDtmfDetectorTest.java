/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2018, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.restcomm.media.plugin.dtmf;

import net.ripe.hadoop.pcap.packet.Packet;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.restcomm.media.core.codec.g711.alaw.Decoder;
import org.restcomm.media.core.pcap.GenericPcapReader;
import org.restcomm.media.core.pcap.PcapFile;
import org.restcomm.media.core.resource.dtmf.DtmfEvent;
import org.restcomm.media.core.resource.dtmf.DtmfEventObserver;
import org.restcomm.media.core.rtp.RtpPacket;
import org.restcomm.media.core.spi.memory.Frame;
import org.restcomm.media.core.spi.memory.Memory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * @author yulian oifa
 * @author Vladimir Morosev (vladimir.morosev@telestax.com)
 */
public class GoertzelDtmfDetectorTest {

    private static final Logger log = Logger.getLogger(GoertzelDtmfDetectorTest.class);

    private ScheduledExecutorService scheduler;
    private Decoder decoder;

    @Before
    public void setUp() {
        scheduler = Executors.newScheduledThreadPool(1);
        decoder = new Decoder();
    }

    @After
    public void tearDown() {
        scheduler.shutdown();
    }

    @Test
    public void testDtmf4DigitsFast() throws InterruptedException {
        // given
        final int duration = 1200;
        final DtmfEventObserver observer = mock(DtmfEventObserver.class);
        final GoertzelDtmfDetector detector = new GoertzelDtmfDetector(-35, 100, 100);
        detector.observe(observer);

        // when
        playDtmfPcapFile("/dtmf_4_digits_fast.pcap", detector);

        // then
        ArgumentCaptor<DtmfEvent> argument = ArgumentCaptor.forClass(DtmfEvent.class);
        verify(observer, after(duration).times(4)).onDtmfEvent(argument.capture());
        List<DtmfEvent> capturedEvents = argument.getAllValues();
        assertEquals("1", capturedEvents.get(0).getTone());
        assertEquals("2", capturedEvents.get(1).getTone());
        assertEquals("3", capturedEvents.get(2).getTone());
        assertEquals("4", capturedEvents.get(3).getTone());

        detector.forget(observer);
    }

    @Test
    public void testDtmf4DigitsSlow() throws InterruptedException {
        // given
        final int duration = 6400;
        final DtmfEventObserver observer = mock(DtmfEventObserver.class);
        final GoertzelDtmfDetector detector = new GoertzelDtmfDetector(-35, 100, 500);
        detector.observe(observer);

        // when
        playDtmfPcapFile("/dtmf_4_digits_slow.pcap", detector);

        // then
        ArgumentCaptor<DtmfEvent> argument = ArgumentCaptor.forClass(DtmfEvent.class);
        verify(observer, after(duration).times(4)).onDtmfEvent(argument.capture());
        List<DtmfEvent> capturedEvents = argument.getAllValues();
        assertEquals("1", capturedEvents.get(0).getTone());
        assertEquals("2", capturedEvents.get(1).getTone());
        assertEquals("3", capturedEvents.get(2).getTone());
        assertEquals("4", capturedEvents.get(3).getTone());

        detector.forget(observer);
    }

    @Test
    public void testDtmf2DigitPairs() throws InterruptedException {
        // given
        final int duration = 4100;
        final DtmfEventObserver observer = mock(DtmfEventObserver.class);
        final GoertzelDtmfDetector detector = new GoertzelDtmfDetector(-35, 100, 200);
        detector.observe(observer);

        // when
        playDtmfPcapFile("/dtmf_2_digit_pairs.pcap", detector);

        // then
        ArgumentCaptor<DtmfEvent> argument = ArgumentCaptor.forClass(DtmfEvent.class);
        verify(observer, after(duration).times(4)).onDtmfEvent(argument.capture());
        List<DtmfEvent> capturedEvents = argument.getAllValues();
        assertEquals("1", capturedEvents.get(0).getTone());
        assertEquals("1", capturedEvents.get(1).getTone());
        assertEquals("2", capturedEvents.get(2).getTone());
        assertEquals("2", capturedEvents.get(3).getTone());

        detector.forget(observer);
    }

    public void playDtmfPcapFile(String resourceName, GoertzelDtmfDetector detector) {
        final URL inputFileUrl = this.getClass().getResource(resourceName);
        PcapFile pcap = new PcapFile(inputFileUrl);
        try {
            pcap.open();
            final PlayPacketTask task = new PlayPacketTask(pcap, detector, null, 0, 0.0);
            scheduler.scheduleAtFixedRate(task, 0L, 20L, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            log.error("Could not read file", e);
            fail("DTMF tone detector test file access error");
        }
    }

    private class PlayPacketTask implements Runnable {

        private PcapFile pcap;
        private GoertzelDtmfDetector detector;
        private double lastPacketTimestamp;

        public PlayPacketTask(PcapFile pcap, GoertzelDtmfDetector detector, byte[] decodedData, int duration, double timestamp) {
            this.pcap = pcap;
            this.detector = detector;
            this.lastPacketTimestamp = timestamp;
        }

        public void run() {
            if (!pcap.isComplete()) {
                final Packet packet = pcap.read();
                byte[] payload = (byte[]) packet.get(GenericPcapReader.PAYLOAD);

                final RtpPacket rtpPacket = new RtpPacket(false);
                rtpPacket.wrap(payload);

                final byte[] rtpPayload = new byte[rtpPacket.getPayloadLength()];
                rtpPacket.getPayload(rtpPayload);

                final double timestamp = (double) packet.get(Packet.TIMESTAMP_USEC);
                final int duration = (lastPacketTimestamp == 0.0) ? 20 : (int) ((timestamp - lastPacketTimestamp) * 1000);;

                Frame encodedFrame = Memory.allocate(rtpPayload.length);
                encodedFrame.setOffset(0);
                encodedFrame.setLength(rtpPayload.length);
                encodedFrame.setFormat(decoder.getSupportedInputFormat());
                encodedFrame.setTimestamp(System.currentTimeMillis());
                encodedFrame.setDuration(duration);
                System.arraycopy(rtpPayload, 0, encodedFrame.getData(), 0, rtpPayload.length);
                Frame decodedFrame = decoder.process(encodedFrame);

                this.detector.detect(decodedFrame.getData(), duration);
            } else {
                try {
                    pcap.close();
                } catch (IOException e) {
                    log.error("Could not read file", e);
                    fail("DTMF tone detector test file access error");
                }
            }
        }
    }
}
