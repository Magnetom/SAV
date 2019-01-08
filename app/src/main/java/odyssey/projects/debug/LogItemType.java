package odyssey.projects.debug;

import odyssey.projects.sav.driver.R;

public enum LogItemType {
    TYPE_DEBUG, TYPE_INFO, TYPE_WARNING, TYPE_ERROR;

    @Override
    public String toString() {

        switch (this){
            case TYPE_DEBUG:
                return "DEBUG";
            case TYPE_INFO:
                return "INFO";
            case TYPE_WARNING:
                return "WARNING";
            case TYPE_ERROR:
                return "ERROR";
            default:
                return "UNKNOWN";
        }
    }
}
