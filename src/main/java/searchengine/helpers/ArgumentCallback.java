package searchengine.helpers;

import java.util.List;

/**
 * Callback for when arguments are found.
 */
public interface ArgumentCallback {
    /**
     * An argument was found.
     * @param opts options associated with this argument.
     * @throws Exception an error occurred setting up the argument.
     */
    void argumentFound(List<String> opts) throws Exception;
}
