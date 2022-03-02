package org.krylivi.sp.repo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.krylivi.sp.model.EventBusAddress;
import org.krylivi.sp.model.ServiceInfo;
import org.krylivi.sp.model.ServiceStatus;
import org.krylivi.sp.rest.dto.AddServiceInfoRequest;
import org.krylivi.sp.service.dto.UpdateServiceStatusRequest;

import java.util.function.Function;
import java.util.stream.StreamSupport;


public class ServiceInfoRepository extends AbstractVerticle {

    private MySQLPool msqlClient;
    Function<Row, ServiceInfo> SERVICE_INFO_ROW_MAPPER = row -> new ServiceInfo(
            row.getLong("id"),
            row.getString("name"),
            row.getString("url"),
            row.getString("status") != null ? ServiceStatus.valueOf(row.getString("status")) : null
    );

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        MySQLConnectOptions mySQLConnectOptions = new MySQLConnectOptions()
                .setPort(3309)
                .setHost("localhost")
                .setDatabase("dev")
                .setUser("dev")
                .setPassword("secret");
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

        this.msqlClient = MySQLPool.pool(vertx, mySQLConnectOptions, poolOptions);
    }

    @Override
    public void start() {
        vertx.eventBus().consumer(
                EventBusAddress.GET_SERVICE.toString(),
                message -> this.getOne(Long.parseLong(message.body().toString()))
                        .onSuccess(serviceInfo -> message.reply(JsonObject.mapFrom(serviceInfo).toBuffer()))
                        .onFailure(e -> message.fail(0, e.getMessage())));

        vertx.eventBus().consumer(
                EventBusAddress.GET_SERVICES.toString(),
                message -> this.getAll()
                        .onSuccess(rows -> message.reply(new JsonArray(StreamSupport.stream(rows.spliterator(), false).toList()).toBuffer()))
                        .onFailure(e -> message.fail(0, e.getMessage())));

        vertx.eventBus().consumer(
                EventBusAddress.DELETE_SERVICE.toString(),
                message -> this.deleteService(Long.parseLong(message.body().toString()))
                        .onSuccess(rows -> message.reply(true))
                        .onFailure(e -> message.fail(0, e.getMessage())));

        vertx.eventBus().consumer(
                EventBusAddress.ADD_SERVICE.toString(),
                message -> {
                    AddServiceInfoRequest addServiceInfoRequest = ((Buffer) message.body()).toJsonObject().mapTo(AddServiceInfoRequest.class);
                    this.save(addServiceInfoRequest)
                            .onSuccess(savedServiceInfo -> message.reply(JsonObject.mapFrom(savedServiceInfo).toBuffer()))
                            .onFailure(e -> message.fail(0, e.getMessage()));
                });

        vertx.eventBus().<Buffer>consumer(
                EventBusAddress.UPDATE_SERVICE_STATUS.toString(),
                message -> {
                    UpdateServiceStatusRequest updateServiceStatusRequest = message.body().toJsonObject().mapTo(UpdateServiceStatusRequest.class);
                    this.updateServiceStatus(updateServiceStatusRequest)
                            .onSuccess(savedServiceInfo -> message.reply(JsonObject.mapFrom(savedServiceInfo).toBuffer()))
                            .onFailure(e -> message.fail(0, e.getMessage()));
                });
    }

    private Future<RowSet<ServiceInfo>> getAll() {
        return this.msqlClient
                .query("select * from service_info")
                .mapping(SERVICE_INFO_ROW_MAPPER)
                .execute();
    }

    private Future<ServiceInfo> getOne(Long id) {
        return this.msqlClient
                .preparedQuery("select * from service_info where id=?")
                .mapping(SERVICE_INFO_ROW_MAPPER)
                .execute(Tuple.of(id))
                .map(serviceInfos -> serviceInfos.iterator().next());
    }

    private Future<ServiceInfo> save(AddServiceInfoRequest addServiceInfoRequest) {
        return this.msqlClient
                .preparedQuery("insert into service_info(name, url) value (?, ?)")
                .execute(Tuple.of(addServiceInfoRequest.name(), addServiceInfoRequest.url()))
                .compose(rows -> {
                    Long id = rows.property(MySQLClient.LAST_INSERTED_ID);
                    return this.getOne(id);
                });
    }

    private Future<ServiceInfo> updateServiceStatus(UpdateServiceStatusRequest updateServiceStatusRequest) {
        return this.msqlClient
                .preparedQuery("update service_info set status=? where id=?")
                .execute(Tuple.of(updateServiceStatusRequest.status(), updateServiceStatusRequest.id()))
                .compose(rows -> this.getOne(updateServiceStatusRequest.id()));
    }

    private Future<Long> deleteService(Long id) {
        return this.msqlClient
                .preparedQuery("delete from service_info where id=?")
                .execute(Tuple.of(id))
                .compose(rows -> {
                    Long deletedId = rows.property(MySQLClient.LAST_INSERTED_ID);
                    return Future.succeededFuture(deletedId);
                });
    }

}
