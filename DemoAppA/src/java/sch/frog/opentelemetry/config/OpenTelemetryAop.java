package sch.frog.opentelemetry.config;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "frog.opentelemetry.enable", havingValue = "true", matchIfMissing = true)
@Aspect
public class OpenTelemetryAop {

    private static final Logger logger = LoggerFactory.getLogger(OpenTelemetryAop.class);

    @Autowired
    private OpenTelemetryEnv openTelemetryEnv;

    @Around("within(sch.frog.opentelemetry..*) && !within(sch.frog.opentelemetry.config..*)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable{
        Span currentSpan = null;

        Span parent = Span.current();
        Signature signature = pjp.getSignature();
        String spanName = signature.getDeclaringTypeName() + "." + signature.getName();
        currentSpan = openTelemetryEnv.getTracer().spanBuilder(spanName).setParent(Context.current().with(parent)).startSpan();

        currentSpan.setAttribute("class", signature.getDeclaringTypeName());
        currentSpan.setAttribute("method", signature.getName());

        try(
            Scope scope = currentSpan.makeCurrent()
        ){
            return pjp.proceed();
        }catch(Throwable t){
            currentSpan.recordException(t);
            currentSpan.setStatus(StatusCode.ERROR);
            throw t;
        }finally{
            currentSpan.end();
        }
    }
    
}
