package os.manager.file;

import os.OS;
import os.constant.OSConstant;
import os.controller.MainController;
import os.manager.process.ProcessCreator;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @package: os.manager.file
 * @Description:
 * @author: Brandon
 * @date: 2023/1/12 10:06
 **/
public class FileOperator {

    // 已打开文件项
    List<OpenedFile> openedFiles;

    // 磁盘文件
    RandomAccessFile disk;

    // 进程操作
    ProcessCreator processCreator;

    // 界面控制
    private MainController mainController;

    public FileOperator() throws Exception {
        this.processCreator = OS.processCreator;
        this.disk = OS.disk;
        this.openedFiles = new ArrayList<>();
    }

    public void init() throws Exception {

    }

    public byte[] getFat() throws IOException {
        disk.seek(0);
        byte[] buffer = new byte[OSConstant.DISK_BLOCK_QUANTITY];
        disk.read(buffer, 0, buffer.length);
        return buffer;
    }

    /**
     * 建立目录
     */
    public void mkdir(String dirPath) throws Exception {
        create(dirPath, 8);
    }

    /**
     * 显示目录的内容
     */
    public List<Catalog> dir(String dirPath) throws Exception {
        int catalogBlock = getCatalogBlock(dirPath, 2);
        List<Catalog> catalogs = new ArrayList<>();
        Catalog catalog = readCatalog(catalogBlock);
        int nextBlock = catalog.getStartBlock();
        while (nextBlock != -1) {
            Catalog c = readCatalog(nextBlock);
            catalogs.add(c);
            nextBlock = getNextBlock(nextBlock);
        }
        return catalogs;
    }

