import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Лабораторна робота 2.2 — Дослідження алгоритмів ідентифікації
 *
 * Синтаксична будова слова:
 *   Починається з '#', далі цифри 0–9 (необов'язково),
 *   потім '%', '*' або послідовність A–Z, закінчується '#'.
 *
 * ───────────────────────────────────────────────────────────────
 * ЗАВДАННЯ 1: пошук у файлі за регулярним виразом
 * ЗАВДАННЯ 2: скінченний автомат із розпізнаванням з клавіатури
 * ───────────────────────────────────────────────────────────────
 *
 * ГРАФ АВТОМАТА:
 *
 *   [старт] ──'#'──► [S1] ──'0'-'9'──► [S2] ─┐
 *                      │                  │    │ '0'-'9'
 *                      │           ┌──────┘    │
 *                    '%','*'  '%','*','A'-'Z'  ◄┘
 *                      │           │
 *                      ▼           ▼
 *                    [S3]        [S4] ◄──'A'-'Z'──┐
 *                      │           │              │
 *                      └────'#'────┘              S4
 *                              ▼
 *                            [S5]  ← допускальний стан
 *
 * ТАБЛИЦЯ ПЕРЕХОДІВ δ(стан, символ):
 *
 * | Стан | '#' | '0'-'9' | '%' | '*' | 'A'-'Z' | інше |
 * |------|-----|---------|-----|-----|---------|------|
 * | S0   | S1  | ERR     | ERR | ERR | ERR     | ERR  |
 * | S1   | ERR | S2      | S3  | S3  | S4      | ERR  |
 * | S2   | ERR | S2      | S3  | S3  | S4      | ERR  |
 * | S3   | S5  | ERR     | ERR | ERR | ERR     | ERR  |
 * | S4   | S5  | ERR     | ERR | ERR | S4      | ERR  |
 * | S5   | ERR | ERR     | ERR | ERR | ERR     | ERR  |
 * | ERR  | ERR | ERR     | ERR | ERR | ERR     | ERR  |
 */
public class Lab22 {

    // ================================================================
    // ЗАВДАННЯ 1 — пошук за регулярним виразом
    // ================================================================

    private static final String REGEX = "^#\\d*(%|\\*|[A-Z]+)#$";

    private static void runTask1() {
        String filename = "words.txt";
        List<String> matched = new ArrayList<>();

        Pattern pattern = Pattern.compile(REGEX);

        System.out.println("=== Завдання 1: пошук у файлі ===");
        System.out.println("Регулярний вираз: " + REGEX);
        System.out.println("Файл: " + filename);
        System.out.println("----------------------------------");

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                String word = line.trim();
                if (word.isEmpty()) continue;

                Matcher matcher = pattern.matcher(word);
                if (matcher.matches()) {
                    matched.add(word);
                    System.out.printf("[%2d] %-20s  ✓ ВІДПОВІДАЄ%n", lineNum, word);
                } else {
                    System.out.printf("[%2d] %-20s  ✗ не відповідає%n", lineNum, word);
                }
            }
        } catch (IOException e) {
            System.err.println("Помилка читання файлу: " + e.getMessage());
            return;
        }

        System.out.println("----------------------------------");
        System.out.println("Знайдено: " + matched.size() + " слів → " + matched);
    }

    // ================================================================
    // ЗАВДАННЯ 2 — скінченний автомат
    // ================================================================

    // Перелічуваний тип для станів автомата
    enum State {
        S0,   // початковий стан
        S1,   // прочитано перший '#'
        S2,   // читаємо цифри 0–9
        S3,   // прочитано '%' або '*'
        S4,   // читаємо великі літери A–Z
        S5,   // прочитано завершальний '#' — допускальний стан
        ERR   // стан помилки
    }

    /** Функція переходу δ(state, ch) — реалізована через switch */
    private static State transition(State state, char ch) {
        switch (state) {
            case S0:
                switch (ch) {
                    case '#': return State.S1;
                    default:  return State.ERR;
                }

            case S1:
                if (ch >= '0' && ch <= '9') return State.S2;
                if (ch >= 'A' && ch <= 'Z') return State.S4;
                switch (ch) {
                    case '%': return State.S3;
                    case '*': return State.S3;
                    default:  return State.ERR;
                }

            case S2:
                if (ch >= '0' && ch <= '9') return State.S2;
                if (ch >= 'A' && ch <= 'Z') return State.S4;
                switch (ch) {
                    case '%': return State.S3;
                    case '*': return State.S3;
                    default:  return State.ERR;
                }

            case S3:
                switch (ch) {
                    case '#': return State.S5;
                    default:  return State.ERR;
                }

            case S4:
                if (ch >= 'A' && ch <= 'Z') return State.S4;
                switch (ch) {
                    case '#': return State.S5;
                    default:  return State.ERR;
                }

            case S5:
                return State.ERR;

            case ERR:
            default:
                return State.ERR;
        }
    }

    /** Розпізнавання рядка автоматом з покроковим виведенням */
    private static boolean recognize(String input) {
        State state = State.S0;

        System.out.println("\nТрасування для: \"" + input + "\"");
        System.out.printf("%-5s | %-10s | %-10s%n", "Крок", "Символ", "Стан");
        System.out.println("-".repeat(32));
        System.out.printf("%-5s | %-10s | %-10s%n", "0", "—", state);

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            state = transition(state, ch);
            System.out.printf("%-5d | %-10s | %-10s%n", i + 1, "'" + ch + "'", state);
            if (state == State.ERR) {
                System.out.println("→ ERR на позиції " + (i + 1) + ", зупинка.");
                return false;
            }
        }

        boolean accepted = (state == State.S5);
        System.out.println("→ Фінальний стан: " + state
                + (accepted ? " ✓ (допускальний)" : " ✗ (не допускальний)"));
        return accepted;
    }

    private static void runTask2() {
        System.out.println("\n=== Завдання 2: скінченний автомат ===");
        System.out.println("Розпізнає: #\\d*(%|\\*|[A-Z]+)#");

        // Автоматичне тестування
        String[] testCases = {
                "#123%#",   // ✓
                "#0ABC#",   // ✓
                "#99*#",    // ✓
                "#%#",      // ✓ цифри необов'язкові
                "#*#",      // ✓
                "#ABC#",    // ✓
                "#1#",      // ✗ немає %, *, або A-Z
                "#abc#",    // ✗ малі літери
                "123%#",    // ✗ немає початкового #
                "#123%",    // ✗ немає завершального #
                "#55%%#",   // ✗ два %
        };

        System.out.println("\n--- Автоматичне тестування ---");
        for (String test : testCases) {
            boolean result = recognize(test);
            System.out.println("Результат: " + (result ? "✓ РОЗПІЗНАНО" : "✗ ВІДХИЛЕНО"));
        }

        // Введення з клавіатури
        System.out.println("\n--- Введення з клавіатури (exit — вихід) ---");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введіть рядок: ");
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) break;
            boolean result = recognize(input);
            System.out.println("Результат: " + (result ? "✓ РОЗПІЗНАНО" : "✗ ВІДХИЛЕНО"));
            System.out.print("\nВведіть рядок: ");
        }
        scanner.close();
    }

    // ================================================================
    // ТОЧКА ВХОДУ
    // ================================================================

    public static void main(String[] args) {
        runTask1();
        runTask2();
    }
}