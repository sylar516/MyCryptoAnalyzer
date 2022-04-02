package cryptoanalyzer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CryptoAnalyzer {

    private static final String alphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя.,\":-!? ";
    private static int key;
    private static String inputFileName;
    private static String outputFileName;
    private static String inputStatFileName;
    private static StringBuilder stringBuilder;

    public static void intro(Action action) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введи полный путь к файлу, содержимое которого хочешь " + (action == Action.ENCRYPT ? "зашифровать:" : "расшифровать:"));
        inputFileName = scanner.nextLine();
        switch (action) {
            case ENCRYPT, DECRYPT -> {
                System.out.println("Введи ключ шифрования:");
                key = scanner.nextInt();
                scanner.nextLine();
            }
            case DECRYPTSTAT -> {
                System.out.println("Введи полный путь к файлу, в котором содержится текст для статистики:");
                inputStatFileName = scanner.nextLine();
            }
        }
        System.out.println("Введи полный путь к файлу, в который хочешь записать " + (action == Action.ENCRYPT ? "зашифрованный текст:" : "расшифрованный текст:"));
        outputFileName = scanner.nextLine();
    }

    public void crypt(Action action) { //метод для шифрования и дешифровки текста с заданным ключом
        stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFileName)); BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName))) {
            while (bufferedReader.ready()) {
                Character[] characters = bufferedReader.readLine().chars().mapToObj(value -> (char) value).toArray(Character[]::new);
                List<Character> characters1 = Arrays.stream(characters).map(character -> {
                    int index = alphabet.indexOf(Character.toLowerCase(character));
                    if (index == -1) return character;
                    int shift = action == Action.ENCRYPT ? (index + key) % alphabet.length() : (index - key) % alphabet.length();
                    shift = shift < 0 ? shift + alphabet.length() : shift;
                    return alphabet.charAt(shift);
                }).collect(Collectors.toList());
                characters1.forEach(character -> stringBuilder.append(character));
                stringBuilder.append("\n");
            }
            if(action != Action.DECRYPTBRUT) bufferedWriter.write(stringBuilder.toString());
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }

    public void crypt(Action action, int key) {
        CryptoAnalyzer.key = key;
        crypt(action);
    }

    public void decryptBrut() { //метод для дешифровки текста путём подбора подходящего ключа шифрования
        List<String> decryptText;
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName))) {
            for (int key = 1; key < alphabet.length(); key++) {
                crypt(Action.DECRYPTBRUT, key);
                decryptText = new ArrayList<>(Arrays.asList(stringBuilder.toString().split("\n")));

                Stream<String> decryptTextStream1 = decryptText.stream().filter(string -> !string.matches("(.*)[a-zA-Z](.*)"));
                Stream<String> decryptTextStream2 = decryptText.stream().filter(string -> !string.matches("(.*)[a-zA-Z](.*)"));
                Stream<String> decryptTextStream3 = decryptText.stream().filter(string -> !string.matches("(.*)[a-zA-Z](.*)"));

                boolean isCorrectLengthWord = decryptTextStream1.allMatch(string -> { //Переменная, отображающая наличие/отсутствие в расшифрованном тексте слов с длинной более 25 символов
                    String[] stringsLength = string.split(" ");
                    for(String subString : stringsLength) {
                        if (subString.length() > 25) return false;
                    }
                    return true;
                });

                int countWords = decryptTextStream2.map(string -> string.split(" ").length).reduce(Integer::sum).get(); //Переменная для хранения количества слов в расшифрованном тексте
                int notCorrectPunct = decryptTextStream3.map(string -> string.split("[?!.]")) //Переменная для хранения количества ситуаций, когда после знаков препинания не было пробела в расшифрованном тексте
                        .filter(strings -> {
                            for (String string : strings) {
                                if (strings.length == 1 | string.isEmpty()) return false;
                            } return true;
                        })
                        .map(strings -> {
                            int countErrors = 0;
                            for (String string : strings)
                                if(!string.startsWith(" ")) countErrors++;
                            return countErrors;
                        }).reduce(Integer::sum).get();
                boolean isCorrectPunct = notCorrectPunct <= countWords / 10; //Переменная, которая показывает, прошёл ли текст проверку по наличию пробелов после знаков препинания

                if (isCorrectLengthWord & isCorrectPunct) {
                    System.out.println("ключ подобран - " + key);
                    break;
                }
                decryptText.clear();
            }
            bufferedWriter.write(stringBuilder.toString());
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }

    public void decryptStat() { //метод для дешифровки текста с помощью статистического анализа на основе загруженного дополнительного файла с текстом
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName))) {

            stringBuilder = new StringBuilder();
            List<String> inputData = Files.readAllLines(Path.of(inputFileName));

            Map<Character, Integer> inputChars = new HashMap<>();
            Files.readAllLines(Path.of(inputFileName)).forEach(string -> { for(char ch : string.toLowerCase().toCharArray()) inputChars.merge(ch, 1, Integer::sum); });
            List<Map.Entry<Character, Integer>> inputList = new ArrayList<>(inputChars.entrySet());
            inputList.sort((o1, o2) -> o2.getValue() - o1.getValue());

            Map<Character, Integer> statChars = new HashMap<>();
            Files.readAllLines(Path.of(inputStatFileName)).forEach(string -> { for(char ch : string.toLowerCase().toCharArray()) statChars.merge(ch, 1, Integer::sum); });
            List<Map.Entry<Character, Integer>> statList = new ArrayList<>(statChars.entrySet());
            statList.sort((o1, o2) -> o2.getValue() - o1.getValue());

            HashMap<Character, Character> totalMap = new HashMap<>();
            for (int i = 0; i < inputList.size() & i < statList.size(); i++) {
                totalMap.put(inputList.get(i).getKey(), statList.get(i).getKey());
            }
            inputData.forEach(string -> {
                char[] chars = string.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if (totalMap.containsKey(chars[i])) {
                        chars[i] = totalMap.get(chars[i]);
                    }
                }
                stringBuilder.append(new String(chars)).append("\n");
            });

            bufferedWriter.write(stringBuilder.toString());
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }
}

