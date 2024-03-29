package os.manager.process;

import os.OS;

/**
 * @package: os.manager.process
 * @Description: 系统时钟
 * @author: Brandon
 * @date: 2023/1/12 10:07
 **/
public class Clock implements Runnable {

    // 时间片长度
    private static final long TIMESLICE_LENGTH = 5;

    // 时间片单位（毫秒）
    public static final long TIMESLICE_UNIT = 1000;

    // 系统时钟
    private long systemTime;

    // 当前进程剩下的运行时间
    private long restTime;

    // CPU
    private CPU cpu;

    public Clock() {
        this.cpu = OS.cpu;
        init();
    }

    /**
     * 初始化时钟
     */
    public void init() {
        systemTime = 0;
        restTime = TIMESLICE_LENGTH;
    }

    @Override
    public void run() {
        while (OS.launched) {
            try {
                Thread.sleep(TIMESLICE_UNIT);
                systemTime += TIMESLICE_UNIT/1000;
                restTime = (restTime + TIMESLICE_LENGTH - TIMESLICE_UNIT / 1000) % TIMESLICE_LENGTH;
                // 时间片到了
                if (restTime == 0) {
                    cpu.lock.lock();
                    cpu.toReady();
                    cpu.dispatch();
                    cpu.lock.unlock();
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public long getSystemTime() {
        return systemTime;
    }

    public long getRestTime() {
        return restTime;
    }
}
