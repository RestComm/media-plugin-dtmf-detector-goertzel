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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restcomm.media.core.component.audio.GoertzelFilter;
import org.restcomm.media.core.resource.dtmf.detector.AbstractDtmfDetector;
import org.restcomm.media.core.resource.dtmf.detector.DtmfEvent;

/**
 * Implements inband DTMF detector.
 * <p>
 * Inband means that DTMF is transmitted within the audio of the phone conversation, i.e. it is audible to the conversation
 * partners. Therefore only uncompressed codecs like g711 alaw or ulaw can carry inband DTMF reliably. Female voice are known to
 * once in a while trigger the recognition of a DTMF tone. For analog lines inband is the only possible means to transmit DTMF.
 * <p>
 * Though Inband DTMF detection may work for other codecs like SPEEX, GSM, G729 as DtmfDetector is using DSP in front of
 * InbandDetector there is no guarantee that it will always work. In future MMS may not have DSP in front of InbandDetector and
 * hence Inband detection for codecs like SPEEX, GSM, G729 may completely stop
 *
 * @author yulian oifa
 * @author amit bhayani
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 */
public class GoertzelDtmfDetector extends AbstractDtmfDetector {

    private static final Logger logger = LogManager.getLogger(GoertzelDtmfDetector.class);

    private final static String[][] events = new String[][]{{"1", "2", "3", "A"}, {"4", "5", "6", "B"}, {"7", "8", "9", "C"}, {"*", "0", "#", "D"}};

    private final static int[] lowFreq = new int[]{697, 770, 852, 941};
    private final static int[] highFreq = new int[]{1209, 1336, 1477, 1633};

    private final GoertzelFilter[] lowFreqFilters = new GoertzelFilter[4];
    private final GoertzelFilter[] highFreqFilters = new GoertzelFilter[4];

    private final double threshold;

    private int offset;

    private final int toneVolume;
    private final int toneDuration;
    private final int toneInterval;
    private final int N;

    private final double p[];
    private final double P[];

    private final double[] signal;
    private double maxAmpl;
    private String lastTone;
    private long elapsedTime;
    private volatile boolean waiting;

    public GoertzelDtmfDetector(int toneVolume, int toneDuration, int toneInterval) {
        // Detector Configuration
        this.threshold = Math.pow(Math.pow(10, toneVolume), 0.1) * Short.MAX_VALUE;
        this.toneVolume = toneVolume;
        this.toneDuration = toneDuration;
        this.toneInterval = toneInterval;
        double scale = toneDuration / 1000.0;
        this.N = 8 * toneDuration;
        this.signal = new double[N];
        for (int i = 0; i < 4; i++) {
            this.lowFreqFilters[i] = new GoertzelFilter(lowFreq[i], N, scale);
            this.highFreqFilters[i] = new GoertzelFilter(highFreq[i], N, scale);
        }

        // Runtime Detection
        this.p = new double[4];
        this.P = new double[4];
        this.offset = 0;
        this.lastTone = "";
        this.elapsedTime = 0;
        this.waiting = false;
    }

    public GoertzelDtmfDetector() {
        this(-35, 80, 20);
    }

    @Override
    public void detect(byte[] data, long duration) {
        // If Detector is in WAITING state, then drop packets
        // until a period of data (based on frame duration accumulation) elapses.
        if (waiting) {
            this.elapsedTime += duration;
            this.waiting = (this.elapsedTime < this.toneInterval);

            if (waiting) {
                return;
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Waiting: " + waiting + " [last tone=" + this.lastTone + ", elapsed time=" + elapsedTime + "]");
                }
            }
        }

        int M = data.length;
        int k = 0;
        while (k < M) {
            while (offset < N && k < M - 1) {
                double s = ((data[k++] & 0xff) | (data[k++] << 8));
                double sa = Math.abs(s);
                if (sa > maxAmpl) {
                    maxAmpl = sa;
                }
                signal[offset++] = s;
            }

            // if dtmf buffer full check signal
            if (offset == N) {
                offset = 0;

                // and if max amplitude of signal is greater threshold
                // try to detect tone.
                if (maxAmpl >= threshold) {
                    maxAmpl = 0;

                    getPower(lowFreqFilters, signal, 0, p);
                    getPower(highFreqFilters, signal, 0, P);

                    String tone = getTone(p, P);

                    if (tone != null) {
                        // Keep reference to latest identified tone
                        this.elapsedTime = 0;
                        this.lastTone = tone;
                        this.waiting = true;

                        if (logger.isTraceEnabled()) {
                            logger.trace("Waiting: " + waiting + " [last tone=" + this.lastTone + ", elapsed time=" + elapsedTime + "]");
                        }

                        // Inform liteners about DTMF tone detection
                        notify(new DtmfEvent(tone));
                    }
                }
            }
        }
    }

    private void getPower(GoertzelFilter[] filters, double[] data, int offset, double[] power) {
        for (int i = 0; i < 4; i++) {
            power[i] = filters[i].getPower(data, offset);
        }
    }

    /**
     * Searches maximum value in the specified array.
     *
     * @param data input data.
     * @return the index of the maximum value in the data array.
     */
    private int getMax(double data[]) {
        int idx = 0;
        double max = data[0];
        for (int i = 1; i < data.length; i++) {
            if (max < data[i]) {
                max = data[i];
                idx = i;
            }
        }
        return idx;
    }

    /**
     * Searches DTMF tone.
     *
     * @param f the low frequency array
     * @param F the high frequency array.
     * @return DTMF tone.
     */
    private String getTone(double f[], double F[]) {
        int fm = getMax(f);
        boolean fd = true;

        for (int i = 0; i < f.length; i++) {
            if (fm == i) {
                continue;
            }
            double r = f[fm] / (f[i] + 1E-15);
            if (r < threshold) {
                fd = false;
                break;
            }
        }

        if (!fd) {
            return null;
        }

        int Fm = getMax(F);
        boolean Fd = true;

        for (int i = 0; i < F.length; i++) {
            if (Fm == i) {
                continue;
            }
            double r = F[Fm] / (F[i] + 1E-15);
            if (r < threshold) {
                Fd = false;
                break;
            }
        }

        if (!Fd) {
            return null;
        }

        return events[fm][Fm];
    }

}

