package cert.aiops.pega.bean.mapping;

public class WorkAssignment implements Comparable<WorkAssignment>{
    private Long systemId;
    private long header;
    private long trailer;
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Long getSystemId() {
        return systemId;
    }

    public void setSystemId(Long systemId) {
        this.systemId = systemId;
    }

    public long getHeader() {
        return header;
    }

    public void setHeader(long header) {
        this.header = header;
    }

    public long getTrailer() {
        return trailer;
    }

    public void setTrailer(long trailer) {
        this.trailer = trailer;
    }

    @Override
    public String toString() {
        return "WorkAssignment{" +
                "systemId=" + systemId +
                ", header=" + header +
                ", trailer=" + trailer +
                ", count=" + count +
                '}';
    }

    @Override
    public int compareTo(WorkAssignment candidate) {
        return (this.getCount() < candidate.getCount() ? -1 : (this.getCount() == candidate.getCount() ? 0 : 1));
    }
}
