package sch.frog.opentelemetry.app;

public class ThreadInfo {

    private Integer threadId;

    private String threadName;

    public Integer getThreadId() {
        return threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public static class Builder {
        private final ThreadInfo threadInfo = new ThreadInfo();

        public Builder setThreadName(String threadName){
            threadInfo.threadName = threadName;
            return this;
        }

        public Builder setThreadId(Integer threadId){
            threadInfo.threadId = threadId;
            return this;
        }

        public static Builder newBuilder(){
            return new Builder();
        }

        public ThreadInfo build(){
            return threadInfo;
        }
    }
}
