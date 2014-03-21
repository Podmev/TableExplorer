import org.ini4j.Profile;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: 1
 * Date: 13.03.13
 * Time: 1:16
 * To change this template use File | Settings | File Templates.
 */
public class Config {
    String configPath = "config.ini";
    private File configFile;
    private static Config config;
    private Wini ini = null;
    private String sourceDirectoryPath;
    private String outDirectoryPath;
    private String outName;
    private boolean isRead;
    private String encoding = "windows-1251";
    private String separators = ";\\t";
    private boolean ignoreHeaders = true;


    private Config() {
        try {
            configFile = new File(configPath);
            readConfigFile(configFile);
            isRead = true;
        } catch (IOException e) {
            /*e.printStackTrace();
            MainFrame.showErrorMessage("Cannot save because of " + e.getMessage());
            System.exit(1);*/
        }

    }

    public Config(File configFile) throws IOException {
        this.configFile = configFile;
        this.configPath = configFile.getPath();
        readConfigFile(configFile);
    }

/*    public static boolean setFile(File file) {
        try {
            config = new Config(file);
        } catch (IOException e) {
            e.printStackTrace();
            MainFrame.showErrorMessage("Config file error");
            return false;
        }
        return true;
    }*/

    public static Config getInstance() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }

    public String getEncoding() {
        return encoding;
    }

    private void readConfigFile(File configFile) throws IOException {
        ini = new Wini(configFile);
        setSystemConfigs();
    }


    private void setSystemConfigs() {
        Profile.Section section = ini.get("system");
        outDirectoryPath = section.get("outDirectoryPath");
        sourceDirectoryPath = section.get("sourceDirectoryPath");
        outName = section.get("outName");
        encoding = section.get("encoding");
        separators = section.get("separators");
        ignoreHeaders = section.get("ignoreHeaders").equals("true");
    }


    public String getSourceDirectoryPath() {
        return sourceDirectoryPath;
    }

    public String getOutDirectoryPath() {
        return outDirectoryPath;
    }

    public String getOutName() {
        return outName;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getSeparators() {
        return separators;
    }

    public boolean isIgnoreHeaders() {
        return ignoreHeaders;
    }
}
