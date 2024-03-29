package os;

import os.constant.OSConstant;
import os.controller.MainController;
import os.manager.file.FileOperator;
import os.manager.memory.Memory;
import os.manager.process.CPU;
import os.manager.process.Clock;
import os.manager.process.ProcessCreator;

import java.io.*;

import static os.constant.OSConstant.DISK_BLOCK_QUANTITY;
import static os.constant.OSConstant.DISK_BLOCK_SIZE;

/**
 * @package: os
 * @Description:
 * @author: Brandon
 * @date: 2023/1/12 9:42
 **/
public class OS {

    // 模拟磁盘
    public static RandomAccessFile disk;

    // 文件操作管理
    public static FileOperator fileOperator;

    // 进程创建器
    public static ProcessCreator processCreator;

    // CPU
    public static CPU cpu;

    // 内存
    public static Memory memory;

    // 时钟
    public static Clock clock;

    // 电源
    public static volatile boolean launched;

    // 界面控制类
    public MainController mainController;

    public static OS os;

    static {
        // 创建操作系统可调度的资源
        try {

            // 模拟磁盘
            initDisk();
            disk = new RandomAccessFile(OSConstant.DISK_FILE, "rw");

            // 内存
            memory = new Memory();

            // CPU
            cpu = new CPU();

            // 时钟
            clock = new Clock();

            // 进程控制
            processCreator = new ProcessCreator();

            // 文件管理
            fileOperator = new FileOperator();

            // 操作系统
            os = new OS();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化操作系统
     */
    public void init() throws Exception {
        cpu.init();
        memory.init();
        clock.init();
        fileOperator.init();
    }


    /**
     * 初始化模拟磁盘
     */
    static void initDisk() {
        File file = new File(OSConstant.DISK_FILE);
        FileOutputStream fout = null;
        // 判断模拟磁盘是否已创建
        if (!file.exists()) {
            try {
                fout = new FileOutputStream(file);
                byte[] bytes;
                for (int i = 0; i < DISK_BLOCK_QUANTITY; i++) {
                    bytes = new byte[DISK_BLOCK_SIZE];
                    // 写入初始文件分配表
                    if (i == 0) {
                        // 前三个盘块不可用
                        bytes[0] = -1;
                        bytes[1] = -1;
                        bytes[2] = -1;
                    }

                    // 写入根目录
                    if (i == 2) {
                        // 根目录名为root
                        bytes[0] = 'r';
                        bytes[1] = 'o';
                        bytes[2] = 'o';
                        bytes[3] = 't';
                        bytes[4] = 0;
                        // 目录属性
                        bytes[5] = Byte.parseByte("00001000", 2);
                        // 起始盘号
                        bytes[6] = -1;
                        // 保留一字节未使用
                        bytes[7] = 0;
                    }
                    fout.write(bytes);
                }
            } catch (FileNotFoundException e) {
                java.lang.System.out.println("打开/新建磁盘文件失败！");
                e.printStackTrace();
                java.lang.System.exit(0);
            } catch (IOException e) {
                java.lang.System.out.println("写入文件时发生错误");
                e.printStackTrace();
                java.lang.System.exit(0);
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (IOException e) {
                        java.lang.System.out.println("关闭文件流时发生错误");
                        e.printStackTrace();
                    }
                }
            }
        } else {
            // 模拟磁盘已存在，无需重新创建
        }
    }


    /**
     * 启动系统
     */
    public void start() throws Exception {
        init();
        new Thread(cpu).start();
        new Thread(clock).start();

    }

    public static synchronized OS getInstance( ) throws Exception {
        return os;
    }


    /**
     * 关闭系统资源
     */
    public void close() {
        launched = false;
    }

    public static RandomAccessFile getDisk() {
        return disk;
    }

    public static FileOperator getFileOperator() {
        return fileOperator;
    }

    public static ProcessCreator getProcessCreator() {
        return processCreator;
    }

    public static CPU getCpu() {
        return cpu;
    }

    public static Memory getMemory() {
        return memory;
    }

    public static Clock getClock() {
        return clock;
    }

    public static boolean isLaunched() {
        return launched;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        fileOperator.setMainController(mainController);
    }

    public MainController getMainController() {
        return mainController;
    }



}
