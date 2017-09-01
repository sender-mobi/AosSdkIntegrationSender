package mobi.sender.tool;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 11.02.16
 * Time: 13:20
 */
public class TimeMeter {

    private long startTime;
    private String oper;

    public TimeMeter(String oper) {
        this.startTime = System.currentTimeMillis();
        this.oper = oper;
    }

    public void end() {
        long t = System.currentTimeMillis() - startTime;
        Tool.log("[TM] " + oper + " " + t + " ms");
    }
}
