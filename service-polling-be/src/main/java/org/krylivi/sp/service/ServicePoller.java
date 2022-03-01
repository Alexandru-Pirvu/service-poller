package org.krylivi.sp.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.TimeoutStream;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.krylivi.sp.model.EventBusAddress;
import org.krylivi.sp.model.ServiceInfo;
import org.krylivi.sp.model.ServiceStatus;
import org.krylivi.sp.service.dto.UpdateServiceStatusRequest;

import java.util.concurrent.atomic.AtomicInteger;

public class ServicePoller extends AbstractVerticle {

    private final Logger LOGGER = LoggerFactory.getLogger(ServicePoller.class);

    @Override
    public void start() {
        TimeoutStream pollingStream = vertx.periodicStream(15000);

        pollingStream
                .handler(time -> {
                    pollingStream.pause();
                    vertx.eventBus().<Buffer>request(EventBusAddress.GET_SERVICES.toString(), null, servicesReply -> {
                        if (servicesReply.succeeded()) {

                            AtomicInteger servicesChecked = new AtomicInteger(0);

                            JsonArray serviceInfosJson = servicesReply.result().body().toJsonArray();

                            serviceInfosJson
                                    .stream()
                                    .parallel()
                                    .map(jsonObject -> JsonObject.mapFrom(jsonObject).mapTo(ServiceInfo.class))
                                    .forEach(serviceInfo ->
                                            vertx.eventBus().<String>request(EventBusAddress.CALL_SERVICE.name(), serviceInfo.url(), serviceStatusReply -> {
                                                UpdateServiceStatusRequest updateServiceStatusRequest = new UpdateServiceStatusRequest(serviceInfo.id(), ServiceStatus.valueOf(serviceStatusReply.result().body()));

                                                vertx.eventBus().<Buffer>request(EventBusAddress.UPDATE_SERVICE_STATUS.name(), JsonObject.mapFrom(updateServiceStatusRequest).toBuffer(), (updateStatusReply -> {
                                                    LOGGER.info(String.format("Checked service ----- %d = %s", serviceInfo.id(), serviceStatusReply.result().body()));
                                                    if (servicesChecked.incrementAndGet() == serviceInfosJson.size()) {
                                                        pollingStream.resume();
                                                    }
                                                }));
                                            }));
                        } else {
                            LOGGER.error("Failed to retrieve services", servicesReply.cause());
                            pollingStream.resume();
                        }
                    });
                });
    }
}
