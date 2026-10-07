package com.wordsearch;
import java.util.*;
import java.util.concurrent.*;

public class WordSearchGame {
private static final int BOARD_SIZE = 10;
private static final int WORD_COUNT = 5;
private static final int GAME_TIME_SECONDS = 180;

private char[][] board;
private char[][] solutionBoard;
private Set<String> wordSet = new HashSet<>();
private Map<String, WordPlacement> wordPlacements = new HashMap<>();
private List<String> allWords = Arrays.asList(
    "APPLE", "BANANA", "CHERRY", "GRAPE", "ORANGE", "PEACH", "PINEAPPLE",
    "STRAWBERRY", "WATERMELON", "BLUEBERRY", "MANGO", "KIWI", "LEMON",
    "APRICOT", "PLUM", "PEAR", "POMEGRANATE", "CANTALOUPE", "RASPBERRY", "BLACKBERRY"
);
private Scanner scanner = new Scanner(System.in);
private boolean timeUp = false;

public static void main(String[] args) {
WordSearchGame game = new WordSearchGame();
game.start();
}

private void start() {
boolean keepPlaying = true;
while (keepPlaying) {
    loadWords();
    generateBoard();
    displayBoard();
    keepPlaying = play();
}
System.out.println("Thank you for playing!");
}

private void loadWords() {
wordSet.clear();
wordPlacements.clear();
Collections.shuffle(allWords);
for (int i = 0; i < WORD_COUNT; i++) {
    wordSet.add(allWords.get(i));
}
System.out.println("\nWords to find: " + wordSet);
}

private void generateBoard() {
board = new char[BOARD_SIZE][BOARD_SIZE];
solutionBoard = new char[BOARD_SIZE][BOARD_SIZE];
for (int i = 0; i < BOARD_SIZE; i++) Arrays.fill(board[i], ' ');
for (String word : wordSet) placeWord(word);
Random rand = new Random();
for (int i = 0; i < BOARD_SIZE; i++) {
    for (int j = 0; j < BOARD_SIZE; j++) {
        if (board[i][j] == ' ') board[i][j] = (char) ('A' + rand.nextInt(26));
        solutionBoard[i][j] = board[i][j];
    }
}
}

private void placeWord(String word) {
Random rand = new Random();
int len = word.length();
boolean placed = false;
while (!placed) {
    int x = rand.nextInt(BOARD_SIZE), y = rand.nextInt(BOARD_SIZE);
    Direction dir = Direction.values()[rand.nextInt(Direction.values().length)];
    int endX = x + dir.dx * (len - 1), endY = y + dir.dy * (len - 1);
    if (endX < 0 || endY < 0 || endX >= BOARD_SIZE || endY >= BOARD_SIZE) continue;
    boolean canPlace = true;
    int tempX = x, tempY = y;
    for (int i = 0; i < len; i++) {
        char c = board[tempX][tempY];
        if (c != ' ' && c != word.charAt(i)) {
            canPlace = false;
            break;
        }
        tempX += dir.dx; tempY += dir.dy;
    }
    if (canPlace) {
        tempX = x; tempY = y;
        for (int i = 0; i < len; i++) {
            board[tempX][tempY] = word.charAt(i);
            tempX += dir.dx; tempY += dir.dy;
        }
        wordPlacements.put(word, new WordPlacement(x, y, dir));
        placed = true;
    }
}
}

private void displayBoard() {
System.out.println("\nWord Search Board:");
for (char[] row : solutionBoard) {
    for (char c : row) System.out.print(c + " ");
    System.out.println();
}
}

private boolean play() {
Set<String> found = new HashSet<>();
timeUp = false;
ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
scheduler.schedule(() -> timeUp = true, GAME_TIME_SECONDS, TimeUnit.SECONDS);
System.out.println("Timer started: You have " + (GAME_TIME_SECONDS / 60) + " minutes.");
while (found.size() < WORD_COUNT && !timeUp) {
    System.out.print("Enter word (or type 'hint', 'reset', 'exit'): ");
    String input = scanner.nextLine().trim().toUpperCase();
    if (timeUp) break;
    switch (input) {
        case "RESET":
            scheduler.shutdownNow();
            System.out.println("\nGame is resetting...\n");
            return true;
        case "EXIT":
            scheduler.shutdownNow();
            return false;
        case "HINT":
            giveHint(found);
            break;
        default:
            if (wordSet.contains(input) && !found.contains(input)) {
                found.add(input);
                markWord(input);
                System.out.println("Word found and marked!");
                displayBoard();
            } else if (found.contains(input)) {
                System.out.println("You already found that word.");
            } else {
                System.out.println("Word not found.");
            }
            break;
    }
}
scheduler.shutdownNow();
if (timeUp) {
    System.out.println("\nTime's up! Game over.");
    System.out.println("You found " + found.size() + " out of " + WORD_COUNT + " words.");
} else if (found.size() == WORD_COUNT) {
    System.out.println("\nCongratulations! You've found all the words.");
    System.out.print("Would you like to play again? (yes / no / reset / exit): ");
    String response = scanner.nextLine().trim().toLowerCase();
    return response.equals("yes") || response.equals("reset");
}
return true;
}

private void markWord(String word) {
WordPlacement placement = wordPlacements.get(word);
if (placement == null) return;
int x = placement.x, y = placement.y;
Direction dir = placement.direction;
for (int i = 0; i < word.length(); i++) {
    solutionBoard[x][y] = Character.toLowerCase(board[x][y]);
    x += dir.dx; y += dir.dy;
}
}

private void giveHint(Set<String> found) {
List<String> remaining = new ArrayList<>();
for (String word : wordSet) if (!found.contains(word)) remaining.add(word);
if (remaining.isEmpty()) {
    System.out.println("No more hints needed. You've found everything!");
    return;
}
String hintWord = remaining.get(new Random().nextInt(remaining.size()));
WordPlacement placement = wordPlacements.get(hintWord);
if (placement != null) {
    System.out.println("Hint: The word starts with '" + hintWord.charAt(0) +
        "' at row " + (placement.x + 1) + ", column " + (placement.y + 1));
}
}

private enum Direction {
RIGHT(0, 1), LEFT(0, -1), DOWN(1, 0), UP(-1, 0),
DOWN_RIGHT(1, 1), DOWN_LEFT(1, -1),
UP_RIGHT(-1, 1), UP_LEFT(-1, -1);
final int dx, dy;
Direction(int dx, int dy) {
    this.dx = dx;
    this.dy = dy;
}
}

private static class WordPlacement {
int x, y;
Direction direction;
WordPlacement(int x, int y, Direction direction) {
    this.x = x;
    this.y = y;
    this.direction = direction;
}
}
}