package com.stupidtree.hita.fragments;

public class BaseOperationTask<T>
        extends WeakAsyncTask<Boolean, Integer, T, BaseOperationTask.OperationListener<T>> {

    protected String id = "";

    public BaseOperationTask(OperationListener listRefreshedListener) {
        super(listRefreshedListener);
    }

    @Override
    protected T doInBackground(OperationListener<T> listRefreshedListener, Boolean... booleans) {
        return null;
    }

    @Override
    protected void onPreExecute(OperationListener<T> listRefreshedListener) {
        super.onPreExecute(listRefreshedListener);
        if (listRefreshedListener != null) listRefreshedListener.onOperationStart(id, params);
    }

    @Override
    protected void onPostExecute(OperationListener<T> listRefreshedListener, T ts) {
        super.onPostExecute(listRefreshedListener, ts);
        if (listRefreshedListener != null) {
            try {
                listRefreshedListener.onOperationDone(id, this, params, ts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface OperationListener<T> {
        void onOperationStart(String id, Boolean[] params);

        void onOperationDone(String id, BaseOperationTask task, Boolean[] params, T result);
    }

}
