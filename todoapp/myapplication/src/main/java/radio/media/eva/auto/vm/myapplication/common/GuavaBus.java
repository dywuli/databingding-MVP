package radio.media.eva.auto.vm.myapplication.common;

import android.os.Handler;
import android.os.Looper;

import com.google.common.eventbus.EventBus;

/**
 *
 * @author lcz
 * @date 18-1-17
 */

public class GuavaBus {
    private static GuavaBus sBus = null;
    private EventBus mBus;
    private Handler mHandler;

    private GuavaBus() {
        mBus = new EventBus();
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * To get a bus instance.
     *
     * @return A instance of GuavaBus.
     */
    public static GuavaBus getInstance() {
        if (null == sBus) {
            synchronized (GuavaBus.class) {
                if (null == sBus) {
                    sBus = new GuavaBus();
                }
            }
        }

        return sBus;
    }

    /**
     * To register a subscriber.
     *
     * @param subscriber The subscriber need to register.
     */
    public void register(final Object subscriber) {
        mBus.register(subscriber);
    }

    /**
     * To remove a subscriber previous register.
     *
     * @param subscriber The subscriber to remove.
     */
    public void unregister(final Object subscriber) {
        mBus.unregister(subscriber);
    }

    /**
     * Post an event to subscribers, will post on main thread.
     *
     * @param event The event need to post to subscribers.
     */
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mBus.post(event);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBus.post(event);
                }
            });
        }
    }
}
