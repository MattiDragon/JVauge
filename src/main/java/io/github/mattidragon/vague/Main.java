package io.github.mattidragon.vague;

import org.fusesource.jansi.internal.JansiLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    static {
        JansiLoader.initialize();
    }
    
    public static void main(String[] args) throws IOException {
        new Program(Files.readString(Path.of(args[0]))).run();
    }
}
