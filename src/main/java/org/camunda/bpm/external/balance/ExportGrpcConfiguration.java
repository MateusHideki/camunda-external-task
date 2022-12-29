package org.camunda.bpm.external.balance;

import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.SERVICE_NAME;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

/**
 * All SDK management takes place here, away from the instrumentation code, which should only access
 * the OpenTelemetry APIs.
 */
public class ExportGrpcConfiguration {

  /**
   * Initialize OpenTelemetry.
   *
   * @return a ready-to-use {@link OpenTelemetry} instance.
   */
  public static OpenTelemetry initOpenTelemetry() {
    // Include required service.name resource attribute on all spans and metrics
    Resource resource =
        Resource.getDefault()
            .merge(Resource.builder().put(SERVICE_NAME, "OtlpExporterAutorizador").build());

            for(int i=0;i<5;i++){

            }

    OpenTelemetrySdk openTelemetrySdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .setResource(resource)
                    .addSpanProcessor(
                        BatchSpanProcessor.builder(
                                OtlpGrpcSpanExporter.builder()
                                .setTimeout(1, TimeUnit.SECONDS)
                                	.build())
                            .setScheduleDelay(1000, TimeUnit.MILLISECONDS)
                            .build())
                    .build())
            .setMeterProvider(
                SdkMeterProvider.builder()
                    .setResource(resource)
                    .registerMetricReader(
                        PeriodicMetricReader.builder(OtlpGrpcMetricExporter.getDefault())
                            .build())
                    .build()).buildAndRegisterGlobal();
    
    Runtime.getRuntime()
    	.addShutdownHook(new Thread(openTelemetrySdk.getSdkTracerProvider()::shutdown));
    
    Runtime.getRuntime()
    	.addShutdownHook(new Thread(openTelemetrySdk.getSdkMeterProvider()::shutdown));

  
    return openTelemetrySdk;
  }
}