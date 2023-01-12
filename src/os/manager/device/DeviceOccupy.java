package os.manager.device;

import os.manager.process.PCB;
import os.others.DelayItem;

import java.util.concurrent.TimeUnit;

/**
 * @package: os.manager.device
 * @Description:
 * @author: Brandon
 * @date: 2023/1/12 10:08
 **/
public class DeviceOccupy extends DelayItem<PCB> {
    private String deviceName;

    public DeviceOccupy(PCB obj, long workTime, TimeUnit timeUnit) {
        super(obj, workTime, timeUnit);
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
