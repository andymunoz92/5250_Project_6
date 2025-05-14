import java.io.*;

/**
 * Encapsulates access to the input code. Reads an assembly language command,
 * parses it, and provides convenient access to the command’s components
 * (fields and symbols). In addition, removes all white space and comments
 */
public class Parser {
    enum CommandType {
        A_COMMAND,
        C_COMMAND,
        L_COMMAND,
        INVALID
    }

    private final BufferedReader reader;
    private String currentCommand;
    private CommandType currentType;
    private String currentDest;
    private String currentComp;
    private String currentJump;
    private String currentSymbol;

    public Parser(String filename) throws IOException {
        reader = new BufferedReader(new FileReader(filename));
        currentCommand = null;
    }

    // Are there more commands in the input?
    public boolean hasMoreCommands() throws IOException {
        reader.mark(1000);
        String line = reader.readLine();

        while (line != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("//")) {
                reader.reset();
                return true;
            }
            reader.mark(1000);
            line = reader.readLine();
        }

        return false;
    }

    // Reads the next command from the input and makes it the current command. Should be called only
    // if hasMoreCommands() is true. Initially there is no current command.
    public void advance() throws IOException {
        String line = reader.readLine();

        while (line != null) {
            // Removal of comments and whitespace
            int commentIndex = line.indexOf("//");
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex);
            }

            line = line.trim();

            if (!line.isEmpty()) {
                currentCommand = line;
                parse();
                return;
            }

            line = reader.readLine();
        }

        currentCommand = null;
    }

    // Current command type
    public CommandType commandType() {
        return currentType;
    }

    // Returns the symbol or decimal Xxx of the current command @Xxx or (Xxx). Should be called
    // only when commandType() is A_COMMAND or L_COMMAND.
    public String symbol() {
        return currentSymbol;
    }


     // Returns the dest mnemonic in the current C-command (8 possibilities).
     // Should only be called when commandType() is C_COMMAND
    public String dest() {
        return currentDest;
    }

    // Returns the comp mnemonic in the current C-command (28 possibilities).
    // Should be called only when commandType() is C_COMMAND
    public String comp() {
        return currentComp;
    }

    // Returns the jump mnemonic in the current C-command (8 possibilities).
    // Should be called only when commandType() is C_COMMAND.
    public String jump() {
        return currentJump;
    }

    // Parse and set appropriate fields
    private void parse() {
        if (currentCommand.startsWith("@")) {
            currentType = CommandType.A_COMMAND;
            currentSymbol = currentCommand.substring(1);
        } else if (currentCommand.startsWith("(") && currentCommand.endsWith(")")) {
            currentType = CommandType.L_COMMAND;
            currentSymbol = currentCommand.substring(1, currentCommand.length() - 1);
        } else {
            currentType = CommandType.C_COMMAND;

            if (currentCommand.contains("=")) {
                String[] parts = currentCommand.split("=", 2);
                currentDest = parts[0];
                currentCommand = parts[1];
            } else {
                currentDest = "";
            }

            if (currentCommand.contains(";")) {
                String[] parts = currentCommand.split(";", 2);
                currentComp = parts[0];
                currentJump = parts[1];
            } else {
                currentComp = currentCommand;
                currentJump = "";
            }
        }
    }
}