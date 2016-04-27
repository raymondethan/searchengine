package searchengine.helpers;

import java.util.List;

/**
 * Command line argument/option.
 */
public class Argument {

    /**
     * Long version of the argument.
     */
    private String longArg;

    /**
     * Short version of the argument.
     */
    private String shortArg;

    /**
     * Description of the argument.
     */
    private String description;

    /**
     * Callback associated with the argument.
     */
    private ArgumentCallback callback;

    /**
     * Creates a new command line argument.
     * @param shortArgument short version of the argument.
     * @param longArgument long version of the argument.
     * @param description description of the argument.
     * @param callback callback
     */
    public Argument(final String shortArgument, final String longArgument,
                    final String description, final ArgumentCallback callback) {
        shortArg = shortArgument;
        longArg = longArgument;
        this.description = description;
        this.callback = callback;
    }

    /**
     * Gets the long version of the argument.
     * @return the long version of the argument.
     */
    public String getLongArg() {
        return longArg;
    }

    /**
     * Gets the short version of the argument.
     * @return the short version of the argument.
     */
    public String getShortArg() {
        return shortArg;
    }

    /**
     * Gets the description associated with this argument.
     * @return the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Executes the callback associated with this argument.
     * @param args options passed to the callback
     * @throws Exception on an error executing the arguments callback
     */
    public void call(final List<String> args) throws Exception {
        callback.argumentFound(args);
    }
}
