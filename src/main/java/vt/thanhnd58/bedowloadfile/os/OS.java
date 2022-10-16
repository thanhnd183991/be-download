package vt.thanhnd58.bedowloadfile.os;

public class OS {
    public static OSName getOS() {
        String nameOS = System.getProperty("os.name");
        if(nameOS.toLowerCase().contains(OSName.WINDOWS.toString().toLowerCase())){
            return OSName.WINDOWS;
        }
        else if(nameOS.toLowerCase().contains(OSName.LINUX.toString().toLowerCase())){
            return OSName.LINUX;
        }
        else if(nameOS.toLowerCase().contains(OSName.MACOS.toString().toLowerCase())){
            return OSName.MACOS;
        }
        return null;

    }
}
