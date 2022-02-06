package com.skyywastaken.binarycompatchecker.util;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {
    public static boolean isZipFile(File passedFile) {
        if (!passedFile.isFile() || passedFile.length() < 4) {
            return false;
        }
        DataInputStream dataInputStream;
        try {
            dataInputStream = new DataInputStream((new BufferedInputStream(new FileInputStream(passedFile))));
        } catch (FileNotFoundException e) {
            return false;
        }
        int magicNumber;
        try {
            magicNumber = dataInputStream.readInt();
            dataInputStream.close();
        } catch(IOException e) {
            return false;
        }
        return magicNumber == 0x504b0304;
    }

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

    public static boolean isClass(File passedFile) {
        return false;
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

    public static List<ClassNode> getAllClassFilesInZip(ZipInputStream zipStream) {
        ArrayList<ClassNode> returnList = new ArrayList<>();
        ZipEntry currentEntry = getNextEntry(zipStream);
        while (currentEntry != null) {
            if (currentEntry.isDirectory()) {
                currentEntry = getNextEntry(zipStream);
                continue;
            }
            byte[] possibleClass;
            try {
                possibleClass = FileUtils.readByteArray(zipStream);
            } catch (IOException e) {
                currentEntry = getNextEntry(zipStream);
                continue;
            }
            if (!FileUtils.isClassFile(possibleClass)) {
                currentEntry = getNextEntry(zipStream);
                continue;
            }
            ClassNode newClassNode = ASMUtils.getClassNode(possibleClass);
            returnList.add(newClassNode);
        }
        try {
            zipStream.closeEntry();
            zipStream.close();
        } catch (IOException ignored) {

        }
        return returnList;
    }

    private static ZipEntry getNextEntry(ZipInputStream inputStream) {
        try {
            return inputStream.getNextEntry();
        } catch (IOException e) {
            return null;
        }
    }
}
