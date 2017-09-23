
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FilePacker {

    /**
     * Retrieves a file from a zip file.
     *
     * @param zipPath The path to the zip file.
     * @param fileContents The complete or partial name of the file to be retrieved. The first file that matches will be retrieved.
     * @return The path to the retrieved file.
     */
    public static String retrieveFromZip(String zipPath, String fileContents) {
        String fileName = "";
        String tempDir = "";
        if(System.getProperty("os.name").equals("Linux")) {
            tempDir = "/tmp/";
        }else{
            tempDir =  System.getProperty("java.io.tmpdir");
        }

        try {
            ZipFile zip = new ZipFile(zipPath);
            Enumeration entries = zip.entries();
            int entryNum = 0;
            while(entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if(entry.getName().contains(fileContents)) {
                    InputStream in = zip.getInputStream(entry);
                    fileName = tempDir + entry.getName();
                    fileName = entry.getName().substring(entry.getName().lastIndexOf("\\")+1); // needed to nav file path to location for contents
                    if(new File(fileName).exists()) {
                        new File(fileName).delete();
                    }
                    Files.copy(in, Paths.get(fileName));
                }
                entryNum++;
            }
            System.out.println("Entries: " + entryNum);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * Compresses a group of files into a zip file.
     *
     * @param zipName The name of the zip file.
     * @param fileNames The path to each file being compressed, in an arraylist.
     */
    public static void createZip(String zipName, ArrayList<String> fileNames) {
        try {
            FileOutputStream fOut = new FileOutputStream(zipName);
            ZipOutputStream zOut = new ZipOutputStream(fOut);

            for(String fileName : fileNames) {
                addToZip(fileName, zOut);
            }
            zOut.flush();
            fOut.flush();
            zOut.close();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a file to a zip file.
     *
     * @param fileName The path to the file being added
     * @param zos The ZipOutputStream being used
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void addToZip(String fileName, ZipOutputStream zos) throws FileNotFoundException, IOException {

        System.out.println("Writing '" + fileName + "' to zip file");

        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }


}