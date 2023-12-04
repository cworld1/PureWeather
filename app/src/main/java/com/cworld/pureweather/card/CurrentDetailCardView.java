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

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.cworld.pureweather.R;
import com.cworld.pureweather.activity.ForecastActivity;
import com.cworld.pureweather.data.CurrentWeather;
import com.cworld.pureweather.data.WeatherModel;
import com.cworld.pureweather.pref.RadarQuality;
import com.cworld.pureweather.pref.TemperatureUnit;
import com.cworld.pureweather.pref.WeatherPreferences;
import com.cworld.pureweather.util.ColorHelper;
import com.cworld.pureweather.util.WeatherUtils;
import com.ominous.tylerutils.util.ColorUtils;
import com.ominous.tylerutils.util.ViewUtils;

import java.util.Calendar;
import java.util.Locale;

public class CurrentDetailCardView extends BaseDetailCardView {
    private final Calendar calendar = Calendar.getInstance(Locale.getDefault());

    public CurrentDetailCardView(Context context) {
        super(context);

        TextView forecastItem1Spacer = findViewById(R.id.forecast_item1_spacer);
        TextView forecastItem2Spacer = findViewById(R.id.forecast_item2_spacer);
        TextView forecastTitleSpacer = findViewById(R.id.forecast_title_spacer);

        WeatherUtils weatherUtils = WeatherUtils.getInstance(getContext());
        TemperatureUnit temperatureUnit = WeatherPreferences.getInstance(getContext()).getTemperatureUnit();
        String spacerText = weatherUtils.getTemperatureString(temperatureUnit, 100, 0);

        forecastItem2Spacer.setText(spacerText);
        forecastItem1Spacer.setText(spacerText);
        forecastTitleSpacer.setText(R.string.text_today);

        ViewUtils.setAccessibilityInfo(this, context.getString(R.string.format_label_open, context.getString(R.string.forecast_desc)), null);
    }

    @Override
    public void update(WeatherModel weatherModel, int position) {
        WeatherPreferences weatherPreferences = WeatherPreferences.getInstance(getContext());
        WeatherUtils weatherUtils = WeatherUtils.getInstance(getContext());
        TemperatureUnit temperatureUnit = weatherPreferences.getTemperatureUnit();
        ColorHelper colorHelper = ColorHelper.getInstance(getContext());
        boolean isDarkModeActive = ColorUtils.isNightModeActive(getContext());

        int alerts = weatherModel.currentWeather.alerts == null ? 0 : weatherModel.currentWeather.alerts.length;
        int radar = weatherPreferences.getRadarQuality() == RadarQuality.DISABLED ? 0 : 1;
        int day = position - (2 + alerts + radar);

        CurrentWeather.DataPoint data = weatherModel.currentWeather.daily[day];

        calendar.setTimeInMillis(data.dt * 1000);

        forecastIcon.setImageResource(data.weatherIconRes);

        forecastTitle.setText(day == 0 ? getContext().getString(R.string.text_today) : calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));

        forecastItem1.setText(weatherUtils.getTemperatureString(temperatureUnit, data.maxTemp, 0));
        forecastItem1.setTextColor(colorHelper.getColorFromTemperature(data.maxTemp, true, isDarkModeActive));

        forecastItem2.setText(weatherUtils.getTemperatureString(temperatureUnit, data.minTemp, 0));
        forecastItem2.setTextColor(colorHelper.getColorFromTemperature(data.minTemp, true, isDarkModeActive));

        forecastDescription.setText(data.weatherDescription);

        setContentDescription(getContext().getString(R.string.format_current_forecast_desc,
                day == 0 ? getContext().getString(R.string.text_today) : calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()),
                data.weatherDescription,
                weatherUtils.getTemperatureString(temperatureUnit, data.maxTemp, 0),
                weatherUtils.getTemperatureString(temperatureUnit, data.minTemp, 0)
        ));
    }

    @Override
    public void onClick(View v) {
        getContext().startActivity(
                new Intent(getContext(), ForecastActivity.class)
                        .putExtra(ForecastActivity.EXTRA_DATE, calendar.getTimeInMillis()),
                ActivityOptions.makeCustomAnimation(getContext(), R.anim.slide_right_in, R.anim.slide_left_out).toBundle());
    }
}
