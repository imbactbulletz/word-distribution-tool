package app.component.cruncher;

import app.component.cruncher.typealias.BagOfWords;
import app.component.cruncher.typealias.CalculationResult;
import app.global.Config;
import javafx.application.Platform;
import ui.controller.MainController;

import java.util.concurrent.RecursiveTask;

public class CounterCruncherWorker extends RecursiveTask<CalculationResult> {

    private final int bagOfWordSize;
    private final boolean hasParentTask;
    private final String jobName;
    private final CruncherComponent cruncherComponent;
    private final int chunkStartIndex;
    private String inputText;
    private int chunkEndIndex;

    public CounterCruncherWorker(CruncherComponent cruncherComponent, int bagOfWordSize, String jobName, int chunkStartIndex, String text, boolean hasParentTask) {
        this.bagOfWordSize = bagOfWordSize;
        this.inputText = text;
        this.hasParentTask = hasParentTask;
        this.cruncherComponent = cruncherComponent;
        this.jobName = jobName;
        this.chunkStartIndex = chunkStartIndex;
    }

    @Override
    protected CalculationResult compute() {
        try {
            chunkEndIndex = findLastIndexForWordAt(chunkStartIndex + Config.COUNTER_DATA_LIMIT_CHARS);
            if (inputText.length() - 1 > chunkEndIndex) {
                CounterCruncherWorker leftTask = new CounterCruncherWorker(cruncherComponent, bagOfWordSize, jobName, chunkEndIndex + 1, inputText, true);
                leftTask.fork();

                CalculationResult result = crunchChunk();
                result.combineWith(leftTask.join());

                if (!hasParentTask) {
                    notifyUIOfFinishedJob(cruncherComponent, jobName);
                }

                return result;
            } else {
                return crunchChunk();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            notifyUIOfTermination();
            return null;
        }
    }

    private int findLastIndexForWordAt(int index) {
        int inputTextLastCharacterIndex = inputText.length() - 1;
        if (index >= inputTextLastCharacterIndex) return inputTextLastCharacterIndex;

        while (index + 1 < inputTextLastCharacterIndex) {
            if (inputText.charAt(index + 1) == ' ') return index;
            index++;
        }

        return inputTextLastCharacterIndex;
    }

    private int findFirstIndexOfNextWordAt(int index) {
        int inputTextLastCharacterIndex = inputText.length() - 1;
        if (index >= inputTextLastCharacterIndex) return inputTextLastCharacterIndex;

        while (index < inputTextLastCharacterIndex) {
            if (inputText.charAt(index) == ' ') break;
            index++;
        }

        if (index + 1 < inputTextLastCharacterIndex) return index + 1;

        return inputTextLastCharacterIndex;
    }

    private CalculationResult crunchChunk() {
        CalculationResult result = new CalculationResult();
        int currentIndexInString = chunkStartIndex;
        while (currentIndexInString < chunkEndIndex) {
            String bagWords = grabBagWordsFrom(currentIndexInString);
            currentIndexInString = findFirstIndexOfNextWordAt(currentIndexInString);
            if (bagWords.isBlank()) {
                continue;
            }
            BagOfWords bagOfWords = new BagOfWords(bagWords.split(" "));
            if (bagOfWords.size() < bagOfWordSize) break;
            Long valueForKey = result.get(bagOfWords);
            if (valueForKey != null) {
                result.put(bagOfWords, valueForKey + 1);
            } else {
                result.put(bagOfWords, 1L);
            }
        }
        return result;
    }

    private String grabBagWordsFrom(final int startPosition) {
        StringBuilder stringBuilder = new StringBuilder();

        int wordsFound = 0;
        int currentStringPosition = startPosition;
        while (wordsFound < bagOfWordSize && currentStringPosition <= chunkEndIndex) {
            char charAtCurrentStringPosition = inputText.charAt(currentStringPosition);
            if (charAtCurrentStringPosition == ' ') wordsFound++;
            if (wordsFound != bagOfWordSize) stringBuilder.append(charAtCurrentStringPosition);
            currentStringPosition++;
        }

        return stringBuilder.toString();
    }

    private void notifyUIOfFinishedJob(CruncherComponent cruncherComponent, String jobName) {
        Platform.runLater(() -> MainController.CRUNCHER_CONTROLLER.refreshJobStatus(cruncherComponent, jobName, CruncherJobStatus.IS_DONE));
    }

    private void notifyUIOfTermination() {
        Platform.runLater(() -> MainController.showOutOfMemoryErrorDialog());
    }
}