package com.zw.platform.event;

public class SimNumberUpdateEvent {
    private String oldSim;
    private String newSim;

    public String getOldSim() {
        return oldSim;
    }

    public void setOldSim(String oldSim) {
        this.oldSim = oldSim;
    }

    public String getNewSim() {
        return newSim;
    }

    public void setNewSim(String newSim) {
        this.newSim = newSim;
    }

    public SimNumberUpdateEvent(String oldNumber, String newNumber) {
        this.oldSim = oldNumber;
        this.newSim = newNumber;
    }
}
