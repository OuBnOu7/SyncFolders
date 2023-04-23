import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Clone {
    public static void cloneFolder(File sourceFolder, File destinationFolder) throws IOException {
        if (sourceFolder.isDirectory()) {
            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }

            String[] files = sourceFolder.list();

            for (String file : files) {
                File srcFile = new File(sourceFolder, file);
                File destFile = new File(destinationFolder, file);

                cloneFolder(srcFile, destFile);
            }
        } else {
            Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
