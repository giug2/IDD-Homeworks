package org.example;

import util.CreaIndice;
import util.RicercaIndice;
import java.util.Scanner;

public class Main {

    private static CreaIndice creator = new CreaIndice();
    private static RicercaIndice searcher = new RicercaIndice();

    public static void main(String[] args) {
        creator.creaIndice();

        // Loop per ricerche ripetute
        while(true){
            searcher.cercaIndice();

            Scanner scanner = new Scanner(System.in);
            System.out.print("Vuoi continuare? Y/n");
            String exit = scanner.nextLine();

            if (exit.equalsIgnoreCase("n")) {
                System.out.println("Uscita dal programma.");
                break; // Esce dal ciclo
            }
        }
    }
}