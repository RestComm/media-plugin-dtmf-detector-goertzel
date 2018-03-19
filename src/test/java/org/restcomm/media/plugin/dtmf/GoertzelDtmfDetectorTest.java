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
import org.restcomm.media.core.pcap.GenericPcapReader;
import org.restcomm.media.core.pcap.PcapFile;
import org.restcomm.media.core.resource.dtmf.DtmfDetectorListener;
import org.restcomm.media.core.rtp.RtpPacket;
import org.restcomm.media.core.spi.memory.Frame;
import org.restcomm.media.core.spi.memory.Memory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author yulian oifa
 * @author Vladimir Morosev (vladimir.morosev@telestax.com)
 */
public class GoertzelDtmfDetectorTest {

    private static final Logger log = Logger.getLogger(GoertzelDtmfDetectorTest.class);

    private GoertzelDtmfDetector detector;

    @Before
    public void setUp() {
        detector = new GoertzelDtmfDetector(-35, 40, 500);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDtmf4DigitsFast() {

        // given
        final DtmfDetectorListener observer = mock(DtmfDetectorListener.class);
        detector.observe(observer);

        // when
        playDtmfPcapFile("/dtmf_4_digits_fast.pcap", observer);

        // then
        verify(observer, times(1)).onDtmfDetected("1");
        verify(observer, times(1)).onDtmfDetected("2");
        verify(observer, times(1)).onDtmfDetected("3");
        verify(observer, times(1)).onDtmfDetected("4");

        detector.forget(observer);
    }

    @Test
    public void testDtmf4DigitsSlow() {

        // given
        final DtmfDetectorListener observer = mock(DtmfDetectorListener.class);
        detector.observe(observer);

        // when
        playDtmfPcapFile("/dtmf_4_digits_slow.pcap", observer);

        // then
        verify(observer, times(1)).onDtmfDetected("1");
        verify(observer, times(1)).onDtmfDetected("2");
        verify(observer, times(1)).onDtmfDetected("3");
        verify(observer, times(1)).onDtmfDetected("4");

        detector.forget(observer);
    }

    @Test
    public void testDtmf2DigitPairs() {

        // given
        final DtmfDetectorListener observer = mock(DtmfDetectorListener.class);
        detector.observe(observer);

        // when
        playDtmfPcapFile("/dtmf_2_digit_pairs.pcap", observer);

        // then
        verify(observer, times(2)).onDtmfDetected("1");
        verify(observer, times(2)).onDtmfDetected("2");

        detector.forget(observer);
    }

    public void playDtmfPcapFile(String resourceName, DtmfDetectorListener observer) {

        final URL inputFileUrl = this.getClass().getResource(resourceName);
        final org.restcomm.media.core.codec.g711.alaw.Decoder decoder = new org.restcomm.media.core.codec.g711.alaw.Decoder();

        PcapFile pcap = new PcapFile(inputFileUrl);
        try {
            pcap.open();
            while (!pcap.isComplete()) {
                final Packet packet = pcap.read();
                byte[] payload = (byte[]) packet.get(GenericPcapReader.PAYLOAD);

                final RtpPacket rtpPacket = new RtpPacket(false);
                rtpPacket.wrap(payload);

                byte[] rtpPayload = new byte[rtpPacket.getPayloadLength()];
                rtpPacket.getPayload(rtpPayload);

                Frame encodedFrame = Memory.allocate(rtpPayload.length);
                encodedFrame.setOffset(0);
                encodedFrame.setLength(rtpPayload.length);
                encodedFrame.setFormat(decoder.getSupportedInputFormat());
                encodedFrame.setTimestamp(System.currentTimeMillis());
                encodedFrame.setDuration(20);
                System.arraycopy(rtpPayload, 0, encodedFrame.getData(), 0, rtpPayload.length);
                Frame decodedFrame = decoder.process(encodedFrame);
                detector.detect(decodedFrame.getData(), 20);
            }
            pcap.close();
        } catch (IOException e) {
            log.error("Could not read file", e);
            fail("DTMF tone detector test file access error");
        }
    }
}
