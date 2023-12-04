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

package com.cworld.pureweather.data;

import android.content.Context;
import android.location.Location;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.cworld.pureweather.R;
import com.cworld.pureweather.api.openmeteo.OpenMeteo;
import com.cworld.pureweather.api.openweather.OpenWeatherMap;
import com.cworld.pureweather.location.LocationDisabledException;
import com.cworld.pureweather.location.LocationPermissionNotAvailableException;
import com.cworld.pureweather.location.LocationUnavailableException;
import com.cworld.pureweather.location.WeatherLocationManager;
import com.cworld.pureweather.pref.OwmApiVersion;
import com.cworld.pureweather.pref.WeatherProvider;
import com.cworld.pureweather.pref.WeatherPreferences;
import com.ominous.tylerutils.async.Promise;
import com.ominous.tylerutils.http.HttpException;

import org.json.JSONException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class WeatherDataManager {
    private final Map<Pair<Double, Double>, CurrentWeather> currentWeatherCache = new HashMap<>();
    private final int CACHE_EXPIRATION = 60 * 1000; //1 minute
    private final int MAX_ATTEMPTS = 3;
    private final int ATTEMPT_SLEEP_DURATION = 5000;
    private final WeatherLocationManager weatherLocationManager = WeatherLocationManager.getInstance();

    private WeatherProvider currentProvider = null;

    private static WeatherDataManager instance;

    private WeatherDataManager() {

    }

    public static WeatherDataManager getInstance() {
        if (instance == null) {
            instance = new WeatherDataManager();
        }

        return instance;
    }

    public Promise<Void, WeatherModel> getWeatherAsync(Context context, @Nullable MutableLiveData<WeatherModel> weatherLiveData, boolean obtainForecast, boolean isBackground) {
        return Promise.create(a -> {
            WeatherModel result;

            if (weatherLiveData != null) {
                weatherLiveData.postValue(new WeatherModel(WeatherModel.WeatherStatus.UPDATING, null, null));
            }

            try {
                WeatherDatabase.WeatherLocation weatherLocation = WeatherDatabase.getInstance(context).locationDao().getSelected();
                Pair<Double, Double> locationKey;

                if (weatherLocation.isCurrentLocation) {
                    Location location = weatherLocationManager.getLastKnownLocation(context, isBackground);

                    if (location == null) {
                        if (weatherLiveData != null) {
                            weatherLiveData.postValue(new WeatherModel(WeatherModel.WeatherStatus.OBTAINING_LOCATION, null, null));
                        }

                        location = weatherLocationManager.obtainCurrentLocation(context, isBackground);

                        if (location == null) {
                            result = new WeatherModel(
                                    WeatherModel.WeatherStatus.ERROR_LOCATION_UNAVAILABLE,
                                    context.getString(R.string.error_null_location),
                                    new LocationUnavailableException());

                            if (weatherLiveData != null) {
                                weatherLiveData.postValue(result);
                            }

                            return result;
                        }
                    }

                    locationKey = new Pair<>(
                            BigDecimal.valueOf(location.getLatitude()).setScale(3, RoundingMode.HALF_UP).doubleValue(),
                            BigDecimal.valueOf(location.getLongitude()).setScale(3, RoundingMode.HALF_UP).doubleValue()
                    );
                } else {
                    locationKey = new Pair<>(
                            weatherLocation.latitude,
                            weatherLocation.longitude
                    );
                }

                WeatherPreferences weatherPreferences = WeatherPreferences.getInstance(context);
                WeatherProvider weatherProvider = weatherPreferences.getWeatherProvider();
                OwmApiVersion owmApiVersion = weatherPreferences.getOwmApiVersion();

                if (weatherProvider == WeatherProvider.DEFAULT) {
                    weatherPreferences.setWeatherProvider(WeatherProvider.OPENWEATHERMAP);
                    weatherProvider = WeatherProvider.OPENWEATHERMAP;
                }

                if (weatherProvider == WeatherProvider.OPENWEATHERMAP &&
                        owmApiVersion == OwmApiVersion.DEFAULT) {
                    weatherPreferences.setOwmApiVersion(OwmApiVersion.ONECALL_2_5);
                    owmApiVersion = OwmApiVersion.ONECALL_2_5;
                }

                if (currentProvider == null || currentProvider != weatherProvider) {
                    currentProvider = weatherProvider;
                    clearCache();
                }

                String apiKey = weatherProvider == WeatherProvider.OPENMETEO ?
                        weatherPreferences.getOpenMeteoAPIKey() :
                        weatherPreferences.getOWMAPIKey();

                String weatherProviderInstance =  weatherProvider == WeatherProvider.OPENMETEO ?
                        weatherPreferences.getOpenMeteoInstance() :
                        null;

                //TODO move cache logic to its own method

                CurrentWeather currentWeather = getCurrentWeather(context, weatherProvider, apiKey, weatherProviderInstance, owmApiVersion, locationKey);

                if (weatherProvider != WeatherProvider.OPENMETEO &&
                        currentWeather.trihourly == null &&
                        obtainForecast) {
                    currentWeather.trihourly = getForecastWeather(context, weatherProvider, apiKey, locationKey);

                    currentWeatherCache.put(locationKey, currentWeather);
                }

                if (currentWeather == null || currentWeather.current == null ||
                        (obtainForecast && currentWeather.trihourly == null)) {
                    result = new WeatherModel(
                            WeatherModel.WeatherStatus.ERROR_OTHER,
                            context.getString(R.string.error_null_response),
                            new WeatherDataUnavailableException());
                } else {
                    result = new WeatherModel(
                            currentWeather,
                            weatherLocation,
                            locationKey,
                            WeatherModel.WeatherStatus.SUCCESS);

                }

                if (weatherLiveData != null) {
                    weatherLiveData.postValue(result);
                }
            } catch (LocationPermissionNotAvailableException e) {
                result = new WeatherModel(WeatherModel.WeatherStatus.ERROR_LOCATION_ACCESS_DISALLOWED, context.getString(R.string.snackbar_background_location_notifications), e);

                if (weatherLiveData != null) {
                    weatherLiveData.postValue(result);
                }
            } catch (LocationDisabledException e) {
                result = new WeatherModel(WeatherModel.WeatherStatus.ERROR_LOCATION_DISABLED, context.getString(R.string.error_gps_disabled), e);

                if (weatherLiveData != null) {
                    weatherLiveData.postValue(result);
                }
            } catch (IOException e) {
                result = new WeatherModel(WeatherModel.WeatherStatus.ERROR_OTHER, context.getString(R.string.error_connecting_api), e);

                if (weatherLiveData != null) {
                    weatherLiveData.postValue(result);
                }
            } catch (JSONException e) {
                result = new WeatherModel(WeatherModel.WeatherStatus.ERROR_OTHER, context.getString(R.string.error_unexpected_api_result), e);

                if (weatherLiveData != null) {
                    weatherLiveData.postValue(result);
                }
            } catch (InstantiationException | IllegalAccessException | NullPointerException e) {
                result = new WeatherModel(WeatherModel.WeatherStatus.ERROR_OTHER, context.getString(R.string.error_creating_result), e);

                if (weatherLiveData != null) {
                    weatherLiveData.postValue(result);
                }
            } catch (HttpException e) {
                result = new WeatherModel(WeatherModel.WeatherStatus.ERROR_OTHER, e.getMessage(), e);

                if (weatherLiveData != null) {
                    weatherLiveData.postValue(result);
                }
            }

            return result;
        });
    }

    private CurrentWeather getCurrentWeather(Context context,
                                             @NonNull WeatherProvider weatherProvider,
                                             String apiKey,
                                             String weatherProviderInstance,
                                             OwmApiVersion owmApiVersion,
                                             Pair<Double, Double> locationKey) throws
            JSONException, HttpException, IOException, InstantiationException, IllegalAccessException {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        CurrentWeather newWeather = null;

        if (currentWeatherCache.containsKey(locationKey)) {
            CurrentWeather previousWeather = currentWeatherCache.get(locationKey);

            if (previousWeather != null && now.getTimeInMillis() - previousWeather.timestamp < CACHE_EXPIRATION) {
                return previousWeather;
            }
        }

        int attempt = 0;
        HttpException lastException = null;

        do {
            try {
                if (weatherProvider == WeatherProvider.OPENWEATHERMAP) {
                    if (owmApiVersion != null && owmApiVersion != OwmApiVersion.DEFAULT) {
                        newWeather = OpenWeatherMap.getInstance().getCurrentWeather(
                                context,
                                owmApiVersion,
                                locationKey.first,
                                locationKey.second,
                                apiKey);
                    } else {
                        throw new IllegalArgumentException("Illegal OwmApiVersion provided");
                    }
                } else if (weatherProvider == WeatherProvider.OPENMETEO) {
                    newWeather = OpenMeteo.getInstance()
                            .getCurrentWeather(context,
                                    locationKey.first,
                                    locationKey.second,
                                    apiKey,
                                    weatherProviderInstance);
                } else {
                    throw new IllegalArgumentException("Illegal WeatherProvider provided");
                }
            } catch (HttpException e) {
                lastException = e;
                try {
                    Thread.sleep(ATTEMPT_SLEEP_DURATION);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            } catch (IOException | JSONException | InstantiationException |
                     IllegalAccessException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException("Uncaught Exception occurred");
            }
        } while (newWeather == null && attempt++ < MAX_ATTEMPTS);

        if (newWeather == null && lastException != null) {
            throw lastException;
        }

        currentWeatherCache.put(locationKey, newWeather);
        return newWeather;
    }

    private CurrentWeather.DataPoint[] getForecastWeather(Context context,
                                                        @NonNull WeatherProvider weatherProvider,
                                                        String apiKey,
                                                        Pair<Double, Double> locationKey) throws
            JSONException, HttpException, IOException, InstantiationException, IllegalAccessException {
//        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
//        ForecastWeather newWeather = null;
//
//        if (forecastWeatherCache.containsKey(locationKey)) {
//            ForecastWeather previousWeather = forecastWeatherCache.get(locationKey);
//
//            if (previousWeather != null && now.getTimeInMillis() - previousWeather.timestamp * 1000 < CACHE_EXPIRATION) {
//                return previousWeather;
//            }
//        }
//
        int attempt = 0;
        HttpException lastException = null;

        CurrentWeather.DataPoint[] newWeather = null;

        do {
            try {
                if (weatherProvider == WeatherProvider.OPENWEATHERMAP) {
                    newWeather = OpenWeatherMap.getInstance()
                            .getForecastWeather(
                                    context,
                                    locationKey.first,
                                    locationKey.second,
                                    apiKey);
                } else {
                    throw new IllegalArgumentException("Illegal WeatherProvider provided");
                }
            } catch (HttpException e) {
                lastException = e;
                try {
                    Thread.sleep(ATTEMPT_SLEEP_DURATION);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            } catch (IOException | JSONException | InstantiationException |
                     IllegalAccessException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException("Uncaught Exception occurred");
            }
        } while (newWeather == null && attempt++ < MAX_ATTEMPTS);

        if (newWeather == null && lastException != null) {
            throw lastException;
        }

        return newWeather;
    }

    public void clearCache() {
        currentWeatherCache.clear();
    }
}
