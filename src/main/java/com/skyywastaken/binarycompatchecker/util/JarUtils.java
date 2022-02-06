package com.skyywastaken.binarycompatchecker.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class JarUtils {
    public static boolean isZipFile(File passedFile) {
        if(!passedFile.isFile() || passedFile.length() < 4) {
            return false;
        }
        DataInputStream dataInputStream;
        try {
            dataInputStream = new DataInputStream((new BufferedInputStream(new FileInputStream(passedFile))));
        } catch(FileNotFoundException e) {
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
}
