package org.krylivi.sp.repo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
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

import java.util.function.Function;
import java.util.stream.StreamSupport;


public class ServiceInfoRepository extends AbstractVerticle {

    private final MySQLPool msqlClient;
    Function<Row, ServiceInfo> SERVICE_INFO_ROW_MAPPER = row -> new ServiceInfo(
            row.getLong("id"),
            row.getString("name"),
            row.getString("url"),
            row.getString("status") != null ? ServiceStatus.valueOf(row.getString("status")) : null
    );

    public ServiceInfoRepository() {
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
        vertx.eventBus().consumer(EventBusAddress.GET_SERVICE.toString(), message -> {
            this.getOne(Long.parseLong(message.body().toString())).onSuccess(serviceInfo -> {
                message.reply(JsonObject.mapFrom(serviceInfo).toBuffer());
            });
        });

        vertx.eventBus().consumer(EventBusAddress.GET_SERVICES.toString(), message -> {
            this.getAll().onSuccess(rows -> {
                message.reply(new JsonArray(StreamSupport.stream(rows.spliterator(), false).toList()).toBuffer());
            });
        });

        vertx.eventBus().consumer(EventBusAddress.ADD_SERVICE.toString(), message -> {
            AddServiceInfoRequest addServiceInfoRequest = ((Buffer) message.body()).toJsonObject().mapTo(AddServiceInfoRequest.class);
            this.save(addServiceInfoRequest).onSuccess(savedServiceInfo -> {
                message.reply(JsonObject.mapFrom(savedServiceInfo).toBuffer());
            });
        });
    }

    public Future<RowSet<ServiceInfo>> getAll() {
        return this.msqlClient
                .query("select * from service_info")
                .mapping(SERVICE_INFO_ROW_MAPPER)
                .execute();

    }

    public Future<ServiceInfo> getOne(Long id) {
        return this.msqlClient
                .preparedQuery("select * from service_info where id=?")
                .mapping(SERVICE_INFO_ROW_MAPPER)
                .execute(Tuple.of(id))
                .map(serviceInfos -> serviceInfos.iterator().next());

    }

    public Future<ServiceInfo> save(AddServiceInfoRequest addServiceInfoRequest) {
        return this.msqlClient
                .preparedQuery("insert into service_info(name, url) value (?, ?)")
                .execute(Tuple.of(addServiceInfoRequest.name(), addServiceInfoRequest.url()))
                .compose(rows -> {
                    Long id = rows.property(MySQLClient.LAST_INSERTED_ID);
                    return this.getOne(id);
                });
    }

}
