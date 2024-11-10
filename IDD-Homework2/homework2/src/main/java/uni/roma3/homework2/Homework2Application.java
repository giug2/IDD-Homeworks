package uni.roma3.homework2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.Locale;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@SpringBootApplication
public class Homework2Application {

    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        String indexPath = "lucene-index";

        File folder = new File(indexPath);
        if(!(folder.exists() && folder.isDirectory())){
            try {
                // Memorizzo l'istante di inizio dell'indicizzazione
                long startTime = System.currentTimeMillis();
                Directory directory = FSDirectory.open(Paths.get(indexPath));
                Map<String, Analyzer> analyzerPerField = new HashMap<>();
                analyzerPerField.put("filename", CustomAnalyzer.getSimpleAnalyzer());
                analyzerPerField.put("title", CustomAnalyzer.getTitleAnalyzer());
                analyzerPerField.put("content", CustomAnalyzer.getContentAnalyzer());
                analyzerPerField.put("authors", CustomAnalyzer.getSimpleAnalyzer());
                analyzerPerField.put("abstract", CustomAnalyzer.getAbstractAnalyzer());

                PerFieldAnalyzerWrapper analyzerWrapper = new PerFieldAnalyzerWrapper(CustomAnalyzer.getContentAnalyzer(), analyzerPerField);

                IndexWriterConfig config = new IndexWriterConfig(analyzerWrapper);
                config.setCodec(new SimpleTextCodec());
                IndexWriter writer = new IndexWriter(directory, config);

                File htmlDir = new File("all_htmls");
                File[] htmlFiles = htmlDir.listFiles((dir, name) -> name.endsWith(".html"));

                if (htmlFiles != null) {
                    for (File htmlFile : htmlFiles) {
                        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(htmlFile, "UTF-8");

                        // Ottieni il percorso completo del progetto
                        String projectPath = System.getProperty("user.dir");

                        // Ottieni il percorso della cartella "all_htmls" e il nome del file
                        String cartellaFiles = htmlFile.getParent();
                        String filename = htmlFile.getName();

                        // Componi il percorso completo del file
                        String fullPath = projectPath + File.separator + cartellaFiles + File.separator + filename;

                        // Codifica il percorso per essere un URL valido (gestisce caratteri speciali)
                        String encodedPath = URLEncoder.encode(fullPath, "UTF-8");

                        // Gestione delle barre rovesciate che sono specifiche del sistema Windows
                        encodedPath = encodedPath.replace("%5C", "/");

                        // Aggiungi il prefisso "file://"
                        //filename = "file:///" + encodedPath;

                        filename = "http://localhost:8080/all_htmls/" + filename;

                        String title = jsoupDoc.title();
                        String content = jsoupDoc.body().text();

                        // Estrai autori da <div class="ltx_authors">...</div>
                        Elements authorsElements = jsoupDoc.select("div.ltx_authors span.ltx_personname");
                        List<String> authors = new ArrayList<>();
                        for (Element authorElement : authorsElements) {
                            String authorName = authorElement.text();
                            authors.add(authorName);
                        }
                        // Unisci i nomi degli autori in una singola stringa, separati da una virgola
                        String authorString = String.join(", ", authors);

                        // Estrai l'abstract, assicurati che ci sia un campo per l'abstract
                        String abstractText = jsoupDoc.select("div.ltx_abstract").text();

                        // Stampa riassuntiva del documento
                        System.out.println("Documento indicizzato: " + filename);
                        System.out.println("Titolo: " + title);
                        System.out.println("Autori: " + authorString);
                        System.out.println("Abstract: " + (abstractText.length() > 100 ? abstractText.substring(0, 100) + "..." : abstractText));
                        System.out.println("Contenuto: " + (content.length() > 100 ? content.substring(0, 100) + "..." : content));

                        Document doc = new Document();
                        doc.add(new TextField("filename", filename, Field.Store.YES));
                        doc.add(new TextField("title", title, Field.Store.YES));
                        doc.add(new TextField("content", content, Field.Store.YES));
                        doc.add(new TextField("authors", authorString, Field.Store.YES));
                        doc.add(new TextField("abstract", abstractText, Field.Store.YES));
                        writer.addDocument(doc);
                    }
                }

                writer.commit();
                writer.close();
                directory.close();
                System.out.println("Indicizzazione completata!");
                // Calcola il tempo impiegato per l'indicizzazione
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                System.out.println("Tempo impiegato per l'indicizzazione: " + elapsedTime + " ms");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // run springboot application
        SpringApplication.run(Homework2Application.class, args);
    }
}
