package com.skyywastaken.binarycompatchecker.util;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

public class ASMUtils {
    public static ClassNode getClassNode(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode returnNode = new ClassNode();
        reader.accept(returnNode, 0);
        return returnNode;
    }
}
