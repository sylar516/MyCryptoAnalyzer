package cryptoanalyzer;

import java.util.Scanner;

/*
        Программа-криптоанализатор, работающая с шифром Цезаря.
        Функционал программы:
        1. Шифрование текста заданным ключом шифрования;
        2. Расшифровка текста заданным ключом шифрования;
        3. Расшифровка текста путём подбора подходящего ключа шифрования;
        4. Расшифровка текста методом статистического анализа на основе загруженного дополнительного файла с текстом.
*/

public class Runner {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String menu = """
                Выбери действие, введя его номер:
                1) зашифровать текст в файле с помощью ключа
                2) расшифровать текст в файле с помощью ключа
                3) расшифровать текст в файле методом перебора ключей
                4) расшифровать текст в файле методом статистического анализа.
                """;
        System.out.println(menu);
        System.out.println("Введи действие:");

        CryptoAnalyzer cryptoAnalyzer = new CryptoAnalyzer();
        String action = scanner.nextLine();

        switch (action) {
            case "1" -> {
                CryptoAnalyzer.intro(Action.ENCRYPT);
                cryptoAnalyzer.crypt(Action.ENCRYPT);
            }
            case "2" -> {
                CryptoAnalyzer.intro(Action.DECRYPT);
                cryptoAnalyzer.crypt(Action.DECRYPT);
            }
            case "3" -> {
                CryptoAnalyzer.intro(Action.DECRYPTBRUT);
                cryptoAnalyzer.decryptBrut();
            }
            case "4" -> {
                CryptoAnalyzer.intro(Action.DECRYPTSTAT);
                cryptoAnalyzer.decryptStat();
            }
        }
    }
}

