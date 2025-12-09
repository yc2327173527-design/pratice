package com.yupi.yupicturebackend.utils;

/**
 * 颜色转换工具类
 */
public class ColorTransformUtils {

    private ColorTransformUtils() {
        // 工具类无需实例化
    }

    /**
     * 获取标准颜色（将数据万象的5位色值转成6位）
     *
     * @param color 颜色值
     * @return 标准化颜色
     */
    public static String getStandardColor(String color) {
        if (color == null) {
            return null;
        }
        // 每一种rgb 色值都有可能只有一个0，要转换成00)
        // 如果是六位，不用转换，如果是五位，要给第三位后面加个 0
        // 示例：
        // 0x080e0 => 0x0800e
        if (color.length() == 7) {
            color = color.substring(0, 4) + "0" + color.substring(4, 7);
        }
        return color;
    }
}
