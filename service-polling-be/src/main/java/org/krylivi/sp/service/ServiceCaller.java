package org.krylivi.sp.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.client.WebClient;
import org.krylivi.sp.model.EventBusAddress;
import org.krylivi.sp.model.ServiceStatus;

public class ServiceCaller extends AbstractVerticle {

    @Override
    public void start() {
        WebClient client = WebClient.create(vertx);

        vertx.eventBus().<String>consumer(
                EventBusAddress.CALL_SERVICE.name(),
                message -> {
                    String url = message.body();

                    client.getAbs(url)
                            .send()
                            .onSuccess(response -> {
                                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                                    message.reply(ServiceStatus.OK.name());
                                } else {
                                    message.reply(ServiceStatus.FAIL.name());
                                }
                            })
                            .onFailure(e -> message.reply(ServiceStatus.FAIL.name()));
                });
    }
}
