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

package org.restcomm.media.plugin.dtmf.spring;

import org.restcomm.media.core.resource.dtmf.DtmfDetector;
import org.restcomm.media.core.resource.dtmf.DtmfDetectorProvider;
import org.restcomm.media.plugin.dtmf.GoertzelDtmfDetector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Goertzel DTMF detector implemented as Spring Boot plugin component.
 *
 * @author Vladimir Morosev (vladimir.morosev@telestax.com)
 */
@Component("media-plugin-dtmf-detector-goertzel")
@ConditionalOnBean(GoertzelDtmfDetectorSpringProvider.class)
public class GoertzelDtmfDetectorSpringProvider implements DtmfDetectorProvider {

    private int toneVolume;
    private int toneDuration;
    private int toneInterval;

    public GoertzelDtmfDetectorSpringProvider(GoertzelDtmfDetectorConfiguration configuration) {
        this.toneVolume = configuration.getToneVolume();
        this.toneDuration = configuration.getToneDuration();
        this.toneInterval = configuration.getToneInterval();
    }

    public DtmfDetector provide() {
        return new GoertzelDtmfDetector(toneVolume, toneDuration, toneInterval);
    }

}

