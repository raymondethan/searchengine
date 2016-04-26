package searchengine.searcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import searchengine.crawler.WebPage;
import searchengine.indexer.Index;
import searchengine.indexer.Posting;

/**
 *
 */
public class Searcher {
    public static final int titleWeighting = 5;

    private Index index;

    public Searcher(Index index) {
        this.index = index;
    }

    public List<SearchResult> search(String query) throws IOException {
        Tokenizer tokenizer = new Tokenizer(query);

        List<SearchResult> bodyResults = getResults(tokenizer, index::idf, index::getDoc);
        List<SearchResult> titleResults = getResults(tokenizer, index::titleIdf, index::getTitleDoc);

        Stream<SearchResult> results = mergeResults(titleResults, bodyResults);

        return results
                .sorted((i1, i2) -> i1.getSimilarity() > i2.getSimilarity() ? -1 : 1)
                .collect(Collectors.toList());
    }

    public List<SearchResult> getResults(Tokenizer tokenizer, IOExceptingFunction<String, Double> idfFetcher, IOExceptingFunction<String, List<Posting>> docFetcher) throws IOException {
        //Cache for word idfs
        Map<String, Double> idfCache = new HashMap<>();

        //Vector representing the query
        DocumentVector queryVector = new DocumentVector(getQueryTfIdfs(tokenizer.allWords(), idfCache, idfFetcher));
        //Vectors representing all matching documents
        Map<Integer, DocumentVector> documentVectors = new HashMap<>();

        //The number of terms in the query
        int queryLength = queryVector.dimensions();

        //All the tokens in the query
        List<Token> tokens = tokenizer.getTokens();

        ArrayList<SearchResult> matched_documents = new ArrayList<>();
        for (int i = 0; i < tokens.size(); ++i) {
            int min_index = 0;
            int min_size = -1;
            ArrayList<HashMap<Integer, Posting>> postings = new ArrayList<HashMap<Integer, Posting>>();
            for (int j = 0; j < tokens.get(i).getWords().size(); ++ j) {
                HashMap<Integer, Posting> matched = new HashMap<>();
                postings.add(matched);
                String word = tokens.get(i).getWords().get(j);
                //get all f the docs matching the word in the phrase
                List<Posting> docs = docFetcher.apply(word);
                if (docs == null) continue;
                //get the size so we can filter based on the smallest number of matched documents
                if (-1 == min_size || docs.size() < min_size) {
                    min_size = docs.size();
                    min_index = j;
                }
                //add all Postings to a dictionary for constant access later
                docs.stream().forEach(posting -> postings.get(postings.size()-1).put(posting.doc,posting));
            }
            //filter out the documents where words in the phrase are not adjacent
            //ArrayList<SearchResult> result = new ArrayList<SearchResult>();
            if (postings.size() > 1) {
                for (Posting doc_match : postings.get(min_index).values()) {


                    //set this variable to true now, we AND it with the boolean variable to determine if all documents contain a valid position
                    boolean found_valid_position_difference = true;
                    boolean doc_missing_from_other_posting_list = false;
                    int j = 0;
                    while (found_valid_position_difference && !doc_missing_from_other_posting_list && j < postings.size()) {
                        String word = tokens.get(i).getWords().get(j);
                        Posting doc_in_other_posting = postings.get(j).get(doc_match.doc);

                        if (doc_in_other_posting != null) {
                            double tf = doc_in_other_posting.positions.size();
                            double tfIdf = tf * getIdf(word, idfCache, idfFetcher);

                            if (!documentVectors.containsKey(doc_match.doc)) {
                                DocumentVector vector = new DocumentVector(queryLength);
                                documentVectors.put(doc_match.doc, vector);
                            }

                            documentVectors.get(doc_match.doc).getTfIdfs().set(tokenizer.getTokens().get(i).getFirstWordIndex() + j, tfIdf);
                        }

                        //make sure we don't compare against the smallest posting list because thats what we're iterating over
                        if (j != min_index) {
                            if (null != doc_in_other_posting) {
                                //Assumes posting stores words position in an ordered array
                                //Since we parse a document in order, this should remain a valid assumption
                                int doc_match_index = 0;
                                int other_doc_index = 0;
                                boolean doc_contains_valid_position = false;
                                //now we have to iterate through the list of word occurances to verify that the words in each posting occur in the same order as in the query
                                while (!doc_contains_valid_position && doc_match_index < doc_match.positions.size() && other_doc_index < doc_in_other_posting.positions.size()) {

                                    int calculated_position_difference = doc_match.positions.get(doc_match_index) - doc_in_other_posting.positions.get(other_doc_index);
                                    //min_index is the position of doc_match's word in the posting list and query
                                    //j is the position of the other doc's word in the posting list and query
                                    int needed_position_difference = tokens.get(i).getPositions().get(min_index) - tokens.get(i).getPositions().get(j);

                                    if (calculated_position_difference == needed_position_difference) {
                                        doc_contains_valid_position = true;
                                    }
                                    //efficiently iterate through the position arrays of each posting
                                    else if (calculated_position_difference > needed_position_difference) {
                                        other_doc_index += 1;
                                    }
                                    else {
                                        doc_match_index += 1;
                                    }
                                }
                                found_valid_position_difference = found_valid_position_difference && doc_contains_valid_position;
                            }
                            else {
                                doc_missing_from_other_posting_list = true;
                                found_valid_position_difference = false;
                            }
                        }
                        ++j;
                    }
                    if (!found_valid_position_difference)  {
                        documentVectors.remove(doc_match.doc);
                    }
                }
            }
            else {
                for (Integer docId : postings.get(0).keySet()) {

                    String word = tokens.get(i).getWords().get(0);

                    double tf = postings.get(0).get(docId).positions.size();
                    double tfIdf = tf * getIdf(word, idfCache, idfFetcher);

                    if (!documentVectors.containsKey(docId)) {
                        DocumentVector vector = new DocumentVector(queryLength);
                        documentVectors.put(docId, vector);
                    }

                    documentVectors.get(docId).getTfIdfs().set(tokenizer.getTokens().get(i).getFirstWordIndex(), tfIdf);
                }
            }
        }

        for (Integer key : documentVectors.keySet()) {
            DocumentVector vector = documentVectors.get(key);
            double similarity = vector.dot(queryVector);

            matched_documents.add(getSearchResult(key, similarity));
        }

        return matched_documents;
    }

