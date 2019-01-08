package odyssey.projects.callbacks;

import odyssey.projects.debug.LogItemType;

public interface DebugLogListener {
    public void addToLog(String tag, LogItemType type, String message);
}
