package os.manager.memory;

import os.constant.OSConstant;
import os.manager.process.PCB;

import java.util.*;

/**
 * @package: os.manager.memory
 * @Description: 内存
 * @author: Brandon
 * @date: 2023/1/12 10:07
 **/
public class Memory {

    // 内存分配表
    private List<SubArea> subAreas;

    // 就绪进程控制块
    private Queue<PCB> waitPCB;

    // 阻塞进程控制块
    private Queue<PCB> blockPCB;

    // 运行进程
    private PCB runningPCB;

    // 闲逛进程
    private PCB hangOutPCB;

    // 用户区内存
    private byte[] userArea;

    public Memory() {
        subAreas = Collections.synchronizedList(new LinkedList<>());
        waitPCB = new LinkedList<>();
        blockPCB = new LinkedList<>();
        hangOutPCB = new PCB();
        userArea = new byte[OSConstant.USER_AREA_SIZE];
    }

    public void init(){
        Arrays.fill(userArea,(byte) 0);
        waitPCB.removeAll(waitPCB);
        blockPCB.removeAll(blockPCB);
        hangOutPCB.setStatus(PCB.STATUS_RUN);
        runningPCB = hangOutPCB;
        subAreas.removeAll(subAreas);
        SubArea subArea = new SubArea();
        subArea.setSize(OSConstant.USER_AREA_SIZE);
        subArea.setStartAdd(0);
        subArea.setStatus(SubArea.STATUS_FREE);
        subAreas.add(subArea);
    }
    public List<SubArea> getSubAreas() {
        return subAreas;
    }

    public void setSubAreas(List<SubArea> subAreas) {
        this.subAreas = subAreas;
    }

    public Queue<PCB> getWaitPCB() {
        return waitPCB;
    }

    public void setWaitPCB(Queue<PCB> waitPCB) {
        this.waitPCB = waitPCB;
    }

    public Queue<PCB> getBlockPCB() {
        return blockPCB;
    }

    public void setBlockPCB(Queue<PCB> blockPCB) {
        this.blockPCB = blockPCB;
    }

    public byte[] getUserArea() {
        return userArea;
    }

    public void setUserArea(byte[] userArea) {
        this.userArea = userArea;
    }

    public PCB getRunningPCB() {
        return runningPCB;
    }

    public void setRunningPCB(PCB runningPCB) {
        this.runningPCB = runningPCB;
    }

    public PCB getHangOutPCB() {
        return hangOutPCB;
    }

    public List<PCB> getAllPCB() {
        List<PCB> allPCB=new ArrayList<>(10);
        allPCB.add(runningPCB);
        allPCB.addAll(blockPCB);
        allPCB.addAll(waitPCB);
        return allPCB;
    }
}
