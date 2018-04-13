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

//https://www.dropbox.com/developers/start/setup#android

package com.formmicro.gpslogger.ui.fragments.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import com.formmicro.gpslogger.GpsMainActivity;
import com.formmicro.gpslogger.R;
import com.formmicro.gpslogger.common.EventBusHook;
import com.formmicro.gpslogger.common.PreferenceHelper;
import com.formmicro.gpslogger.common.events.UploadEvents;
import com.formmicro.gpslogger.common.slf4j.Logs;
import com.formmicro.gpslogger.loggers.Files;
import com.formmicro.gpslogger.senders.dropbox.DropBoxManager;
import com.formmicro.gpslogger.ui.Dialogs;
import com.formmicro.gpslogger.ui.fragments.PermissionedPreferenceFragment;
import de.greenrobot.event.EventBus;
import org.slf4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class DropboxAuthorizationFragment extends PermissionedPreferenceFragment implements Preference.OnPreferenceClickListener {

    private static final Logger LOG = Logs.of(DropboxAuthorizationFragment.class);
    DropBoxManager manager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.dropboxsettings);

        Preference pref = findPreference("dropbox_resetauth");

        manager = new DropBoxManager(PreferenceHelper.getInstance());

        if (manager.isLinked()) {
            pref.setTitle(R.string.osm_resetauth);
            pref.setSummary(R.string.dropbox_unauthorize_description);
        } else {
            pref.setTitle(R.string.osm_lbl_authorize);
            pref.setSummary(R.string.dropbox_authorize_description);
        }

        pref.setOnPreferenceClickListener(this);
        findPreference("dropbox_test_upload").setOnPreferenceClickListener(this);
        registerEventBus();

    }

    private void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            if (manager.finishAuthorization()) {
                startActivity(new Intent(getActivity(), GpsMainActivity.class));
                getActivity().finish();
            }
        } catch (Exception e) {
            Dialogs.alert(getString(R.string.error), getString(R.string.dropbox_couldnotauthorize),
                    getActivity());
            LOG.error(".", e);
        }

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if(preference.getKey().equalsIgnoreCase("dropbox_test_upload")){
            uploadTestFile();
        }

        else {
            // This logs you out if you're logged in, or vice versa
            if (manager.isLinked()) {
                manager.unLink();
                startActivity(new Intent(getActivity(), GpsMainActivity.class));
                getActivity().finish();
            } else {
                try {
                    manager.startAuthentication(this.getActivity());
                } catch (Exception e) {
                    LOG.error(".", e);
                }
            }
        }

        return true;

    }

    private void uploadTestFile() {
        Dialogs.progress(getActivity(), getString(R.string.please_wait), getString(R.string.please_wait));

        try {
            File testFile = Files.createTestFile();
            manager.uploadFile(testFile.getName());

        } catch (Exception ex) {
            LOG.error("Could not create local test file", ex);
            EventBus.getDefault().post(new UploadEvents.Dropbox().failed("Could not create local test file", ex));
        }

    }

    @EventBusHook
    public void onEventMainThread(UploadEvents.Dropbox d){
        LOG.debug("Dropbox Event completed, success: " + d.success);
        Dialogs.hideProgress();
        if(!d.success){
            Dialogs.error(getString(R.string.sorry), "Could not upload to Dropbox", d.message, d.throwable, getActivity());
        }
        else {
            Dialogs.alert(getString(R.string.success), "", getActivity());
        }
    }
}
