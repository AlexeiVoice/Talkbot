package com.pd.a2.talkbot.util;

/**
 * Contains event for EventBus
 */
public class PlaybackEvent {
    public final State state;
    public PlaybackEvent(State state) {
        this.state = state;
    }
}