    public SearchResult getSearchResult(int id, double similarity) throws IOException {
        WebPage webPage = index.getWebPage(id);
        return new SearchResult(id, webPage.title, "description", webPage.url, similarity);
    }

    public List<Double> getQueryTfIdfs(List<String> query, Map<String, Double> cache, IOExceptingFunction<String, Double> fetcher) throws IOException {
        List<Double> idfs = new ArrayList<Double>();
        for (String word : query) {
            idfs.add(query.stream().filter(w -> w.equals(word)).count() * getIdf(word, cache, fetcher));
        }
        return idfs;
    }

    private double getIdf(String word, Map<String, Double> cache, IOExceptingFunction<String, Double> fetcher) throws IOException {
        if (cache.containsKey(word)) return cache.get(word);

        double idf = fetcher.apply(word);
        cache.put(word, idf);

        return idf;
    }

    public Stream<SearchResult> mergeResults(List<SearchResult> titleResults, List<SearchResult> bodyResults) {
        Map<Integer, SearchResult> results = new HashMap<>(titleResults.size() + bodyResults.size());

        for (SearchResult result : bodyResults) {
            results.put(result.getId(), result);
        }

        for (SearchResult result : titleResults){
            double similarity = result.getSimilarity()*titleWeighting;

            if (results.containsKey(result.getId())) {
                similarity += results.get(result.getId()).getSimilarity();
                result.setSimilarity(similarity);
            }

            results.put(result.getId(), result);
        }

        return results
                .values()
                .stream();
    }
}
