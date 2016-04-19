package searchengine.searcher;

import com.sun.tools.classfile.Opcode;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.parser.Tokens;
import searchengine.indexer.Index;
import searchengine.indexer.Posting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import searchengine.indexer.Index;

/**
 *
 */
public class Searcher {

    private Index index;

    public Searcher(Index index) {
        this.index = index;
    }

    public void search(String query) throws IOException {
        List<Token> tokens = new Tokenizer(query, index).getTokens();
        ArrayList<Posting> matched_documents = new ArrayList<Posting>();
        for (int i = 0; i < tokens.size(); ++i) {
            int min_index = 0;
            int min_size = -1;
            ArrayList<HashMap<Integer, Posting>> postings = new ArrayList<HashMap<Integer, Posting>>();
            for (int j = 0; j < tokens.get(i).words.size(); ++ j) {
                HashMap<Integer, Posting> matched = new HashMap<>();
                postings.add(matched);
                //get all of the docs matching the word in the phrase
                ArrayList<Posting> docs = index.getDocs(tokens.get(i).getWords().get(j));
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
            for (Posting doc_match : postings.get(min_index).values()) {
                boolean found_valid_position_difference = false;
                for (int j = 0; j < postings.size(); ++j) {
                    //make sure we don't compare against the smallest posting list because thats what we're iterating over
                    if (j != min_index) {
                        Posting doc_in_other_posting = postings.get(j).get(doc_match.doc);
                        if (null != doc_in_other_posting) {
                            //Assumes posting stores words position in an ordered array
                            //Since we parse a document in order, this should remain a valid assumption
                            int doc_match_index = 0;
                            int other_doc_index = 0;
                            //noe we have to iterate through the list of word occurances to verify that the words in each posting occur in the same order as in the query
                            while (doc_match_index < doc_match.positions.size() && other_doc_index < doc_in_other_posting.positions.size()) {

                                if (doc_match.positions.get(doc_match_index) - doc_in_other_posting.positions.get(other_doc_index) == postings.indexOf(doc_match) - postings.indexOf(doc_in_other_posting)) {
                                    found_valid_position_difference = true;
                                }
                            }
                        }
                    }
                }
                if (found_valid_position_difference) {
                    result.add(doc_match);
                }
            }
            for (Posting p : result) {
                System.out.println(index.getWebPage(p.doc));
            }
        }
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
