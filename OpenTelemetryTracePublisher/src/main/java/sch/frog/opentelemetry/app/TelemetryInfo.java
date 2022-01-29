package sch.frog.opentelemetry.app;

import io.opentelemetry.proto.common.v1.KeyValue;
import sch.frog.opentelemetry.util.CollectionUtil;
import sch.frog.opentelemetry.util.OpenTelemetryProtoUtil;

import java.util.HashSet;
import java.util.Set;

public class TelemetryInfo {

    private static final String AUTO_VERSION = "telemetry.auto.version";

    private static final String SDK_LANGUAGE = "telemetry.sdk.language";

    private static final String SDK_NAME = "telemetry.sdk.name";

    private static final String SKD_VERSION = "telemetry.sdk.version";

    private String autoVersion;

    private String sdkLanguage;

    private String sdkName;

    private String sdkVersion;

    public String getAutoVersion() {
        return autoVersion;
    }

    public void setAutoVersion(String autoVersion) {
        this.autoVersion = autoVersion;
    }

    public String getSdkLanguage() {
        return sdkLanguage;
    }

    public void setSdkLanguage(String sdkLanguage) {
        this.sdkLanguage = sdkLanguage;
    }

    public String getSdkName() {
        return sdkName;
    }

    public void setSdkName(String sdkName) {
        this.sdkName = sdkName;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public Set<KeyValue> buildKeyValues(){
        HashSet<KeyValue> keyValues = new HashSet<>();

        CollectionUtil.addIfNotNull(keyValues, OpenTelemetryProtoUtil.build(AUTO_VERSION, this.autoVersion));
        CollectionUtil.addIfNotNull(keyValues, OpenTelemetryProtoUtil.build(SDK_LANGUAGE, this.sdkLanguage));
        CollectionUtil.addIfNotNull(keyValues, OpenTelemetryProtoUtil.build(SDK_NAME, this.sdkName));
        CollectionUtil.addIfNotNull(keyValues, OpenTelemetryProtoUtil.build(SKD_VERSION, this.sdkVersion));

        return keyValues;
    }

    public static class Builder{

        private final TelemetryInfo telemetryInfo = new TelemetryInfo();

        private String autoVersion;

        private String sdkLanguage;

        private String sdkName;

        private String sdkVersion;

        public Builder setAutoVersion(String autoVersion) {
            telemetryInfo.autoVersion = autoVersion;
            return this;
        }

        public Builder setSdkLanguage(String sdkLanguage) {
            telemetryInfo.sdkLanguage = sdkLanguage;
            return this;
        }

        public Builder setSdkName(String sdkName) {
            telemetryInfo.sdkName = sdkName;
            return this;
        }

        public Builder setSdkVersion(String sdkVersion) {
            telemetryInfo.sdkVersion = sdkVersion;
            return this;
        }

        public static Builder newBuilder(){
            return new Builder();
        }

        public TelemetryInfo build(){
            return telemetryInfo;
        }
    }
}
