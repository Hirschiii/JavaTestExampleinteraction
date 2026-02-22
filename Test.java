package test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Test {

    public static void main(String[] args) {
        boolean verbose = false;
        Path testFile = Path.of("test/beispielinteraktion.txt");
        String className = "Main";

        for (String string : args) {
            if (!string.matches(".*=.*")) {
                return;
            }

            String[] parts = string.split("=");

            switch (parts[0]) {
                case "verbose":
                    if (parts[1].equals("true")) {
                        verbose = true;
                    } else {
                        verbose = false;
                    }
                    break;
                case "test":
                    testFile = Path.of(parts[1]);
                default:
                    break;
            }

        }
        try {
            List<String> allLines = Files.readAllLines(testFile, StandardCharsets.UTF_8);

            // 1. App-Argumente aus der ersten Zeile extrahieren
            String firstLine = allLines.get(0);
            String[] appArgs;
            int startIdx = 0;

            if (firstLine.startsWith("%> java ")) {
                String content = firstLine.substring(8).trim();
                String[] parts = content.split("\\s+");

                if (parts.length > 0) {
                    className = parts[0]; // Das erste Wort nach 'java' ist die Klasse

                    // Die restlichen Wörter sind die Argumente
                    appArgs = new String[parts.length - 1];
                    System.arraycopy(parts, 1, appArgs, 0, parts.length - 1);

                    startIdx = 1;
                    System.out.println("Starte Klasse: " + className + " mit Argumenten: " + String.join(" ", appArgs));
                } else {
                    appArgs = new String[0];
                }
            } else {
                // Fallback, falls die Zeile fehlt (Standardwerte)
                System.out.println("Keine Argument-Zeile gefunden, nutze Standard-Args.");
                appArgs = new String[] { "seed=-4022738", "deck=input/decks/default.txt", "verbosity=compact",
                        "units=input/units/default.txt" };
            }

            // 2. Inputs extrahieren (alles ab startIdx, was mit > beginnt)
            String inputData = allLines.subList(startIdx, allLines.size()).stream()
                    .filter(line -> line.startsWith("> "))
                    .map(line -> line.substring(1).trim())
                    .collect(Collectors.joining("\n")) + "\n";

            // 3. Erwarteten Output extrahieren (alles was NICHT mit > oder %> beginnt und
            // keine reine Zahl ist)
            List<String> expectedLines = allLines.subList(startIdx, allLines.size());

            // List<String> expectedLines = allLines.subList(startIdx,
            // allLines.size()).stream()
            // .map(String::trim)
            // .collect(Collectors.toList());

            runTest(className, appArgs, inputData, expectedLines, verbose);

        } catch (Exception e) {
            System.err.println("TEST FEHLGESCHLAGEN!");
            e.printStackTrace();
        }
    }

    private static void runTest(String className, String[] appArgs, String inputData, List<String> expectedLines,
            boolean verbose)
            throws Exception {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(new ByteArrayInputStream(inputData.getBytes(StandardCharsets.UTF_8)));
        System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));

        try {
            // 1. Klasse laden
            Class<?> clazz = Class.forName(className);

            // 2. Die main-Methode finden (sie nimmt ein String-Array als Parameter)
            java.lang.reflect.Method mainMethod = clazz.getMethod("main", String[].class);
            // edu.kit.kastel.Main.main(appArgs);
            mainMethod.invoke(null, (Object) appArgs);

            String actualOutput = outContent.toString(StandardCharsets.UTF_8);
            String[] actualLines = actualOutput.split("\\R");

            int expectedIdx = 0;
            int actualIdx = 0;
            boolean commandRunning = false;
            String runningCommand = new String();

            // Wir gehen die erwarteten Zeilen durch
            while (expectedIdx < expectedLines.size() && actualIdx < actualLines.length) {
                String expected = expectedLines.get(expectedIdx).trim();
                String actual = actualLines[actualIdx].trim();

                // Überspringe zeilen mit Input
                if (expected.startsWith("> ")) {
                    if (commandRunning) {
                        System.err.println("-----------------------------------");
                        System.err.println("Passed   : %s".formatted(runningCommand));
                        runningCommand = expected.substring(2);
                        System.err.println("Test next: %s".formatted(runningCommand));
                        System.err.println("-----------------------------------");
                    } else {
                        commandRunning = true;
                        runningCommand = expected.substring(2);
                    }

                    expectedIdx++;
                    continue;
                }

                // Settings / flags
                if (expected.startsWith("%> ")) {
                }

                // System.err.println("ACT: %s".formatted(actualLines[actualIdx]));
                if (verbose) {
                    System.err.println("%03d : %s".formatted(actualIdx, actualLines[actualIdx]));
                }

                // Vergleich (enthält die Zeile den erwarteten Teil?)
                if (actual.contains(expected)) {
                    // System.err.println("EXP: %s".formatted(expected));
                    // Treffer! Beide Zähler hoch
                    expectedIdx++;
                    actualIdx++;
                } else {
                    // Kein Treffer: Wir zeigen genau an, wo es hakt
                    printErrorMessage(expectedLines, actualLines, actualIdx, expectedIdx);
                    return; // Test abbrechen
                }
            }

            if (expectedIdx < expectedLines.size() && !expectedLines.get(expectedIdx).matches("> quit")) {
                System.err.println("\nFEHLER: Das Programm endete vorzeitig!");
                System.err.println("Erwartet wurde noch: " + expectedLines.get(expectedIdx));
            } else {
                System.err.println("\nSUCCESS: Alle " + expectedLines.size() + " Prüfpunkte erfolgreich bestanden.");
                System.err.println("Du kannst dich jetzt auf weiter Tests freuen, vor allem die von Artemis :)");
            }

        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    private static void printErrorMessage(List<String> expected, String[] actual, int actualLine, int expectedLine) {
        int min = Math.min(12, expectedLine);
        int max = Math.min(12, expected.size() - expectedLine);
        System.err.println("\n--- TEST FEHLGESCHLAGEN ---");
        System.err.println("IN E LINE: \"" + (expectedLine + 1) + "\"");
        System.err.println("IN A LINE: \"" + (actualLine + 1) + "\"");
        System.err.println("ERWARTET: \"" + expected.get(expectedLine) + "\"");
        System.err.println("GEFUNDEN: \"" + actual[actualLine] + "\"");
        System.err.println("---------------------------\n");
        System.err.println("Kontext:");
        for (int i = actualLine - min; i < actualLine; i++) {
            System.err.println("%03d : %s".formatted(i, actual[i]));
        }
        System.err.println("%03d : %s".formatted(actualLine, actual[actualLine]));
        // System.err.println(actual[actualLine]);
        System.err.println("      ^^^^^^^^^");
        for (int i = actualLine + 1; i < actualLine + max; i++) {
            System.err.println("%03d : %s".formatted(i, actual[i]));
        }

    }
}
