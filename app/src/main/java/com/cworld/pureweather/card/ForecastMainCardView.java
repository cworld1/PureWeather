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

package com.cworld.pureweather.card;

import android.content.Context;

import com.cworld.pureweather.R;
import com.cworld.pureweather.data.WeatherModel;
import com.cworld.pureweather.data.CurrentWeather;
import com.cworld.pureweather.pref.DistanceUnit;
import com.cworld.pureweather.pref.SpeedUnit;
import com.cworld.pureweather.pref.TemperatureUnit;
import com.cworld.pureweather.pref.WeatherPreferences;
import com.cworld.pureweather.util.WeatherUtils;
import com.ominous.tylerutils.util.LocaleUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

//TODO Add cloud cover
public class ForecastMainCardView extends BaseMainCardView {
    private final Calendar calendar = Calendar.getInstance(Locale.getDefault());

    public ForecastMainCardView(Context context) {
        super(context);

        additionalConditions.removeView(feelsLikeIconTextView);
        additionalConditions.removeView(visibilityIconTextView);
    }

    @Override
    public void update(WeatherModel weatherModel, int position) {
        super.update(weatherModel, position);

        int day = -1;
        long thisDate = LocaleUtils.getStartOfDay(weatherModel.date, weatherModel.currentWeather.timezone);
        CurrentWeather.DataPoint thisDailyData = null;
        for (int i = 0, l = weatherModel.currentWeather.daily.length; i < l; i++) {
            CurrentWeather.DataPoint dailyData = weatherModel.currentWeather.daily[i];

            if (LocaleUtils.getStartOfDay(new Date(dailyData.dt * 1000), weatherModel.currentWeather.timezone) == thisDate) {
                thisDailyData = dailyData;
                day = i;
                i = l;
            }
        }

        if (thisDailyData != null) {
            calendar.setTimeInMillis(thisDailyData.dt * 1000);

            WeatherUtils weatherUtils = WeatherUtils.getInstance(getContext());
            WeatherPreferences weatherPreferences = WeatherPreferences.getInstance(getContext());
            TemperatureUnit temperatureUnit = weatherPreferences.getTemperatureUnit();
            SpeedUnit speedUnit = weatherPreferences.getSpeedUnit();
            DistanceUnit distanceUnit = weatherPreferences.getDistanceUnit();

            String weatherString = thisDailyData.weatherLongDescription;
            String maxTemperatureString = weatherUtils.getTemperatureString(temperatureUnit, thisDailyData.maxTemp, 0);
            String minTemperatureString = weatherUtils.getTemperatureString(temperatureUnit, thisDailyData.minTemp, 0);
            String dewPointString = weatherUtils.getTemperatureString(temperatureUnit, thisDailyData.dewPoint, 1);
            String humidityString = LocaleUtils.getPercentageString(Locale.getDefault(), thisDailyData.humidity / 100.0);
            String pressureString = getContext().getString(R.string.format_pressure, thisDailyData.pressure);
            String uvIndexString = getContext().getString(R.string.format_uvi, thisDailyData.uvi);

            mainIcon.setImageResource(thisDailyData.weatherIconRes);

            mainTemperature.setText(getContext().getString(R.string.format_forecast_temp, maxTemperatureString, minTemperatureString));
            mainDescription.setText(weatherString);

            windIconTextView.getTextView().setText(weatherUtils.getWindSpeedString(speedUnit, thisDailyData.windSpeed, thisDailyData.windDeg, false));
            rainIconTextView.getTextView().setText(weatherUtils.getPrecipitationString(distanceUnit, thisDailyData.precipitationIntensity, thisDailyData.precipitationType, false));
            uvIndexIconTextView.getTextView().setText(uvIndexString);
            dewPointIconTextView.getTextView().setText(getContext().getString(R.string.format_dewpoint, dewPointString));
            humidityIconTextView.getTextView().setText(getContext().getString(R.string.format_humidity, humidityString));
            pressureIconTextView.getTextView().setText(pressureString);

            setContentDescription(getContext().getString(R.string.format_forecast_desc,
                    day == 0 ? getContext().getString(R.string.text_today) : calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()),
                    weatherString,
                    maxTemperatureString,
                    minTemperatureString,
                    weatherUtils.getPrecipitationString(distanceUnit, thisDailyData.precipitationIntensity, thisDailyData.precipitationType, true),
                    weatherUtils.getWindSpeedString(speedUnit, thisDailyData.windSpeed, thisDailyData.windDeg, true),
                    humidityString,
                    pressureString,
                    dewPointString,
                    uvIndexString
            ));
        }
    }
}