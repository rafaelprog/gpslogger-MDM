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

package com.formmicro.gpslogger.senders.dropbox;




import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import com.formmicro.gpslogger.common.PreferenceHelper;
import com.formmicro.gpslogger.common.events.UploadEvents;
import com.formmicro.gpslogger.common.slf4j.Logs;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import de.greenrobot.event.EventBus;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;


public class DropboxJob extends Job {


    private static final Logger LOG = Logs.of(DropboxJob.class);
    private static PreferenceHelper preferenceHelper = PreferenceHelper.getInstance();
    String fileName;


    protected DropboxJob(String fileName) {
        super(new Params(1).requireNetwork().persist().addTags(getJobTag(fileName)));

        this.fileName = fileName;
    }

    @Override
    public void onAdded() {
        LOG.debug("Dropbox job added");
    }

    @Override
    public void onRun() throws Throwable {
        File gpsDir = new File(preferenceHelper.getgpsloggerFolder());
        File gpxFile = new File(gpsDir, fileName);

        try {
            LOG.debug("Beginning upload to dropbox...");
            InputStream inputStream = new FileInputStream(gpxFile);
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("Formmicro").build();
            DbxClientV2 mDbxClient = new DbxClientV2(requestConfig, PreferenceHelper.getInstance().getDropBoxAccessKeyName());
            mDbxClient.files().uploadBuilder("/" + fileName).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
            EventBus.getDefault().post(new UploadEvents.Dropbox().succeeded());
        } catch (Exception e) {
            LOG.error("Could not upload to Dropbox" , e);
            EventBus.getDefault().post(new UploadEvents.Dropbox().failed(e.getMessage(), e));
        }

    }


    @Override
    protected void onCancel() {
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        EventBus.getDefault().post(new UploadEvents.Dropbox().failed("Could not upload to Dropbox", throwable));
        LOG.error("Could not upload to Dropbox", throwable);
        return false;
    }

    public static String getJobTag(String fileName) {
        return "DROPBOX" + fileName;
    }
}
