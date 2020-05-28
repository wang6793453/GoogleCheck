package google;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.*;
import java.util.Properties;

/**
 * Created by sss on 2020/5/28 10:32.
 * email jkjkjk.com
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Util {

    public static String getProperty(String key) {

        try {
            InputStream in = new BufferedInputStream(new FileInputStream(
                    new File("src/main/local.properties")));
            Properties prop = new Properties();
            prop.load(in);
            return prop.getProperty(key);

        } catch (FileNotFoundException e) {
            System.out.println("properties文件路径书写有误，请检查！");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    @Test
    public void test() {
        System.out.println(getProperty("package_name"));
    }
}
