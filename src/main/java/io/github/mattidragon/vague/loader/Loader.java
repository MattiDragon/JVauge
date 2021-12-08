package io.github.mattidragon.vague.loader;

import io.github.mattidragon.vague.Main;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class Loader {
    
    public static void main(String[] args) throws IOException {
        try {
            var fs = FileSystems.newFileSystem(Path.of(Loader.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
    
            AtomicBoolean libsUpdates = new AtomicBoolean(false);
            
            Files.walk(fs.getPath("libs"), 1)
                    .parallel()
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        var name = path.getFileName();
                
                        var externalLib = Path.of("libs/" + name);
                        try {
                            if (Files.exists(externalLib)) return;
                    
                            Files.createDirectories(externalLib.getParent());
                            System.out.println("Preparing library " + name + " for first run...");
                            libsUpdates.set(true);
                            Files.copy(path, externalLib);
                        } catch (IOException e) {
                            throw new UncheckedIOException("Error while preparing library " + name, e);
                        }
                    });
            
            if (libsUpdates.get()) {
                System.out.println("Libraries have been prepared. Please run again to use.");
                return;
            }
        } catch (IOException | URISyntaxException | UncheckedIOException e) {
            System.err.println("Error preparing libraries:");
            e.printStackTrace(System.err);
            return;
        }
        Main.main(args);
    }
}
