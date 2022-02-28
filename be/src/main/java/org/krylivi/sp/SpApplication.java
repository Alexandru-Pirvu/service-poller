package org.krylivi.sp;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.krylivi.sp.repo.ServiceInfoRepository;
import org.krylivi.sp.rest.RestServer;
import org.krylivi.sp.service.ServiceCaller;
import org.krylivi.sp.service.ServicePoller;

public class SpApplication {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new RestServer());
        vertx.deployVerticle(new ServiceInfoRepository());
        vertx.deployVerticle(new ServiceCaller());
        vertx.deployVerticle(new ServicePoller(), new DeploymentOptions().setWorker(true));
    }

}
