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
import android.content.res.Resources;
import android.text.SpannableString;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;

import com.cworld.pureweather.R;
import com.ominous.tylerutils.view.LinkedTextView;

public class TextDialog {
    private final Resources resources;
    private final AlertDialog dialog;
    private final LinkedTextView textView;

    public TextDialog(Context context) {
        this.resources = context.getResources();

        textView = new LinkedTextView(new ContextThemeWrapper(context, R.style.PureWeather_Text_Dialog));

        textView.setLinkTextColor(ContextCompat.getColor(context, R.color.color_accent_text));

        int padding = context.getResources().getDimensionPixelSize(R.dimen.margin_standard);
        textView.setPadding(padding, padding, padding, padding);

        dialog = new AlertDialog.Builder(context)
                .setView(textView)
                .create();

        dialog.setOnShowListener(d -> {
            for (Button button : new Button[]{
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE),
                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL),
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            }) {
                button.setTextColor(ContextCompat.getColor(context, R.color.color_accent_text));
            }
        });
    }

    public TextDialog setButton(int which, String text, @Nullable Runnable onAcceptRunnable) {
        dialog.setButton(which, text, null, (d, w) -> {
            if (onAcceptRunnable != null) {
                onAcceptRunnable.run();
            }

            d.dismiss();
        });

        return this;
    }

    public TextDialog addCloseButton() {
        return setButton(AlertDialog.BUTTON_NEGATIVE, resources.getString(R.string.dialog_button_close), null);
    }

    public TextDialog setContent(CharSequence content) {
        textView.setText(new SpannableString(content));

        return this;
    }

    public TextDialog setTitle(CharSequence title) {
        dialog.setTitle(title);

        return this;
    }

    public void show() {
        dialog.show();
    }
}
