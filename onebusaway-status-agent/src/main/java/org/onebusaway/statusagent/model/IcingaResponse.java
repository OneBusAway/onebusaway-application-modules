package org.onebusaway.statusagent.model;

public class IcingaResponse {
    IcingaItem[] result;
    boolean success;
    int total;

    public IcingaItem[] getResult() {
        return result;
    }

    public void setResult(IcingaItem[] result) {
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

}
