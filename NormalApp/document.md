* 应用打包: mvn clean package
* 应用启动: java -javaagent:agent/opentelemetry-javaagent.jar -Dotel.javaagent.configuration-file=agent/conf.properties -jar NormalApp.jar

启动目录文件结构:
NormalApp.jar
agent
  -- opentelemetry-javaagent.jar
  -- conf.properties

conf文件内容:

```
otel.service.name=NormalApp

# jaeger
otel.traces.exporter=jaeger
otel.exporter.jaeger.endpoint=http://localhost:14250
otel.exporter.jaeger.timeout=10000

# opentelemetry
#otel.traces.exporter=otlp
#otel.metrics.exporter=otlp
#otel.logs.exporter=otlp
#otel.exporter.otlp.endpoint
```