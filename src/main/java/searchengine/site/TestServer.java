package searchengine.site;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import searchengine.indexer.Index;
import searchengine.searcher.Searcher;
import searchengine.searcher.Tokenizer;

/**
 *
 */
public class TestServer {
    private static Searcher searcher;
    public static void main(String[] args) throws IOException {
        searcher = new Searcher(new Index("inverted_index"));

        SocketConfig config = SocketConfig.custom()
                .setSoTimeout(5000)
                .setTcpNoDelay(true)
                .build();

        final HttpServer server = ServerBootstrap.bootstrap()
                .setListenerPort(8080)
                .setServerInfo("A magical test server")
                .setSocketConfig(config)
                .setExceptionLogger(e -> {
                    System.err.println(e.getMessage());
                    System.err.println(e.getStackTrace());
                })
                .registerHandler("/controller.js", (httpRequest, httpResponse, httpContext) -> {
                    //TODO send html file
                    File file = new File("controller.js");
                    FileEntity entity = new FileEntity(file);
                    httpResponse.setEntity(entity);
                })
                .registerHandler("/", (httpRequest, httpResponse, httpContext) -> {
                    //TODO send html file
                    File file = new File("index.html");
                    FileEntity entity = new FileEntity(file, ContentType.TEXT_HTML);
                    httpResponse.setEntity(entity);
                })
                .registerHandler("/search/*", (httpRequest, httpResponse, httpContext) -> {
                    String query = httpRequest.getRequestLine().getUri().substring(8);
                    List<Object> results = getResults(query);

                    String json = (new Gson()).toJson(results);
                    StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                    httpResponse.setEntity(entity);
                })
                .create();

        server.start();
        System.out.println("Started server!");

        try {
            server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        }catch (InterruptedException e) {
            System.out.println("Server stopped");
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown(5, TimeUnit.SECONDS);
            }
        });
    }

    private static List getResults(String query) throws IOException {
        //searcher.search(query);
        Tokenizer tokenizer = new Tokenizer(query);
        return tokenizer.getTokens();
    }
}
