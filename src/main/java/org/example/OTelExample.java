package org.example;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
//import .sdk.trace..semconv.resource.attributes.ResourceAttributes;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OTelExample {


    public static void main(String[] args) throws Exception {
        Logger.getLogger("io.opentelemetry.sdk").setLevel(Level.FINEST);
        String  ENDPOINT2 = "https://0f794371efec42beafbcc6bf4e7ece51.apm.us-central1.gcp.cloud.es.io:443";
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(ENDPOINT2)
                .addHeader("Authorization", "Bearer BUy1NCsfKb4y9nPk5T")
                .build();
        OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.builder()
                .setEndpoint(ENDPOINT2)
                .addHeader("Authorization", "Bearer BUy1NCsfKb4y9nPk5T")
                .build();


        String  ENDPOINT1 = "https://3b4a40972a494625a76de9ef337ef425.apm.us-central1.gcp.cloud.es.io:443";
        OtlpGrpcSpanExporter spanExporter2 = OtlpGrpcSpanExporter.builder()
                .setEndpoint(ENDPOINT1)
                .addHeader("Authorization", "Bearer RXX6gm1WeXlx8qHw3a")
                .build();
        OtlpGrpcMetricExporter metricExporter2 = OtlpGrpcMetricExporter.builder()
                .setEndpoint(ENDPOINT1)
                .addHeader("Authorization", "Bearer RXX6gm1WeXlx8qHw3a")
                .build();
        Resource resource = Resource.getDefault().merge(Resource.create(
                Attributes.of(AttributeKey.stringKey("service.name"), "my-java-app")));

        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .registerMetricReader(PeriodicMetricReader.builder(metricExporter).setInterval(1,TimeUnit.SECONDS).build())
                .registerMetricReader(PeriodicMetricReader.builder(metricExporter2).setInterval(1,TimeUnit.SECONDS).build())
                .build();
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter2).build())
                .build();


        OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setMeterProvider(sdkMeterProvider)
                .buildAndRegisterGlobal();


        //test
        Tracer tracer = GlobalOpenTelemetry.getTracer("my-java-app");
        Span span = tracer.spanBuilder("MySpanName").startSpan();
        Meter meter = GlobalOpenTelemetry.getMeter("my-java-app");
        LongCounter mycounter = meter.counterBuilder("mycounter").build();
        mycounter.add(123);

        try (Scope scope = span.makeCurrent()) {
            System.out.println("Doing some work...");

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
