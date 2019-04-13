import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.CopyOption;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO; 
import javax.imageio.ImageReader; 
import javax.imageio.stream.ImageInputStream;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.zip.CRC32;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.lang.StringBuilder;

public class createHashCSV {
     // default settings
    private static final String HEADER = "\"id\",\"screenname\",\"locale\",\"scriptname\",\"activityname\",\"windowwidth\",\"windowheight\",\"orientation\",\"build\",\"modtime\",\"windowHashCode\",\"windowtype\",\"screenhash\",\"tags\",\"fingerprint\"";
    private static File screensDir = new File("/Users/google/Desktop/Final");
    private static String formatString = "locale_index_script_name";

    private static int targetScreenCount = -1;
    private static List<String> targetNames = new ArrayList<String>();
    private static String targetScript = "";

    private static String getArg(String filename, String targetParam) {
        String[] format = formatString.split("_");
        String[] nameDataIn = filename.split("_", format.length);
        String arg = "";
        for (int i=0; i<format.length; i++) {
            if(format[i].contains(targetParam)){
                if(format[i].contains("=")) {
                    arg = format[i].split("=")[1];
                } else {
                    arg = nameDataIn[i];
                }
            }
        }
        return arg;
    }

    private static String[] parseName(String filename, int count) {

        String[] nameData = {"", "", "", ""};

        // example formatString = "locale_index_script_name"
        // locale will always be first
        // out order will need to be locale, scriptname, screenname, index

        nameData[0] = getArg(filename, "locale");
        nameData[1] = getArg(filename, "script");
        nameData[2] = getArg(filename, "name");
        nameData[3] = getArg(filename, "index");

        // name is to be '_' separated in csv file
        nameData[2] = nameData[2].replace('-', '_');

        return nameData;
    }

    private static String screenHash(File file) throws IOException {
        String screenHash = null;
        try {   
            byte[] fileBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            MessageDigest md = MessageDigest.getInstance("MD5");
            // Base36
            screenHash = new BigInteger(1, md.digest(fileBytes)).toString(36);
        } catch (Exception e) {
          e.printStackTrace();
        }
        return screenHash;
    }

    private static String fingerprint(String screenEntry) {
        CRC32 fingerPrint = new CRC32();
        fingerPrint.update(screenEntry.getBytes());
        int fingerPrintVal = (int)fingerPrint.getValue();

        return Integer.toString(fingerPrintVal);
    }

