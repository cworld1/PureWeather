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

package com.cworld.pureweather.pref;

public enum OwmApiVersion {
    ONECALL_3_0("onecall3.0"),
    ONECALL_2_5("onecall2.5"),
    WEATHER_2_5("weather2.5"),
    DEFAULT("");

    private final String value;

    OwmApiVersion(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OwmApiVersion from(String value, OwmApiVersion defaultValue) {
        for (OwmApiVersion v : values()) {
            if (v.getValue().equals(value)) {
                return v;
            }
        }

        return defaultValue;
    }
}
