package vt.thanhnd58.bedowloadfile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
        private String fileName;
        private String downloadUri;
        private long size;
}
