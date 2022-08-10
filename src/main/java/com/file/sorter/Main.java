package com.file.sorter;

import com.file.sorter.exception.FileNotFoundCustomException;
import com.file.sorter.exception.InvalidParameterException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static String sortMode;
    private static String dataType;
    private static String resultFileName;
    private static String[] inputFileNames;

    public static void main(String[] args) {
        System.out.println("Program is starting...");

        List<BufferedReader> readers = null;
        BufferedWriter writer = null;
        try {
            readAndCheckArgsParameters(args);
            readers = getReaders();
            writer = getWriter();
            mergeFiles(readers, writer);

            System.out.println("Done!");
        } catch (Exception e) {
            System.err.println(String.format("The program terminated with an error. %s", e.getMessage()));
        } finally {
            closeReaders(readers);
            closeWriter(writer);
        }
    }

    private static void mergeFiles(List<BufferedReader> readers, BufferedWriter writer) throws IOException {
        boolean inProcess = true;
        boolean[] skipMarkers = new boolean[inputFileNames.length];
        Object[] inputCache = new Object[inputFileNames.length];

        //read one line from files and write to cache
        while (inProcess) {
            for (int i = 0; i < readers.size(); i++) {
                if (!skipMarkers[i]) {
                    boolean nextIteration = true;
                    while (nextIteration) {
                        if (readers.get(i).ready()) {
                            String line = readers.get(i).readLine();
                            if (dataType.equals("INTEGER")) {
                                try {
                                    inputCache[i] = Integer.parseInt(line);
                                    nextIteration = false;
                                } catch (NumberFormatException e) {
                                    System.err.println(String.format
                                            ("Invalid value: '%s' from file '%s'", line, inputFileNames[i]));
                                }
                            } else if (dataType.equals("STRING")) {
                                if (!line.contains(" ")) {
                                    inputCache[i] = line;
                                    nextIteration = false;
                                } else {
                                    System.err.println(String.format
                                            ("Invalid value: '%s' from file '%s'", line, inputFileNames[i]));
                                }
                            }
                        } else {
                            nextIteration = false;
                        }
                    }
                }
            }

            //find min or max value
            Object valueToWrite = inputCache[0];
            int indexOfListWithValueToWrite = 0;
            for (int i = 1; i < inputCache.length; i++) {
                if (valueToWrite == null) {
                    if (inputCache[i] != null) {
                        valueToWrite = inputCache[i];
                        indexOfListWithValueToWrite = i;
                    }
                }

                if (inputCache[i] != null) {
                    if (dataType.equals("INTEGER")) {
                        Integer tmp = (Integer) valueToWrite;
                        Integer tmpCache = (Integer) inputCache[i];
                        if (sortMode.equals("ASC")) {
                            if (tmp > tmpCache) {
                                valueToWrite = inputCache[i];
                                indexOfListWithValueToWrite = i;
                            }
                        } else if (sortMode.equals("DESC")) {
                            if (tmp < tmpCache) {
                                valueToWrite = inputCache[i];
                                indexOfListWithValueToWrite = i;
                            }
                        }
                    } else if (dataType.equals("STRING")) {
                        String tmp = (String) valueToWrite;
                        String tmpCache = (String) inputCache[i];
                        if (sortMode.equals("ASC")) {
                            if (tmp.compareTo(tmpCache) > 0) {
                                valueToWrite = inputCache[i];
                                indexOfListWithValueToWrite = i;
                            }
                        } else if (sortMode.equals("DESC")) {
                            if (tmp.compareTo(tmpCache) < 0) {
                                valueToWrite = inputCache[i];
                                indexOfListWithValueToWrite = i;
                            }
                        }
                    }
                }
            }

            //write value to output and correct skip markers
            Arrays.fill(skipMarkers, true);
            if (valueToWrite != null) {
                writer.write(valueToWrite + "\n");
            }
            inputCache[indexOfListWithValueToWrite] = null;
            skipMarkers[indexOfListWithValueToWrite] = false;

            //check all files were read
            inProcess = false;
            for (int i = 0; i < readers.size(); i++) {
                if (readers.get(i).ready() || inputCache[i] != null) {
                    inProcess = true;
                }
            }
        }
    }

    private static List<BufferedReader> getReaders() {
        List<BufferedReader> readers = new ArrayList<>();
        for (String inputFileName : inputFileNames) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
                readers.add(reader);
            } catch (FileNotFoundException e) {
                throw new FileNotFoundCustomException(String.format("File with name '%s' was not found", inputFileName));
            }
        }
        return readers;
    }

    private static BufferedWriter getWriter() throws IOException {
        return new BufferedWriter(new FileWriter(resultFileName));
    }

    private static void closeReaders(List<BufferedReader> readers) {
        if (readers != null) {
            for (BufferedReader reader : readers) {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {/* NOP */}
            }
        }
    }

    private static void closeWriter(BufferedWriter writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {/* NOP */}
    }

    private static void readAndCheckArgsParameters(String[] args) {
        try {
            int index = 0;
            if (args[index].equals("-d")) {
                sortMode = "DESC";
                index++;
            } else {
                sortMode = "ASC";
                if (args[index].equals("-a")) {
                    index++;
                }
            }

            if (args[index].equals("-s")) {
                dataType = "STRING";
                index++;
            } else if (args[index].equals("-i")) {
                dataType = "INTEGER";
                index++;
            } else {
                throw new InvalidParameterException(String.format("Check your parameters: '%s'", Arrays.toString(args)));
            }

            resultFileName = args[index];
            inputFileNames = new String[(args.length - 1) - index];
            index++;

            if (index >= args.length) {
                throw new InvalidParameterException(String.format("Check your parameters: '%s'", Arrays.toString(args)));
            }

            for (int i = 0; index < args.length; index++, i++) {
                inputFileNames[i] = args[index];
            }

        } catch (IndexOutOfBoundsException e) {
            throw new InvalidParameterException(String.format("Check your parameters: '%s'", Arrays.toString(args)));
        }
    }
}