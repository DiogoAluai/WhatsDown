package daluai.app.whatsdown.ui;

import android.util.Log;

/**
 * Aluai Log, developed by Diogo Aluai.
 * Reduces the hassle of logging by setting up a tag.
 */
public class ALog {

    private final String tag;

    public ALog(Class<?> tag) {
        this.tag = tag.getSimpleName();
    }

    public void i(String message) {
        Log.i(tag, message);
    }

    public void i(String message, Throwable e) {
        Log.i(tag, message, e);
    }

    public void e(String message) {
        Log.e(tag, message);
    }

    public void e(String message, Throwable e) {
        Log.e(tag, message, e);
    }

    public void w(String message) {
        Log.w(tag, message);
    }

    public void w(String message, Throwable e) {
        Log.w(tag, message, e);
    }
}
