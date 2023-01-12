package os.manager.process;

import os.OS;
import os.manager.device.DeviceManager;
import os.manager.device.DeviceRequest;
import os.manager.memory.Memory;
import os.manager.memory.SubArea;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @package: os.manager.process
 * @Description:
 * @author: Brandon
 * @date: 2023/1/12 10:07
 **/
public class CPU implements Runnable {

    static ReentrantLock lock = new ReentrantLock();

    // 指令寄存器
    private int IR;
    // 寄存器组
    // 0
    private int AX;
    // 1
    private int BX;
    // 2
    private int CX;
    // 3
    private int DX;
    // 程序计数器
    private int PC;

    private int nextIR;

    private int OP;

    private int DR;

    private int SR;

    private String result = "NOP";

    private Memory memory;

    private DeviceManager deviceManager;

    public CPU() {
        this.memory = OS.memory;
        deviceManager = new DeviceManager(this);
    }

    /**
     * 初始化 CPU
     */
    public void init() {
        IR = 0;
        AX = 0;
        BX = 0;
        CX = 0;
        DX = 0;
        PC = 0;
        // todo 先初始化设备
        deviceManager.init();
    }

    /**
     * 取指令
     */
    public void fetchInstruction() {
        if (memory.getRunningPCB() == memory.getHangOutPCB()) {
            // NOP不执行，闲逛进程让出处理器资源
            IR = 0;
        } else {
            byte[] userArea = memory.getUserArea();
            IR = userArea[PC];
            PC++;
        }
    }

    /**
     * 译码
     */
    public void identifyInstruction() {

        // 高4位代表操作码，五六位代表目的寄存器，剩余的两位代表原寄存器
        OP = (IR >> 4) & 0x0f;
        DR = (IR >> 2) & 0x03;
        SR = IR & 0x03;

        if(OP == 5)
        {
            byte[] userArea = memory.getUserArea();
            nextIR = userArea[PC];
            // PC++;
        }
    }

    /**
     * 执行和写回
     */
    public void execute() {
        result = "NOP";
        if(IR != 0)
        {
            result ="";
            switch (OP) {

                // ADD
                case 1:
                    switch (DR) {
                        case 0:
                            AX++;result +="INC AX, AX="+AX;
                            break;
                        case 1:
                            BX++;result +="INC BX, BX="+BX;
                            break;
                        case 2:
                            CX++;result +="INC CX, CX=" +CX;
                            break;
                        case 3:
                            DX++;
                            result += "INC DX, DX=" + DX;
                            break;
                    }
                    break;

                // DEC
                case 2:
                    switch (DR) {
                        case 0:
                            AX--;
                            result += "DEC AX, AX=" + AX;
                            break;
                        case 1:
                            BX--;
                            result += "DEC BX, BX=" + BX;
                            break;
                        case 2:
                            CX--;
                            result += "DEC CX, CX=" + CX;
                            break;
                        case 3:
                            DX--;
                            result += "DEC DX, DX=" + DX;
                            break;
                    }
                    break;

                // 使用设备执行一些任务
                case 3:
                    String deviceName = null;
                    switch (DR) {
                        case 0:
                            deviceName = "A";
                            break;
                        case 1:
                            deviceName = "B";
                            break;
                        case 2:
                            deviceName = "C";
                            break;
                    }

                    result += "! Device: " + DR + ", Time:" + SR;
                    DeviceRequest deviceRequest=new DeviceRequest();
                    deviceRequest.setDeviceName(deviceName);
                    deviceRequest.setWorkTime(SR*5000);
                    deviceRequest.setPcb(memory.getRunningPCB());
                    deviceManager.requestDevice(deviceRequest);
                    // 阻塞进程
                    block();
                    dispatch();

                    break;

                // END
                case 4:
                    result += "END";
                    destroy();
                    dispatch();
                    break;

                // MOV
                case 5:switch (DR) {
                    case 0:
                        AX = nextIR;
                        result += "MOV AX," + nextIR + ", AX=" + AX;
                        break;
                    case 1:
                        BX = nextIR;
                        result += "MOV BX," + nextIR + ", BX=" + BX;
                        break;
                    case 2:
                        CX = nextIR;
                        result += "MOV CX," + nextIR + ", CX=" + CX;
                        break;
                    case 3:
                        DX = nextIR;
                        result += "MOV DX," + nextIR + ", DX=" + DX;
                        break;
                }
                break;
            }
        }
    }

