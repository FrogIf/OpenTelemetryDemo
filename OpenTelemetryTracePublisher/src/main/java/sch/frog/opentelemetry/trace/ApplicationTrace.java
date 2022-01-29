package sch.frog.opentelemetry.trace;

import sch.frog.opentelemetry.app.Application;
import sch.frog.opentelemetry.data.InstrumentationData;

import java.util.Objects;
import java.util.UUID;

public class ApplicationTrace {

    private ApplicationTrace(){
        // do nothing
    }

    private final String markUuid = UUID.randomUUID().toString();

    private Application application;

    private String schemaUrl;

    private InstrumentationData instrumentationData;

    public Application getApplication() {
        return application;
    }

    public String getSchemaUrl() {
        return schemaUrl;
    }

    public InstrumentationData getInstrumentationData() {
        return instrumentationData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationTrace that = (ApplicationTrace) o;
        return Objects.equals(markUuid, that.markUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(markUuid);
    }

    public static class Builder {

        private final ApplicationTrace applicationTrace = new ApplicationTrace();

        public static Builder newBuilder(){
            return new Builder();
        }

        public Builder setApplication(Application application){
            applicationTrace.application = application;
            return this;
        }

        public Builder setSchemaUrl(String schemaUrl){
            applicationTrace.schemaUrl = schemaUrl;
            return this;
        }

        public Builder setInstrumentationData(InstrumentationData instrumentationData){
            applicationTrace.instrumentationData = instrumentationData;
            return this;
        }

        public ApplicationTrace build(){
            return applicationTrace;
        }


    }
}
