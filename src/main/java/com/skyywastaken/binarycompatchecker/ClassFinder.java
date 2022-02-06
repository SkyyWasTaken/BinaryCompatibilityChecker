package com.skyywastaken.binarycompatchecker;

import com.skyywastaken.binarycompatchecker.util.ASMUtils;
import com.skyywastaken.binarycompatchecker.util.JarUtils;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClassFinder {
    private final File OLD_FILE;
    private final File NEW_FILE;
    private final HashMap<String, ClassNode> OLD_CLASSES;
    private final HashMap<String, ClassNode> NEW_CLASSES;

    public ClassFinder(String oldLocation, String newLocation) {
        OLD_FILE = getFileFromPassedPath(oldLocation);
        NEW_FILE = getFileFromPassedPath(newLocation);
        if(OLD_FILE == null || NEW_FILE == null) {
            System.err.println("Couldn't find a file at " + ((OLD_FILE == null)?oldLocation:newLocation));
            System.exit(-1);
        } else if(!OLD_FILE.canRead() || !NEW_FILE.canRead()) {
            System.err.println("Couldn't read the file at " + (OLD_FILE.canRead()?newLocation:oldLocation));
            System.exit(-1);
        }
        OLD_CLASSES = findClasses(OLD_FILE);
        NEW_CLASSES = findClasses(NEW_FILE);
    }

    private HashMap<String, ClassNode> findClasses(File passedFile) {
        if(JarUtils.isZipFile(passedFile)) {
            return findClassesInJar(passedFile);
        } else if(passedFile.isDirectory()) {
            return findClassesInFolder(passedFile);
        } else {
            return handlePossibleClass(passedFile);
        }
    }

    private HashMap<String, ClassNode> findClassesInJar(File passedJar) {
        System.out.println("Finding classes in compressed file " + passedJar.getAbsolutePath());
        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(passedJar));
        } catch(FileNotFoundException e) {
            System.err.println("Couldn't find the jar at " + passedJar.getAbsolutePath() + "! Was it modified?");
            System.exit(-1);
        }
        HashMap<String, ClassNode> returnMap = new HashMap<>();
        ZipEntry currentEntry = getNextEntry(zipInputStream);
        while(currentEntry != null) {
            if(currentEntry.isDirectory()) {
                currentEntry = getNextEntry(zipInputStream);
                continue;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1000];
            try {
                int temp;

                while ((temp = zipInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0,
                            temp);
                }
            } catch(IOException e) {
                System.err.println("Failed to read an entry in the compressed file!");
                System.exit(-1);
            }
            byte[] possibleClass = outputStream.toByteArray();
            if(!JarUtils.isClassFile(possibleClass)) {
                currentEntry = getNextEntry(zipInputStream);
                continue;
            }
            ClassNode newClassNode = ASMUtils.getClassNode(possibleClass);
            returnMap.put(newClassNode.name, newClassNode);
        }
        try {
            zipInputStream.closeEntry();
            zipInputStream.close();
        } catch(IOException ignored) {

        }
        System.out.println("Found " + returnMap.size() + " classes!");
        return returnMap;
    }

    private ZipEntry getNextEntry(ZipInputStream inputStream) {
        try {
            return inputStream.getNextEntry();
        } catch(IOException e) {
            return null;
        }
    }

    private HashMap<String, ClassNode> findClassesInFolder(File passedFolder) {
        return new HashMap<>();
    }

    private HashMap<String, ClassNode> handlePossibleClass(File passedFile) {
        return new HashMap<>();
    }

    private @Nullable File getFileFromPassedPath(String passedPath) {
        File newFile = new File(passedPath);
        if(!newFile.exists()) {
            String currentDirectory = System.getProperty("user.dir");
                    newFile = new File(currentDirectory, passedPath);
            if(newFile.exists()) {
                return newFile;
            }
            return null;
        }
        return newFile;
    }
}
