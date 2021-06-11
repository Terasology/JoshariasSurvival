// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.joshariasSurvival.components;

import org.terasology.engine.entitySystem.Component;

public class ShearableSheepComponent implements Component {
    int clicks = 0;
    long lastShearingTimestamp = -1;
    boolean sheared = false;

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public long getLastShearingTimestamp() {
        return lastShearingTimestamp;
    }

    public void setLastShearingTimestamp(long lastShearingTimestamp) {
        this.lastShearingTimestamp = lastShearingTimestamp;
    }

    public boolean isSheared() {
        return sheared;
    }

    public void setSheared(boolean sheared) {
        this.sheared = sheared;
    }
}
