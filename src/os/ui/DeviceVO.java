package os.ui;

import javafx.beans.property.SimpleStringProperty;

/**
 * @package: os.ui
 * @Description:
 * @author: Brandon
 * @date: 2023/1/12 10:09
 **/
public class DeviceVO {
    private SimpleStringProperty deviceName;
    private SimpleStringProperty PID;

    public DeviceVO(String deviceName,int PID) {
        this.deviceName = new SimpleStringProperty(deviceName);
        this.PID = new SimpleStringProperty(PID + "");
    }

    public String getDeviceName() {
        return deviceName.get();
    }

    public SimpleStringProperty deviceNameProperty() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName.set(deviceName);
    }

    public String getPID() {
        return PID.get();
    }

    public SimpleStringProperty PIDProperty() {
        return PID;
    }

    public void setPID(String PID) {
        this.PID.set(PID);
    }
}
