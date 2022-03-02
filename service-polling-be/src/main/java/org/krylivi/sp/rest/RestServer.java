package org.krylivi.sp.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import org.krylivi.sp.model.EventBusAddress;

public class RestServer extends AbstractVerticle {

    private final Logger LOGGER = LoggerFactory.getLogger(RestServer.class);

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);

        router.post("/services").handler(this::createServiceRequestHandler);
        router.get("/services").handler(this::getServicesRequestHandler);
        router.get("/services/:serviceId").handler(this::getServiceRequestHandler);
        router.delete("/services/:serviceId").handler(this::deleteServiceRequestHandler);

        router.route("/eventbus/*").subRouter(eventBusHandler());

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

    private Router eventBusHandler() {
        SockJSBridgeOptions options = new SockJSBridgeOptions().addOutboundPermitted(new PermittedOptions().setAddress("services"))
                .addInboundPermitted(new PermittedOptions().setAddress("services"));
        return SockJSHandler.create(vertx).bridge(options, event -> {
            if (event.type() == BridgeEventType.SOCKET_CREATED) {
                LOGGER.info("A socket was created");
            }
            event.complete(true);
        });
    }

}
