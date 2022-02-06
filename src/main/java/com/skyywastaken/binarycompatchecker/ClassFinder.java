package com.skyywastaken.binarycompatchecker;

import com.skyywastaken.binarycompatchecker.util.FileUtils;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipInputStream;

public class ClassFinder {
    private final HashMap<String, ClassNode> OLD_CLASSES;
    private final HashMap<String, ClassNode> NEW_CLASSES;

    public ClassFinder(String oldLocation, String newLocation) {
        File oldFile = getFileFromPassedPath(oldLocation);
        File newFile = getFileFromPassedPath(newLocation);
        if (oldFile == null || newFile == null) {
            System.err.println("Couldn't find a file at " + ((oldFile == null) ? oldLocation : newLocation));
            System.exit(-1);
        } else if (!oldFile.canRead() || !newFile.canRead()) {
            System.err.println("Couldn't read the file at " + (oldFile.canRead() ? newLocation : oldLocation));
            System.exit(-1);
        }
        OLD_CLASSES = findClasses(oldFile);
        NEW_CLASSES = findClasses(newFile);
    }

    private HashMap<String, ClassNode> findClasses(File passedFile) {
        if (FileUtils.isZipFile(passedFile)) {
            return findClassesInJar(passedFile);
        } else if (passedFile.isDirectory()) {
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
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't find the jar at " + passedJar.getAbsolutePath() + "! Was it modified?");
            System.exit(-1);
        }
        List<ClassNode> nodes = FileUtils.getAllClassFilesInZip(zipInputStream);
        HashMap<String, ClassNode> returnMap = new HashMap<>();
        for (ClassNode currentnode : nodes) {
            returnMap.put(currentnode.name, currentnode);
        }
        System.out.println("Found " + returnMap.size() + " classes!");
        return returnMap;
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
