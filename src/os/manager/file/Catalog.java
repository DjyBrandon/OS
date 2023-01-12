package os.manager.file;

import os.OS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @package: os.manager.file
 * @Description:
 * @author: Brandon
 * @date: 2023/1/12 10:08
 **/
public class Catalog {
    // 目录项占用空间
    private byte[] bytes;

    // 文件名
    private String name;

    // 文件类型，如果是目录则为空格
    private String type;

    // 文件属性
    private int property;

    // 文件内容起始盘块号
    private int startBlock;

    // 文件长度，单位为盘块，如果是目录则为0
    private int fileLength;

    // 是否是可执行文件
    public boolean executable;

    // 是否是目录
    private boolean isDirectory;

    // 目录所在磁盘块号
    private int catalogBlock;

    // 是否为空
    private boolean isBlank;

    public Catalog(byte[] bytes) {
        this.bytes = bytes;
        // todo 目录项各字段长度，从0长度5，从3长度2
        this.name = new String(bytes, 0, 5);
        this.type = new String(bytes, 3, 2);
        setProperty(bytes[5]);
        this.startBlock = bytes[6];
        // todo 移位操作，除以2^4余1，可以设置属性后为属性赋值
        if (property >> 4 == 1) {
            executable = true;
        } else if (property >> 3 == 1) {
            isDirectory = true;
        }
        if (startBlock == 1) {
            isBlank = true;
        } else {
            isBlank = false;
        }

        this.fileLength = bytes[7];
    }

    public Catalog(String fileName, int property) throws Exception {
        this.bytes = new byte[8];
        this.setStartBlock(-1);
        this.setDirectory(false);
        this.setFileLength(0);
        this.setName(fileName);
        this.setProperty(property);
        // todo 文件属性 = 8
        if (property == 8) {
            isDirectory = true;
        } else {
            isDirectory = false;
        }
        this.isBlank = true;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getName() {
        // 移除字符串右侧的空白字符或其他预定义字符
        return name.trim();
    }

    public void setName(String name) throws Exception {
        this.name = name;
        byte[] nameBytes = name.getBytes();
        // todo 文件名长度5
        if (nameBytes.length > 5) {
            throw new Exception("文件名过长！");
        }
        for (int i = 0; i < nameBytes.length; i++) {
            bytes[i] = nameBytes[i];
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getProperty() {
        return property;
    }

    public void setProperty(int property) {
        this.property = property;
        // todo FCB第5项文件属性
        bytes[5] = (byte) property;
        if (property >> 4 == 1) {
            executable = true;
        } else {
            executable = false;
        }
        if (property >> 3 == 1) {
            isDirectory = false;
        }

    }

    public int getStartBlock() {
        return startBlock;
    }

    public void setStartBlock(int startBlock) {
        this.startBlock = startBlock;
        // todo FCB第6项起始盘块
        bytes[6] = (byte) startBlock;
    }

    public int getFileLength() {
        return fileLength;
    }

    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
        // todo FCB第7项文件长度
        bytes[7] = (byte) fileLength;
    }

    public boolean isExecutable() {
        return executable;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public int getCatalogBlock() {
        return catalogBlock;
    }

    public void setCatalogBlock(int catalogBlock) {
        this.catalogBlock = catalogBlock;
    }

    public boolean isBlank() {
        return isBlank;
    }

    public void setBlank(boolean blank) {
        isBlank = blank;
    }

    public List<Catalog> list() throws IOException {
        List<Catalog> catalogs = new ArrayList<>();
        Catalog catalog = OS.fileOperator.readCatalog(catalogBlock);
        int nextBlock = catalog.getCatalogBlock();
        // todo 终止盘块号为-1
        while (nextBlock != -1) {
            Catalog c = OS.fileOperator.readCatalog(nextBlock);
            catalogs.add(c);
            nextBlock = OS.fileOperator.getNextBlock(nextBlock);
        }
        return catalogs;
    }
}
