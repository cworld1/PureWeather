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

package com.cworld.pureweather.dialog;

import android.content.Context;
import android.view.ViewGroup;
import android.view.Window;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.cworld.pureweather.R;
import com.cworld.pureweather.activity.ILifecycleAwareActivity;
import com.cworld.pureweather.data.WeatherDatabase;
import com.cworld.pureweather.view.WeatherMapView;

public class LocationMapDialog {
    private final AlertDialog mapDialog;
    private OnLocationChosenListener onLocationChosenListener;
    private WeatherDatabase.WeatherLocation weatherLocation =
            new WeatherDatabase.WeatherLocation(0, 0, null);

    public LocationMapDialog(Context context, ILifecycleAwareActivity lifecycleAwareActivity) {
        mapDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_map_title)
                .setView(R.layout.dialog_map)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok,
                        (d, w) -> new LocationManualDialog(context)
                                .show(getWeatherLocation(), onLocationChosenListener))
                .create();

        mapDialog.setOnShowListener(d -> {
            int textColor = ContextCompat.getColor(context, R.color.color_accent_text);

            mapDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(textColor);
            mapDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textColor);

            Window window = mapDialog.getWindow();

            if (window != null) {
                mapDialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        context.getResources().getDimensionPixelSize(R.dimen.mappicker_height));
            }

            WeatherMapView weatherMapView = mapDialog.findViewById(R.id.map);

            if (weatherMapView != null) {
                weatherMapView.setOnMapClickListener(point -> {
                    setWeatherLocation(point.getLatitude(), point.getLongitude());

                    return true;
                });

                weatherMapView.attachToActivity(lifecycleAwareActivity);
            }
        });
    }

    public void show(OnLocationChosenListener onLocationChosenListener) {
        mapDialog.show();
        this.onLocationChosenListener = onLocationChosenListener;
    }

    private WeatherDatabase.WeatherLocation getWeatherLocation() {
        return weatherLocation;
    }

    public void setWeatherLocation(double lat, double lon) {
        weatherLocation = new WeatherDatabase.WeatherLocation(lat, lon, null);
    }
}
