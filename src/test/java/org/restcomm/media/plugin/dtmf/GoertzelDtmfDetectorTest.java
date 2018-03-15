/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.restcomm.media.plugin.dtmf;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restcomm.media.component.audio.AudioComponent;
import org.restcomm.media.component.audio.AudioMixer;
import org.restcomm.media.resource.dtmf.GeneratorImpl;
import org.restcomm.media.spi.dtmf.DtmfDetectorListener;
import org.restcomm.media.spi.dtmf.DtmfEvent;
import org.restcomm.media.spi.listener.TooManyListenersException;

/**
 *
 * @author yulian oifa
 */
public class GoertzelDtmfDetectorTest implements DtmfDetectorListener {
    
    private GoertzelDtmfDetector detector;
    private GeneratorImpl generator;
    
    private String tone;
    
    @Before
    public void setUp() throws TooManyListenersException {
        
        generator = new GeneratorImpl("dtmf", null);
        generator.setToneDuration(500);
        generator.setVolume(-20);
        
        detector = new GoertzelDtmfDetector(-35, 40, 500);
        
        detector.addListener(null);
    }
    
    @After
    public void tearDown() {
    	generator.deactivate();
    	detector.deactivate();
    }

    /**
     * Test of setDuration method, of class DetectorImpl.
     */
    @Test
    public void testDigit1() throws InterruptedException {
    	generator.setDigit("1");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
    	
        assertEquals("1", tone);    	
        
        tone="";
        generator.setOOBDigit("1");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
    	
        assertEquals("1", tone);
    }

    @Test
    public void testDigit2() throws InterruptedException {
        generator.setDigit("2");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
    	
        assertEquals("2", tone);
        
        tone="";
        generator.setOOBDigit("2");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
    	
        assertEquals("2", tone);
    }
    
    @Test
    public void testDigit3() throws InterruptedException {
        generator.setDigit("3");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
    	
        assertEquals("3", tone);
        
        tone="";
        generator.setOOBDigit("3");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
    	
        assertEquals("3", tone);
    }

    @Test
    public void testDigit4() throws InterruptedException {
        generator.setDigit("4");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
        
        assertEquals("4", tone);
        
        tone="";
        generator.setOOBDigit("4");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
    	
        assertEquals("4", tone);
    }
    
    @Test
    public void testDigit5() throws InterruptedException {
        generator.setDigit("5");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
        
        assertEquals("5", tone);
        
        tone="";
        generator.setOOBDigit("5");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
    	
        assertEquals("5", tone);
    }
    
    @Test
    public void testDigit6() throws InterruptedException {
        generator.setDigit("6");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
        
        assertEquals("6", tone);  
        
        tone="";
        generator.setOOBDigit("6");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
    	
        assertEquals("6", tone);
    }
    
    @Test
    public void testDigit7() throws InterruptedException {
        generator.setDigit("7");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
        
        assertEquals("7", tone);
        
        tone="";
        generator.setOOBDigit("7");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
    	
        assertEquals("7", tone);
    }
    
    @Test
    public void testDigit8() throws InterruptedException {
        generator.setDigit("8");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
        
        assertEquals("8", tone);
        
        tone="";
        generator.setOOBDigit("8");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
    	
        assertEquals("8", tone);
    }
    
    @Test
    public void testDigit9() throws InterruptedException {
        generator.setDigit("9");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
        
        assertEquals("9", tone);
        
        tone="";
        generator.setOOBDigit("9");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
    	
        assertEquals("9", tone);
    }
    
    @Test
    public void testDigit0() throws InterruptedException {
        generator.setDigit("0");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
        
        assertEquals("0", tone);
        
        tone="";
        generator.setOOBDigit("0");
        generator.activate();
        detector.activate();
        
        Thread.sleep(1000);
        
        generator.deactivate();
        detector.deactivate();
    	
        assertEquals("0", tone);
    }
    
    public void process(DtmfEvent event) {
        tone = event.getTone();
    }
}