    private static int[] getScreenDimensions(File file) throws IOException{
        int[] dimensions = {0, 0};
        try(ImageInputStream in = ImageIO.createImageInputStream(file)){
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    dimensions[0] = reader.getWidth(0);
                    dimensions[1] = reader.getHeight(0);
                } finally {
                    reader.dispose();
                }
            }
        }
        return dimensions;
    }

    private static String makeScreenDataString(File file, int count, String[] nameData) throws IOException {
        String filename = file.getName();
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int[] dimensions = getScreenDimensions(file);;

        String id = nameData[3].replaceFirst("^0+(?!$)", "");
        String screenname = nameData[1]+"_"+nameData[2]+"_"+id;
        String locale = nameData[0];
        String scriptname = "babel."+nameData[1]+"."+nameData[1];
        String activityname = "";
        String windowwidth = Integer.toString(dimensions[0]);
        String windowheight = Integer.toString(dimensions[1]);
        String orientation = (dimensions[0] < dimensions[1]) ? "0" : "1";
        String build = "";
        String modtime = sdf.format(file.lastModified());
        String windowHashCode = "";
        String windowtype = "";
        String screenhash = screenHash(file);
        String tags = "";
        String fingerprint = "";
        // String fingerprintText = ""; // Do not include

        sb.append("\n");
        sb.append("\""+id+"\",");
        sb.append("\""+screenname+"\",");
        sb.append("\""+locale+"\",");
        sb.append("\""+scriptname+"\",");
        sb.append("\""+activityname+"\",");
        sb.append("\""+windowwidth+"\",");
        sb.append("\""+windowheight+"\",");
        sb.append("\""+orientation+"\",");
        sb.append("\""+build+"\",");
        sb.append("\""+modtime+"\",");
        sb.append("\""+windowHashCode+"\",");
        sb.append("\""+windowtype+"\",");
        sb.append("\""+screenhash+"\",");
        sb.append("\""+tags+"\",");
        sb.append("\""+fingerprint(sb.toString())+"\",");

        return sb.toString();
    }

    private static File setTargetDir (File targetDir) {
        if (!(targetDir.exists() && targetDir.isDirectory())) {
           targetDir.mkdir();
        }
        return targetDir;
    }

    private static void checkLocaleScreenCount(int count, String locale) {
        // check screen count to be consistent with previous locales
        if (targetScreenCount == -1) {
            targetScreenCount = count;
        }
        if (count != targetScreenCount) {
            System.out.println("Error: Screen count mismatch for "+locale);
            System.exit(1);
        }
    }

    private static void checkFileName(int count, String filename, boolean firstLocale) {
        // check the fileName portion after the locale segment to be consistent 

        if(firstLocale) { 
            targetNames.add(filename);
        } else {
            if (!filename.split("_", 2)[1].equals(targetNames.get(count-1).split("_", 2)[1])) {
                System.out.println("Error! Inconsistent file name.");
                System.out.println("* Expected form:  "+targetNames.get(count-1));
                System.out.println("* Found:          "+filename);
                System.exit(1);                    
            }
        }
    }

    private static void finalizeCSV(File csvDir, String script, String locale, int count) throws IOException {
        checkLocaleScreenCount(count, locale);

        // rename CSV file to include count
        String nameStringCSV = csvDir.getAbsolutePath()+"/"+script+"_"+locale;
        File completeCSV = new File(nameStringCSV+".csv");
        File newName = new File(nameStringCSV+".["+Integer.toString(count-1)+"].csv");
        completeCSV.renameTo(newName);
        System.out.println("Created CSV file: "+newName.getName());        
    }

    public static void main(String[] argv) throws IOException {
        // get parameters
        for (int i=0; i<argv.length; i++) {
            if(argv[i].equals("-d")) {
                screensDir = new File(argv[i+1]);
            } else if (argv[i].equals("-f")){
                formatString = argv[i+1];
            }
        }

        File outDir = setTargetDir(new File(screensDir+"/hashed"));
        File csvDir = setTargetDir(new File(screensDir+"/csv"));
        File dupDir = setTargetDir(new File(screensDir+"/duplicates"));    

        // boolean firstRun = true;
        boolean firstFile = true;
        boolean firstLocale = true;
        int count = 1;
        String currLocale = "";
        String[] nameData = null;
        FileWriter writer = null;
        FileWriter dupWriter = new FileWriter(dupDir.getAbsolutePath()+"/duplicates.txt", true);

        // examine each screencap, in sorted order (by locale)
        File[] fileList = screensDir.listFiles();
        Arrays.sort(fileList);
        for (File file : fileList) {
            String filename = file.getName();

            // skip non-image files
            String ext = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
            if (!ext.equals("png")) { continue; }

            checkFileName(count, filename, firstLocale);

            nameData = parseName(filename.substring(0, filename.length() - 4), count);

            // if new locale, finalize old csv, create new csv and append HEADER
            if (!nameData[0].equals(currLocale)) {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }

                if (firstFile) { 
                    firstFile = false;
                } else {
                    firstLocale = false;
                    finalizeCSV(csvDir, nameData[1], currLocale, count);
                }

                currLocale = nameData[0];
                count = 1;
                writer = new FileWriter(csvDir.getAbsolutePath()+"/"+nameData[1]+"_"+currLocale+".csv", true);
                writer.append(HEADER);
            }

            // generate file data entry
            String screenData = makeScreenDataString(file, count, nameData);

            // append to csv file
            writer.append(screenData);

            // copy to outDir and rename file to screenHash
            Path source = file.toPath();
            Path dest = Paths.get(outDir.getAbsolutePath()+"/"+screenHash(file)+".png");
                //overwrite existing file, if exists
            CopyOption[] options = new CopyOption[]{
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
            }; 
            // print message when hashed file already exists
            File oldFile = new File(outDir.getAbsolutePath()+"/"+screenHash(file)+".png");
            if (oldFile.exists()) {
                dupWriter.append(String.format("%1$-" + 70 + "s", source.getFileName())+"-> "+dest.getFileName()+"\n");

            }

            Files.copy(source, dest, options);

            count++;
        }

        writer.flush();
        writer.close();

        finalizeCSV(csvDir, nameData[1], currLocale, count);

        dupWriter.flush();
        dupWriter.close();

        System.out.println("Screen captures have been processed.");
    }
}




