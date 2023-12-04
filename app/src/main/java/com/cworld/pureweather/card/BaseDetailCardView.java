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

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cworld.pureweather.R;

//TODO Swipe != Click
public abstract class BaseDetailCardView extends BaseCardView {
    protected final TextView forecastItem1;
    protected final TextView forecastItem2;
    protected final TextView forecastTitle;
    protected final TextView forecastDescription;
    protected final ImageView forecastIcon;
    protected final HorizontalScrollView scrollView;

    public BaseDetailCardView(Context context) {
        super(context);

        inflate(context, R.layout.card_detail, this);

        forecastItem1 = findViewById(R.id.forecast_item1);
        forecastItem2 = findViewById(R.id.forecast_item2);
        forecastTitle = findViewById(R.id.forecast_title);
        forecastDescription = findViewById(R.id.forecast_desc);
        forecastIcon = findViewById(R.id.forecast_icon);
        scrollView = findViewById(R.id.scrollview);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scrollView.onTouchEvent(event);

        return super.onTouchEvent(event);
    }
}
