package rsp.component;

import rsp.server.Path;
import rsp.server.http.HttpRequest;

import java.util.function.BiFunction;

public abstract class HttpRequestStatefulComponentDefinition<S> extends StatefulComponentDefinition<HttpRequest, S> {

    @Override
    protected Class<HttpRequest> stateFunctionInputClass() {
        return HttpRequest.class;
    }

    @Override
    protected BiFunction<S, Path, Path> state2pathFunction() {
        return (s, p) -> p;
    }
}
