/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of Formmicro for Android.
 *
 * Formmicro for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Formmicro for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Formmicro for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.formmicro.gpslogger.loggers.customurl;

import android.location.Location;
import com.formmicro.gpslogger.common.AppSettings;
import com.formmicro.gpslogger.common.SerializableLocation;
import com.formmicro.gpslogger.common.Session;
import com.formmicro.gpslogger.common.Strings;
import com.formmicro.gpslogger.common.events.UploadEvents;
import com.formmicro.gpslogger.loggers.FileLogger;
import com.path.android.jobqueue.JobManager;


import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;

public class CustomUrlLogger implements FileLogger {

    private final String name = "URL";
    private final String customLoggingUrl;
    private final int batteryLevel;
    private final String androidId;
    private final String httpMethod;
    private final String httpBody;
    private final String httpHeaders;

    public CustomUrlLogger(String customLoggingUrl, int batteryLevel, String androidId, String httpMethod, String httpBody, String httpHeaders) {
        this.customLoggingUrl = customLoggingUrl;
        this.batteryLevel = batteryLevel;
        this.androidId = androidId;
        this.httpMethod = httpMethod;
        this.httpBody = httpBody;
        this.httpHeaders = httpHeaders;
    }

    @Override
    public void write(Location loc) throws Exception {
        if (!Session.getInstance().hasDescription()) {
            annotate("", loc);
        }
    }

    @Override
    public void annotate(String description, Location loc) throws Exception {

        String finalUrl = getFormattedTextblock(customLoggingUrl, loc, description, androidId, batteryLevel, Strings.getBuildSerial(),
                Session.getInstance().getStartTimeStamp(), Session.getInstance().getCurrentFormattedFileName());
        String finalBody = getFormattedTextblock(httpBody, loc, description, androidId, batteryLevel, Strings.getBuildSerial(),
                Session.getInstance().getStartTimeStamp(), Session.getInstance().getCurrentFormattedFileName());
        String finalHeaders = getFormattedTextblock(httpHeaders, loc, description, androidId, batteryLevel, Strings.getBuildSerial(),
                Session.getInstance().getStartTimeStamp(), Session.getInstance().getCurrentFormattedFileName());


        JobManager jobManager = AppSettings.getJobManager();
        jobManager.addJobInBackground(new CustomUrlJob(new CustomUrlRequest(finalUrl,httpMethod, finalBody, finalHeaders), new UploadEvents.CustomUrl()));
    }


    public String getFormattedTextblock(String customLoggingUrl, Location loc, String description, String androidId,
                                        float batteryLevel, String buildSerial, long sessionStartTimeStamp, String fileName)
            throws Exception {

        String logUrl = customLoggingUrl;
        SerializableLocation sLoc = new SerializableLocation(loc);
        logUrl = logUrl.replaceAll("(?i)%lat", String.valueOf(sLoc.getLatitude()));
        logUrl = logUrl.replaceAll("(?i)%lon", String.valueOf(sLoc.getLongitude()));
        logUrl = logUrl.replaceAll("(?i)%sat", String.valueOf(sLoc.getSatelliteCount()));
        logUrl = logUrl.replaceAll("(?i)%desc", String.valueOf(URLEncoder.encode(Strings.htmlDecode(description), "UTF-8")));
        logUrl = logUrl.replaceAll("(?i)%alt", String.valueOf(sLoc.getAltitude()));
        logUrl = logUrl.replaceAll("(?i)%acc", String.valueOf(sLoc.getAccuracy()));
        logUrl = logUrl.replaceAll("(?i)%dir", String.valueOf(sLoc.getBearing()));
        logUrl = logUrl.replaceAll("(?i)%prov", String.valueOf(sLoc.getProvider()));
        logUrl = logUrl.replaceAll("(?i)%spd", String.valueOf(sLoc.getSpeed()));
        logUrl = logUrl.replaceAll("(?i)%timestamp", String.valueOf(sLoc.getTime()/1000));
        logUrl = logUrl.replaceAll("(?i)%time", String.valueOf(Strings.getIsoDateTime(new Date(sLoc.getTime()))));
        logUrl = logUrl.replaceAll("(?i)%starttimestamp", String.valueOf(sessionStartTimeStamp/1000));
        logUrl = logUrl.replaceAll("(?i)%batt", String.valueOf(batteryLevel));
        logUrl = logUrl.replaceAll("(?i)%aid", String.valueOf(androidId));
        logUrl = logUrl.replaceAll("(?i)%ser", String.valueOf(buildSerial));
        logUrl = logUrl.replaceAll("(?i)%act", String.valueOf(sLoc.getDetectedActivity()));
        logUrl = logUrl.replaceAll("(?i)%filename", fileName);

        return logUrl;
    }


    @Override
    public String getName() {
        return name;
    }
}


