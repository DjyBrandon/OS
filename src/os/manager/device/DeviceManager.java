package os.manager.device;

import os.OS;
import os.manager.process.CPU;
import os.manager.process.Clock;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * @package: os.manager.device
 * @Description:
 * @author: Brandon
 * @date: 2023/1/12 10:08
 **/
public class DeviceManager {

    // CPU
    private CPU cpu;

    // 设备A
    private A a;

    // 设备B
    private B b;

    // 设备C
    private C c;

    // 使用中的设备
    private DelayQueue<DeviceOccupy> usingDevices;

    // 等待使用设备的进程队列
    private BlockingQueue<DeviceRequest> waitForDevice;

    public DeviceManager(CPU cpu){
        // 2个A设备
        a=new A(2);
        // 3个B设备
        b=new B(3);
        // 3个C设备
        c=new C(3);
        usingDevices =new DelayQueue<>();
        waitForDevice=new ArrayBlockingQueue<>(20);
        this.cpu=cpu;
    }

    public void init() {
        a.setCount(2);
        b.setCount(3);
        c.setCount(3);
        usingDevices.removeAll(usingDevices);
        waitForDevice.removeAll(waitForDevice);
        // 释放设备线程
        new Thread(() -> {
            while (OS.launched) {
                try {
                    DeviceOccupy deviceOccupy = usingDevices.take();
                    System.out.println(deviceOccupy.getDeviceName() + "设备使用完毕！");
                    deviceDone(deviceOccupy);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        // 处理设备请求线程
        new Thread(() -> {
            while (OS.launched) {
                try {
                    DeviceRequest deviceRequest = waitForDevice.take();
                    DeviceOccupy deviceOccupy = new DeviceOccupy(deviceRequest.getPcb(), deviceRequest.getWorkTime(), TimeUnit.MILLISECONDS);
                    deviceOccupy.setDeviceName(deviceRequest.getDeviceName());
                    switch (deviceRequest.getDeviceName()) {
                        case "A":
                            // 如果有A设备空闲就使用A设备
                            if (a.getCount() > 0) {
                                // 可用设备减1
                                System.out.println("设备" + deviceRequest.getDeviceName() + "可用，分配给进程" + deviceRequest.getPcb().getPID());
                                a.decreaseCount();
                                usingDevices.put(deviceOccupy);
                            }
                            // 否则将设备请求重新放到请求队列中
                            else {
                                System.out.println("无可用" + deviceRequest.getDeviceName() + "设备，等待分配");
                                waitForDevice.put(deviceRequest);
                            }
                            break;
                        case "B":
                            // 如果有B设备空闲就使用B设备
                            if (b.getCount() > 0){
                                // 可用设备减1
                                System.out.println("设备" + deviceRequest.getDeviceName() + "可用，分配给进程" + deviceRequest.getPcb().getPID());
                                b.decreaseCount();
                                usingDevices.put(deviceOccupy);
                            }
                            // 否则将设备请求重新放到请求队列中
                            else {
                                System.out.println("无可用" + deviceRequest.getDeviceName() + "设备，等待分配");
                                waitForDevice.put(deviceRequest);
                            }
                            break;
                        case "C":
                            // 如果有C设备空闲就使用C设备
                            if (c.getCount()>0){
                                // 可用设备减1
                                System.out.println("设备" + deviceRequest.getDeviceName() + "可用，分配给进程" + deviceRequest.getPcb().getPID());
                                c.decreaseCount();
                                usingDevices.put(deviceOccupy);
                            }
                            // 否则将设备请求重新放到请求队列中
                            else {
                                System.out.println("无可用" + deviceRequest.getDeviceName() + "设备，等待分配");
                                waitForDevice.put(deviceRequest);
                            }
                            break;
                    }
                    Thread.sleep(Clock.TIMESLICE_UNIT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 请求使用设备
     * @param
     */
    public void requestDevice(DeviceRequest deviceRequest){
        try {
            System.out.println("进程" + deviceRequest.getPcb().getPID() + "请求使用设备" + deviceRequest.getDeviceName());
            waitForDevice.put(deviceRequest);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设备使用结束，释放资源，请求中断
     */
    private void deviceDone(DeviceOccupy deviceOccupy){
        // 释放资源
        switch (deviceOccupy.getDeviceName()) {
            case "A":
                a.increaseCount();
                break;
            case "B":
                b.increaseCount();
                break;
            case "C":
                c.increaseCount();
                break;
        }
        // 将进程从阻塞队列中移到就绪队列
        cpu.awake(deviceOccupy.getObj());
    }

    public DelayQueue<DeviceOccupy> getUsingDevices() {
        return usingDevices;
    }

    public BlockingQueue<DeviceRequest> getWaitForDevice() {
        return waitForDevice;
    }
}
