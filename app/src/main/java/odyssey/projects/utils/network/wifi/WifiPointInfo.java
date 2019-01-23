package odyssey.projects.utils.network.wifi;

public class WifiPointInfo {
    private String SSID;
    private String BSSID;

    public WifiPointInfo() {}

    public WifiPointInfo(String SSID, String BSSID) {
        this.SSID = SSID;
        this.BSSID = BSSID;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }
}
