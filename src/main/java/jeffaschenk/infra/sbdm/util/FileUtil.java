package jeffaschenk.infra.sbdm.util;

import jeffaschenk.infra.sbdm.common.Constants;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.stream.Stream;

/**
 * FileUtil
 *
 * @author jaschenk
 */
public class FileUtil {

    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(FileUtil.class);


    /**
     * copyFolder Utility
     *
     * @param src -- Directory
     * @param dest -- Directory
     */
    public static void copyFolder(File src, File dest) {

        if (src.isDirectory()) {
            //if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
                LOGGER.info("{} Directory created from {} for {}", Constants.LOG_HEADER_SHORT, src, dest);
            }
            //list all the directory contents
            String files[] = src.list();
            // Iterate over each File/Directory adn recursively copy...
            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyFolder(srcFile, destFile);
            }
        } else {
            //if file, then copy it
            copyFile(src, dest);
        }
    }

    /**
     * copyFile Utility
     *
     * @param src -- File
     * @param dest -- File
     * @return boolean indicating operation was successful or not...
     */
    public static boolean copyFile(File src, File dest) {
        //Use bytes stream to support all file types
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dest, false);
            byte[] buffer = new byte[1024];
            int length;
            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            LOGGER.info("{} File copied from {} to {} ", Constants.LOG_HEADER_SHORT, src, dest);
            return true;
        } catch (IOException ioe) {
            LOGGER.info("{} Exception copying File from {} to {} -- {}", Constants.LOG_HEADER_SHORT, src, dest,
                    ioe.getMessage(), ioe);
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    LOGGER.error("{} Exception Closing Source File: {} -- {}",
                            Constants.LOG_HEADER_SHORT, src, ioe.getMessage(), ioe);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    LOGGER.error("{} Exception Closing Destination File: {} -- {}",
                            Constants.LOG_HEADER_SHORT, dest, ioe.getMessage(), ioe);
                }
            }
        }
    }

    public static void deleteFolder(String fPath) {
        // Pass one delete the files...
        deleteFolder(fPath,false);
        // Pass one delete the folders...
        deleteFolder(fPath, true);
    }

    private static void deleteFolder(String fPath, boolean directoryFlag) {
        try (final Stream<Path> pathStream = Files.walk(Paths.get(fPath), FileVisitOption.FOLLOW_LINKS)) {
            pathStream
                    .filter((p) -> p.toFile().isDirectory()==directoryFlag)
                    .forEach(p ->  deleteFile(p.toFile()));
        } catch (final IOException ioe) {
            LOGGER.error("{} Exception Deleting Folder:{} -- {}",
                    Constants.LOG_HEADER_SHORT, fPath, ioe.getMessage(), ioe);
        }
    }

    public static void deleteFile(File file) {
        if(file.isDirectory()){
            //directory is empty, then delete it
            if(file.list().length==0){
                boolean result = file.delete();
                LOGGER.info("{} Directory is deleted: {} ? {}", Constants.LOG_HEADER_SHORT, file.getAbsolutePath(), result);
            } else {
                //list all the directory contents
                String files[] = file.list();
                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);
                    //recursive delete
                    deleteFile(fileDelete);
                }
                //check the directory again, if empty then delete it
                if(file.list().length==0){
                    boolean result = file.delete();
                    LOGGER.info("{} Directory is deleted: {} ? {}", Constants.LOG_HEADER_SHORT, file.getAbsolutePath(), result);
                }
            }
        } else {
            //if file, then delete it
            boolean result = file.delete();
            LOGGER.info("{} File is deleted: {} ? {}", Constants.LOG_HEADER_SHORT, file.getAbsolutePath(), result);
        }
    }

    /**
     * loadFileAsResource
     *
     * @param file - Reference to Obtain Resource.
     * @return Resource contrived from File Path.
     */
    public static Resource loadFileAsResource(File file) {
        try {
            Resource resource = new UrlResource(file.toPath().toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + file.getAbsolutePath());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + file.getAbsolutePath(), e);
        }
    }

}
