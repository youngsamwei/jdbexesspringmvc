package cn.sdkd.ccse.jdbexes.checkmission;

/**
 * Created by sam on 2019/1/4.
 */
public class CheckJobThread extends Thread {
    private CheckJob checkJob;

    public CheckJobThread(CheckJob checkJob){

        super(checkJob);
        this.checkJob = checkJob;

    }

    public CheckJob getCheckJob() {
        return checkJob;
    }
}
