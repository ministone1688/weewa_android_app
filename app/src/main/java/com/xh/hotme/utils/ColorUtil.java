//
// Copyright (c) 2017, ledong.com
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation
// and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//


package com.xh.hotme.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.Keep;
import androidx.core.graphics.ColorUtils;

/**
 * 颜色操作工具类
 */
@Keep
public class ColorUtil {

    private ColorUtil() {
    }

    /**
     * 解析颜色值，支持"#rrggbb"和"#rgb"格式
     *
     * @param colorString 颜色字符串
     * @return 颜色值
     */
    public static int parseColor(String colorString) {
        return parseColor(colorString, Color.TRANSPARENT);
    }

    /**
     * 标准化为 #rrggbb 的格式, 输入字符串可以是 rgb, #rgb, rrggbb
     * @param str
     * @return
     */
    public static String standardizeColor(String str) {
        String str2 = null;
        try {
            if (!TextUtils.isEmpty(str)) {
                str = str.trim();
                if(!str.startsWith("#")) {
                    str = "#" + str;
                }
                if (str.length() == 4) {
                    char charAt = str.charAt(1);
                    char charAt2 = str.charAt(2);
                    char charAt3 = str.charAt(3);
                    int data = Integer.parseInt(String.format("%c%c%c%c%c%c", Character.valueOf(charAt), Character.valueOf(charAt), Character.valueOf(charAt2), Character.valueOf(charAt2), Character.valueOf(charAt3), Character.valueOf(charAt3)), 16);
                    str2 = String.format("#%06x", data);
                } else if(str.length() == 7) {
                    str2 = str;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (str2 == null) {
            return "#ffffff";
        }
        return str2;
    }

    public static int parseRgba(String colorStr) {
        int i = 0;
        if (colorStr == null || colorStr.length() == 0) {
            return i;
        }
        if (colorStr.charAt(i) == '#') {
            long colorLong;
            if (colorStr.length() == 7) {
                colorLong = Long.parseLong(colorStr.substring(1), 16) | -16777216;
            } else if (colorStr.length() != 9) {
                throw new IllegalArgumentException("Unknown color");
            } else {
                colorLong = Long.parseLong(colorStr.substring(1, 7), 16) | (Long.parseLong(colorStr.substring(7, 9), 16) << 24);
            }
            return (int) colorLong;
        }
        try {
            return Color.parseColor(colorStr);
        } catch (IllegalArgumentException e) {
            return i;
        }
    }

    /**
     * 解析颜色值，支持"#rrggbb", "#rgb", #aarrggbb格式
     *
     * @param colorString  颜色字符串
     * @param defaultColor 默认颜色值，解析失败时返回
     * @return 颜色值
     */
    public static int parseColor(String colorString, int defaultColor) {
        if (TextUtils.isEmpty(colorString)) {
            return defaultColor;
        }

        if (colorString.charAt(0) == '#' && colorString.length() == 4) {
            char r = colorString.charAt(1);
            char g = colorString.charAt(2);
            char b = colorString.charAt(3);
            String rrggbb = "#" +
                    r + r +
                    g + g +
                    b + b;
            return Color.parseColor(rrggbb);
        }

        try {
            return Color.parseColor(colorString);
        } catch (Exception e) {
            return defaultColor;
        }
    }

    private static final int ENABLE_ATTR = android.R.attr.state_enabled;
    private static final int CHECKED_ATTR = android.R.attr.state_checked;
    private static final int PRESSED_ATTR = android.R.attr.state_pressed;

    public static ColorStateList generateThumbColorWithTintColor(final int tintColor) {
        int[][] states = new int[][]{
                {-ENABLE_ATTR, CHECKED_ATTR},
                {-ENABLE_ATTR},
                {PRESSED_ATTR, -CHECKED_ATTR},
                {PRESSED_ATTR, CHECKED_ATTR},
                {CHECKED_ATTR},
                {-CHECKED_ATTR}
        };

        int[] colors = new int[] {
                tintColor - 0xAA000000,
                0xFFBABABA,
                tintColor - 0x99000000,
                tintColor - 0x99000000,
                tintColor | 0xFF000000,
                0xFFEEEEEE
        };
        return new ColorStateList(states, colors);
    }

    public static ColorStateList generateBackColorWithTintColor(final int tintColor) {
        int[][] states = new int[][]{
                {-ENABLE_ATTR, CHECKED_ATTR},
                {-ENABLE_ATTR},
                {CHECKED_ATTR, PRESSED_ATTR},
                {-CHECKED_ATTR, PRESSED_ATTR},
                {CHECKED_ATTR},
                {-CHECKED_ATTR}
        };

        int[] colors = new int[] {
                tintColor - 0xE1000000,
                0x10000000,
                tintColor - 0xD0000000,
                0x20000000,
                tintColor - 0xD0000000,
                0x20000000
        };
        return new ColorStateList(states, colors);
    }

    public static int getHighlightColor(int color) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        hsl[2] += 0.1f;
        hsl[2] = Math.max(0f, Math.min(hsl[2], 1f));
        return ColorUtils.HSLToColor(hsl);
    }
}
