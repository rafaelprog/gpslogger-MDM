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

package com.formmicro.gpslogger.common.slf4j;

import ch.qos.logback.core.rolling.RollingFileAppender;
import com.formmicro.gpslogger.common.PreferenceHelper;


public class GpsRollingFileAppender<E> extends RollingFileAppender<E> {

    @Override
    protected void subAppend(E e) {

        //This extends the RollingFileAppender.
        // It checks if the user has requested a
        // debug log file and only then writes
        // to a file.
        if (PreferenceHelper.getInstance().shouldDebugToFile()) {
            super.subAppend(e);
        }
    }
}
