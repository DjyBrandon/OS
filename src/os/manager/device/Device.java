package os.manager.device;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @package: os.manager.device
 * @Description:
 * @author: Brandon
 * @date: 2023/1/12 10:08
 **/
public class Device {
    public static final int STATUS_FREE = 0;

    public static final int STATUS_BUSY = 1;

    // 设备状态
    private int status;

    // 占用时间
    private int timeout;

    // 设备名称
    protected String name;

    // 设备数量
    private volatile AtomicInteger count;

    public Device(int count) {
        this.count = new AtomicInteger(count);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count.intValue();
    }

    public void increaseCount(){
        count.getAndIncrement();
    }

    public int decreaseCount(){
        return count.getAndDecrement();
    }

    public void setCount(int count) {
        this.count.set(count);
    }

}
