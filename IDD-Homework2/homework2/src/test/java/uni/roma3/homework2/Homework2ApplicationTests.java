package uni.roma3.homework2;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.lucene.search.similarities.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.springframework.boot.test.context.SpringBootTest;
import uni.roma3.homework2.controller.LuceneService;
import uni.roma3.homework2.model.SearchResult;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.BooleanQuery;


@SpringBootTest
public class Homework2ApplicationTests {
    private LuceneService luceneService = new LuceneService();

    // Test for a complex TermQuery on a very specific research topic in the title
    @Test
    public void testIndexingAndSearchTQ() throws Exception {
        String query = "Efficient approximation algorithms for NP-hard problems";
        List<SearchResult> results = luceneService.search("title", query);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        printResults(results);
    }

    // Test for a PhraseQuery in the content field with a very specific multi-term phrase
    @Test
    public void testIndexingAndSearchPQ() throws Exception {
        String query = "\"Distributed ledger technology in financial systems\"";
        List<SearchResult> results = luceneService.search("content", query);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        printResults(results);
    }

    // Test for PhraseQuery with slop on a highly specific research topic in the content
    @Test
    public void testIndexingAndSearchPQWithSlop() throws Exception {
        String query = "\"Blockchain consensus mechanisms\"~2"; // slop of 2 for flexibility in word order
        List<SearchResult> results = luceneService.search("content", query);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        printResults(results);
    }


    // Test for BooleanQuery combining title and content with complex conditions
    @Test
    public void testIndexingAndSearchComplexBQ() throws Exception {
        Query phraseQueryTitle = new PhraseQuery("title", "Quantum", "algorithms");
        Query termQueryContent1 = new TermQuery(new Term("content", "Shor's algorithm"));
        Query termQueryContent2 = new TermQuery(new Term("content", "Grover's algorithm"));

        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(phraseQueryTitle, BooleanClause.Occur.MUST)
                .add(termQueryContent1, BooleanClause.Occur.SHOULD)
                .add(termQueryContent2, BooleanClause.Occur.SHOULD)
                .build();

        List<SearchResult> results = luceneService.search("content", booleanQuery.toString());
        assertNotNull(results);
        assertFalse(results.isEmpty());
        printResults(results);
    }

    // Test for ranking with different similarity metrics (e.g., Cosine, Euclidean, etc.)
    @Test
    public void testRankingWithDifferentSimilarities() throws Exception {
        String query = "artificial intelligence in healthcare";

        //Similarity
        Similarity similarity = new ClassicSimilarity();
        List<SearchResult> resultsCosine = luceneService.searchWithSimilarity("content", query, similarity);

        // Using euclidean Similarity
        similarity = new MultiSimilarity(new Similarity[] { new ClassicSimilarity(), new BM25Similarity(), new BooleanSimilarity(), new LMDirichletSimilarity()});
        List<SearchResult> resultsMultisimilarity = luceneService.searchWithSimilarity("content", query, similarity);


        // Assert that both rankings return results
        assertNotNull(resultsCosine);
        assertFalse(resultsCosine.isEmpty());

        assertNotNull(resultsMultisimilarity);
        assertFalse(resultsMultisimilarity.isEmpty());

        System.out.println("\nResults using Cosine Similarity:");
        printResults(resultsCosine);

        System.out.println("\nResults using MultiSimilarity ( ClassicSimilarity, BM25Similarity, BooleanSimilarity, LMDirichletSimilarity):");
        printResults(resultsMultisimilarity);
    }

    // Test for PhraseQuery with a query parser for searching content
    @Test
    public void testIndexingAndSearchQP() throws Exception {
        String query = "\"Neural networks in natural language processing\"";

        // Using a query parser to interpret and execute the search
        QueryParser parser = new QueryParser("content", new StandardAnalyzer());
        Query parsedQuery = parser.parse(query);

        List<SearchResult> results = luceneService.search("content", parsedQuery.toString());
        assertNotNull(results);
        assertFalse(results.isEmpty());
        printResults(results);
    }

    // Test for querying with new objects like the use of Similarity
    @Test
    public void testIndexingAndSearchWithSimilarity() throws Exception {
        String query = "quantum computing";

        // Using a custom similarity (e.g., ClassicSimilarity) for ranking
        Similarity similarity = new ClassicSimilarity();
        List<SearchResult> results = luceneService.searchWithSimilarity("content", query, similarity);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        printResults(results);
    }

    // Test for a PhraseQuery with multi-term phrase including specific mathematical research topic
    @Test
    public void testIndexingAndSearchPQ2() throws Exception {
        String query = "\"Approximation schemes for scheduling problems\"";
        List<SearchResult> results = luceneService.search("content", query);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        printResults(results);
    }

    // Test for BooleanQuery with combination of specific terms and their negation (advanced search)
    @Test
    public void testIndexingAndSearchNegationBQ() throws Exception {
        Query termQuery1 = new TermQuery(new Term("content", "dynamic programming"));
        Query termQuery2 = new TermQuery(new Term("content", "greedy algorithms"));

        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(termQuery1, BooleanClause.Occur.MUST)
                .add(termQuery2, BooleanClause.Occur.MUST_NOT)
                .build();

        List<SearchResult> results = luceneService.search("content", booleanQuery.toString());
        assertNotNull(results);
        assertFalse(results.isEmpty());
        printResults(results);
    }

    // Test for a PhraseQuery with multiple terms and a complex scientific topic
    @Test
    public void testIndexingAndSearchScientificPQ() throws Exception {
        String query = "\"High-dimensional data visualization using t-SNE\"";
        List<SearchResult> results = luceneService.search("content", query);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        printResults(results);
    }


    // Helper method to print results in a readable format
    private void printResults(List<SearchResult> results) {
        if (results.isEmpty()) {
            System.out.println("No results found.\n");
        } else {
            System.out.println("\n\n");
            for (int i = 0; i < results.size(); i++) {
                SearchResult result = results.get(i);
                System.out.println("Result " + (i + 1) + ":");
                System.out.println("Score: " + result.getScore());
                System.out.println("Paper: " + result.getFilename());
                System.out.println("Title: " + result.getTitle());
                System.out.println("Abstract: " + result.getAbstractText());
                System.out.println("---------------------------------------");
            }
        }
    }

}