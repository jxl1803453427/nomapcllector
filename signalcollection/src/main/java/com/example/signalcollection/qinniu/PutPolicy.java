package com.example.signalcollection.qinniu;

/**
 * Created by hehe on 2016/4/23.
 */
public class PutPolicy {
    private String scope;
    private long deadline;
    private ReturnBody returnBody;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public ReturnBody getReturnBody() {
        return returnBody;
    }

    public void setReturnBody(ReturnBody returnBody) {
        this.returnBody = returnBody;
    }
}
