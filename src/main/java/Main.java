import datadog.opentracing.DDTracer;
import datadog.trace.api.DDTags;
import datadog.trace.api.interceptor.MutableSpan;
import datadog.trace.api.interceptor.TraceInterceptor;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

import java.util.ArrayList;
import java.util.Collection;

public class Main {
    private static DDTracer tracer;
    private static Span parentspan;

    public static void main(String[] args) throws InterruptedException {
        //tracer = GlobalTracer.get();

        tracer = new DDTracer("hub");
        GlobalTracer.register(tracer);
        datadog.trace.api.GlobalTracer.registerIfAbsent(tracer);


        tracer.addTraceInterceptor(new TraceInterceptor() {
            @Override
            public Collection<? extends MutableSpan> onTraceComplete(Collection<? extends MutableSpan> trace) {

                /*
                for (MutableSpan span : trace) {

                    if (span.getTags() != null) {

                        if (Long.valueOf((Long) span.getTags().get("thread.id")).equals(Long.valueOf(1))){
                            span.setTag("thread.id", "2");
                        }

                    }
                }
                */

                ((ArrayList) trace).remove(1);
                return trace;
            }

            @Override
            public int priority() {
                return 1;
            }
        });



        try (Scope scope = tracer.buildSpan("main").startActive(true)) {
            parentspan = scope.span();
            doSomething();
            Thread.sleep(120L);
        }

    }

    public static void doSomething() throws InterruptedException {
        Tracer.SpanBuilder sb = tracer.buildSpan("dosomething").asChildOf(parentspan);
        try (Scope scope = sb.startActive(true)) {
            Span childspan = scope.span();
            System.out.println("I'm doing something");
            System.out.println(childspan.context().toString());
            Thread.sleep(250L);
        }
    }
}
