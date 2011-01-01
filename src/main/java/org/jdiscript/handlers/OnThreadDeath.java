package org.jdiscript.handlers;

import com.sun.jdi.event.ThreadDeathEvent;

public interface OnThreadDeath extends DebugEventHandler
{
    void threadDeath(ThreadDeathEvent event);
}
