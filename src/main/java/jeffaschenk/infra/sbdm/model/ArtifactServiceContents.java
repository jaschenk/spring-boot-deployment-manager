package jeffaschenk.infra.sbdm.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;

/**
 * DeployOrUploadServiceContents
 *
 * @author jaschenk
 */
@Data
public class ArtifactServiceContents implements Serializable {
    private byte[] fileData;
    private String originalFilename;
    private String contentType;
    private String name;
    private Long size;

    /**
     * DeployOrUploadServiceContents
     * @param file - Multipart File Reference
     * @param contentTypeOverride - Content Type Override
     * @throws IOException - thrown if issues
     */
    public ArtifactServiceContents(MultipartFile file, String contentTypeOverride) throws IOException {
        if (file != null) {
            this.fileData = file.getBytes();
            this.originalFilename = file.getOriginalFilename();
            this.contentType = contentTypeOverride;
            this.name = file.getName();
            this.size = file.getSize();
        } else {
            this.fileData = new byte[0];
            this.originalFilename = "";
            this.contentType = "";
            this.name = "";
            this.size = 0L;
        }

    }

    /**
     * * DeployOrUploadServiceContents
     *
     * @param fileData - Reference to Byte Array of Data File.
     * @param originalFilename - Reference
     * @param contentType - Content Type Override
     */
    public ArtifactServiceContents(byte[] fileData, String originalFilename, String contentType) {
        this.fileData = fileData;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.name = originalFilename;
        if (this.fileData != null) {
            this.size = new Long((long)this.fileData.length);
        } else {
            this.size = 0L;
        }

    }


}
