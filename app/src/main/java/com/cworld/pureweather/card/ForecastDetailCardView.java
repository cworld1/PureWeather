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

package com.cworld.pureweather.card;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.cworld.pureweather.R;
import com.cworld.pureweather.data.CurrentWeather;
import com.cworld.pureweather.data.WeatherModel;
import com.cworld.pureweather.pref.TemperatureUnit;
import com.cworld.pureweather.pref.WeatherPreferences;
import com.cworld.pureweather.util.ColorHelper;
import com.cworld.pureweather.util.WeatherUtils;
import com.ominous.tylerutils.util.ColorUtils;
import com.ominous.tylerutils.util.LocaleUtils;
import com.ominous.tylerutils.util.ViewUtils;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ForecastDetailCardView extends BaseDetailCardView {
    public ForecastDetailCardView(Context context) {
        super(context);

        TextView forecastItem1Spacer = findViewById(R.id.forecast_item1_spacer);
        TextView forecastItem2Spacer = findViewById(R.id.forecast_item2_spacer);
        TextView forecastTitleSpacer = findViewById(R.id.forecast_title_spacer);

        WeatherUtils weatherUtils = WeatherUtils.getInstance(getContext());
        TemperatureUnit temperatureUnit = WeatherPreferences.getInstance(getContext()).getTemperatureUnit();

        forecastItem1Spacer.setText(weatherUtils.getTemperatureString(temperatureUnit, 100, 0));
        forecastItem2Spacer.setText(LocaleUtils.getPercentageString(Locale.getDefault(), 1f));
        forecastTitleSpacer.setText(LocaleUtils.formatHour(getContext(),
                Locale.getDefault(),
                new Date(1681603199000L), // 2023-04-15 23:59:59 GMT
                TimeZone.getTimeZone("GMT")));

        ViewUtils.setAccessibilityInfo(this, null, null);
    }

    @Override
    public void update(WeatherModel weatherModel, int position) {
        WeatherUtils weatherUtils = WeatherUtils.getInstance(getContext());
        TemperatureUnit temperatureUnit = WeatherPreferences.getInstance(getContext()).getTemperatureUnit();
        ColorHelper colorHelper = ColorHelper.getInstance(getContext());

        long thisDay = LocaleUtils.getStartOfDay(weatherModel.date, weatherModel.currentWeather.timezone) / 1000;
        CurrentWeather.DataPoint data = null;

        int alertCount = 0;

        if (weatherModel.currentWeather.alerts != null) {
            for (int i = 0, l = weatherModel.currentWeather.alerts.length; i < l; i++) {
                if (weatherModel.currentWeather.alerts[i].end >= thisDay) {
                    alertCount++;
                }
            }
        }

        for (int i = 0, l = weatherModel.currentWeather.trihourly.length; i < l; i++) {
            if (weatherModel.currentWeather.trihourly[i].dt >= thisDay) {
                data = weatherModel.currentWeather.trihourly[i + position - alertCount - 2];
                i = l;
            }
        }

        if (data != null) {
            String hourText = LocaleUtils.formatHour(
                    getContext(),
                    Locale.getDefault(),
                    new Date(data.dt * 1000),
                    weatherModel.currentWeather.timezone);

            forecastIcon.setImageResource(data.weatherIconRes);

            forecastTitle.setText(hourText);

            forecastItem1.setText(weatherUtils.getTemperatureString(temperatureUnit, data.temp, 0));
            forecastItem1.setTextColor(colorHelper.getColorFromTemperature(data.temp, true, ColorUtils.isNightModeActive(getContext())));

            forecastDescription.setText(data.weatherDescription);

            if (data.pop > 0) {
                forecastItem2.setText(LocaleUtils.getPercentageString(Locale.getDefault(), data.pop  / 100.));
                forecastItem2.setTextColor(colorHelper.getPrecipColor(data.precipitationType));
            } else {
                forecastItem2.setText(null);
            }

            setContentDescription(getContext().getString(R.string.format_forecast_detail_desc,
                    hourText,
                    data.weatherDescription,
                    weatherUtils.getTemperatureString(temperatureUnit, data.temp, 0),
                    getContext().getString(R.string.format_precipitation_chance, LocaleUtils.getPercentageString(Locale.getDefault(), data.pop / 100.), weatherUtils.getPrecipitationTypeString(data.precipitationType))
            ));
        }
    }

    @Override
    public void onClick(View v) {
        //Nothing
    }
}
