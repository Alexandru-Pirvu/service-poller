package org.krylivi.sp;

import io.vertx.core.Vertx;
import org.krylivi.sp.rest.RestServer;
import org.krylivi.sp.repo.ServiceInfoRepository;

public class SpApplication {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new RestServer());
        vertx.deployVerticle(new ServiceInfoRepository());
    }

}
