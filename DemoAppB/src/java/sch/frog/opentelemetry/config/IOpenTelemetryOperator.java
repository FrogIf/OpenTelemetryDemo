package sch.frog.opentelemetry.config;

import java.util.Map;

public interface IOpenTelemetryOperator {

    void event(String name, Map<String, String> attributes);

}
