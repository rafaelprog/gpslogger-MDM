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

package com.formmicro.gpslogger.ui.fragments.settings;


import android.Manifest;
import android.os.Bundle;
import android.preference.Preference;
import com.afollestad.materialdialogs.prefs.MaterialEditTextPreference;
import com.canelmas.let.AskPermission;
import com.formmicro.gpslogger.R;
import com.formmicro.gpslogger.common.EventBusHook;
import com.formmicro.gpslogger.common.network.Networks;
import com.formmicro.gpslogger.common.PreferenceHelper;
import com.formmicro.gpslogger.common.events.UploadEvents;
import com.formmicro.gpslogger.common.network.ServerType;
import com.formmicro.gpslogger.common.slf4j.Logs;
import com.formmicro.gpslogger.senders.PreferenceValidator;
import com.formmicro.gpslogger.senders.owncloud.OwnCloudManager;
import com.formmicro.gpslogger.ui.Dialogs;
import com.formmicro.gpslogger.ui.components.CustomSwitchPreference;
import com.formmicro.gpslogger.ui.fragments.PermissionedPreferenceFragment;
import de.greenrobot.event.EventBus;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

public class OwnCloudSettingsFragment
        extends PermissionedPreferenceFragment implements Preference.OnPreferenceClickListener, PreferenceValidator {

    private static final Logger LOG = Logs.of(OwnCloudSettingsFragment.class);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.owncloudsettings);

        findPreference("owncloud_test").setOnPreferenceClickListener(this);
        findPreference("owncloud_validatecustomsslcert").setOnPreferenceClickListener(this);

        registerEventBus();
    }

    @Override
    public void onDestroy() {

        unregisterEventBus();
        super.onDestroy();
    }

    private void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    private void unregisterEventBus(){
        try {
            EventBus.getDefault().unregister(this);
        } catch (Throwable t){
            //this may crash if registration did not go through. just be safe
        }
    }

    @Override
    public boolean isValid() {
        CustomSwitchPreference chkEnabled = (CustomSwitchPreference) findPreference("owncloud_enabled");
        MaterialEditTextPreference txtServer = (MaterialEditTextPreference) findPreference("owncloud_server");
        MaterialEditTextPreference txtUserName = (MaterialEditTextPreference) findPreference("owncloud_username");
        return !chkEnabled.isChecked() || (
                txtServer.getText() != null && txtServer.getText().length() > 0 &&
                txtUserName.getText() != null && txtUserName.getText().length() > 0
        );
    }


    @Override
    @AskPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public boolean onPreferenceClick(Preference preference) {

        if(preference.getKey().equals("owncloud_validatecustomsslcert")){
            try {
                URL u = new URL(PreferenceHelper.getInstance().getOwnCloudServerName());
                Networks.beginCertificateValidationWorkflow(getActivity(), u.getHost(), u.getPort() < 0 ? u.getDefaultPort() : u.getPort(), ServerType.HTTPS);
            } catch (MalformedURLException e) {
                LOG.error("Could not validate certificate, OwnCloud URL is not valid", e);
            }

        }
        else if(preference.getKey().equals("owncloud_test")){
            MaterialEditTextPreference servernamePreference = (MaterialEditTextPreference) findPreference("owncloud_server");
            MaterialEditTextPreference usernamePreference = (MaterialEditTextPreference) findPreference("owncloud_username");
            MaterialEditTextPreference passwordPreference = (MaterialEditTextPreference) findPreference("owncloud_password");
            MaterialEditTextPreference directoryPreference = (MaterialEditTextPreference) findPreference("owncloud_directory");

            if (!OwnCloudManager.validSettings(
                    servernamePreference.getText(),
                    usernamePreference.getText(),
                    passwordPreference.getText(),
                    directoryPreference.getText())) {
                Dialogs.alert(getString(R.string.autoftp_invalid_settings),
                        getString(R.string.autoftp_invalid_summary),
                        getActivity());
                return false;
            }

            Dialogs.progress(getActivity(), getString(R.string.owncloud_testing), getString(R.string.please_wait));
            OwnCloudManager helper = new OwnCloudManager(PreferenceHelper.getInstance());
            helper.testOwnCloud(servernamePreference.getText(), usernamePreference.getText(), passwordPreference.getText(),
                    directoryPreference.getText());
        }


        return true;
    }


    @EventBusHook
    public void onEventMainThread(UploadEvents.OwnCloud o){
        LOG.debug("OwnCloud Event completed, success: " + o.success);

        Dialogs.hideProgress();
        if(!o.success){
            Dialogs.error(getString(R.string.sorry), "OwnCloud Test Failed", o.message, o.throwable, getActivity());
        }
        else {
            Dialogs.alert(getString(R.string.success), "OwnCloud Test Succeeded", getActivity());
        }
    }
}

