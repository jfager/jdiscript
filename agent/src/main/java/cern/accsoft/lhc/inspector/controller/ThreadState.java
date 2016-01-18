package cern.accsoft.lhc.inspector.controller;

import cern.accsoft.lhc.inspector.inspectable.LocationRange;
import com.sun.jdi.Location;

/**
 * An immutable state for a {@link com.sun.jdi.ThreadReference}.
 */
public class ThreadState {

    private final StepDirection stepDirection;
    private final LocationRange inspectableRange;
    private final Location currentLocation;

    public ThreadState(StepDirection stepDirection, LocationRange inspectableRange, Location currentLocation) {
        this.stepDirection = stepDirection;
        this.inspectableRange = inspectableRange;
        this.currentLocation = currentLocation;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public StepDirection getStepDirection() {
        return stepDirection;
    }

    public Location getStartLocation() {
        return inspectableRange.getStart();
    }

    public Location getEndLocation() {
        return inspectableRange.getEnd();
    }

    public ThreadState setLocation(Location location) {
        return new ThreadState(stepDirection, inspectableRange, location);
    }

    public enum StepDirection {
        BACKWARD, FORWARD;
    }
}
