package vt.thanhnd58.bedowloadfile.controller;


import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vt.thanhnd58.bedowloadfile.constant.BEConstant;
import vt.thanhnd58.bedowloadfile.dto.FileUploadResponse;
import vt.thanhnd58.bedowloadfile.dto.VersionDTO;
import vt.thanhnd58.bedowloadfile.os.OSName;
import vt.thanhnd58.bedowloadfile.utils.FileUtils;

import java.io.IOException;
import java.util.Properties;

@RestController()
@RequestMapping("/api/file")
public class FileController {

    private boolean checkVersion(String version) {
        String[] versionArr = version.split(".");
        for (int i = 0; i < versionArr.length; i++) {
            try {
                Integer.valueOf(versionArr[i]);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private boolean checkUploadFileParams(OSName os, String version, MultipartFile file) {
        if (os == null || !checkVersion(version) || file == null) {
            return false;
        }
        return true;
    }

    private boolean validFileType(String fileType) {
        if (fileType.toLowerCase().equals("exe") || fileType.toLowerCase().equals("hddt") || fileType.toLowerCase().equals("updater")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkDownloadFileRequest(String version, OSName os, String typeFile) {
        if (os == null || !checkVersion(version) || !validFileType(typeFile)) {
            return false;
        }
        return true;
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("os") OSName os,
            @RequestParam("version") String version,
            @RequestParam("required") boolean required,
            @RequestParam("file") MultipartFile multipartFile)
            throws IOException {

        if (!checkUploadFileParams(os, version, multipartFile)) {
            return ResponseEntity.badRequest().body(null);
        }


        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        long size = multipartFile.getSize();

        String filecode = FileUtils.saveFile(os, version, fileName, multipartFile);

        if (!fileName.toLowerCase().contains("exe")) {
            Properties props = new Properties();

            props.put("required" + os.toString(), String.valueOf(required));
            props.put("version" + os.toString(), version);
            props.put("size" + os.toString(), String.valueOf(size));
            if (fileName.toLowerCase().contains("updater")) {
                props.put("updater" + os.toString(), String.valueOf(true));
            }
            FileUtils.modifyVersionPropFile(props);
        }

        VersionDTO response = new VersionDTO();

        response.setRequired(required);
        response.setNewVersion(version);
        response.setOsName(os);
        if (fileName.toLowerCase().contains("updater")) {
            response.setUpdater(true);
        }

        return new ResponseEntity(response, HttpStatus.OK);
    }

    @GetMapping("/download-last-version")
    public ResponseEntity<?> downloadFile(@RequestParam("os") OSName os, @RequestParam("typeFile") String typeFile) {
        if (!validFileType(typeFile)) {
            return ResponseEntity.badRequest().body("typeFile nhan cac gia tri: HDDT, EXE, UPDATER");
        }
        Properties versionProps = FileUtils.getPropFileFromFolderContainApp(BEConstant.VERSION_PROPS);
        String version = (String) versionProps.get("version" + os.toString());
        FileUtils downloadUtil = new FileUtils();
        Resource resource = null;
        try {
            resource = downloadUtil.getFileAsResource(os, version, typeFile);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        if (resource == null) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }

        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }

    @GetMapping("/download-version")
    public ResponseEntity<?> downloadFile(@RequestParam("version") String version, @RequestParam("os") OSName os, @RequestParam("typeFile") String typeFile) {
        if (!checkDownloadFileRequest(version, os, typeFile)) {
            return ResponseEntity.badRequest().body(null);
        }
        FileUtils downloadUtil = new FileUtils();
        Resource resource = null;
        try {
            resource = downloadUtil.getFileAsResource(os, version, typeFile);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        if (resource == null) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }

        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }

    @GetMapping("/check-version")
    public ResponseEntity<?> checkVersion(@RequestParam("os") OSName os) {
        Properties versionProps = FileUtils.getPropFileFromFolderContainApp(BEConstant.VERSION_PROPS);
        VersionDTO versionDTO = new VersionDTO();
        if (versionProps.get("updater" + os.toString()) == null) {
            versionDTO.setUpdater(false);
        } else {
            versionDTO.setUpdater(Boolean.valueOf((String) versionProps.getProperty("updater" + os.toString())));
        }
        versionDTO.setNewVersion((String) versionProps.get("version" + os.toString()));
        versionDTO.setOsName(os);
        versionDTO.setSize(Long.valueOf((String) versionProps.get("size" + os.toString())));
        versionDTO.setRequired(Boolean.valueOf((String) versionProps.get("required" + os.toString())));
        return ResponseEntity.ok(versionDTO);

    }

}
