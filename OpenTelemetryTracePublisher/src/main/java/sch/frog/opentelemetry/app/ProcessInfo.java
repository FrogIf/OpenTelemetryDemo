package sch.frog.opentelemetry.app;

import io.opentelemetry.proto.common.v1.KeyValue;
import sch.frog.opentelemetry.util.CollectionUtil;
import sch.frog.opentelemetry.util.OpenTelemetryProtoUtil;

import java.util.HashSet;
import java.util.Set;

public class ProcessInfo {

    private static final String COMMAND_LINE = "process.command_line";

    private static final String EXECUTABLE_PATH = "process.executable.path";

    private static final String PID = "process.pid";

    private static final String RUNTIME_DESCRIPTION = "process.runtime.description";

    private static final String RUNTIME_NAME = "process.runtime.name";

    private static final String RUNTIME_VERSION = "process.runtime.version";

    private String commandLine;

    private String executablePath;

    private Integer pid;

    private String runtimeDescription;

    private String runtimeName;

    private String runtimeVersion;

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getRuntimeDescription() {
        return runtimeDescription;
    }

    public void setRuntimeDescription(String runtimeDescription) {
        this.runtimeDescription = runtimeDescription;
    }

    public String getRuntimeVersion() {
        return runtimeVersion;
    }

    public void setRuntimeVersion(String runtimeVersion) {
        this.runtimeVersion = runtimeVersion;
    }


    public String getRuntimeName() {
        return runtimeName;
    }

    public void setRuntimeName(String runtimeName) {
        this.runtimeName = runtimeName;
    }

    public Set<KeyValue> buildKeyValues(){
        HashSet<KeyValue> keyValues = new HashSet<>();

        CollectionUtil.addIfNotNull(keyValues, OpenTelemetryProtoUtil.build(COMMAND_LINE, commandLine));
        CollectionUtil.addIfNotNull(keyValues, OpenTelemetryProtoUtil.build(EXECUTABLE_PATH, executablePath));
        CollectionUtil.addIfNotNull(keyValues, OpenTelemetryProtoUtil.build(PID, pid));
        CollectionUtil.addIfNotNull(keyValues, OpenTelemetryProtoUtil.build(RUNTIME_DESCRIPTION, runtimeDescription));
        CollectionUtil.addIfNotNull(keyValues, OpenTelemetryProtoUtil.build(RUNTIME_NAME, runtimeName));
        CollectionUtil.addIfNotNull(keyValues, OpenTelemetryProtoUtil.build(RUNTIME_VERSION, runtimeVersion));

        return keyValues;
    }

    public static class Builder{
        private final ProcessInfo processInfo = new ProcessInfo();

        private String commandLine;

        private String executablePath;

        private Integer pid;

        private String runtimeDescription;

        private String runtimeName;

        private String runtimeVersion;

        public Builder setCommandLine(String commandLine) {
            processInfo.commandLine = commandLine;
            return this;
        }

        public Builder setExecutablePath(String executablePath) {
            processInfo.executablePath = executablePath;
            return this;
        }

        public Builder setPid(Integer pid) {
            processInfo.pid = pid;
            return this;
        }

        public Builder setRuntimeDescription(String runtimeDescription) {
            processInfo.runtimeDescription = runtimeDescription;
            return this;
        }

        public Builder setRuntimeName(String runtimeName) {
            processInfo.runtimeName = runtimeName;
            return this;
        }

        public Builder setRuntimeVersion(String runtimeVersion) {
            processInfo.runtimeVersion = runtimeVersion;
            return this;
        }

        public static Builder newBuilder(){
            return new Builder();
        }

        public ProcessInfo build(){
            return processInfo;
        }
    }
}
