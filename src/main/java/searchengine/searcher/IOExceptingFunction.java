package searchengine.searcher;

import java.io.IOException;

/**
 *
 */
public interface IOExceptingFunction<T, U> {
    U apply(T arg) throws IOException;
}
