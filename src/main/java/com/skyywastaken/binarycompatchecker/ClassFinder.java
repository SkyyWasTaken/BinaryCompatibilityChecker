package com.skyywastaken.binarycompatchecker;

import com.skyywastaken.binarycompatchecker.util.ASMUtils;
import com.skyywastaken.binarycompatchecker.util.FileUtils;
import com.skyywastaken.binarycompatchecker.util.ZipUtils;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
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
        if (ZipUtils.isZipFile(passedFile)) {
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
        List<ClassNode> nodes = ZipUtils.getAllClassFilesInZip(zipInputStream);
        HashMap<String, ClassNode> returnMap = new HashMap<>();
        for (ClassNode currentNode : nodes) {
            returnMap.put(currentNode.name, currentNode);
        }
        System.out.println("Found " + returnMap.size() + " classes!");
        return returnMap;
    }

    private HashMap<String, ClassNode> findClassesInFolder(File passedFolder) {
        List<ClassNode> directoryClasses = FileUtils.getClassesInDirectory(passedFolder);
        if (directoryClasses.size() == 0) {
            System.err.println("Couldn't find any class files in the folder " + passedFolder.toPath().toAbsolutePath());
            System.exit(-1);
        }
        HashMap<String, ClassNode> returnMap = new HashMap<>();
        directoryClasses.forEach(classNode -> returnMap.put(classNode.name, classNode));
        return returnMap;
    }

    private HashMap<String, ClassNode> handlePossibleClass(File passedFile) {
        byte[] possibleClass = null;
        try {
            possibleClass = Files.readAllBytes(passedFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        if (FileUtils.isClassFile(possibleClass)) {
            ClassNode newClassNode = ASMUtils.getClassNode(possibleClass);
            HashMap<String, ClassNode> classMap = new HashMap<>();
            classMap.put(newClassNode.name, newClassNode);
            return classMap;
        }
        System.err.println(passedFile.getAbsolutePath() + " is not a class file, directory, or jar file!");
        System.exit(-1);
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
