package org.example;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

import java.util.concurrent.TimeUnit;

public class OTelExamplehttp {

    private static String ENDPOINT;

    public static void main(String[] args) throws Exception {

        String ENDPOINT = "http://localhost:8200";

        OtlpHttpSpanExporter spanExporter = OtlpHttpSpanExporter.builder()
                .setEndpoint(ENDPOINT) // Replace with your endpoint
                .build();
        OtlpHttpMetricExporter metricExporter = OtlpHttpMetricExporter.builder()
                .setEndpoint(ENDPOINT) // Replace with your endpoint
                .build();
        Resource resource = Resource.getDefault().merge(Resource.create(
                Attributes.of(AttributeKey.stringKey("service.name"), "my-java-app")));

        // 2. Configure Tracer Provider
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .registerMetricReader(PeriodicMetricReader.builder(metricExporter).setInterval(1, TimeUnit.SECONDS).build())
                .build();
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .build();
        OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setMeterProvider(sdkMeterProvider)
                .buildAndRegisterGlobal();

        // 3. Create Tracer and Span
        Tracer tracer = GlobalOpenTelemetry.getTracer("my-java-app");
        Span span = tracer.spanBuilder("MySpanName").startSpan();
        Meter meter = GlobalOpenTelemetry.getMeter("my-java-app");
        LongCounter mycounter = meter.counterBuilder("mycounter").build();
        mycounter.add(123);

        try (Scope scope = span.makeCurrent()) {
            // Your application logic here...
            System.out.println("Doing some work...");

            // Add attributes to the span (optional)
            span.setAttribute("someKey", "someValue");
        } finally {
            // 4. End the Span
            span.end();
            Thread.sleep(20000);

            // 5. Shutdown
            sdkTracerProvider.shutdown().join(10, TimeUnit.SECONDS);
        }
    }
}
