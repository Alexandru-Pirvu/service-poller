package org.krylivi.sp.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.krylivi.sp.model.EventBusAddress;

public class RestServer extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);

        router.post("/services").handler(this::createServiceRequestHandler);
        router.get("/services").handler(this::getServicesRequestHandler);
        router.get("/services/:serviceId").handler(this::getServiceRequestHandler);
        router.delete("/services/:serviceId").handler(this::deleteServiceRequestHandler);

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

    private void createServiceRequestHandler(RoutingContext routingContext) {
        Handler<Buffer> successHandler =
                buffer -> vertx.eventBus().request(
                        EventBusAddress.ADD_SERVICE.toString(),
                        buffer,
                        reply -> {
                            if (reply.succeeded()) {
                                routingContext.response().send(reply.result().body().toString());
                            } else {
                                routingContext.response().send(reply.cause().getMessage());
                            }
                        });

        routingContext
                .request()
                .body()
                .onSuccess(successHandler);
    }

    private void getServicesRequestHandler(RoutingContext routingContext) {
        vertx.eventBus().request(
                EventBusAddress.GET_SERVICES.toString(),
                null,
                reply -> {
                    if (reply.succeeded()) {
                        routingContext.response().send(reply.result().body().toString());
                    } else {
                        routingContext.response().send(reply.cause().getMessage());
                    }
                });
    }

    private void getServiceRequestHandler(RoutingContext routingContext) {
        vertx.eventBus().request(
                EventBusAddress.GET_SERVICE.toString(),
                routingContext.pathParam("serviceId"),
                reply -> {
                    if (reply.succeeded()) {
                        routingContext.response().send(reply.result().body().toString());
                    } else {
                        routingContext.response().send(reply.cause().getMessage());
                    }
                });
    }

    private void deleteServiceRequestHandler(RoutingContext routingContext) {
        vertx.eventBus().request(
                EventBusAddress.DELETE_SERVICE.toString(),
                routingContext.pathParam("serviceId"),
                reply -> {
                    if (reply.succeeded()) {
                        routingContext.response().send(Boolean.TRUE.toString());
                    } else {
                        routingContext.response().send(reply.cause().getMessage());
                    }
                });
    }

}
