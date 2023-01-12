package os.manager.file;

/**
 * @package: os.manager.file
 * @Description:
 * @author: Brandon
 * @date: 2023/1/12 11:50
 **/
public class Pointer {

    // 块号
    private int blockNo;
    // 块内地址
    private int address;

    public int getBlockNo() {
        return blockNo;
    }

    public void setBlockNo(int blockNo) {
        this.blockNo = blockNo;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

}
