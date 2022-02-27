package org.krylivi.sp.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import org.krylivi.sp.model.EventBusAddress;

public class RestServer extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);
        router.post("/services")
                .handler(routingContext ->
                        routingContext
                                .request()
                                .body()
                                .onSuccess(buffer ->
                                        vertx
                                                .eventBus()
                                                .request(EventBusAddress.ADD_SERVICE.toString(),
                                                        buffer,
                                                        reply -> routingContext.response().send(reply.result().body().toString()))));

        router.get("/services")
                .handler(routingContext ->
                        vertx
                                .eventBus()
                                .request(EventBusAddress.GET_SERVICES.toString(),
                                        null,
                                        reply -> routingContext.response().send(reply.result().body().toString())));

        router.get("/services/:serviceId")
                .handler(routingContext ->
                        vertx
                                .eventBus()
                                .request(EventBusAddress.GET_SERVICE.toString(),
                                        routingContext.pathParam("serviceId"),
                                        reply -> routingContext.response().send(reply.result().body().toString())));

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        startPromise.complete();
                    } else {
                        startPromise.fail(result.cause());
                    }
                });
    }

}
