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

package com.formmicro.gpslogger.ui.components;


import com.formmicro.gpslogger.R;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;


public class GpsLoggerDrawerItem {


    public static PrimaryDrawerItem newPrimary(int resTitle, int resSummary, int resIcon, int identifier) {

        return new PrimaryDrawerItem()
        .withName(resTitle)
        .withDescription(resSummary)
        .withIcon(resIcon)
        .withIdentifier(identifier)
        .withTextColorRes(R.color.primaryColorText)
        .withDescriptionTextColorRes(R.color.secondaryColorText)
        .withSelectable(false);

    }

    public static SecondaryDrawerItem newSecondary(int resTitle, int resIcon, int identifier) {

        return new SecondaryDrawerItem()
                .withName(resTitle)
                .withIcon(resIcon)
                .withIdentifier(identifier)
                .withTextColorRes(R.color.secondaryColorText)
                .withDescriptionTextColorRes(R.color.secondaryColorText)
                .withSelectable(false);

    }


}

