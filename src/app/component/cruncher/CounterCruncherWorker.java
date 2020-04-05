package app.component.cruncher;

import app.component.cruncher.typealias.BagOfWords;
import app.component.cruncher.typealias.CrunchResult;
import app.global.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.regex.PatternSyntaxException;

public class CounterCruncherWorker extends RecursiveTask<CrunchResult> {

    private final int bagOfWordSize;
    private final String inputText;

    public CounterCruncherWorker(int bagOfWordSize, String text) {
        this.bagOfWordSize = bagOfWordSize;
        this.inputText = text;
    }

    @Override
    protected CrunchResult compute() {
        String chunkToCrunch = getChunkOfInputTextToAnalyze();

        // chunk is lesser than whole input text divide into more tasks
        if (inputText.length() > chunkToCrunch.length()) {
            return crunchChunk(chunkToCrunch);
        } else {
            CrunchResult crunchResult = crunchChunk(chunkToCrunch);
            return crunchResult;
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
            String[] words = inputText.split(" ");
            for (int currentWordIndex = 0; currentWordIndex <= words.length - bagOfWordSize; currentWordIndex++) {
                BagOfWords bagOfWords = new BagOfWords();
                for( int lookAheadIndex = 0; lookAheadIndex < bagOfWordSize; lookAheadIndex++) {
                    bagOfWords.add(words[currentWordIndex + lookAheadIndex]);
                }

                System.out.println( "[" + currentWordIndex + "/" + words.length + "]" + "Updating " + bagOfWords);
                if(crunchResult.get(bagOfWords) != null) {
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
}