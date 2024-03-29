package rsp.server.http;

import java.util.Objects;

public record HttpStateOrigin(HttpRequest httpRequest, RelativeUrl relativeUrl) {
    public HttpStateOrigin {
        Objects.requireNonNull(httpRequest);
        Objects.requireNonNull(relativeUrl);
    }
}
