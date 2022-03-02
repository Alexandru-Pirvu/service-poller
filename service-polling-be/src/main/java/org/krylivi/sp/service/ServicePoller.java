package org.krylivi.sp.service;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.krylivi.sp.model.EventBusAddress;
import org.krylivi.sp.model.ServiceInfo;
import org.krylivi.sp.model.ServiceStatus;
import org.krylivi.sp.service.dto.UpdateServiceStatusRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ServicePoller extends AbstractVerticle {

    private final Logger LOGGER = LoggerFactory.getLogger(ServicePoller.class);

    @Override
    public void start() {
        TimeoutStream pollingStream = vertx.periodicStream(15000);

        pollingStream
                .handler(time -> vertx.eventBus().<Buffer>request(EventBusAddress.GET_SERVICES.toString(), null, servicesReply -> {
                    pollingStream.pause();

                    if (servicesReply.succeeded()) {

                        JsonArray serviceInfosJson = servicesReply.result().body().toJsonArray();

                        //noinspection rawtypes
                        List<Future> serviceCalls = serviceInfosJson
                                .stream()
                                .parallel()
                                .map(jsonObject -> JsonObject.mapFrom(jsonObject).mapTo(ServiceInfo.class))
                                .map(serviceInfo -> {
                                    Promise<Message<String>> p = Promise.promise();

                                    vertx.eventBus().<String>request(EventBusAddress.CALL_SERVICE.name(), serviceInfo.url(), serviceStatusReply -> {
                                        if (serviceStatusReply.succeeded()) {
                                            UpdateServiceStatusRequest updateServiceStatusRequest = new UpdateServiceStatusRequest(serviceInfo.id(), ServiceStatus.valueOf(serviceStatusReply.result().body()));

                                            vertx.eventBus().<Buffer>request(
                                                    EventBusAddress.UPDATE_SERVICE_STATUS.name(),
                                                    JsonObject.mapFrom(updateServiceStatusRequest).toBuffer(),
                                                    (updateStatusReply -> {
                                                        if (updateStatusReply.succeeded()) {
                                                            p.complete();
                                                            LOGGER.info(String.format("Checked service ----- %d = %s", serviceInfo.id(), serviceStatusReply.result().body()));
                                                        } else {
                                                            p.fail(updateStatusReply.cause());
                                                            LOGGER.info(String.format("Could not update status for service %d", serviceInfo.id()));
                                                        }
                                                    }));

                                        } else {
                                            p.fail(serviceStatusReply.cause());
                                        }
                                    });

                                    return p.future();
                                })
                                .collect(Collectors.toList());

                        CompositeFuture.all(serviceCalls).onComplete(ar -> pollingStream.resume());

                    } else {
                        LOGGER.error("Failed to retrieve services", servicesReply.cause());
                        pollingStream.resume();
                    }
                }));
    }
}
