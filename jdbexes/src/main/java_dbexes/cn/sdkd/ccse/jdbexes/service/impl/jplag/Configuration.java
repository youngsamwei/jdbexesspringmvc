package cn.sdkd.ccse.jdbexes.service.impl.jplag;

public abstract class Configuration {
    public static final float SIM_THRESHOLD = 90f;     // 大于该数值被认定抄袭
    public static final float SIM_THRESHOLD_MIN = 50f; // 小于该数值不会计入
    public static final float SIM_THRESHOLD_SAME = 99f; // 大于该数值认定相同

    public static String getSimDesc(int size, float simValue) {
        return "与" + size + "个同学的作业相似度超过" + simValue + "%.";
    }

    public static String getSimDescNormal() {
        return "正常";
    }
}
