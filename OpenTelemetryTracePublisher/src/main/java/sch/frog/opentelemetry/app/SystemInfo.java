package sch.frog.opentelemetry.app;

import io.opentelemetry.proto.common.v1.KeyValue;
import sch.frog.opentelemetry.util.CollectionUtil;
import sch.frog.opentelemetry.util.OpenTelemetryProtoUtil;

import java.util.HashSet;
import java.util.Set;

public class SystemInfo {

    private static final String HOST_ARCH = "host.arch";

    private static final String HOST_NAME = "host.name";

    private static final String OS_DESCRIPTION = "os.description";

    private static final String OS_TYPE = "os.type";

    private String hostArch;

    private String hostName;

    private String osDescription;

    private String osType;

    public String getHostArch() {
        return hostArch;
    }

    public void setHostArch(String hostArch) {
        this.hostArch = hostArch;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getOsDescription() {
        return osDescription;
    }

    public void setOsDescription(String osDescription) {
        this.osDescription = osDescription;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public Set<KeyValue> buildKeyValues(){
        HashSet<KeyValue> keyValues = new HashSet<>();
        CollectionUtil.addIfNotNull(keyValues, OpenTelemetryProtoUtil.build(HOST_ARCH, this.hostArch));
        CollectionUtil.addIfNotNull(keyValues, OpenTelemetryProtoUtil.build(HOST_NAME, this.hostName));
        CollectionUtil.addIfNotNull(keyValues, OpenTelemetryProtoUtil.build(OS_DESCRIPTION, this.osDescription));
        CollectionUtil.addIfNotNull(keyValues, OpenTelemetryProtoUtil.build(OS_TYPE, this.osType));
        return keyValues;
    }

    public static class Builder{
        private final SystemInfo systemInfo = new SystemInfo();

        public Builder setHostArch(String hostArch){
            systemInfo.setHostArch(hostArch);
            return this;
        }

        public Builder setHostName(String hostName){
            systemInfo.setHostName(hostName);
            return this;
        }

        public Builder setOsDescription(String osDescription){
            systemInfo.setOsDescription(osDescription);
            return this;
        }

        public Builder setOsType(String osType){
            systemInfo.setOsType(osType);
            return this;
        }

        public static Builder newBuilder(){
            return new Builder();
        }

        public SystemInfo build(){
            return systemInfo;
        }


    }
}
