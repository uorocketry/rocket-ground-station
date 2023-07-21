package uorocketry.basestation.data;

public class UserEvent {
    private String name;
    private int index;
    private long epochTime;
    private long[] datasetTime;

    public UserEvent(String name, int index, long epochTime, long[] datasetTime) {
        this.name = name;
        this.index = index;
        this.epochTime = epochTime;
        this.datasetTime = datasetTime;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public long getEpochTime() {
        return epochTime;
    }

    public long[] getDatasetTime() {
        return datasetTime;
    }
}
