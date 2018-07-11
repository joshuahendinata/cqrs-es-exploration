package com.exploration.cqrs.ecommerce;

import java.util.Map;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;

public class MainVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
	//public static final String CONFIG_WIKIDB_QUEUE = "wikidb.queue";

	private String wikiDbQueue = "wikidb.queue";
	//--redeploy="src/**/*.java,src/**/*.ts,src/**/*.html" --launcher-class=io.vertx.core.Launcher
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		//wikiDbQueue = config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue");

		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);
		// Router for serving angular client app
		router.get("/app/*").handler(StaticHandler.create("client/dist").setCachingEnabled(false));
		router.get("/app/*").handler(context -> {
			context.response().sendFile("client/dist/index.html");
		});
		router.get("/").handler(context -> context.reroute("app/index.html"));
		
		// Router for handling service request
		router.post().handler(BodyHandler.create());
		router.post("/api/*").handler(this::apiDiscoveryHandler)
				.failureHandler(this::failureHandler);
		
		int portNumber = config().getInteger(ProjectConstants.CONFIG_HTTP_SERVER_PORT, 8080);
		server.requestHandler(router::accept).listen(portNumber, ar -> {
			if (ar.succeeded()) {
				LOGGER.info("HTTP server running on port " + portNumber);
				//startFuture.complete();
			} else {
				LOGGER.error("Could not start a HTTP server", ar.cause());
				startFuture.fail(ar.cause());
			}
		});
		
		// For the sake of exploration. query and command verticle will be deployed from here.
		// in production, they can be deployed separately
		vertx.rxDeployVerticle("com.exploration.cqrs.ecommerce.ReadVerticle",
				new DeploymentOptions().setInstances(1))
		.flatMap(id -> {
			return vertx.rxDeployVerticle("com.exploration.cqrs.ecommerce.CommandVerticle",
					new DeploymentOptions().setInstances(1));
		}).subscribe(id -> {
			startFuture.complete();
		}, err -> {
			startFuture.fail(err);	
		});
		
		/*vertx.rxDeployVerticle("com.exploration.cqrs.ecommerce.command.CommandVerticle",
						new DeploymentOptions().setConfig(new JsonObject()
								.put(ProjectConstants.CONFIG_HTTP_SERVER_PORT, 8082)))
				.subscribe(id -> startFuture.complete(), startFuture::fail);*/
	}
	
	private void apiDiscoveryHandler(RoutingContext context) {
		String url = context.request().uri();
		String[] commands = url.substring(5, url.length()).split("/");
		
		if (commands.length < 2) {
			context.response().end("error: invalid command");
		}
		ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
		
		// Get the event bus name from discovery
		discovery.rxGetRecord(rec -> {
			return "event-bus-name".equals(rec.getType()) && commands[0].equals(rec.getName());
		})
		.doFinally(() -> {
			discovery.close();
		})
		.subscribe(res -> {
			if (res == null) {
				LOGGER.error("no matching event bus");
				discovery.close();
				context.response().end("no matching event bus");
				return;
			}
			
			String qName = res.getLocation().getString("qName");
			
			// set message headers
			DeliveryOptions deliveryOptions = new DeliveryOptions()
					.setHeaders(context.request().headers().getDelegate())
					.addHeader("action", commands[1])
					.setSendTimeout(65000);

			// set message body
			JsonObject request = new JsonObject();
			for (Map.Entry<String, String> entry : context.request().formAttributes().getDelegate().entries()) {
				request.put(entry.getKey(), entry.getValue());
			}

			// send via vertx event bus
			vertx.eventBus().<JsonObject>rxSend(qName, request, deliveryOptions)
			.subscribe(reply -> {
				// set the response from the receiving verticle
				context.response().headers().addAll(reply.headers());
				context.response().end(reply.body().encode());
			}, err -> {
				LOGGER.error("error after sending message to event bus", err);
				context.fail(err);
			});
		});
		
		// For HTTP microservice communication
		/*HttpEndpoint.rxGetWebClient(discovery,
			rec -> rec.getName().startsWith(commands[0])
		)
		.flatMap(client -> 
			client.post("/" + commands[1]).as(BodyCodec.string())
			.rxSend()
			.doOnError(err -> {
				LOGGER.error(err);
				context.response().end(err.getMessage());
			})
		)
		.doFinally(() -> discovery.close())
		.doOnError(err -> {
			LOGGER.error(err);
			context.response().end(err.getMessage());
		})
		.subscribe(res -> {
			LOGGER.debug(res.body());
			context.response().end(res.body());
		});*/
	}
	
	private void failureHandler(RoutingContext context) {
		context.reroute("/app/errorPage");
	}
}
