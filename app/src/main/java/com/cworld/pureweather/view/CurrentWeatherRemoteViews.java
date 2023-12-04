/*
 *   Copyright 2019 - 2023 Tyler Williamson
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

package com.cworld.pureweather.view;

import android.content.Context;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import com.cworld.pureweather.R;
import com.cworld.pureweather.data.WeatherDatabase;
import com.cworld.pureweather.data.CurrentWeather;
import com.cworld.pureweather.pref.WeatherPreferences;
import com.cworld.pureweather.util.ColorHelper;
import com.cworld.pureweather.util.WeatherUtils;
import com.ominous.tylerutils.util.BitmapUtils;

public class CurrentWeatherRemoteViews extends RemoteViews {
    public final Context context;

    public CurrentWeatherRemoteViews(Context context) {
        super(context.getPackageName(), R.layout.remoteviews_current);

        this.context = context;
    }

    public void update(WeatherDatabase.WeatherLocation weatherLocation, CurrentWeather currentWeather) {
        WeatherUtils weatherUtils = WeatherUtils.getInstance(context);
        WeatherPreferences weatherPreferences = WeatherPreferences.getInstance(context);

        setTextViewText(R.id.main_location, weatherLocation.isCurrentLocation ? context.getString(R.string.text_current_location) : weatherLocation.name);
        setTextViewText(R.id.current_main_temperature, weatherUtils.getTemperatureString(weatherPreferences.getTemperatureUnit(), currentWeather.current.temp, 1));
        setTextViewText(R.id.main_description, currentWeather.current.weatherDescription);
        setImageViewBitmap(R.id.current_main_icon,
                BitmapUtils.drawableToBitmap(
                        ContextCompat.getDrawable(context, currentWeather.current.weatherIconRes),
                        ColorHelper.getInstance(context).getNotificationTextColor(context).getDefaultColor() | 0xFF000000));
    }
}
