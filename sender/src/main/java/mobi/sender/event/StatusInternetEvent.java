package mobi.sender.event;

import mobi.sender.Bus;

public class StatusInternetEvent implements Bus.Event {

    private boolean isVisible;

    public StatusInternetEvent(boolean isConnected) {
        this.isVisible = isConnected;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

}
