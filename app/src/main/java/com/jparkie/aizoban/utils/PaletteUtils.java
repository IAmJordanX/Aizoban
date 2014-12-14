package com.jparkie.aizoban.utils;

import android.support.v7.graphics.Palette;

public class PaletteUtils {
    public static int getColorWithDefault(Palette palette, int defaultColor) {
        Palette.Swatch currentItem = null;

        if (currentItem == null) {
            currentItem = palette.getVibrantSwatch();
        }
        if (currentItem == null) {
            currentItem = palette.getLightVibrantSwatch();
        }
        if (currentItem == null) {
            currentItem = palette.getDarkVibrantSwatch();
        }
        if (currentItem == null) {
            currentItem = palette.getMutedSwatch();
        }
        if (currentItem == null) {
            currentItem = palette.getLightMutedSwatch();
        }
        if (currentItem == null) {
            currentItem = palette.getDarkMutedSwatch();
        }

        return currentItem != null ? currentItem.getRgb() : defaultColor;
    }
}
