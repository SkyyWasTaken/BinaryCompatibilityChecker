package com.skyywastaken.binarycompatchecker;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting up...");
        OptionParser parser = getParser();
        OptionSet options = parser.parse(args);
        String oldLocation = (String) options.valueOf("old");
        String newLocation = (String) options.valueOf("new");
        ClassFinder classFinder = new ClassFinder(oldLocation, newLocation);
    }

    private static OptionParser getParser() {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(Arrays.asList("n", "new"), "The new folder/jar/class location").withRequiredArg().required();
        parser.acceptsAll(Arrays.asList("o", "old"), "The old folder/jar/class location").withRequiredArg().required();
        parser.acceptsAll(Arrays.asList("h", "help"), "Display help information").forHelp();
        return parser;
    }
}
