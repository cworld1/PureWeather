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
import android.util.AttributeSet;

import com.cworld.pureweather.data.WeatherModel;
import com.ominous.tylerutils.browser.CustomTabs;

public abstract class BaseCardView extends com.ominous.tylerutils.card.BaseCardView {
    static CustomTabs customTabs;

    public BaseCardView(Context context) {
        this(context, null, 0);
    }

    public BaseCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (customTabs == null) {
            customTabs = CustomTabs.getInstance(context);
        }
    }

    abstract public void update(WeatherModel weatherModel, int position);
}