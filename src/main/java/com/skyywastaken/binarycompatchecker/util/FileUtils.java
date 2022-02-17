package com.skyywastaken.binarycompatchecker.util;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static boolean isClassFile(byte[] passedBytes) {
        if(passedBytes.length < 4) {
            return false;
        }
        int byteOne = Byte.toUnsignedInt(passedBytes[0]);
        int byteTwo = Byte.toUnsignedInt(passedBytes[1]);
        int byteThree = Byte.toUnsignedInt(passedBytes[2]);
        int byteFour = Byte.toUnsignedInt(passedBytes[3]);

        return byteOne == 0xCA && byteTwo == 0xFE && byteThree == 0xBA && byteFour == 0xBE;
    }

    public static byte[] readByteArray(InputStream passedStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1000];
        try {
            int temp;

            while ((temp = passedStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0,
                        temp);
            }
        } catch (IOException e) {
            System.err.println("Failed to read an entry in the compressed file!");
            System.exit(-1);
        }
        return outputStream.toByteArray();
    }

    public static List<ClassNode> getClassesInDirectory(File passedFile) {
        ArrayList<Path> paths = new ArrayList<>();
        try {
            Files.walk(passedFile.toPath()).filter(Files::isRegularFile).forEach(paths::add);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        ArrayList<ClassNode> classes = new ArrayList<>();
        for (Path path : paths) {
            byte[] potentialClass;
            try {
                potentialClass = Files.readAllBytes(path);
            } catch (IOException ignored) {
                continue;
            }
            if (FileUtils.isClassFile(potentialClass)) {
                ClassNode newClassNode = ASMUtils.getClassNode(potentialClass);
                classes.add(newClassNode);
            }
        }
        return classes;
    }
}
