/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

package org.restcomm.media.plugin.dtmf.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Vladimir Morosev (vladimir.morosev@telestax.com) created on 15/03/2018
 */
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "media-plugin-dtmf-detector-goertzel")
@ConditionalOnProperty(value = "media-plugin-dtmf-detector-goertzel.enabled", havingValue = "true")
public class GoertzelDtmfDetectorConfiguration {

    private int toneVolume;
    private int toneDuration;
    private int toneInterval;

    public GoertzelDtmfDetectorConfiguration() {
        this.toneVolume = 0;
        this.toneDuration = 0;
	this.toneInterval = 0;
    }

    public int getToneVolume() {
        return toneVolume;
    }

    public void setStartNoiseThreshold(int toneVolume) {
        this.toneVolume = toneVolume;
    }

    public int getToneDuration() {
        return toneDuration;
    }

    public void setToneDuration(int toneDuration) {
        this.toneDuration = toneDuration;
    }
   
    public int getToneInterval() {
        return toneInterval;
    }

    public void setToneInterval(int toneInterval) {
        this.toneInterval = toneInterval;
    }
}
