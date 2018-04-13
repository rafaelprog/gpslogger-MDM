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

package com.formmicro.gpslogger.loggers.csv;

import android.location.Location;
import android.support.annotation.Nullable;

import com.formmicro.gpslogger.common.BundleConstants;
import com.formmicro.gpslogger.common.Maths;
import com.formmicro.gpslogger.common.PreferenceHelper;
import com.formmicro.gpslogger.common.Session;
import com.formmicro.gpslogger.common.Strings;
import com.formmicro.gpslogger.loggers.FileLogger;
import com.formmicro.gpslogger.loggers.Files;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Locale;


public class CSVFileLogger implements FileLogger {

    private final Integer batteryLevel;
    private File file;
    protected final String name = "TXT";
    private PreferenceHelper preferenceHelper = PreferenceHelper.getInstance();

    public CSVFileLogger(File file, @Nullable Integer batteryLevel) {
        this.file = file;
        this.batteryLevel = batteryLevel;
    }

    @Override
    public void write(Location loc) throws Exception {
        if (!Session.getInstance().hasDescription()) {
            annotate("", loc);
        }
    }

    String getCsvLine(Location loc, String dateTimeString) {
        return getCsvLine("", loc, dateTimeString);
    }

    String getCsvLine(String description, Location loc, String dateTimeString) {

        if (description.length() > 0) {
            description = "\"" + description.replaceAll("\"", "\"\"") + "\"";
        }

        String outputString = String.format(Locale.US, "%s,%f,%f,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", dateTimeString,
                loc.getLatitude(),
                loc.getLongitude(),
                loc.hasAltitude() ? loc.getAltitude() : "",
                loc.hasAccuracy() ? loc.getAccuracy() : "",
                loc.hasBearing() ? loc.getBearing() : "",
                loc.hasSpeed() ? loc.getSpeed() : "",
                Maths.getBundledSatelliteCount(loc),
                loc.getProvider(),
                (loc.getExtras() != null && !Strings.isNullOrEmpty(loc.getExtras().getString(BundleConstants.HDOP))) ? loc.getExtras().getString(BundleConstants.HDOP) : "",
                (loc.getExtras() != null && !Strings.isNullOrEmpty(loc.getExtras().getString(BundleConstants.VDOP))) ? loc.getExtras().getString(BundleConstants.VDOP) : "",
                (loc.getExtras() != null && !Strings.isNullOrEmpty(loc.getExtras().getString(BundleConstants.PDOP))) ? loc.getExtras().getString(BundleConstants.PDOP) : "",
                (loc.getExtras() != null && !Strings.isNullOrEmpty(loc.getExtras().getString(BundleConstants.GEOIDHEIGHT))) ? loc.getExtras().getString(BundleConstants.GEOIDHEIGHT) : "",
                (loc.getExtras() != null && !Strings.isNullOrEmpty(loc.getExtras().getString(BundleConstants.AGEOFDGPSDATA))) ? loc.getExtras().getString(BundleConstants.AGEOFDGPSDATA) : "",
                (loc.getExtras() != null && !Strings.isNullOrEmpty(loc.getExtras().getString(BundleConstants.DGPSID))) ? loc.getExtras().getString(BundleConstants.DGPSID) : "",
                (loc.getExtras() != null && !Strings.isNullOrEmpty(loc.getExtras().getString(BundleConstants.DETECTED_ACTIVITY))) ? loc.getExtras().getString(BundleConstants.DETECTED_ACTIVITY) : "",
                (batteryLevel != null) ? batteryLevel : "",
                description,preferenceHelper.getIMEIslot1(),preferenceHelper.getIMEIslot2(),preferenceHelper.getPhoneNumber(),preferenceHelper.getSignalStrenght()
        );
        return outputString;
    }

    @Override
    public void annotate(String description, Location loc) throws Exception {
        if (!file.exists()) {
            file.createNewFile();

            FileOutputStream writer = new FileOutputStream(file, true);
            BufferedOutputStream output = new BufferedOutputStream(writer);
            String header = "time,lat,lon,elevation,accuracy,bearing,speed,satellites,provider,hdop,vdop,pdop,geoidheight,ageofdgpsdata,dgpsid,activity,battery,annotation,IMEI Slot1, IMEI Slot 2, Telefone ,Signal Strenght\n";
            output.write(header.getBytes());
            output.flush();
            output.close();

        }

        FileOutputStream writer = new FileOutputStream(file, true);
        BufferedOutputStream output = new BufferedOutputStream(writer);

        String dateTimeString = Strings.getIsoDateTime(new Date(loc.getTime()));
        String csvLine = getCsvLine(description, loc, dateTimeString);


        output.write(csvLine.getBytes());
        output.flush();
        output.close();
        Files.addToMediaDatabase(file, "text/csv");
    }

    @Override
    public String getName() {
        return name;
    }



}
