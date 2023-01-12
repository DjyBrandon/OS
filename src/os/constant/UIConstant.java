package os.constant;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @package: os.constant
 * @Description: 操作系统UI界面资源常量
 * @author: Brandon
 * @date: 2023/1/12 9:33
 **/
public class UIConstant {

    public static Node getDirectoryIcon() {
        return new ImageView(new Image(UIConstant.class.getResourceAsStream("/os/resources/directory.png")));
    }

    public static Node getFileIcon() {
        return new ImageView(new Image(UIConstant.class.getResourceAsStream("/os/resources/file.png")));
    }

    public static Node getProgramIcon() {
        return new ImageView(new Image(UIConstant.class.getResourceAsStream("/os/resources/program.png")));
    }
}
