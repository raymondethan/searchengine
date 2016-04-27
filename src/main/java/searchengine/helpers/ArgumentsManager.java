package searchengine.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class to manage CLI arguments.
 */
public final class ArgumentsManager {

    /**
     * Static instance of the arguments manager.
     */
    private static ArgumentsManager manager;
    /**
     * The prefix for the long version of the arguments.
     */
    private final String longPrefix = "--";
    /**
     * The prefix for the short version of the arguments.
     */
    private final String shortPrefix = "-";
    /**
     * Collection of options that have been registered.
     */
    private Collection<Argument> regArguments;
    /**
     * Arguments that have been parsed.
     */
    private String[] parsedArguments;

    /**
     * Creates a new singleton ArgumentsManager.
     * Initialises the ArgumentsManager class.
     */
    private ArgumentsManager() {
        regArguments = new ArrayList<>();
        registerArgument("h", "help", "Shows this help.", args -> {
            if (args.size() > 0) {
                throw new Exception("Invalid use of the help option.");
            }

            for (Argument argument : regArguments) {
                System.out.println("\t" + shortPrefix + argument.getShortArg()
                        + ", " + longPrefix + argument.getLongArg());
                System.out.println("\t  " + argument.getDescription().replaceAll("(?m)^", "\t  "));
            }

            System.exit(0);
        });
    }

    /**
     * Gets the instance of the arguments manager.
     * @return the instance.
     */
    public static ArgumentsManager get() {
        if (manager == null) {
            manager = new ArgumentsManager();
        }
        return manager;
    }

    /**
     * Adds a new command line argument.
     * @param shortArgument short version of the CLI argument.
     * @param longArgument long version of the CLI argument.
     * @param description description of the argument.
     * @param callback callback associated with the argument. This will be fired when the argument is found.
     * @return the argument manager
     */
    public ArgumentsManager registerArgument(final String shortArgument, final String longArgument,
                                 final String description, final ArgumentCallback callback) {
        regArguments.add(new Argument(shortArgument, longArgument, description, callback));

        return this;
    }

    /**
     * Parses CLI arguments and fires appropriate setup callbacks.
     * @param arguments arguments that were passed into the application
     */
    public void parseArguments(final String[] arguments) {
        if (parsedArguments != null) {
            throw new UnsupportedOperationException("Cannot parse arguments after already parsing.");
        }
        parsedArguments = arguments;
        Argument currentArgument = null, newArgument = null;
        List<String> args = new ArrayList<>();
        for (String argument : arguments) {
            if (argument.startsWith(longPrefix)) {
                newArgument = regArguments.stream()
                        .filter(a -> a.getLongArg().equalsIgnoreCase(argument
                                .substring(longPrefix.length()))).findFirst().orElse(null);
            }
            else if (argument.startsWith(shortPrefix)) {
                newArgument = regArguments.stream()
                        .filter(a -> a.getShortArg().equalsIgnoreCase(argument
                                .substring(shortPrefix.length()))).findFirst().orElse(null);
            }

            if (currentArgument == null && newArgument == null) {
                abortError("Unknown arguments provided.");
            }

            if (currentArgument != null && newArgument == null) {
                args.add(argument);
            }

            if (newArgument != null) {
                if (currentArgument != null) {
                    try {
                        currentArgument.call(args);
                    }
                    catch (Exception e) {
                        abortError(e.getMessage());
                    }
                    args = new ArrayList<>();
                }
                currentArgument = newArgument;
                newArgument = null;
            }
        }
        if (currentArgument != null) {
            try {
                currentArgument.call(args);
            } catch (Exception e) {
                abortError(e.getMessage());
            }
        }
    }

    /**
     * Writes an error to the console and terminates the program.
     * @param error the error to display.
     */
    private void abortError(final String error) {
        System.out.println("Critical Error: " + error);
        System.exit(0);
    }

    /**
     * Gets the command line arguments the application was started with.
     * @return the command line arguments.
     */
    public String[] getArguments() {
        return parsedArguments;
    }
}