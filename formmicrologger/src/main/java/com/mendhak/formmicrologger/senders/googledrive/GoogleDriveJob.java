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

package com.formmicro.gpslogger.senders.googledrive;

import com.formmicro.gpslogger.common.PreferenceHelper;
import com.formmicro.gpslogger.common.Strings;
import com.formmicro.gpslogger.common.events.UploadEvents;
import com.formmicro.gpslogger.common.slf4j.Logs;
import com.formmicro.gpslogger.loggers.Streams;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import de.greenrobot.event.EventBus;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class GoogleDriveJob extends Job {
    private static final Logger LOG = Logs.of(GoogleDriveJob.class);
    String token;
    File gpxFile;
    String googleDriveFolderName;

    protected GoogleDriveJob(File gpxFile, String googleDriveFolderName) {
        super(new Params(1).requireNetwork().persist().addTags(getJobTag(gpxFile)));
        this.gpxFile = gpxFile;
        this.googleDriveFolderName = googleDriveFolderName;

    }

    public static String getJobTag(File gpxFile){
        return "GOOGLEDRIVE" + gpxFile.getName();
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

        GoogleDriveManager manager = new GoogleDriveManager(PreferenceHelper.getInstance());
        token = manager.getToken();

        FileInputStream fis = new FileInputStream(gpxFile);
        String fileName = gpxFile.getName();

        String formmicrologgerFolderId = PreferenceHelper.getInstance().getGoogleDriveFolderId();
        LOG.debug("Formmicro folder ID - " + formmicrologgerFolderId);

        if(Strings.isNullOrEmpty(formmicrologgerFolderId) || !folderExists(token, formmicrologgerFolderId)){
            LOG.debug("Formmicro folder not found, searching by name");
            formmicrologgerFolderId = getFileIdFromFileName(token, googleDriveFolderName, null);

            if(Strings.isNullOrEmpty(formmicrologgerFolderId)){
                LOG.debug("Formmicro folder still not found, will create");
                formmicrologgerFolderId = createEmptyFile(token, googleDriveFolderName, "application/vnd.google-apps.folder", "root");
            }

            if (Strings.isNullOrEmpty(formmicrologgerFolderId)) {
                EventBus.getDefault().post(new UploadEvents.GDrive().failed("Could not create folder"));
                return;
            }

            PreferenceHelper.getInstance().setGoogleDriveFolderId(formmicrologgerFolderId);
        }

        //Now search for the file
        String gpxFileId = getFileIdFromFileName(token, fileName, formmicrologgerFolderId);

        if (Strings.isNullOrEmpty(gpxFileId)) {
            //Create empty file first
            gpxFileId = createEmptyFile(token, fileName, getMimeTypeFromFileName(fileName), formmicrologgerFolderId);

            if (Strings.isNullOrEmpty(gpxFileId)) {
                EventBus.getDefault().post(new UploadEvents.GDrive().failed("Could not create file"));
                return;
            }
        }

        if (!Strings.isNullOrEmpty(gpxFileId)) {
            //Set file's contents
            updateFileContents(token, gpxFileId, Streams.getByteArrayFromInputStream(fis), fileName);
        }
        EventBus.getDefault().post(new UploadEvents.GDrive().succeeded());
    }



    private String updateFileContents(String authToken, String gpxFileId, byte[] fileContents, String fileName) {
        HttpURLConnection conn = null;
        String fileId = null;

        String fileUpdateUrl = "https://www.googleapis.com/upload/drive/v2/files/" + gpxFileId + "?uploadType=media";

        try {

            URL url = new URL(fileUpdateUrl);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("User-Agent", "Formmicro for Android");
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
            conn.setRequestProperty("Content-Type", getMimeTypeFromFileName(fileName));
            conn.setRequestProperty("Content-Length", String.valueOf(fileContents.length));

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

			conn.setConnectTimeout(10000);
			conn.setReadTimeout(30000);

            DataOutputStream wr = new DataOutputStream(
                    conn.getOutputStream());
            wr.write(fileContents);
            wr.flush();
            wr.close();

            String fileMetadata = Streams.getStringFromInputStream(conn.getInputStream());

            JSONObject fileMetadataJson = new JSONObject(fileMetadata);
            fileId = fileMetadataJson.getString("id");
            LOG.debug("File updated : " + fileId);

        } catch (Exception e) {
            LOG.error("Could not update contents", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return fileId;
    }

    private String createEmptyFile(String authToken, String fileName, String mimeType, String parentFolderId) {

        String fileId = null;
        HttpURLConnection conn = null;

        String createFileUrl = "https://www.googleapis.com/drive/v2/files";

        String createFilePayload = "   {\n" +
                "             \"title\": \"" + fileName + "\",\n" +
                "             \"mimeType\": \"" + mimeType + "\",\n" +
                "             \"parents\": [\n" +
                "              {\n" +
                "               \"id\": \"" + parentFolderId + "\"\n" +
                "              }\n" +
                "             ]\n" +
                "            }";

        try {

            URL url = new URL(createFileUrl);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Formmicro for Android");
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
            conn.setRequestProperty("Content-Type", "application/json");

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

			conn.setConnectTimeout(10000);
			conn.setReadTimeout(30000);

            DataOutputStream wr = new DataOutputStream(
                    conn.getOutputStream());
            wr.writeBytes(createFilePayload);
            wr.flush();
            wr.close();

            fileId = null;

            String fileMetadata = Streams.getStringFromInputStream(conn.getInputStream());

            JSONObject fileMetadataJson = new JSONObject(fileMetadata);
            fileId = fileMetadataJson.getString("id");
            LOG.debug("File created with ID " + fileId + " of type " + mimeType);

        } catch (Exception e) {
            LOG.error("Could not create file", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }

        }

        return fileId;
    }


    private String getFileIdFromFileName(String authToken, String fileName, String inFolderId) {

        HttpURLConnection conn = null;
        String fileId = "";

        try {

            fileName = URLEncoder.encode(fileName, "UTF-8");

            String inFolderParam = "";
            if(!Strings.isNullOrEmpty(inFolderId)){
                inFolderParam = "+and+'" + inFolderId + "'+in+parents";
            }

            //To search in a folder:
            String searchUrl = "https://www.googleapis.com/drive/v2/files?q=title%20%3D%20%27" + fileName + "%27%20and%20trashed%20%3D%20false" + inFolderParam;

            URL url = new URL(searchUrl);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Formmicro for Android");
            conn.setRequestProperty("Authorization", "OAuth " + authToken);
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(30000);
	
            String fileMetadata = Streams.getStringFromInputStream(conn.getInputStream());


            JSONObject fileMetadataJson = new JSONObject(fileMetadata);
            if (fileMetadataJson.getJSONArray("items") != null && fileMetadataJson.getJSONArray("items").length() > 0) {
                fileId = fileMetadataJson.getJSONArray("items").getJSONObject(0).get("id").toString();
                LOG.debug("Found file with ID " + fileId);
            }

        } catch (Exception e) {
            LOG.error("SearchForFormmicroFile", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return fileId;
    }

    private boolean folderExists(String authToken, String formmicrologgerFolderId) {
        HttpURLConnection conn = null;
        try{
            String searchUrl = "https://www.googleapis.com/drive/v2/files/" + formmicrologgerFolderId;
            URL url = new URL(searchUrl);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Formmicro for Android");
            conn.setRequestProperty("Authorization", "OAuth " + authToken);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            conn.connect();

            return conn.getResponseCode() == 200;
        }
        catch(Exception e){
            LOG.error("folderExists", e);
        }

        return false;
    }


    private String getMimeTypeFromFileName(String fileName) {
        if (fileName.endsWith("kml")) {
            return "application/vnd.google-earth.kml+xml";
        }

        if (fileName.endsWith("gpx")) {
            return "application/gpx+xml";
        }

        if (fileName.endsWith("zip")) {
            return "application/zip";
        }

        if (fileName.endsWith("xml")) {
            return "application/xml";
        }

        if (fileName.endsWith("nmea")) {
            return "text/plain";
        }

        if (fileName.endsWith("geojson")){
            return "application/vnd.geo+json";
        }

        return "application/vnd.google-apps.spreadsheet";
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        LOG.error("Could not upload to Google Drive", throwable);
        EventBus.getDefault().post(new UploadEvents.GDrive().failed("Could not upload to Google Drive", throwable));
        return false;
    }
}
