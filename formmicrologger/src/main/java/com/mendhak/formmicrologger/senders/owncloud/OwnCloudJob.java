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

package com.formmicro.gpslogger.senders.owncloud;

import android.net.Uri;
import com.formmicro.gpslogger.common.AppSettings;
import com.formmicro.gpslogger.common.network.LocalX509TrustManager;
import com.formmicro.gpslogger.common.network.Networks;
import com.formmicro.gpslogger.common.events.UploadEvents;
import com.formmicro.gpslogger.common.slf4j.Logs;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.AdvancedSslSocketFactory;
import com.owncloud.android.lib.common.network.AdvancedX509TrustManager;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.UploadRemoteFileOperation;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import de.greenrobot.event.EventBus;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.slf4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.security.GeneralSecurityException;

public class OwnCloudJob extends Job implements OnRemoteOperationListener {

    private static final Logger LOG = Logs.of(OwnCloudJob.class);


    String servername;
    String username;
    String password;
    String directory;
    File localFile;
    String remoteFileName;

    public OwnCloudJob(String servername, String username, String password, String directory,
                         File localFile, String remoteFileName)
    {
        super(new Params(1).requireNetwork().persist().addTags(getJobTag(localFile)));
        this.servername = servername;
        this.username = username;
        this.password = password;
        this.directory = directory;
        this.localFile = localFile;
        this.remoteFileName = remoteFileName;

    }

    @Override
    public void onAdded() {
        LOG.debug("ownCloud Job: onAdded");
    }

    @Override
    public void onRun() throws Throwable {

        LOG.debug("ownCloud Job: Uploading  '" + localFile.getName() + "'");

        Protocol pr = Protocol.getProtocol("https");

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(
                    null,
                    new TrustManager[] { new LocalX509TrustManager(Networks.getKnownServersStore(AppSettings.getInstance())) },
                    null
            );

            ProtocolSocketFactory psf = new AdvancedSslSocketFactory(sslContext, new AdvancedX509TrustManager(Networks.getKnownServersStore(AppSettings.getInstance())), null);


            Protocol.registerProtocol( "https", new Protocol("https", psf, 443));

        } catch (GeneralSecurityException e) {
            LOG.error("Self-signed confident SSL context could not be loaded", e);
        }


        OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(Uri.parse(servername), AppSettings.getInstance(), true);
        client.setDefaultTimeouts('\uea60', '\uea60');
        client.setFollowRedirects(true);
        client.setCredentials(
                OwnCloudCredentialsFactory.newBasicCredentials(username, password)
        );

        //Create the folder, in case it doesn't already exist on OwnCloud.
        CreateRemoteFolderOperation createOperation = new CreateRemoteFolderOperation(directory, false);
        createOperation.execute( client);

        String remotePath = directory + FileUtils.PATH_SEPARATOR + localFile.getName();
        String mimeType = "application/octet-stream"; //unused
        UploadRemoteFileOperation uploadOperation = new UploadRemoteFileOperation(localFile.getAbsolutePath(), remotePath, mimeType);
        uploadOperation.execute(client,this,null);
    }

    @Override
    protected void onCancel() {

        LOG.debug("ownCloud Job: onCancel");
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        LOG.error("Could not upload to OwnCloud", throwable);
        EventBus.getDefault().post(new UploadEvents.OwnCloud().failed("Could not upload to OwnCloud", throwable));
        return false;
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation remoteOperation, RemoteOperationResult result) {

        if (!result.isSuccess()) {
            LOG.error(result.getLogMessage(), result.getException());
            EventBus.getDefault().post(new UploadEvents.OwnCloud().failed(result.getLogMessage(), result.getException()));
        } else  {
            EventBus.getDefault().post(new UploadEvents.OwnCloud().succeeded());
        }

        LOG.debug("ownCloud Job: onRun finished");
    }

    public static String getJobTag(File gpxFile) {
        return "OWNCLOUD" + gpxFile.getName();
    }
}