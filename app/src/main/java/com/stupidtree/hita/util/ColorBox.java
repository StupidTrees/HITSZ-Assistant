package com.stupidtree.hita.util;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.CalendarContract;

import androidx.core.graphics.ColorUtils;

import java.util.Random;

public class ColorBox {
    static String[] colors_material = new String[]{
            "#ef5350",
            "#ec407a",
            "#9c27b0",
            "#7e57c2",
            "#7c4dff",
            "#3f51b5",
            "#536dfe",
            "#2196f3",
            "#26c6da",
            "#009688",
            "#4caf50",
            "#fdd835"
    };

    public static int getRandomColor_Material() {
        Random random = new Random();
        return Color.parseColor(colors_material[random.nextInt(colors_material.length - 1)]);

    }
}
