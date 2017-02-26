package setup;

import java.net.URLDecoder;

public class PathUtil {

    public static String getTargetPath(Class clazz) throws Exception {
        // use path of jar or class to find target directory
        String jarPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedJarPath = URLDecoder.decode(jarPath, "UTF-8");
        return decodedJarPath.substring(0, decodedJarPath.lastIndexOf("target/") + 7);
    }
}
