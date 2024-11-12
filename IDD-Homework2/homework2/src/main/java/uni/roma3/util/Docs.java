package uni.roma3.util;

import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.select.Elements;

public class Docs {

    public static List<Document> parseHtmlFilesInDirectory(String directoryPath) throws IOException {
        List<Document> docs = new ArrayList<>();

        // Carica tutti i file HTML dalla directory specificata
        File directory = new File(directoryPath);
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".html"));

        if (files == null) {
            throw new IOException("La directory non è accessibile oppure al suo interno non ci sono file di tipo .html.");
        }

        for (File file : files) {

            // Legge e analizza il file HTML
            org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(file, "UTF-8");

            // Estrai il titolo dall'elemento
            Element titleElem = jsoupDoc.selectFirst("h1.ltx_title.ltx_title_document");
            String title = (titleElem != null) ? titleElem.text() : "Titolo non trovato";

            // Estrai gli autori dall'elemento
            Element authorElem = jsoupDoc.selectFirst("div.ltx_authors");
            List<String> authors = extractAuthors(authorElem);

            // Estrai il contenuto testuale rimuovendo i tag HTML
            String content = jsoupDoc.text();

            // Crea un oggetto Documento di Lucene
            Document document = new Document();

            document.add(new TextField("title", title, Field.Store.YES));
            for (String author : authors) {
                document.add(new TextField("authors", author, Field.Store.YES));
            }
            document.add(new TextField("content", content, Field.Store.YES));

            docs.add(document);
        }

        return docs;
    }

    /** Estrazione dei nomi degli autori
     */
    private static List<String> extractAuthors(Element authorsElem) {
        List<String> nameAuthors = new ArrayList<>();

        String cleanRegex = "[^\\p{L}\\p{M}\\-' ]";

        if (authorsElem == null) {
            return nameAuthors;
        }

        // Trova gli elementi con classe ltx_personname
        Elements authorTags = authorsElem.select("span.ltx_personname");

        // Itera per ogni elemento "ltx_personname" trovato
        for (Element tag : authorTags) {

            // Pulisce da eventuali tag residui
            String htmlTag = tag.html()
                    .replaceAll("<br[^>]*>", " ")
                    .replaceAll("<[^>]+>", " ");

            String[] names = htmlTag.split("\\s*(,|;|\\s{2,}| |<br.*?>|\\n|\\\"|\\band\\b)\\s*");

            // Modifica ogni nome per pulirlo e poi lo aggiunge nella lista
            for (String name : names) {

                String defName = name.replaceAll(cleanRegex, "").trim();

                if (!defName.isEmpty()) {
                    nameAuthors.add(defName);
                }
            }
        }
        return nameAuthors;
    }
}