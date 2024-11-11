package uni.roma3;

import uni.roma3.util.Indexer;
import uni.roma3.util.Searcher;
import uni.roma3.util.Stats;
import java.util.Scanner;

public class LuceneHW {

    private static Indexer creator = new Indexer();
    private static Searcher searcher = new Searcher();

    public static void main(String[] args) {

        creator.creaIndice();

        new Stats().statisticheIndice();

        // Inizio ricerche tramite prompt
        while(true){
            searcher.cercaIndice();

            Scanner scanner = new Scanner(System.in);
            System.out.print("Vuoi continuare? Y/N:  ");
            String exit = scanner.nextLine();

            if (exit.equalsIgnoreCase("N")) {
                System.out.println("Fine ricerca.");
                break;
            }
        }
    }
}