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

package com.formmicro.gpslogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.formmicro.gpslogger.common.IntentConstants;
import com.formmicro.gpslogger.common.PreferenceHelper;
import com.formmicro.gpslogger.common.slf4j.Logs;
import org.slf4j.Logger;

public class StartupReceiver extends BroadcastReceiver {

    private static final Logger LOG = Logs.of(StartupReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            boolean startImmediately = PreferenceHelper.getInstance().shouldStartLoggingOnBootup();

            LOG.info("Start on bootup - " + String.valueOf(startImmediately));

            if (startImmediately) {

                Intent serviceIntent = new Intent(context, GpsLoggingService.class);
                serviceIntent.putExtra(IntentConstants.IMMEDIATE_START, true);
                context.startService(serviceIntent);
            }
        } catch (Exception ex) {
            LOG.error("StartupReceiver", ex);

        }

    }

}
