import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import jcifs.smb.*;

public class Sync {
    private static volatile boolean syncCanceled = false; // Variable volatile pour vérifier si la synchronisation a été annulée

    public static void syncFolders(File sourceFolder, File destinationFolder) throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Paths.get(sourceFolder.getAbsolutePath()).register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

        while (!syncCanceled) { // Modifier la condition de boucle pour vérifier si la synchronisation a été annulée
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException ex) {
                return; // Sortir de la méthode quand interrompu
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path fileName = ev.context();

                File sourceFile = new File(sourceFolder, fileName.toString());
                File destFile = new File(destinationFolder, fileName.toString());

                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("File " + fileName + " has been created.");
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("File " + fileName + " has been modified.");
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    Files.deleteIfExists(destFile.toPath());
                    System.out.println("File " + fileName + " has been deleted.");
                }
            }

            key.reset();
        }
    }

    public static void syncFoldersSMB(String sourceFolderURL, String destinationFolderURL,
                                      String username, String password) throws IOException {
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, username, password);
        SmbFile sourceFolder = new SmbFile(sourceFolderURL, auth);
        SmbFile destinationFolder = new SmbFile(destinationFolderURL, auth);
        SmbFile[] sourceFiles = sourceFolder.listFiles();

        for (SmbFile sourceFile : sourceFiles) {
            String fileName = sourceFile.getName();
            SmbFile destFile = new SmbFile(destinationFolderURL + "/" + fileName, auth);

            if (sourceFile.isDirectory()) {
                if (!destFile.exists()) {
                    destFile.mkdir();
                    System.out.println("Directory " + fileName + " has been created.");
                }

                syncFoldersSMB(sourceFile.getCanonicalPath(), destFile.getCanonicalPath(), username, password);
            } else {
                if (!destFile.exists() || sourceFile.getLastModified() > destFile.getLastModified()) {
                    SmbFileInputStream in = new SmbFileInputStream(sourceFile);
                    SmbFileOutputStream out = new SmbFileOutputStream(destFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) > 0) {
                        out.write(buffer, 0, bytesRead);
                    }
                    in.close();
                    out.close();
                    System.out.println("File " + fileName + " has been copied.");
                }
            }
        }
    }

    // Méthode pour annuler la synchronisation
    public static void cancelSync() {
        syncCanceled = true;
    }

    // Méthode pour réinitialiser l'état de la synchronisation annulée
    public static void resetSync() {
        syncCanceled = false;
    }
}
