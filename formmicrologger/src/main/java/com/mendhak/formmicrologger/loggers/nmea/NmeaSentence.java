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

package com.formmicro.gpslogger.loggers.nmea;


import com.formmicro.gpslogger.common.Strings;

public class NmeaSentence {

    String[] nmeaParts;

    public NmeaSentence(String nmeaSentence){
        if(Strings.isNullOrEmpty(nmeaSentence)){
            nmeaParts = new String[]{""};
            return;
        }
        nmeaParts = nmeaSentence.split(",");

    }

    public boolean isLocationSentence(){
        return nmeaParts[0].equalsIgnoreCase("$GPGSA") || nmeaParts[0].equalsIgnoreCase("$GPGGA");
    }

    public String getLatestPdop(){
        if (nmeaParts[0].equalsIgnoreCase("$GPGSA")) {

            if (nmeaParts.length > 15 && !Strings.isNullOrEmpty(nmeaParts[15])) {
                return nmeaParts[15];
            }
        }

        return null;
    }

    public String getLatestVdop(){
        if (nmeaParts[0].equalsIgnoreCase("$GPGSA")) {
            if (nmeaParts.length > 17 &&!Strings.isNullOrEmpty(nmeaParts[17]) && !nmeaParts[17].startsWith("*")) {
                return nmeaParts[17].split("\\*")[0];
            }
        }

        return null;
    }

    public String getLatestHdop(){
        if (nmeaParts[0].equalsIgnoreCase("$GPGGA")) {
            if (nmeaParts.length > 8 &&!Strings.isNullOrEmpty(nmeaParts[8])) {
                return nmeaParts[8];
            }
        }
        else if (nmeaParts[0].equalsIgnoreCase("$GPGSA")) {
            if (nmeaParts.length > 16 &&!Strings.isNullOrEmpty(nmeaParts[16])) {
                    return nmeaParts[16];
                }
        }

        return null;
    }

    public String getGeoIdHeight(){
        if (nmeaParts[0].equalsIgnoreCase("$GPGGA")) {
            if (nmeaParts.length > 11 &&!Strings.isNullOrEmpty(nmeaParts[11])) {
                return nmeaParts[11];
            }
        }

        return null;
    }

    public String getAgeOfDgpsData(){
        if (nmeaParts[0].equalsIgnoreCase("$GPGGA")) {
            if (nmeaParts.length > 13 && !Strings.isNullOrEmpty(nmeaParts[13])) {
                return nmeaParts[13];
            }
        }

        return null;
    }

    public String getDgpsId(){
        if (nmeaParts[0].equalsIgnoreCase("$GPGGA")) {
            if (nmeaParts.length > 14 &&!Strings.isNullOrEmpty(nmeaParts[14]) && !nmeaParts[14].startsWith("*")) {
                return nmeaParts[14].split("\\*")[0];
            }
        }

        return null;
    }

}
