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
        List<Document> documenti = new ArrayList<>();

        // Carica tutti i file HTML dalla directory specificata
        File directory = new File(directoryPath);
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".html"));

        if (files == null) {
            throw new IOException("La directory specificata non contiene file HTML o non è accessibile.");
        }

        for (File file : files) {

            // Parsing del file HTML, jsoupDoc dedicato al parsing
            org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(file, "UTF-8");

            // Estrai il titolo dall'elemento <h1 class="ltx_title ltx_title_document">
            Element titoloElement = jsoupDoc.selectFirst("h1.ltx_title.ltx_title_document");
            String titolo = (titoloElement != null) ? titoloElement.text() : "Titolo non trovato";

            // Estrai il contenuto testuale rimuovendo i tag HTML
            String contenuto = jsoupDoc.text();

            // Estrai gli autori dall'elemento <div class="ltx_authors">
            Element autoriElement = jsoupDoc.selectFirst("div.ltx_authors");
            List<String> autori = estraiAutori(autoriElement);

            // Crea un oggetto Documento di Lucene con l'aggiunta dei relativi campi
            Document documento = new Document();
            documento.add(new TextField("titolo", titolo, Field.Store.YES));

            // Aggiunta di ciascun autore come un valore separato nel campo "authors"
            for (String autore : autori) {
                documento.add(new TextField("autori", autore, Field.Store.YES));
            }
            documento.add(new TextField("contenuto", contenuto, Field.Store.YES));
            documenti.add(documento);
        }

        return documenti;
    }

    /**
     * Metodo per estrarre i nomi degli autori come lista di stringhe.
     */
    private static List<String> estraiAutori(Element autoriElement) {
        List<String> autoriNomiList = new ArrayList<>();

        String cleanRegex = "[^\\p{L}\\p{M}\\-' ]"; // Regex per la pulizia dei nomi

        if (autoriElement == null) {
            return autoriNomiList;  // Restituisce una lista vuota se non ci sono autori
        }

        // Seleziona tutti gli elementi con classe ltx_personname per ottenere i nomi
        Elements autoriBlocchi = autoriElement.select("span.ltx_personname");

        // Itera su ogni elemento "ltx_personname" trovato
        for (Element autoriBlocco : autoriBlocchi) {

            String htmlBlocco = autoriBlocco.html()
                    .replaceAll("<br[^>]*>", " ")      // Sostituisce i tag <br> con uno spazio
                    .replaceAll("<[^>]+>", " ");       // Rimuove eventuali tag html residui

            // Usa la regex generalizzata per dividere i nomi degli autori
            String[] autoriNomi = htmlBlocco.split("\\s*(,|;|\\s{2,}| |<br.*?>|\\n|\\\"|\\band\\b)\\s*");

            // Modifica ogni nome per rimuovere caratteri non validi e aggiungilo alla lista
            for (String nome : autoriNomi) {

                // Rimuove numeri e caratteri non desiderati, mantenendo solo lettere, trattini e apostrofi
                String nomePulito = nome.replaceAll(cleanRegex, "").trim();

                // Aggiungi il nome alla lista solo se non è vuoto e se sembra un nome valido
                if (!nomePulito.isEmpty()) {
                    autoriNomiList.add(nomePulito);
                }
            }
        }
        return autoriNomiList;
    }
}