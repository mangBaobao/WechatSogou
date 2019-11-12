package cert.aiops.pega.bean.mapping;

public class SystemSegment implements Comparable<SystemSegment>{
    private long trailer=0;
    private long header=0;
    private String workerId=null;
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
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
    public int compareTo(SystemSegment candidate) {
        return (this.getHeader()>candidate.getHeader()?-1:(this.getHeader()==candidate.getHeader()?0:1));

    }

    @Override
    public String toString() {
        return "SystemSegment{" +
                "trailer=" + trailer +
                ", header=" + header +
                ", workerId='" + workerId + '\'' +
                ", count=" + count +
                '}';
    }
}
