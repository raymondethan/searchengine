package searchengine.searcher;

import com.sun.tools.classfile.Opcode;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.parser.Tokens;
import searchengine.indexer.Index;
import searchengine.indexer.Posting;

import java.io.IOException;
import java.util.*;

import searchengine.indexer.Index;
import searchengine.indexer.Posting;

/**
 *
 */
public class Searcher {

    private Index index;

    public Searcher(Index index) {
        this.index = index;
    }

    public List<SearchResult> search(String query) throws IOException {
        List<Token> tokens = new Tokenizer(query).getTokens();
        ArrayList<Posting> matched_documents = new ArrayList<Posting>();
        System.out.println(tokens.size());
        for (int i = 0; i < tokens.size(); ++i) {
            int min_index = 0;
            int min_size = -1;
            ArrayList<HashMap<Integer, Posting>> postings = new ArrayList<HashMap<Integer, Posting>>();
            for (int j = 0; j < tokens.get(i).getWords().size(); ++ j) {
                HashMap<Integer, Posting> matched = new HashMap<>();
                postings.add(matched);
                //get all of the docs matching the word in the phrase
                List<Posting> docs = index.getDoc(tokens.get(i).getWords().get(j));
                //get the size so we can filter based on the smallest number of matched documents
                if (-1 != min_size || docs.size() < min_size) {
                    min_size = docs.size();
                    min_index = j;
                }
                //add all Postings to a dictionary for constant access later
                docs.stream().forEach(posting -> postings.get(postings.size()-1).put(posting.doc,posting));
            }
            //filter the documents where words in the phrase are not adjacent
            ArrayList<Posting> result = new ArrayList<Posting>();
            if (postings.size() > 1) {
                for (Posting doc_match : postings.get(min_index).values()) {
                    //set this variable to true now, we AND it with the boolean variable to determine if all docuents contain a valid position
                    boolean found_valid_position_difference = true;
                    boolean doc_missing_from_other_posting_list = false;
                    int j = 0;
                    while (!doc_missing_from_other_posting_list && j < postings.size()) {
                        //make sure we don't compare against the smallest posting list because thats what we're iterating over
                        if (j != min_index) {
                            Posting doc_in_other_posting = postings.get(j).get(doc_match.doc);
                            if (null != doc_in_other_posting) {
                                //Assumes posting stores words position in an ordered array
                                //Since we parse a document in order, this should remain a valid assumption
                                int doc_match_index = 0;
                                int other_doc_index = 0;
                                boolean doc_contains_valid_position = false;
                                //now we have to iterate through the list of word occurances to verify that the words in each posting occur in the same order as in the query
                                while (!doc_contains_valid_position && doc_match_index < doc_match.positions.size() && other_doc_index < doc_in_other_posting.positions.size()) {

                                    int calculated_position_difference = doc_match.positions.get(doc_match_index) - doc_in_other_posting.positions.get(other_doc_index);;
                                    //min_index is the position of doc_match's word in the posting list and query
                                    //j is the position of the other doc's word in the posting list and query
                                    int needed_position_difference = min_index - j;

                                    if (calculated_position_difference == needed_position_difference) {
                                        doc_contains_valid_position = true;
                                    }
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
                            }
                        }
                        ++j;
                    }
                    if (found_valid_position_difference) {
                        result.add(doc_match);
                    }
                }
            }
            else {
                postings.get(0).forEach((integer, posting) -> result.add(posting));
            }
            for (Posting p : result) {
                System.out.println(index.getWebPage(p.doc).url);
            }
        }

        return new ArrayList<>();
    }

//    public ArrayList<HashMap<Integer, Posting>> getAllMatchingPostingsFromIndex(List<Token> tokens, int i, Index index) throws IOException {
//        int min_index = 0;
//        int min_size = -1;
//        ArrayList<HashMap<Integer, Posting>> postings = new ArrayList<HashMap<Integer, Posting>>();
//        for (int j = 0; j < tokens.get(i).words.size(); ++ j) {
//            HashMap<Integer, Posting> matched = new HashMap<>();
//            postings.add(matched);
//            //get all of the docs matching the word in the phrase
//            ArrayList<Posting> docs = index.getDocs(tokens.get(i).getWords().get(j));
//            //get the size so we can filter based on the smallest number of matched documents
//            if (-1 != min_size || docs.size() < min_size) {
//                min_size = docs.size();
//                min_index = j;
//            }
//            //add all Postings to a dictionary for constant access later
//            docs.stream().forEach(posting -> postings.get(postings.size()-1).put(posting.doc,posting));
//        }
//
//        return postings;
//    }

}
