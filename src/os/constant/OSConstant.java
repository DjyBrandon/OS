package os.constant;

/**
 * @package: os.constant
 * @Description: 操作系统常量
 * @author: Brandon
 * @date: 2023/1/12 9:27
 **/
public class OSConstant {

    /**
     * 磁盘
     */

    // 模拟磁盘txt文件
    public static final String DISK_FILE = "src/os/resources/disk.txt";

    // 物理块大小64B
    public static final int DISK_BLOCK_SIZE = 64;

    // 物理块数量128块
    public static final int DISK_BLOCK_QUANTITY = 128;

    /**
     * 内存
     */

    // 用户区
    public static final int USER_AREA_SIZE = 512;

    /**
     * 进程
     */

    // 最大进程数
    public static final int PROCESS_MAX = 10;
}
