package os.manager.device;

import os.manager.process.PCB;

/**
 * @package: os.manager.device
 * @Description:
 * @author: Brandon
 * @date: 2023/1/12 10:08
 **/
public class DeviceRequest {
    private PCB pcb;

    // 占用时间，以毫秒为单位
    private long workTime;

    // 设备名称
    private String deviceName;

    public PCB getPcb() {
        return pcb;
    }

    public void setPcb(PCB pcb) {
        this.pcb = pcb;
    }

    public long getWorkTime() {
        return workTime;
    }

    public void setWorkTime(long workTime) {
        this.workTime = workTime;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}