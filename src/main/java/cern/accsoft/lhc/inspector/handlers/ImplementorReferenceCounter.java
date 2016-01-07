package cern.accsoft.lhc.inspector.handlers;

import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.event.ClassPrepareEvent;
import org.jdiscript.handlers.OnClassPrepare;

import java.util.*;
import java.util.function.Consumer;

/**
 * An {@link OnClassPrepare} implementation which keeps track of new implementations of a given interface
 * ({@link com.sun.jdi.InterfaceType}).
 */
public class ImplementorReferenceCounter implements OnClassPrepare {

    private final Consumer<ClassType> implementorCallback;
    private final Class<?> implementorInterface;

    private Set<ClassType> currentImplementations = Collections.emptySet();
    private InterfaceType implementorType;

    public ImplementorReferenceCounter(Class<?> implementorInterface, Consumer<ClassType> implementorCallback) {
        this.implementorInterface = implementorInterface;
        this.implementorCallback = implementorCallback;
    }

    @Override
    public void classPrepare(ClassPrepareEvent event) {
        ReferenceType referenceType = event.referenceType();
        if (implementorType == null) {
            if (referenceType instanceof InterfaceType &&
                    referenceType.name().equals(implementorInterface.getName())) {
                implementorType = (InterfaceType) referenceType;
            }
        } else {
            Set<ClassType> currentInspectableImplementations = new HashSet<>(implementorType.implementors());
            Set<ClassType> difference = difference(currentImplementations, currentInspectableImplementations);
            currentImplementations = currentInspectableImplementations;
            difference.forEach(implementorCallback::accept);
        }
    }

    private <T> Set<T> difference(Set<T> first, Set<T> second) {
        final Set<T> difference = new HashSet<>();
        final Set<T> longest = first.size() > second.size() ? first : second;
        final Set<T> shortest = first.size() < second.size() ? first : second;
        for (T elem : longest) {
            if (!shortest.contains(elem)) {
                difference.add(elem);
            }
        }
        return Collections.unmodifiableSet(difference);
    }
}
