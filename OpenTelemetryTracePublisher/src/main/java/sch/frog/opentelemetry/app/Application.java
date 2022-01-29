package sch.frog.opentelemetry.app;

import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.resource.v1.Resource;
import sch.frog.opentelemetry.util.OpenTelemetryProtoUtil;

import java.util.HashMap;
import java.util.Map;

public class Application {

    private static final String KEY_SERVICE_NAME = "service.name";

    private String serviceName;

    private SystemInfo systemInfo;

    private ProcessInfo processInfo;

    private TelemetryInfo telemetryInfo;

    private Map<String, String> otherInfo = new HashMap<>();

    public void addAttributes(String key, String value){
        otherInfo.put(key, value);
    }

    public String getServiceName() {
        return serviceName;
    }

    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

    public ProcessInfo getProcessInfo() {
        return processInfo;
    }

    public Map<String, String> getOtherInfo() {
        return otherInfo;
    }

    public Resource buildResource(){
        Resource.Builder builder = Resource.newBuilder();
        KeyValue serviceNameKV = OpenTelemetryProtoUtil.build(KEY_SERVICE_NAME, this.serviceName);
        if(serviceNameKV != null){ builder.addAttributes(serviceNameKV); }
        if(systemInfo != null){ builder.addAllAttributes(systemInfo.buildKeyValues()); }
        if(processInfo != null){ builder.addAllAttributes(processInfo.buildKeyValues()); }
        if(telemetryInfo != null){ builder.addAllAttributes(telemetryInfo.buildKeyValues()); }
        for (Map.Entry<String, String> entry : otherInfo.entrySet()) {
            builder.addAttributes(OpenTelemetryProtoUtil.build(entry.getKey(), entry.getValue()));
        }
        return builder.build();
    }

    public static class Builder{
        private final Application application = new Application();

        public static Builder newBuilder(){
            return new Builder();
        }

        public Builder setServiceName(String serviceName){
            application.serviceName = serviceName;
            return this;
        }

        public Builder setSystemInfo(SystemInfo systemInfo){
            application.systemInfo = systemInfo;
            return this;
        }

        public Builder setProcessInfo(ProcessInfo processInfo){
            application.processInfo = processInfo;
            return this;
        }

        public Builder setTelemetryInfo(TelemetryInfo telemetryInfo){
            application.telemetryInfo = telemetryInfo;
            return this;
        }

        public Builder addAttribute(String key, String value){
            application.addAttributes(key, value);
            return this;
        }

        public Application build(){
            return application;
        }

    }

}
