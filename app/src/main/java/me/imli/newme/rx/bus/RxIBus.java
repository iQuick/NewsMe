package me.imli.newme.rx.bus;

/**
 * Created by Em on 2015/11/27.
 */
public interface RxIBus {


    /**
     * Register an event target
     *
     * @param target
     * @return
     */
    boolean register(Object target);

    /**
     * UnRegister the event target
     *
     * @param target
     * @return
     */
    boolean unregister(Object target);

    /**
     * Post event
     *
     * @param event
     */
    void post(Object event);

}
