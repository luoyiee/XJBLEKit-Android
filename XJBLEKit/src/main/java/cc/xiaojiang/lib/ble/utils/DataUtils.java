package cc.xiaojiang.lib.ble.utils;

import android.text.TextUtils;

import java.util.ArrayList;

public class DataUtils {

    public static String getStr(String name, String defValue) {
        return !TextUtils.isEmpty(name) ? name : defValue;
    }

    public static String getString(Object name, String defValue) {
        return name instanceof String ? (String) name : defValue;
    }

    public static Integer getInt(Object name, int defValue) {
        return name instanceof Integer ? (Integer) name : defValue;
    }

    public static boolean getBool(Object name, boolean defValue) {
        return name instanceof Boolean ? (Boolean) name : defValue;
    }


    public static ArrayList<String> createArrays(int start, int end, String unit, int step) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            if ((i - start) % step == 0) {
                list.add("" + i + unit);
            }

        }
        return list;
    }

    public static ArrayList<String> createHours() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add("" + i);
            }
        }
        return list;
    }

    public static ArrayList<String> createMinutes() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add("" + i);
            }
        }
        return list;
    }
}
