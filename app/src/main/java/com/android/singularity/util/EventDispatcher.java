package com.android.singularity.util;

import java.util.ArrayList;
import java.util.List;

public class EventDispatcher {
    static List<EventListener> listeners = new ArrayList<>();

    public static void addEventListener(EventListener listener) {
        EventDispatcher.listeners.add(listener);
    }

    public static void callOnDataChange() {
        for (EventListener eventListener : EventDispatcher.listeners) {
            eventListener.onChange();
        }
    }
}
