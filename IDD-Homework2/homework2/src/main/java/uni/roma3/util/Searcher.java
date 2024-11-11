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
        Directory dir = null;
        try {
            dir = FSDirectory.open(Paths.get("lucene-index"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        IndexReader reader = DirectoryReader.open(dir);
        totalDocs = reader.numDocs();

        // Verifica che il campo selezionato sia effettivo
        try {
            Analyzer analyzerCustom = CustomAnalyzer.builder()
                    .withTokenizer(WhitespaceTokenizerFactory.class)
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .addTokenFilter(WordDelimiterGraphFilterFactory.class)
                    .build();

            // Query sul campo "titolo"
            QueryParser titoloParser = new QueryParser("titolo", analyzerCustom);

            // Query sul campo "autori"
            QueryParser autoriParser = new QueryParser("autore", analyzerCustom);

            return Pair.of(titoloParser, autoriParser);

        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public QueryParser checkField() {

        // Impostazione dell'analyzer per la query
        Scanner scanner = new Scanner(System.in);
        System.out.print("Su quale campo singolo vuoi eseguire la tua ricerca: ");
        String userField = scanner.nextLine();

        // Verifica che il campo selezionato sia effettivo
        try {
            Analyzer analyzerCustom = CustomAnalyzer.builder()
                    .withTokenizer(WhitespaceTokenizerFactory.class)
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .addTokenFilter(WordDelimiterGraphFilterFactory.class)
                    .build();

            if (userField.equals("titolo") || userField.equals("autori")) {
                QueryParser parserTitoloAutori = new QueryParser(userField, analyzerCustom);
                return parserTitoloAutori;
            } else if (userField.equals("contenuto")) {
                QueryParser parserContenuto = new QueryParser(userField, new StandardAnalyzer(new Stopwords().getStopWords()));
                return parserContenuto;
            } else {
                System.out.println("Campo non valido!");
                return checkField();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void cercaIndice() {
        try {

            // Percorso dell'indice creato in precedenza
            String indexPath = "lucene-index";

            // Apri l'indice
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher searcher = new IndexSearcher(reader);

            Scanner scanner = new Scanner(System.in);
            System.out.print("Vuoi eseguire una ricerca composta (titolo & autore): Y/n ");
            String sceltaComposta = scanner.nextLine();

            if(sceltaComposta.equals("Y")){

                long startTime = System.nanoTime();

                Pair<QueryParser,QueryParser> parserTitoloAutori = checkFields();

                // Impostazione dell'analyzer per la query
                System.out.print("Digita il 'titolo' per la ricerca: ");
                String titoloComposto = scanner.nextLine();

                // Impostazione dell'analyzer per la query
                System.out.print("Digita gli 'autori' per la ricerca: ");
                String autoriComposto = scanner.nextLine();

                Query queryTitolo = parserTitoloAutori.getLeft().parse(titoloComposto);
                Query queryAutori = parserTitoloAutori.getRight().parse(autoriComposto);

                // Combina le query con BooleanQuery
                BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
                booleanQuery.add(queryTitolo, BooleanClause.Occur.MUST); // Il termine deve essere nel titolo
                booleanQuery.add(queryAutori, BooleanClause.Occur.MUST); // L'autore deve essere presente

                // Esegui la ricerca
                ScoreDoc[] hits = searcher.search(booleanQuery.build(), 10).scoreDocs;

                long endTime = System.nanoTime();
                long duration = endTime - startTime;

                System.out.println("Trovati " + hits.length + " documenti.");
                System.out.println("Tempo " + duration);

                // Mostra i risultati
                System.out.println("Risultati della query composta:" + "\n");

                int relevantDocumentsFound = 0;
                long totalDocumentsReturned = hits.length;

                for (ScoreDoc hit : hits) {
                    Document doc = searcher.doc(hit.doc);
                    System.out.println("Titolo: " + doc.get("titolo"));
                    System.out.print("Autori: ");
                    String[] autori = doc.getValues("autori");

                    if(autori != null && autori.length > 0) {
                        System.out.print("[" + autori[0]);
                        String[] autoriNomi = new String[autori.length - 1];
                        System.arraycopy(autori, 1, autoriNomi, 0, autoriNomi.length);
                        for (String a : autoriNomi) {
                            System.out.print(", " + a);
                        }
                        System.out.println("]");
                    }
                    else{
                        System.out.println("Autori non presenti");
                    }

                    System.out.println("Contenuto: " + doc.get("contenuto"));
                    System.out.println("Punteggio: " + hit.score);
                    System.out.println("\n------------------------------------------------\n");

                    String relevantValue = doc.get("relevant");
                    boolean isRelevant = relevantValue != null && Boolean.parseBoolean(relevantValue);

                    if (isRelevant) {
                        relevantDocumentsFound++;
                    }
                }
            } else{
                long startTime = System.nanoTime();
                QueryParser parser = checkField(); // check sul campo selezionato

                // Impostazione della query
                System.out.print("Digita la tua query: ");
                String q = scanner.nextLine();
                Query query = parser.parse(q);

                // Esegue la ricerca
                TopDocs results = searcher.search(query, 10); // Recupera i primi 10 risultati
                ScoreDoc[] hits = results.scoreDocs;

                // Stampa i risultati
                System.out.println("Numero di risultati: " + hits.length + "\n");

                long endTime = System.nanoTime();
                long duration = endTime - startTime;

                System.out.println("Trovati " + hits.length + " documenti.");
                System.out.println("Tempo " + duration);

                // Mostra i risultati
                System.out.println("Risultati della query composta:" + "\n");

                int relevantDocumentsFound = 0;
                long totalDocumentsReturned = hits.length;

                for (ScoreDoc hit : hits) {
                    int docId = hit.doc;
                    Document doc = searcher.doc(docId);
                    System.out.println("Titolo: " + doc.get("titolo"));
                    System.out.print("Autori: ");
                    String[] autori = doc.getValues("autori");

                    if(autori != null && autori.length > 0) {
                        System.out.print("[" + autori[0]);
                        String[] autoriNomi = new String[autori.length - 1];
                        System.arraycopy(autori, 1, autoriNomi, 0, autoriNomi.length);
                        for (String a : autoriNomi) {
                            System.out.print(", " + a);
                        }
                        System.out.println("]");
                    }
                    else{
                        System.out.println("Autori non presenti");
                    }

                    System.out.println("Contenuto: " + doc.get("contenuto"));
                    System.out.println("Punteggio: " + hit.score);
                    System.out.println("\n------------------------------------------------\n");

                    String relevantValue = doc.get("relevant");
                    boolean isRelevant = relevantValue != null && Boolean.parseBoolean(relevantValue);

                    if (isRelevant) {
                        relevantDocumentsFound++;
                    }
                }

                // Calcolo della precisione e del richiamo
                double precision = (totalDocumentsReturned == 0) ? 0 : (double) relevantDocumentsFound / totalDocumentsReturned;
                double recall = (totalDocs == 0) ? 0 : (double) relevantDocumentsFound / totalDocs;

                // Statistiche finali
                System.out.println("\nStatistiche:");
                System.out.println("Tempo di risposta: " + (duration / 1_000_000.0) + " ms");
                System.out.println("Precisione: " + precision);
                System.out.println("Richiamo: " + recall);

            }

            reader.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}