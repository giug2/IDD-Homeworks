package uni.roma3.util;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Indexer {

    public void creaIndice() {
        try {
            System.out.println("Indicizzazione iniziata!");
            // Memorizza l'istante di inizio dell'indicizzazione
            long startTime = System.currentTimeMillis();

            // Definisce la directory dove salvare gli index
            Directory directory = FSDirectory.open(Paths.get("lucene-index"));

            Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();

            Analyzer analyzerCustom = CustomAnalyzer.builder()
                    .withTokenizer(WhitespaceTokenizerFactory.class)
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .addTokenFilter(WordDelimiterGraphFilterFactory.class)
                    .build();

            perFieldAnalyzers.put("titolo", analyzerCustom);
            perFieldAnalyzers.put("autori", analyzerCustom);
            perFieldAnalyzers.put("contenuto", new StandardAnalyzer(new Stopwords().getStopWords()));

            Analyzer perFieldAnalyzer = new PerFieldAnalyzerWrapper(new EnglishAnalyzer(), perFieldAnalyzers);

            // Definisce la configurazione dell' IndexWriter
            IndexWriterConfig config = new IndexWriterConfig(perFieldAnalyzer);
            config.setCodec(new SimpleTextCodec());
            IndexWriter writer = new IndexWriter(directory, config);

            // Recupera i documenti
            List<Document> test_documenti = Docs.parseHtmlFilesInDirectory("C:\\Users\\gi.gaglione\\Desktop\\IDD-Homeworks\\IDD-Homeworks\\IDD-Homework2\\urls_htmls_tables\\all_htmls");

            System.out.println("Inizio ciclo for");
            // Ciclo per indicizzare tutti i documenti
            for(Document documento : test_documenti){
                writer.addDocument(documento);
            }
            System.out.println("Fine ciclo for");

            writer.commit();
            writer.close();
            System.out.println("Indicizzazione completata!");

            // Calcola il tempo impiegato per l'indicizzazione
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            System.out.println("Tempo impiegato per l'indicizzazione: " + elapsedTime + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}