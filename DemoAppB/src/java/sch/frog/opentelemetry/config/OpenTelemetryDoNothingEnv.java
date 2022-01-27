package sch.frog.opentelemetry.config;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(value = "frog.opentelemetry.enable", havingValue = "false", matchIfMissing = false)
public class OpenTelemetryDoNothingEnv implements IOpenTelemetryOperator{
    @Override
    public void event(String name, Map<String, String> attributes) {
        // do nothing
    }
}
