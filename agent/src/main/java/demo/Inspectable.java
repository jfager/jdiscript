package demo;

import cern.accsoft.lhc.inspector.Inspector;

/**
 * A class that is inspectable, i. e. where the {@link #run()} method will be inspected one instruction at a time.
 */
public interface Inspectable {

    /**
     * All code in this method will - when run via the {@link Inspector} - be executed one by one.
     */
    void run();

}
