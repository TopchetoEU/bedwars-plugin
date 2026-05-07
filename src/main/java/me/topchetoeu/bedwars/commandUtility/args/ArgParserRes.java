package me.topchetoeu.bedwars.commandUtility.args;

public class ArgParserRes {
    private int takenCount;
    private Object res;
    private boolean failed;
    private String error;

    public int getTakenCount() {
        return takenCount;
    }
    public Object getResult() {
        return res;
    }
    public String getError() {
        return error;
    }
    public boolean takenAny() {
        return takenCount > 0;
    }
    public boolean hasResult() {
        return res != null;
    }
    public boolean hasFailed() {
        return failed;
    }
    public boolean hasSucceeded() {
        return !failed;
    }
    public boolean hasError() {
        return error != null && !error.isEmpty();
    }

    private ArgParserRes(int takenCount, Object res, String error, boolean failed) {
        this.takenCount = takenCount;
        this.res = res;
        this.failed = failed;
        this.error = error;
    }

    public static ArgParserRes error(String error) {
        if (error == null || error.isEmpty()) error = "An error ocurred while paring the arguments";

        return new ArgParserRes(0, null, error, true);
    }

    public static ArgParserRes fail() {
        return new ArgParserRes(0, null, null, true);
    }

    public static ArgParserRes takenNone() {
        return takenNone(null);
    }
    public static ArgParserRes takenOne() {
        return takenOne(null);
    }
    public static ArgParserRes takenMany(int count) {
        return takenMany(count, null);
    }

    public static ArgParserRes takenNone(Object res) {
        return new ArgParserRes(0, res, null, false);
    }
    public static ArgParserRes takenOne(Object res) {
        return new ArgParserRes(1, res, null, false);
    }
    public static ArgParserRes takenMany(int count, Object res) {
        if (count < 1) return takenNone(res);
        return new ArgParserRes(count, res, null, false);
    }
}
