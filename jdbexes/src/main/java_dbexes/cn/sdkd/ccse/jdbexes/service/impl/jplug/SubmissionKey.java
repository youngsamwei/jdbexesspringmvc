package cn.sdkd.ccse.jdbexes.service.impl.jplug;

/**
 * 用于生成 JPlag Submission 名称
 */
class SubmissionKey {
    String tsno;
    String tsname;
    Long experiment_stu_test_no;

    public SubmissionKey(String tsno, String tsname, Long experiment_stu_test_no) {
        this.tsno = tsno;
        this.tsname = tsname;
        this.experiment_stu_test_no = experiment_stu_test_no;
    }

    static SubmissionKey valueOf(String key) throws IllegalArgumentException {
        try {
            String[] keys = key.split("_");
            String tsno = keys[0];
            String tsname = keys[1];
            Long experiment_stu_test_no = Long.parseLong(keys[2]);
            return new SubmissionKey(tsno, tsname, experiment_stu_test_no);
        } catch (Exception e) {
            throw new IllegalArgumentException("错误的Submission" + key);
        }
    }

    @Override
    public String toString() {
        return tsno + '_' + tsname + '_' + experiment_stu_test_no;
    }
}
