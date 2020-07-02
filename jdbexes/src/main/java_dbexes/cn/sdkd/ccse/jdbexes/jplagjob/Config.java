package cn.sdkd.ccse.jdbexes.jplagjob;

public abstract class Config {
    public static final float SIM_THRESHOLD = 90f;     // 大于该数值被认定抄袭
    public static final float SIM_THRESHOLD_MIN = 50f; // 小于该数值不会计入
    public static final float SIM_THRESHOLD_SAME = 99f; // 大于该数值认定相同

    public static int SIM_STATUS_NOT_YET = -1;
    public static int SIM_STATUS_NORMAL = 0;
    public static int SIM_STATUS_FAILED = 1;
    public static int SIM_STATUS_PLAGIARISM = 3;

    public static String SIM_DESC_RUNNING = "正在检查";
    public static String SIM_DESC_NOT_YET = "未检查";
    public static String SIM_DESC_NORMAL = "正常";
    public static String SIM_DESC_PARSER_ERR = "解析错误";
    public static String SIM_DESC_SYNAX_ERR = "语法错误";

    public static String getSimDesc(int size, float simValue) {
        return "与" + size + "个同学的作业相似度超过" + simValue + "%.";
    }
}