    /**
     * 查找第一个可用磁盘块
     */
    private int firstFreeBlock() throws IOException {
        int nextBlock;
        for (int i = 3; i < OSConstant.DISK_BLOCK_QUANTITY; i++) {
            // 0表示可用
            nextBlock = getNextBlock(i);
            if (nextBlock == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取分配表指向下一个磁盘块号
     */
    public int getNextBlock(int i) throws IOException {
        disk.seek(i);
        return disk.readByte();
    }

    /**
     * 修改文件分配表指向的下一个磁盘块
     */
    private void setNextBlock(int i, int nextBlock) throws IOException {
        disk.seek(i);
        disk.writeByte(nextBlock);
        // todo MainController类更新分区表视图
        mainController.updateFatView();
    }

    /**
     * 将文件路径与文件名分割开来
     */
    private SplitFilePath splitPathAndFileName(String filePath) {
        int fileNameStartIndex = filePath.lastIndexOf('/');
        // 单独的文件名
        String fileName = filePath.substring(fileNameStartIndex + 1);
        // 提取路径
        String path = filePath.substring(0, fileNameStartIndex);
        SplitFilePath splitFilePath = new SplitFilePath();
        splitFilePath.setFileName(fileName);
        splitFilePath.setPath(path);
        return splitFilePath;
    }

    /**
     * 获取文件对应的打开文件项
     */
    private OpenedFile getOpenedFile(String filePath) throws Exception {
        OpenedFile file = null;
        for (OpenedFile openedFile: openedFiles) {
            if (openedFile.getFilePath().equals(filePath)) {
                file = openedFile;
                break;
            }
        }
        if (file == null) {
            throw new Exception("文件未打开！");
        }
        return file;
    }

    /**
     * 读取目录项
     */
    public Catalog readCatalog(int blockPos) {
        Catalog catalog = null;
        try {

            disk.seek(blockPos * OSConstant.DISK_BLOCK_SIZE);
            byte[] buffer = new byte[8];
            disk.read(buffer, 0, buffer.length);
            catalog = new Catalog(buffer);
            catalog.setCatalogBlock(blockPos);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return catalog;
    }

    /**
     * 查找目录所在磁盘块号
     */
    private int getCatalogBlock(String filePath, int startBlockPos) throws Exception {
        int index = filePath.indexOf('/');
        String rootName, sonPath;
        // 该路径最上层的目录名
        if (index != -1) {
            rootName = filePath.substring(0, index);
            // 子路径
            sonPath = filePath.substring(index + 1);
        } else {
            rootName = filePath;
            sonPath = "";
        }
        byte nextBlock = (byte) startBlockPos;
        do {
            Catalog catalog = readCatalog(nextBlock);
            if (catalog.getName().equals(rootName)) {
                if (sonPath.equals("")) {
                    return nextBlock;
                }
                if (!catalog.isDirectory()) {
                    throw new Exception(catalog.getName() + " 不是文件夹");
                }
                return getCatalogBlock(sonPath, catalog.getStartBlock());
            }
            disk.seek(nextBlock);
            nextBlock = disk.readByte();
        } while (nextBlock != 1);

        throw new Exception("未找到文件夹" + rootName);

    }

    /**
     * 找到目录内容所在的最后一个磁盘块
     */
    private int getLastBlock(int i) throws IOException {
        int nextBlock = getNextBlock(i);
        if (nextBlock != -1)
            return getLastBlock(nextBlock);
        return i;
    }

    /**
     * 从开始磁盘块查找，判断是否存在同名目录
     */
    private boolean existsFile(String fileName, int startBlock) throws IOException {
        int nextBlock = startBlock;
        while (nextBlock != -1) {
            Catalog catalog = readCatalog(nextBlock);
            if (catalog.getName().equals(fileName)) {
                return true;
            }
            nextBlock = getNextBlock(nextBlock);
        }
        return false;
    }

    /**
     * 写入目录
     */
    private void writeCatalog(Catalog catalog) throws IOException {
        disk.seek(catalog.getCatalogBlock() * OSConstant.DISK_BLOCK_SIZE);
        disk.write(catalog.getBytes(), 0, catalog.getBytes().length);
        //更新目录树
        mainController.updateTreeItem(catalog);
    }

    /**
     * 删除目录
     */
    public void rmdir(String dirPath) throws Exception {
        SplitFilePath splitFilePath=splitPathAndFileName(dirPath);
        int parentBlock=getCatalogBlock(splitFilePath.getPath(),2);
        Catalog parent=readCatalog(parentBlock);
        int catalogBlock = getCatalogBlock(dirPath, 2);
        Catalog catalog = readCatalog(catalogBlock);
        rmdir(parent,catalog);
        mainController.removeTreeItem(catalog);
    }

    /**
     * 删除目录内部实现
     * @param parent 要删除目录的父目录
     * @param catalog 要删除的目录
     */
    private void rmdir(Catalog parent, Catalog catalog) throws Exception {
        // 如果是文件或空文件夹，则直接删除
        if (!catalog.isDirectory()||catalog.isBlank()){
            delete(parent, catalog);
            return;
        }
        // 先删除所有子目录
        for (Catalog c:catalog.list()){
            rmdir(catalog, c);
        }
        catalog.setBlank(true);
        // 再删除本目录
        rmdir(parent, catalog);
    }

    /**
     * 建立文件
     */
    public void create(String filePath, int property) throws Exception {
        // 找到磁盘中第一个空闲块
        int newFilePos = firstFreeBlock();
        // todo 空闲盘块-1
        if (newFilePos == -1) {
            throw new Exception("硬盘空间不足！");
        }
        // 分割文件名
        SplitFilePath splitFilePath = splitPathAndFileName(filePath);
        // 找到该文件父目录所在磁盘块
        int parentCatalogBlockPos = getCatalogBlock(splitFilePath.getPath(), 2);
        Catalog parentDir = readCatalog(parentCatalogBlockPos);
        // 查找该文件夹下是否有同名目录
        if (existsFile(splitFilePath.getFileName(), parentDir.getStartBlock())) {
            throw new Exception("已经存在同名目录，请先删除");
        }
        // 将该文件夹的起始磁盘块设置为新文件的磁盘块
        if (parentDir.getStartBlock() == -1) {
            parentDir.setStartBlock(newFilePos);
            writeCatalog(parentDir);
        }
        // 将该文件夹的最后一个文件的磁盘块设置为新文件的磁盘块位置
        else {
            int last = getLastBlock(parentDir.getStartBlock());
            disk.seek(last);
            disk.writeByte(newFilePos);
        }
        Catalog newFile = new Catalog(splitFilePath.getFileName(), property);
        newFile.setCatalogBlock(newFilePos);
        // todo 修改文件分配表 -1
        setNextBlock(newFilePos,-1);
        // 将目录项写入磁盘
        writeCatalog(newFile);
        System.out.println("建立文件成功");
        mainController.addTreeItem(parentDir,newFile);
    }

    /**
     * 读取文件
     * @param filePath 文件路径名
     * @param length   要读取的字节数,-1表示读取所有
     */
    public byte[] read(String filePath, int length) throws Exception {
        OpenedFile file = getOpenedFile(filePath);
        return read(file, length);
    }

    /**
     * 写文件
     * @param filePath 文件路径名
     * @param buffer   要写入的缓冲区数据
     * @param length   数据的长度
     */
    public void write(String filePath, byte[] buffer, int length) throws Exception {
        OpenedFile openedFile = getOpenedFile(filePath);
        write(openedFile, buffer, length);
    }

    /**
     * 以追加的方式写入
     * @param filePath
     * @param buffer
     * @param length
     */
    public void append(String filePath, byte[] buffer, int length) throws Exception {
        OpenedFile openedFile = getOpenedFile(filePath);
        append(openedFile, buffer, length);
    }

    /**
     * 显示文件
     */
    public String type(String filePath) throws Exception {
        OpenedFile openedFile=open(filePath,OpenedFile.OP_TYPE_READ);
        byte[] content = read(openedFile, -1);
        close(openedFile);
        return new String(content);
    }

    /**
     * 打开文件
     */
    public OpenedFile open(String filePath, int opType) throws Exception {
        OpenedFile openedFile;
        int catalogBlockPos = -1;
        try {
            catalogBlockPos = getCatalogBlock(filePath, 2);
            System.out.println("文件目录项所在磁盘块：" + catalogBlockPos);
        } catch (Exception e) {
            throw new Exception("没有找到文件！");
        }
        Catalog catalog = readCatalog(catalogBlockPos);
        catalog.setCatalogBlock(catalogBlockPos);
        openedFile = new OpenedFile();
        openedFile.setOpType(opType);
        openedFile.setFilePath(filePath);
        openedFile.setCatalog(catalog);
        Pointer readPointer = new Pointer();
        readPointer.setBlockNo(catalog.getStartBlock());
        readPointer.setAddress(0);
        Pointer writePointer = new Pointer();
        writePointer.setBlockNo(catalog.getStartBlock());
        writePointer.setAddress(0);
        openedFile.setReadPointer(readPointer);
        openedFile.setWritePointer(writePointer);
        openedFiles.add(openedFile);
        return openedFile;
    }

    /**
     * 读取已打开文件
     */
    public  byte[] read(OpenedFile openedFile, int length) throws Exception {
        if (openedFile.getOpType() != OpenedFile.OP_TYPE_READ && openedFile.getOpType() != OpenedFile.OP_TYPE_READ_WRITE&&openedFile.getOpType()!=OpenedFile.OP_TYPE_RUN)
            throw new Exception("文件不处于读或运行模式,不能读取");
        int readByte = 0;
        // 1. 文件内容不够长
        // 2. 遇到结束符
        // 3. 跨越磁盘块
        byte[] buffer = new byte[1024];
        Pointer p = openedFile.getReadPointer();
        byte temp;
        while (p.getBlockNo() != -1 && readByte != length) {
            // 读完一个磁盘块
            if (p.getAddress() == OSConstant.DISK_BLOCK_SIZE) {
                p.setBlockNo(getNextBlock(p.getBlockNo()));
                p.setAddress(0);
            }
            disk.seek(p.getBlockNo() * OSConstant.DISK_BLOCK_SIZE + p.getAddress());
            temp = disk.readByte();
            // 遇到结束符停止读取
            if (temp == '#')
                break;
            buffer[readByte] = temp;
            p.setAddress(p.getAddress() + 1);
            readByte++;
        }
        byte[] content = Arrays.copyOf(buffer, readByte);
        return content;
    }

    /**
     * 写入已打开文件中
     */
    public void write(OpenedFile openedFile, byte[] buffer, int length) throws Exception {
        if (openedFile.getOpType() != OpenedFile.OP_TYPE_WRITE&&openedFile.getOpType()!=OpenedFile.OP_TYPE_READ_WRITE)
            throw new Exception("文件不处于写模式,不能写入");
        Pointer pointer = openedFile.getWritePointer();
        Catalog catalog = openedFile.getCatalog();
        pointer.setBlockNo(catalog.getStartBlock());
        pointer.setAddress(0);
        int writtenBytes = 0;
        while (writtenBytes != length) {
            if (pointer.getAddress() == OSConstant.DISK_BLOCK_SIZE) {
                pointer.setBlockNo(getNextBlock(pointer.getBlockNo()));
            }
            if (pointer.getBlockNo() == -1) {
                // 申请空间,注意修改文件分配表,修改文件长度
                int blockNo = firstFreeBlock();
                if (blockNo == -1)
                    throw new Exception("磁盘空间不足！");
                // 之前是空文件，不占用磁盘空间
                if (catalog.getStartBlock() == -1) {
                    catalog.setStartBlock(blockNo);
                }
                int last = getLastBlock(catalog.getStartBlock());
                // 修改文件分配表
                setNextBlock(last, blockNo);
                setNextBlock(blockNo, -1);
                // 修改写指针位置
                pointer.setBlockNo(blockNo);
                pointer.setAddress(0);
                // 修改文件长度
                catalog.setFileLength(catalog.getFileLength() + 1);
                writeCatalog(catalog);
            }
            disk.seek(pointer.getBlockNo() * OSConstant.DISK_BLOCK_SIZE + pointer.getAddress());
            disk.write(buffer[writtenBytes++]);
            pointer.setAddress(pointer.getAddress() + 1);
        }
        // 写入结束符
        disk.write('#');
    }

    private void append(OpenedFile openedFile, byte[] butter, int length) throws Exception {
        Pointer p = openedFile.getWritePointer();
        // 给空文件分配空间
        if (openedFile.getCatalog().getStartBlock() == -1) {
            int block = firstFreeBlock();
            setNextBlock(block, -1);
            openedFile.getCatalog().setStartBlock(block);
            openedFile.getCatalog().setFileLength(1);
            writeCatalog(openedFile.getCatalog());
            disk.seek(block * OSConstant.DISK_BLOCK_SIZE);
            disk.write('#');
        }
        p.setBlockNo(getLastBlock(openedFile.getCatalog().getStartBlock()));
        disk.seek(p.getBlockNo() * OSConstant.DISK_BLOCK_SIZE);
        byte b;
        int i = 0;
        while ((b = disk.readByte()) != '#')
            i++;
        p.setAddress(i);
        write(openedFile, butter, length);

    }

    /**
     * 关闭文件
     */
    public void close(OpenedFile openedFile) throws Exception {
        openedFiles.remove(openedFile);
    }

    /**
     * 删除文件
     */
    public void delete(String filePath) throws Exception {
        OpenedFile openedFile = null;
        try {
            openedFile = getOpenedFile(filePath);
        } catch (Exception e) {
            System.out.println("文件未打开，可以删除");
        }
        if (openedFile != null)
            throw new Exception("该文件已经被打开，不能删除");
        // 找到文件目录所在磁盘块
        int blockPos = getCatalogBlock(filePath, 2);
        System.out.println("文件目录项所在磁盘块：" + blockPos);
        Catalog catalog = readCatalog(blockPos);
        int nextBlock = catalog.getStartBlock();
        int pre;
        // 清空文件内容(分配表)
        while (nextBlock != -1) {
            pre = nextBlock;
            nextBlock = getNextBlock(pre);
            setNextBlock(pre, 0);

        }
        // 修改目录指针
        // 如果是父文件夹的第一个目录，则修改父文件夹的开始盘块，
        // 否则将上一个目录的指针指向下一个目录
        SplitFilePath splitFilePath = splitPathAndFileName(filePath);
        int parentPos = getCatalogBlock(splitFilePath.getPath(), 2);
        Catalog parentDir = readCatalog(parentPos);
        if (parentDir.getStartBlock() == blockPos) {
            parentDir.setStartBlock(getNextBlock(blockPos));
            writeCatalog(parentDir);
        } else {
            nextBlock = parentDir.getStartBlock();
            pre = nextBlock;
            while (nextBlock != blockPos) {
                pre = nextBlock;
                nextBlock = getNextBlock(pre);
            }
            setNextBlock(pre, getNextBlock(blockPos));
        }
        // 删除目录项
        setNextBlock(blockPos, 0);
        System.out.println("删除文件成功");
        mainController.removeTreeItem(catalog);
    }

    public void delete(Catalog parent,Catalog c) throws Exception {
        for (OpenedFile openedFile: openedFiles) {
            if (openedFile.getCatalog().equals(c)){
                throw new Exception("该文件已经打开，无法删除！");
            }
        }
        if (parent.getStartBlock() == c.getCatalogBlock()) {
            parent.setStartBlock(getNextBlock(c.getCatalogBlock()));
            writeCatalog(parent);
        } else {
            int  nextBlock = parent.getStartBlock();
            int  pre = nextBlock;
            while (nextBlock != c.getCatalogBlock()) {
                pre = nextBlock;
                nextBlock = getNextBlock(pre);
            }
            setNextBlock(pre, getNextBlock(c.getCatalogBlock()));
        }
        // 删除目录项
        setNextBlock(c.getCatalogBlock(), 0);
        System.out.println("删除文件"+c.getName()+"成功");
    }

    /**
     * 运行文件，todo 进程创建
     */
    public void run(String filePath) throws Exception {
        int catalogBlockPos = -1;
        try {
            catalogBlockPos = getCatalogBlock(filePath, 2);
            System.out.println("文件目录项所在磁盘块：" + catalogBlockPos);
        } catch (Exception e) {
            throw new Exception("没有找到文件！");
        }
        Catalog catalog = readCatalog(catalogBlockPos);
        // 如果不是可执行程序，则抛出异常
        if (catalog.getProperty() >> 4 == 0)
            throw new Exception("该文件不是可执行程序！");
        catalog.setCatalogBlock(catalogBlockPos);
        OpenedFile openedFile = new OpenedFile();
        openedFile.setOpType(OpenedFile.OP_TYPE_RUN);
        openedFile.setFilePath(filePath);
        openedFile.setCatalog(catalog);
        Pointer readPointer = new Pointer();
        readPointer.setBlockNo(catalog.getStartBlock());
        readPointer.setAddress(0);
        Pointer writePointer = new Pointer();
        writePointer.setBlockNo(catalog.getStartBlock());
        writePointer.setAddress(0);
        openedFile.setReadPointer(readPointer);
        openedFile.setWritePointer(writePointer);
        openedFiles.add(openedFile);
        byte[] instructions = read(openedFile, -1);
        processCreator.create(instructions);
    }

    /**
     * 改变文件属性
     */
    public void changeProperty(String filePath, int newProperty) throws Exception {
        int catalogBlock = getCatalogBlock(filePath, 2);
        Catalog catalog = readCatalog(catalogBlock);
        catalog.setProperty(newProperty);
        writeCatalog(catalog);
        System.out.println("修改文件属性成功");
        mainController.updateTreeItem(catalog);
    }

    /**
     * 复制文件
     */
    public void copy(String srcFilePath,String desFilePath) throws Exception {
        OpenedFile openedFile1=null,openedFile2=null;
        try {
            openedFile1 = open(srcFilePath, OpenedFile.OP_TYPE_READ);
            create(desFilePath, openedFile1.getCatalog().getProperty());
            openedFile2 = open(desFilePath, OpenedFile.OP_TYPE_WRITE);
            byte[] content = read(openedFile1, -1);
            write(openedFile2, content, content.length);
        }catch (Exception e){
            throw e;
        }finally {
            if (openedFile1!=null) {
                close(openedFile1);
            }
            if (openedFile2!=null) {
                close(openedFile2);
            }
        }

    }


    /**
     * 格式化硬盘
     */
    public void format() throws Exception {
        Catalog root=readCatalog(2);
        for (Catalog catalog:root.list()){
            if(!catalog.isDirectory()) {
                delete(root, catalog);
            } else {
                rmdir(root, catalog);
            }
        }
    }




    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }


}
