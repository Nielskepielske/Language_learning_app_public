package com.final_app.tools;

import com.final_app.globals.GlobalVariables;
import javafx.scene.Scene;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StyleLoader {
    public static void loadStyles(Scene scene){
        try{

            List<String> styleSheets = getResourceFileNamesRecursively(GlobalVariables.STYLES);
            //System.out.println("Styles recursive: " + getResourceFileNamesRecursively(GlobalVariables.STYLES));

            for(String path : styleSheets){
                String path2 = path.replaceAll("\\\\", "/");
                loadStyleSheet(path2, scene);
            }

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> getResourceFileNamesRecursively(String resourcePath) {
        List<String> fileNames = new ArrayList<>();

        try {
            URL resourceUrl = StyleLoader.class.getResource(resourcePath);
            if (resourceUrl == null) {
                return fileNames;
            }

            URI uri = resourceUrl.toURI();
            Path path;

            // Handle both JAR and file system paths
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                path = fileSystem.getPath(resourcePath);

                // After we're done, close the filesystem
                try {
                    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            fileNames.add(file.toString().substring(resourcePath.length()).replaceAll("\\\\", "/"));
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    fileSystem.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                path = Paths.get(uri);
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        // Get relative path from the starting directory
                        Path relativePath = path.relativize(file);
                        fileNames.add(relativePath.toString());
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        return fileNames;
    }

    private static void loadStyleSheet(String path, Scene scene){
        scene.getStylesheets().add(StyleLoader.class.getResource(GlobalVariables.STYLES + path).toExternalForm());
    }
}
