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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import com.commonsware.cwac.anddown.AndDown;
import com.formmicro.gpslogger.common.Strings;
import com.formmicro.gpslogger.common.slf4j.Logs;
import com.formmicro.gpslogger.loggers.Files;
import com.formmicro.gpslogger.ui.components.FaqExpandableListAdapter;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Faqtivity extends AppCompatActivity {

    FaqExpandableListAdapter listAdapter;
    ListView expListView;

    private static final Logger LOG = Logs.of(Faqtivity.class);
    /**
     * Event raised when the form is created for the first time
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_faq);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if(getSupportActionBar() != null){
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

        }
        catch(Exception ex){
            //http://stackoverflow.com/questions/26657348/appcompat-v7-v21-0-0-causing-crash-on-samsung-devices-with-android-v4-2-2
            LOG.error("Thanks for this, Samsung", ex);
        }

        String singleFaqItem = "";
        Bundle extras = getIntent().getExtras();
        if(extras != null && !Strings.isNullOrEmpty(extras.getString("singlefaq"))){
            singleFaqItem = extras.getString("singlefaq");
        }

        expListView = (ListView) findViewById(R.id.lvExp);

        List<String> generalTopics = new ArrayList<>();

        if(Strings.isNullOrEmpty(singleFaqItem)){
            generalTopics.add(getTopic("faq/faq01-why-taking-so-long.md"));
            generalTopics.add(getTopic("faq/faq02-why-sometimes-inaccurate.md"));
            generalTopics.add(getTopic("faq/faq03-no-point-logged.md"));
            generalTopics.add(getTopic("faq/faq04-what-timezone.md"));
            generalTopics.add(getTopic("faq/faq05-what-units.md"));
            generalTopics.add(getTopic("faq/faq06-where-are-gps-files.md"));

            generalTopics.add(getTopic("faq/faq07-settings-changed.md"));
            generalTopics.add(getTopic("faq/faq08-what-settings-mean.md"));
            generalTopics.add(getTopic("faq/faq09-recommended-settings.md"));
            generalTopics.add(getTopic("faq/faq10-exact-time-settings.md"));

            generalTopics.add(getTopic("faq/faq11-remove-notification.md"));
            generalTopics.add(getTopic("faq/faq12-task-managers.md"));
            generalTopics.add(getTopic("faq/faq14-tasker-automation.md"));
            generalTopics.add(getTopic("faq/faq15-preset-files.md"));
            generalTopics.add(getTopic("faq/faq16-custom-url.md"));
            generalTopics.add(getTopic("faq/faq19-profiles.md"));
            generalTopics.add(getTopic("faq/faq20-troubleshooting.md"));
            generalTopics.add(getTopic("faq/faq21-custom-ssl-certificates.md"));
            generalTopics.add(getTopic("faq/faq22-why-gps-jumps.md"));
            generalTopics.add(getTopic("faq/faq23-making-feature-requests.md"));
        }
        else {
            generalTopics.add(getTopic(singleFaqItem));
        }

        listAdapter = new FaqExpandableListAdapter(this, generalTopics);
        expListView.setAdapter(listAdapter);

        if(!Strings.isNullOrEmpty(singleFaqItem)){
            expListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //expListView.setSelection(15);
                    expListView.findViewWithTag("0").performClick();
                }
            }, 100l);
        }
    }


    protected String getTopic(String assetPath){
        String md = Strings.getSanitizedMarkdownForFaqView(Files.getAssetFileAsString(assetPath,getApplicationContext()));
        return new AndDown().markdownToHtml(md).replace("<code>","<strong><tt><font color='#008800'>").replace("</code>","</font></tt></strong>");
    }

    @Override
    protected void onDestroy() {
        setVisible(false);
        super.onDestroy();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setVisible(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
