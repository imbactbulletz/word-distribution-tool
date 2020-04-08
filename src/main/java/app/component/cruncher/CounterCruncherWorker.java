package app.component.cruncher;

import app.component.cruncher.typealias.BagOfWords;
import app.component.cruncher.typealias.CrunchResult;
import app.global.Config;
import javafx.application.Platform;
import ui.controller.MainController;

import java.util.concurrent.RecursiveTask;
import java.util.regex.PatternSyntaxException;

public class CounterCruncherWorker extends RecursiveTask<CrunchResult> {

    private final int bagOfWordSize;
    private String inputText;
    private final boolean hasParentTask;
    private final String jobName;
    private final CruncherComponent cruncherComponent;

    public CounterCruncherWorker(CruncherComponent cruncherComponent, int bagOfWordSize, String jobName, String text, boolean hasParentTask) {
        this.bagOfWordSize = bagOfWordSize;
        this.inputText = text;
        this.hasParentTask = hasParentTask;
        this.cruncherComponent = cruncherComponent;
        this.jobName = jobName;
    }

    @Override
    protected CrunchResult compute() {
        try {
            String chunkToCrunch = getChunkOfInputTextToAnalyze();

            // chunk is lesser than whole input text divide into more tasks
            if (inputText.length() > chunkToCrunch.length()) {
                CounterCruncherWorker leftTask = new CounterCruncherWorker(cruncherComponent, bagOfWordSize, jobName, inputText.substring(chunkToCrunch.length()), true);
                // lose reference (since it's only kept in this tasks, while it lives) in order for garbage collector do its job
                inputText = null;
                leftTask.fork();

                // TODO question for Bane: couldn't crunchChunk() have been called instead of creating a right task and calling compute on it?
                CounterCruncherWorker rightTask = new CounterCruncherWorker(cruncherComponent, bagOfWordSize, jobName, chunkToCrunch, true);
                CrunchResult crunchRightResult = rightTask.compute();
                CrunchResult crunchLeftResult = leftTask.join();

                crunchRightResult.combineWith(crunchLeftResult);
                if(!hasParentTask) {
                    notifyUIOfFinishedJob(cruncherComponent, jobName);
                }
                return crunchRightResult;
            } else {
                return crunchChunk(chunkToCrunch);
            }
        } catch (OutOfMemoryError outOfMemoryError) {
            outOfMemoryError.printStackTrace();
            System.exit(-404);
            return new CrunchResult();
        }
    }

    private String getChunkOfInputTextToAnalyze() {
        if (inputText.length() <= Config.COUNTER_DATA_LIMIT_CHARS) {
            return inputText;
        }

        if (inputEndsWithAWholeWord()) {
            return inputText.substring(0, Config.COUNTER_DATA_LIMIT_CHARS);
        }

        int endIndexOfLastWordInChunk = getEndIndexOfLastWordInChunk();
        return inputText.substring(0, endIndexOfLastWordInChunk + 1);
    }

    private boolean inputEndsWithAWholeWord() {
        return inputText.charAt(Config.COUNTER_DATA_LIMIT_CHARS - 1) == ' ' || inputText.charAt(Config.COUNTER_DATA_LIMIT_CHARS) == ' ';
    }

    private int getEndIndexOfLastWordInChunk() {
        int index = Config.COUNTER_DATA_LIMIT_CHARS;

        while (index < inputText.length() - 1) {
            if (inputText.charAt(index) == ' ') {
                return index - 1;
            } else {
                index++;
            }
        }

        return index;
    }


    private CrunchResult crunchChunk(String inputText) {
        CrunchResult crunchResult = new CrunchResult();
        try {
            String[] words = inputText.trim().split("\\s+");
            for (int currentWordIndex = 0; currentWordIndex <= words.length - bagOfWordSize; currentWordIndex++) {
                BagOfWords bagOfWords = new BagOfWords();
                for (int lookAheadIndex = 0; lookAheadIndex < bagOfWordSize; lookAheadIndex++) {
                    bagOfWords.add(words[currentWordIndex + lookAheadIndex]);
                }

                if (crunchResult.get(bagOfWords) != null) {
                    crunchResult.put(bagOfWords, crunchResult.get(bagOfWords) + 1);
                } else {
                    crunchResult.put(bagOfWords, 1L);
                }
            }
        } catch (PatternSyntaxException psex) {
            psex.printStackTrace();
            return crunchResult;
        }

        return crunchResult;
    }

    private void notifyUIOfFinishedJob(CruncherComponent cruncherComponent, String jobName) {
        Platform.runLater(() -> MainController.CRUNCHER_CONTROLLER.refreshJobStatus(cruncherComponent, jobName, CruncherJobStatus.IS_DONE));
    }
}