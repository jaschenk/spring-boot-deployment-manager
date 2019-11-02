package jeffaschenk.infra.sbdm.util;

import org.apache.tika.Tika;

/**
 * ModaFaxMediaType
 *
 * @author jaschenk
 */
public final class ArtifactMediaType {

    /**
     * getMediaTypeFromOriginalFilename
     *
     * @param originalFilename Original Artifact Document Filename
     * @return String containing valid MediaType or the default of application/octet-stream will be applied.
     */
    public static String getMediaTypeFromOriginalFilename(String originalFilename) {
        Tika tika = new Tika();
        return tika.detect(originalFilename);
    }
}
