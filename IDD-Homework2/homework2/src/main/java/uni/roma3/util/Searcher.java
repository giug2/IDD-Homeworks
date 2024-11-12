package uni.roma3.util;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Searcher {
    private long totalDocs;

    public Pair<QueryParser,QueryParser> checkFields() throws IOException {
        Directory dir = FSDirectory.open(Paths.get("lucene-index"));

        IndexReader reader = DirectoryReader.open(dir);
        totalDocs = reader.numDocs();

        // Verifica che il campo selezionato sia giusto
        try {
            Analyzer analyzerCustom = CustomAnalyzer.builder()
                    .withTokenizer(WhitespaceTokenizerFactory.class)
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .addTokenFilter(WordDelimiterGraphFilterFactory.class)
                    .build();

            // Query sul campo "titolo"
            QueryParser titleParser = new QueryParser("title", analyzerCustom);

            // Query sul campo "autori"
            QueryParser authorParser = new QueryParser("authors", analyzerCustom);

            return Pair.of(titleParser, authorParser);

        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public QueryParser checkField() {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Su quale campo vuoi eseguire la tua ricerca: ");
        String userField = scanner.nextLine();

        // Verifica che il campo selezionato sia giusto
        try {
            Analyzer analyzerCustom = CustomAnalyzer.builder()
                    .withTokenizer(WhitespaceTokenizerFactory.class)
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .addTokenFilter(WordDelimiterGraphFilterFactory.class)
                    .build();

            if (userField.equals("title") || userField.equals("authors")) {
                QueryParser parserDouble = new QueryParser(userField, analyzerCustom);
                return parserDouble;
            } else if (userField.equals("content")) {
                QueryParser parserContent = new QueryParser(userField, new StandardAnalyzer(new Stopwords().getStopWords()));
                return parserContent;
            } else {
                System.out.println("Campo non valido!");
                return checkField();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void searchIndex() {
        try {

            String indexPath = "lucene-index";

            // Apri l'indice
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher searcher = new IndexSearcher(reader);

            Scanner scanner = new Scanner(System.in);
            System.out.print("Vuoi eseguire una ricerca composta inserendo sia titlo sia autore? Y/N ");
            String sceltaComposta = scanner.nextLine();

            // Tempistiche
            long startTime = System.nanoTime();
            int relevantDocumentsFound = 0;
            long totalDocumentsReturned = 0;

            if(sceltaComposta.equals("Y")){

                Pair<QueryParser,QueryParser> parserDouble = checkFields();

                // Impostazione dell'analyzer per la query
                System.out.print("Digita il 'titolo' per la ricerca: ");
                String title = scanner.nextLine();

                // Impostazione dell'analyzer per la query
                System.out.print("Digita gli 'autori' per la ricerca: ");
                String author = scanner.nextLine();

                Query queryTitle = parserDouble.getLeft().parse(title);
                Query queryAuthor = parserDouble.getRight().parse(author);

                // Combina le query con BooleanQuery
                BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
                booleanQuery.add(queryTitle, BooleanClause.Occur.MUST);
                booleanQuery.add(queryAuthor, BooleanClause.Occur.MUST);

                // Esegui la ricerca
                ScoreDoc[] hits = searcher.search(booleanQuery.build(), 10).scoreDocs;

                System.out.println("Trovati " + hits.length + " documenti.");

                // Mostra i risultati
                System.out.println("Risultati della query composta:" + "\n");

                totalDocumentsReturned = hits.length;

                for (ScoreDoc hit : hits) {
                    Document doc = searcher.doc(hit.doc);
                    System.out.println("Titolo: " + doc.get("title"));

                    System.out.print("Autori: ");
                    String[] authors = doc.getValues("authors");

                    if(authors != null && authors.length > 0) {
                        System.out.print("[" + authors[0]);
                        String[] authorsName = new String[authors.length - 1];
                        System.arraycopy(authors, 1, authorsName, 0, authorsName.length);
                        for (String a : authorsName) {
                            System.out.print(", " + a);
                        }
                        System.out.println("]");
                    }
                    else{
                        System.out.println("Autori non presenti.");
                    }

                    String content = doc.get("content");
                    if (content != null && content.length() > 100) {
                        System.out.println("Contenuto: " + content.substring(0, 100));
                    } else {
                        System.out.println("Contenuto: " + content);
                    }

                    System.out.println("Score: " + hit.score);
                    System.out.println("\n-------------------------------------------------------\n");

                    String relevantValue = doc.get("relevant");
                    boolean isRelevant = relevantValue != null && Boolean.parseBoolean(relevantValue);

                    if (isRelevant) {
                        relevantDocumentsFound++;
                    }
                }
            } else{

                QueryParser parser = checkField();

                // Impostazione della query
                System.out.print("Digita la tua query: ");
                String q = scanner.nextLine();
                Query query = parser.parse(q);

                // Esegue la ricerca
                TopDocs results = searcher.search(query, 10);
                ScoreDoc[] hits = results.scoreDocs;

                // Stampa i risultati
                System.out.println("Numero di risultati: " + hits.length + "\n");

                System.out.println("Trovati " + hits.length + " documenti.");

                // Mostra i risultati
                System.out.println("Risultati della query:" + "\n");

                totalDocumentsReturned = hits.length;

                for (ScoreDoc hit : hits) {
                    int docId = hit.doc;
                    Document doc = searcher.doc(docId);
                    System.out.println("Titolo: " + doc.get("title"));
                    System.out.print("Autori: ");
                    String[] authors = doc.getValues("authors");

                    if(authors != null && authors.length > 0) {
                        System.out.print("[" + authors[0]);
                        String[] authorsName = new String[authors.length - 1];
                        System.arraycopy(authors, 1, authorsName, 0, authorsName.length);
                        for (String a : authorsName) {
                            System.out.print(", " + a);
                        }
                        System.out.println("]");
                    }
                    else{
                        System.out.println("Autori non presenti.");
                    }

                    String content = doc.get("content");
                    if (content != null && content.length() > 100) {
                        System.out.println("Contenuto: " + content.substring(0, 100));
                    } else {
                        System.out.println("Contenuto: " + content);
                    }

                    System.out.println("Score: " + hit.score);
                    System.out.println("\n-------------------------------------------------------\n");

                    String relevantValue = doc.get("relevant");
                    boolean isRelevant = relevantValue != null && Boolean.parseBoolean(relevantValue);

                    if (isRelevant) {
                        relevantDocumentsFound++;
                    }
                }
            }

            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            // Calcolo della precisione e del richiamo
            double precision = (totalDocumentsReturned == 0) ? 0 : (double) relevantDocumentsFound / totalDocumentsReturned;
            double recall = (totalDocs == 0) ? 0 : (double) relevantDocumentsFound / totalDocs;

            // Statistiche finali
            System.out.println("\nStatistiche:");
            System.out.println("Tempo di risposta: " + (duration / 1_000_000.0) + " ms");
            System.out.println("Precisione: " + precision);
            System.out.println("Richiamo: " + recall);

            reader.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}