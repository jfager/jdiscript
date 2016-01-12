package cern.accsoft.lhc.inspector.inspectable;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;

/**
 * The range for a piece of code (starting and stopping) measured in {@link com.sun.jdi.Location}s.
 */
public class LocationRange {

    private final Location start;
    private final Location end;

    public LocationRange(Location start, Location end) {
        this.start = start;
        this.end = end;
    }

    public Location getStart() {
        return start;
    }

    public Location getEnd() {
        return end;
    }

    public boolean isWithin(Location location) {
        return location.method().equals(start.method()) && location.lineNumber() >= start.lineNumber()
                && location.lineNumber() <= end.lineNumber();
    }

    public static LocationRange ofMethod(Method method) throws AbsentInformationException {
        List<Location> lines = new ArrayList<>(method.allLineLocations());
        lines.sort((location1, location2) -> Integer.compare(location1.lineNumber(), location2.lineNumber()));
        return new LocationRange(lines.get(0), lines.get(lines.size() - 1));
    }
}
