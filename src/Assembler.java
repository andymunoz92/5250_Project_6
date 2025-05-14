import java.io.*;
import java.nio.file.*;

// Hack Assembler
// The assembler takes as input a stream of assembly commands and generates as
// output a stream of equivalent binary instructions.
public class Assembler {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Assembler <inputFileName>");
            System.err.println("Input files are read from ../Input directory");
            System.err.println("Output files are written to ../Output directory");
            System.exit(1);
        }

        String inputFileName = args[0];
        // Check if .asm extension is present, add if not
        if (!inputFileName.endsWith(".asm")) {
            inputFileName += ".asm";
        }

        // Get current working directory
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentDir);

        // Create paths for input and output directories and files (one level up)
        Path inputDir = Paths.get("..").resolve("Input");
        Path outputDir = Paths.get("..").resolve("Output");

        Path inputPath = inputDir.resolve(inputFileName);
        String outputFileName = inputFileName.replace(".asm", ".hack");
        Path outputPath = outputDir.resolve(outputFileName);

        System.out.println("Looking for input file at: " + inputPath.toAbsolutePath());
        System.out.println();

        // Check if Input directory exists
        if (!Files.exists(inputDir)) {
            System.err.println("Error: Input directory doesn't exist at: " + inputDir.toAbsolutePath());
            System.err.println("Please ensure the Input directory exists at the project root level.");
            System.exit(1);
        }

        // Check if input file exists
        if (!Files.exists(inputPath)) {
            System.err.println("Error: Input file not found: " + inputPath.toAbsolutePath());
            System.err.println("Make sure your .asm file is in the Input directory.");
            System.exit(1);
        }

        try {
            // Create Output directory if it doesn't exist
            if (!Files.exists(outputDir)) {
                System.out.println("Creating Output directory at the project root level...");
                Files.createDirectories(outputDir);
                System.out.println("Output directory created at: " + outputDir.toAbsolutePath());
            }

            // First pass: Go through the entire assembly program, line by line, and build the
            //symbol table without generating any code.
            SymbolTable symbolTable = new SymbolTable();
            Parser firstParser = new Parser(inputPath.toString());
            int addressROM = 0;

            while (firstParser.hasMoreCommands()) {
                firstParser.advance();
                if (firstParser.commandType() == Parser.CommandType.L_COMMAND) {
                    symbolTable.addEntry(firstParser.symbol(), addressROM);
                } else if (firstParser.commandType() != Parser.CommandType.INVALID) {
                    addressROM++;
                }
            }

            // Second pass: Now go again through the entire program, and parse each line.
            Parser secondParser = new Parser(inputPath.toString());
            Code code = new Code();
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toString()));
            // Variable allocation
            int ramAddress = 16;

            while (secondParser.hasMoreCommands()) {
                secondParser.advance();

                switch (secondParser.commandType()) {
                    case A_COMMAND:
                        String symbol = secondParser.symbol();
                        int address;

                        if (symbol.matches("\\d+")) {
                            // Convert directly if a symbol is a number
                            address = Integer.parseInt(symbol);
                        } else {
                            // Use the symbol table if symbol is not a number
                            if (!symbolTable.contains(symbol)) {
                                symbolTable.addEntry(symbol, ramAddress++);
                            }
                            address = symbolTable.getAddress(symbol);
                        }

                        String binary = Integer.toBinaryString(address);
                        // Pad to 16 bits
                        binary = "0".repeat(16 - binary.length()) + binary;
                        writer.write(binary);
                        writer.newLine();
                        break;

                    case C_COMMAND:
                        // Binary Syntax: 1 1 1 a c1 c2 c3 c4 c5 c6 d1 d2 d3 j1 j2 j3
                        String comp = code.comp(secondParser.comp());
                        String dest = code.dest(secondParser.dest());
                        String jump = code.jump(secondParser.jump());
                        writer.write("111" + comp + dest + jump);
                        writer.newLine();
                        break;

                    case L_COMMAND:
                    case INVALID:
                        // Invalid commands
                        break;
                }
            }

            writer.close();
            System.out.println("Hack assembly successful.");
            System.out.println();
            System.out.println("Input: " + inputPath.toAbsolutePath());
            System.out.println("Output: " + outputPath.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("\nAdditional debugging info:");
            System.err.println("Input path: " + inputPath.toAbsolutePath());
            System.err.println("Output path: " + outputPath.toAbsolutePath());
            System.err.println("Can read input file: " + Files.isReadable(inputPath));
            System.err.println("Can write to output directory: " + Files.isWritable(outputDir));
        }
    }
}
