package util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.FSDirectory;
import java.nio.file.Paths;
import java.util.Scanner;

public class RicercaIndice {

    public QueryParser checkField() {

        // Impostazione dell'analyzer per la query
        Scanner scanner = new Scanner(System.in);
        System.out.print("Su quale campo vuoi eseguire la tua ricerca: ");
        String userField = scanner.nextLine();

        // Verifica che il campo selezionato sia effettivo
        try {
            Analyzer analyzerCustom = CustomAnalyzer.builder()
                    .withTokenizer(WhitespaceTokenizerFactory.class)
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .addTokenFilter(WordDelimiterGraphFilterFactory.class)
                    .build();

            if (userField.equals("titolo") || userField.equals("autori")) {
                QueryParser parser = new QueryParser(userField, analyzerCustom);
                return parser;
            } else if (userField.equals("contenuto")) {
                QueryParser parser = new QueryParser(userField, new StandardAnalyzer(new Stopwords().getStopWords()));
                return parser;
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
            String indexPath = "index";

            // Apri l'indice
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher searcher = new IndexSearcher(reader);

            QueryParser parser = checkField(); // check sul campo selezionato

            // Impostazione della query
            Scanner scanner = new Scanner(System.in);
            System.out.print("Digita la tua query: ");
            String q = scanner.nextLine();
            Query query = parser.parse(q);

            // Esegue la ricerca
            TopDocs results = searcher.search(query, 10); // Recupera i primi 10 risultati
            ScoreDoc[] hits = results.scoreDocs;

            // Stampa i risultati
            System.out.println("Numero di risultati: " + hits.length + "\n");
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

                // System.out.println("Contenuto: " + doc.get("contenuto"));
                System.out.println("\n------------------------------------------------\n");
            }
            reader.close();
        } catch(Exception e){
                e.printStackTrace();
        }
    }
}



