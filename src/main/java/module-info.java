module otel.sender.main {
    requires io.grpc;
    requires io.opentelemetry.sdk.common;
    requires io.opentelemetry.exporter.otlp;
    requires io.grpc.stub;
    requires io.opentelemetry.sdk.metrics;
    requires io.opentelemetry.sdk.trace;
    requires io.opentelemetry.sdk;
    requires io.opentelemetry.api;
    requires io.opentelemetry.context;
    requires java.logging;
}