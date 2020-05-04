package com.stupidtree.hita.online;

import androidx.annotation.WorkerThread;

import java.util.List;

public abstract class SearchCore<T> {

    int totalResult = 0;
    private String lastKeyword;
    private int loadedResult = 0;
    private int resultOffset = 0;

    public String getLastKeyword() {
        return lastKeyword;
    }

    public void setLastKeyword(String lastKeyword) {
        this.lastKeyword = lastKeyword;
    }

    public int getLoadedResult() {
        return loadedResult;
    }

    public int getTotalResult() {
        return totalResult;
    }

    public int getOffset() {
        return resultOffset;
    }

    public void reset() {
        totalResult = 0;
        resultOffset = 0;
        loadedResult = 0;
        lastKeyword = null;
    }

    public abstract int getPageSize();

    @WorkerThread
    protected abstract List<T> reloadResult(String text) throws SearchException;

    @WorkerThread
    protected abstract List<T> loadMoreResult(String text) throws SearchException;

    @WorkerThread
    public List<T> search(String text, boolean reLoad) throws SearchException {
        if (text == null) text = "";
        if (reLoad) {
            reset();
            List<T> result = reloadResult(text);
            lastKeyword = text;
            loadedResult = result.size();
            return result;
        } else {
            resultOffset += getPageSize();
            lastKeyword = text;
            List<T> result = loadMoreResult(text);
            if (result != null) loadedResult += result.size();
            return result;
        }
    }

}
