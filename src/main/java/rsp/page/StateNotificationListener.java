package rsp.page;

public class StateNotificationListener {

    private volatile Runnable livePageState;

    public void notifyStateUpdate() {
        livePageState.run();
    }

    public <S> void setListener(final Runnable livePageState) {
        this.livePageState = livePageState;
    }
}