/*
 *   Copyright 2019 - 2023 CWorld
 *
 *   This file is part of PureWeather.
 *
 *   PureWeather is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PureWeather is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PureWeather.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.cworld.pureweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cworld.pureweather.data.WeatherDataManager;
import com.cworld.pureweather.work.WeatherWorkManager;

public class WeatherReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action;

        if ((action = intent.getAction()) != null) {
            switch (action) {
                case Intent.ACTION_BOOT_COMPLETED:
                case Intent.ACTION_PACKAGE_REPLACED:
                    WeatherWorkManager.enqueueNotificationWorker(context, false);
                    break;
                case Intent.ACTION_LOCALE_CHANGED:
                    WeatherDataManager.getInstance().clearCache();
                    break;
            }
        }
    }
}
