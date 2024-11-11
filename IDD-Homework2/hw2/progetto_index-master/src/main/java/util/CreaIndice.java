package util;

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
import java.util.Locale;
import java.util.Map;

public class CreaIndice {

    public void creaIndice() {
        Locale.setDefault(Locale.ENGLISH);
        try {
            System.out.println("Indicizzazione inizio!");
            // Memorizzo l'istante di inizio dell'indicizzazione
            long startTime = System.currentTimeMillis();

            // Definiamo dove salvare il nostro indice Lucene
            Directory directory = FSDirectory.open(Paths.get("index"));

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

            // Definiamo la configurazione dell' IndexWriter
            IndexWriterConfig config = new IndexWriterConfig(perFieldAnalyzer);
            config.setCodec(new SimpleTextCodec());
            IndexWriter writer = new IndexWriter(directory, config);

            // "C:/Users/hp/papers/urls_htmls_tables/test_one_doc" path per i test su 10 documenti
            // Recuperiamo i documenti
            List<Document> test_documenti = CreaDocumenti.parseHtmlFilesInDirectory("C:\\Users\\gi.gaglione\\Desktop\\IDD-Homeworks\\IDD-Homeworks\\IDD-Homework2\\urls_htmls_tables\\all_htmls");

            for(Document documento : test_documenti){
                System.out.println("sono qui");
                writer.addDocument(documento); // Indicizzazione dei documenti
            }

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