    /**
     * 进程调度,将进程从就绪态恢复到运行态
     */
    public void dispatch() {
        // 当前运行的进程
        PCB pcb1 = memory.getRunningPCB();
        // 要运行的进程
        PCB pcb2 = memory.getWaitPCB().poll();
        if (pcb2 == null){
            pcb2 = memory.getRunningPCB();
        }
        // 如果第一个就绪进程是闲逛进程且还有其他的就绪进程
        if (pcb2 == memory.getHangOutPCB() && memory.getWaitPCB().size() > 0){
            memory.getWaitPCB().offer(pcb2);
            pcb2=memory.getWaitPCB().poll();
        }

        memory.setRunningPCB(pcb2);
        pcb2.setStatus(PCB.STATUS_RUN);
        // 保存现场
        saveContext(pcb1);
        // 恢复现场
        recoveryContext(pcb2);
        System.out.println("要运行:" + pcb2.getPID());
    }

    /**
     * 进程撤销
     */
    public void destroy(){
        PCB pcb=memory.getRunningPCB();
        System.out.println("进程" + pcb.getPID() + "运行结束，撤销进程");
        // 回收进程所占内存
        SubArea subArea=null;
        List<SubArea> subAreas = memory.getSubAreas();
        for (SubArea s: subAreas){
            if (s.getTaskNo() == pcb.getPID()){
                subArea=s;
                break;
            }
        }
        subArea.setStatus(SubArea.STATUS_FREE);
        int index=subAreas.indexOf(subArea);
        // 如果不是第一个，判断上一个分区是否为空闲
        if (index > 0){
            SubArea preSubArea = subAreas.get(index-1);
            if(preSubArea.getStatus() == SubArea.STATUS_FREE) {
                preSubArea.setSize(preSubArea.getSize() + subArea.getSize());
                subAreas.remove(subArea);
                subArea = preSubArea;
            }
        }
        // 如果不是最后一个，判断下一个分区是否空闲
        if (index < subAreas.size()-1) {
            SubArea nextSubArea=subAreas.get(index+1);
            if (nextSubArea.getStatus() == SubArea.STATUS_FREE) {
                nextSubArea.setSize(nextSubArea.getSize() + subArea.getSize());
                nextSubArea.setStartAdd(subArea.getStartAdd());
                subAreas.remove(subArea);
            }
        }
    }

    /**
     * 将运行进程转换为就绪态
     */
    public void toReady(){
        PCB pcb = memory.getRunningPCB();
        System.out.println("进程" + pcb.getPID() + "被放入就绪队列");
        memory.getWaitPCB().offer(pcb);
        pcb.setStatus(PCB.STATUS_WAIT);
    }

    /**
     * 将运行进程转换为阻塞态
     */
    public void block(){
        PCB pcb = memory.getRunningPCB();
        // 修改进程状态
        pcb.setStatus(PCB.STATUS_BLOCK);
        // 将进程链入对应的阻塞队列，然后转向进程调度
        memory.getBlockPCB().add(pcb);
    }

    /**
     * 进程唤醒
     */
    public void awake(PCB pcb){
        lock.lock();
         System.out.println("唤醒进程" + pcb.getPID());
        // 将进程从阻塞队列中调入到就绪队列
        pcb.setStatus(PCB.STATUS_WAIT);
        pcb.setEvent(PCB.EVENT_NOTING);
        memory.getBlockPCB().remove(pcb);
        memory.getWaitPCB().add(pcb);
        lock.unlock();
    }

    /**
     * 保护现场
     */
    private void saveContext(PCB pcb){
        pcb.setCounter(PC);
        pcb.setAX(this.AX);
        pcb.setBX(this.BX);
        pcb.setCX(this.CX);
        pcb.setDX(this.DX);
    }

    /**
     * 恢复现场
     */
    private void recoveryContext(PCB pcb){
        pcb.setStatus(PCB.STATUS_RUN);
        this.AX = pcb.getAX();
        this.BX = pcb.getBX();
        this.DX = pcb.getDX();
        this.CX = pcb.getCX();
        this.PC = pcb.getCounter();
    }

    @Override
    public void run() {
        while (OS.launched) {
            try {
                Thread.sleep(Clock.TIMESLICE_UNIT);
            } catch (InterruptedException e) {
                return;
            }
            lock.lock();
            try {
                fetchInstruction();
                identifyInstruction();
                execute();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    public String getResult()
    {
        String temp;
        lock.lock();
        temp=result;
        lock.unlock();
        return temp;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

}
