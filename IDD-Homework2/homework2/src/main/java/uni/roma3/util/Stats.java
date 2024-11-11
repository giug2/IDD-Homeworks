package uni.roma3.util;

import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Paths;

public class Stats {

    public void statisticheIndice() {
        String indexPath = "lucene-index"; // Sostituisci con il percorso dell'indice

        try {
            // Apri l'indice
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            IndexReader reader = DirectoryReader.open(directory);

            // Stampa il numero di documenti indicizzati
            int numDocs = reader.numDocs();
            System.out.println("Numero di documenti indicizzati: " + numDocs);

            // Stampa il numero di termini indicizzati per ogni campo
            System.out.println("\nConteggio dei termini per ciascun campo:");
            for (LeafReaderContext leafContext : reader.leaves()) {
                var leafReader = leafContext.reader();

                // Itera su tutti i FieldInfo (rappresentano i campi)
                for (FieldInfo fieldInfo : leafReader.getFieldInfos()) {
                    String fieldName = fieldInfo.name;
                    Terms terms = leafReader.terms(fieldName);

                    if (terms != null) {
                        TermsEnum termsEnum = terms.iterator();
                        int termCount = 0;

                        // Conta i termini per il campo
                        while (termsEnum.next() != null) {
                            termCount++;
                        }
                        System.out.println("Campo: " + fieldName + " - Termini indicizzati: " + termCount);
                    } else {
                        System.out.println("Campo: " + fieldName + " - Nessun termine trovato.");
                    }
                }
            }

            // Chiudi il reader e il directory
            reader.close();
            directory.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}