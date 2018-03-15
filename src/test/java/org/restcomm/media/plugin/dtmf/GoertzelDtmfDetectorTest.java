/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.restcomm.media.plugin.dtmf;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restcomm.media.core.resource.dtmf.DtmfDetector;
import org.restcomm.media.core.resource.dtmf.DtmfDetectorListener;
import org.restcomm.media.resource.dtmf.GeneratorImpl;
import org.restcomm.media.rtp.RtpPacket;
import org.restcomm.media.scheduler.Clock;
import org.restcomm.media.scheduler.PriorityQueueScheduler;
import org.restcomm.media.scheduler.WallClock;
import org.restcomm.media.spi.memory.Frame;
import org.restcomm.media.spi.memory.Memory;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.restcomm.media.pcap.GenericPcapReader;
import org.restcomm.media.pcap.PcapFile;

import net.ripe.hadoop.pcap.packet.Packet;
import net.ripe.hadoop.pcap.PcapReader;

import static org.junit.Assert.*;

/**
 *
 * @author yulian oifa
 * @author Vladimir Morosev (vladimir.morosev@telestax.com)
 */
public class GoertzelDtmfDetectorTest implements DtmfDetectorListener {

    private static final Logger log = Logger.getLogger(GoertzelDtmfDetectorTest.class);

    private Clock clock;
    private PriorityQueueScheduler scheduler;

    private GoertzelDtmfDetector detector;
    private GeneratorImpl generator;
    
    private String tone;
    
    @Before
    public void setUp() {
        
        clock = new WallClock();

        scheduler = new PriorityQueueScheduler();
        scheduler.setClock(clock);
        scheduler.start();

        generator = new GeneratorImpl("dtmf", scheduler);
        generator.setToneDuration(500);
        generator.setVolume(-20);
        
        detector = new GoertzelDtmfDetector(-35, 40, 500);
        
        detector.addListener(this);
    }
    
    @After
    public void tearDown() {
        detector.removeListener(this);
    	generator.deactivate();
        scheduler.stop();
    }

    @Test
    public void testPcapFile() {
        // given
        final URL inputFileUrl = this.getClass().getResource("/dtmf-g711-recording.pcap");
        final org.restcomm.media.codec.g711.ulaw.Decoder decoder = new org.restcomm.media.codec.g711.ulaw.Decoder();

        // when
        int framesDetected = 0;
        int framesCount = 0;
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
                framesCount++;
                detector.detect(decodedFrame.getData(), 20);
                framesDetected++;
            }
            pcap.close();
        } catch (IOException e) {
            log.error("Could not read file", e);
            fail("Speech Detector test file access error");
        }

        // then
        assertTrue((double)framesDetected / (double)framesCount > 0.38);
    }

    @Override
    public void onDtmfDetected(String tone) {
    }

    /**
     * Test of setDuration method, of class GoertzelDtmfDetector.
     */
    //@Test
    public void testDigit1() throws InterruptedException {
    	generator.setDigit("1");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
    	
        assertEquals("1", tone);    	
        
        tone="";
        generator.setOOBDigit("1");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
    	
        assertEquals("1", tone);
    }

    //@Test
    public void testDigit2() throws InterruptedException {
        generator.setDigit("2");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
    	
        assertEquals("2", tone);
        
        tone="";
        generator.setOOBDigit("2");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
    	
        assertEquals("2", tone);
    }
    
    //@Test
    public void testDigit3() throws InterruptedException {
        generator.setDigit("3");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
    	
        assertEquals("3", tone);
        
        tone="";
        generator.setOOBDigit("3");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
    	
        assertEquals("3", tone);
    }

    //@Test
    public void testDigit4() throws InterruptedException {
        generator.setDigit("4");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        
        assertEquals("4", tone);
        
        tone="";
        generator.setOOBDigit("4");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
    	
        assertEquals("4", tone);
    }
    
    //@Test
    public void testDigit5() throws InterruptedException {
        generator.setDigit("5");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        
        assertEquals("5", tone);
        
        tone="";
        generator.setOOBDigit("5");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
    	
        assertEquals("5", tone);
    }
    
    //@Test
    public void testDigit6() throws InterruptedException {
        generator.setDigit("6");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        
        assertEquals("6", tone);  
        
        tone="";
        generator.setOOBDigit("6");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
    	
        assertEquals("6", tone);
    }
    
    //@Test
    public void testDigit7() throws InterruptedException {
        generator.setDigit("7");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        
        assertEquals("7", tone);
        
        tone="";
        generator.setOOBDigit("7");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
    	
        assertEquals("7", tone);
    }
    
    //@Test
    public void testDigit8() throws InterruptedException {
        generator.setDigit("8");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        
        assertEquals("8", tone);
        
        tone="";
        generator.setOOBDigit("8");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
    	
        assertEquals("8", tone);
    }
    
    //@Test
    public void testDigit9() throws InterruptedException {
        generator.setDigit("9");
        generator.activate();
        
        generator.deactivate();
        
        assertEquals("9", tone);
        
        tone="";
        generator.setOOBDigit("9");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
    	
        assertEquals("9", tone);
    }
    
    //@Test
    public void testDigit0() throws InterruptedException {
        generator.setDigit("0");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        
        assertEquals("0", tone);
        
        tone="";
        generator.setOOBDigit("0");
        generator.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
    	
        assertEquals("0", tone);
    }
}
