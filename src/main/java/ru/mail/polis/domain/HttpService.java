package ru.mail.polis.domain;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.KVService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpService implements KVService {

    @NotNull private final HttpServer server;
    @NotNull private final Repository repository;

    public HttpService(int port, @NotNull Repository repository) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.repository = repository;
        this.server.createContext("/v0/status", withErrors(online()));
        this.server.createContext("/v0/entity", withErrors(entity()));
    }

    @NotNull private static HttpHandler withErrors(HttpHandler delegate) {
        return http -> {
            try {
                delegate.handle(http);
            } catch (NoSuchElementException ex) {
                http.sendResponseHeaders(404, 0);
                http.close();
            } catch (IllegalArgumentException ex) {
                http.sendResponseHeaders(400, 0);
                http.close();
            } catch (Exception ex) {
                final byte[] message = ex.getMessage().getBytes(UTF_8);
                http.sendResponseHeaders(503, message.length);
                http.getResponseBody().write(message);
                http.close();
            }
        };
    }

    @NotNull private static String extractId(@NotNull String query) {
        final String prefix = "id=";
        if (!query.startsWith(prefix)) {
            throw new IllegalArgumentException("Garbage in query");
        } else if (query.length() == prefix.length()) {
            throw new IllegalArgumentException("Empty id");
        } else {
            return query.substring(prefix.length());
        }
    }

    @NotNull private HttpHandler entity() {
        return http -> entityHandler(
                http.getRequestMethod(),
                extractId(http.getRequestURI().getQuery())
        ).handle(http);
    }

    @NotNull private HttpHandler entityHandler(@NotNull String method, @NotNull String id) {
        switch (method) {
            case "GET":
                return getEntity(id);
            case "PUT":
                return putEntity(id);
            case "DELETE":
                return deleteEntity(id);
            default:
                return methodNotAllowed();
        }
    }

    @NotNull private HttpHandler methodNotAllowed() {
        return http -> {
            http.sendResponseHeaders(405, 0);
            http.close();
        };
    }

    @NotNull private HttpHandler deleteEntity(@NotNull String id) {
        return http -> {
            repository.delete(id);
            http.sendResponseHeaders(202, 0);
            http.close();
        };
    }

    @NotNull private HttpHandler putEntity(@NotNull String id) {
        return http -> {
            final int contentLength = Integer.parseInt(http.getRequestHeaders().getFirst("Content-Length"));
            final byte[] body = new byte[contentLength];
            http.getRequestBody().read(body);
            repository.upsert(id, body);
            http.sendResponseHeaders(201, 0);
            http.close();
        };
    }

    @NotNull private HttpHandler getEntity(@NotNull String id) {
        return http -> {
            final byte[] bytes = repository.get(id);
            http.sendResponseHeaders(200, bytes.length);
            http.getResponseBody().write(bytes);
            http.close();
        };
    }

    @NotNull private HttpHandler online() {
        return http -> {
            final byte[] body = "ONLINE".getBytes(UTF_8);
            http.sendResponseHeaders(200, body.length);
            http.getResponseBody().write(body);
            http.close();
        };
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop() {
        server.stop(0);
    }
}
