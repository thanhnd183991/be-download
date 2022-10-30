package vt.thanhnd58.bedowloadfile.utils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;
import vt.thanhnd58.bedowloadfile.constant.BEConstant;
import vt.thanhnd58.bedowloadfile.os.OS;
import vt.thanhnd58.bedowloadfile.os.OSName;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class FileUtils {
    private Path foundFile;

    public static String saveFile(OSName osname, String version, String fileName, MultipartFile multipartFile)
            throws IOException {
        Path uploadPath = Paths.get("version/" + osname + "/" + version);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new IOException("Could not save file: " + fileName, ioe);
        }

        return uploadPath.toAbsolutePath().toString();
    }


    public Resource getFileAsResource(OSName os, String version, String fileName) throws IOException {
        Path dirPath = Paths.get("version/" + os.toString() + "/" + version);

        Files.list(dirPath).forEach(file -> {
            if (file.getFileName().toString().contains(fileName.toLowerCase())) {
                foundFile = file;
                return;
            }
        });

        if (foundFile != null) {
            return new UrlResource(foundFile.toUri());
        }

        return null;
    }

    public static String getCurrentFolderContainAppUsingUserDir() {
        return System.getProperty("user.dir");
    }

    public static File getCurrentFolderContainAppUsingUserDot() {
        return new File(".");

    }

    public static String appendSlash() {
        OSName os = OS.getOS();
        if(os.equals(OSName.LINUX)){
           return "/" ;
        }
        else {
           return "\\" ;
        }
    }
    public static String getFilePathInFolderContainApp(String fileName) {
        String folder = getCurrentFolderContainAppUsingUserDir();
//        String folder = getCurrentFolderContainAppUsingUserDot().getAbsolutePath();
        String filePath = folder + appendSlash() + fileName;
        File file = new File(filePath);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("filePath "+ filePath);

        return filePath;
    }


    public static Properties getPropFileFromFolderContainApp(String fileName) {
        String filePath = getFilePathInFolderContainApp(fileName);
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            // load properties from file
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close objects
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    public static void modifyVersionPropFile(Properties newProps) {
        Properties oldProps = getPropFileFromFolderContainApp(BEConstant.VERSION_PROPS);
        newProps.forEach((key, value) -> oldProps.put(key, value));
        savePropFileFromFolderContainApp(oldProps, BEConstant.VERSION_PROPS);
    }

    public static boolean savePropFileFromFolderContainApp(Properties props, String fileName) {
        String filePath = getFilePathInFolderContainApp(fileName);
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            props.store(fos, null);
            fos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
