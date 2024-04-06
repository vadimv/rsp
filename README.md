# rsp
[![javadoc](https://javadoc.io/badge2/io.github.vadimv/rsp/javadoc.svg)](https://javadoc.io/doc/io.github.vadimv/rsp)
[![maven version](https://img.shields.io/maven-central/v/io.github.vadimv/rsp)](https://search.maven.org/search?q=io.github.vadimv)

* [Maven Central](#maven-central)
* [Code examples](#code-examples)
* [Routing](#routing)  
* [HTML markup Java DSL](#html-markup-java-dsl)
* [SPAs, plain pages head tag](#spas-plain-pages-and-the-head-tag)
* [Page HTTP status code and HTTP headers](#page-http-status-code-and-http-headers)
* [UI components](#ui-components)
* [How to use components](#how-to-use-components)
* [How to write your own component](#how-to-use-components)
* [DOM events](#dom-events)
* [Navigation bar URL path and components state mapping](#navigation-bar-url-path-and-components-state-mapping)
* [DOM elements references](#dom-elements-references)
* [Evaluating code on the client-side](#evaluating-js-code-on-client-side)
* [Navigation bar URL path](#navigation-bar-url-path-and-components-state-mapping)
* [Page lifecycle events](#page-lifecycle-events)
* [Application and server's configuration](#application-servers-configuration)
* [Schedules and timers](#schedules)
* [Logging](#logging)
* [How to build the project and run tests](#how-to-build-the-project-and-run-tests)


rsp is a lightweight modern server-side web framework for Java.

With rsp, for a typical web application, two types of web pages are supported:
- single-page applications (SPAs), written in Java, e.g. for an admin UI
- plain server-rendered detached HTML pages

### Maven Central

This project requires Java version 17 or newer.

To start using it, add the dependency:
```xml
    <dependency>
        <groupId>io.github.vadimv</groupId>
        <artifactId>rsp</artifactId>
        <version>3.0</version>
    </dependency>
```

### Code examples

* [Hello World](src/main/java/rsp/examples/HelloWorld.java)
* [Plain form](src/main/java/rsp/examples/PlainForm.java)
* [TODOs list](https://github.com/vadimv/rsp-todo-list)
* [Tetris](https://github.com/vadimv/rsp-tetris)
* [Conway's Game of Life](https://github.com/vadimv/rsp-game-of-life)
* [Hacker News API client](https://github.com/vadimv/rsp-hn)

### Routing

A page's initial HTML generation consists of two phases:
- routing an incoming HTTP request and/or URL paths results in the page's components state
- rendering these state objects by the components' views generates HTML markup

To define a routing of an incoming request, create a ``Routing`` object for components and/or provide it as an application's constructor parameter:

```java
    import static rsp.html.RoutingDsl.*;
    ...
    final App<State> app = new App<>(routes(), render());
    ...
    private static Routing<HttpRequest, State> routes() {
        final var db = new Database();
        return routing(concat(get("/articles", req -> db.getArticles().thenApply(articles -> State.ofArticles(articles))),
                              get("/articles/:id", (__, id) -> db.getArticle(id).thenApply(article -> State.ofArticle(article))),
                              get("/users/:id", (__, id) -> db.getUser(id).thenApply(user -> State.ofUser(user))),
                              post("/users/:id(^\\d+$)", (req, id) -> db.setUser(id, req.queryParam("name")).thenApply(result -> State.userWriteSuccess(result)))),
                       NotFound.INSTANCE);
    }
```

During a dispatch, the routes are verified one by one for a matching HTTP method and a path pattern. 
Route path patterns can include zero, one or two path-variables, possibly combined with regexes and the wildcard symbol "*".
The matched variables values become available as the correspondent handlers' parameters alongside with the request details object.
The route's handler function should return a ``CompletableFuture`` of the page's state:

```java
    get("/users/*", req -> CompletableFuture.completedFuture(State.ofUsers(List.of(user1, user2))))
```

If needed, extract a paths-specific routing section:

```java
    final Routing<HttpRequest, State> routing = routing(get(__ -> paths()),
                                                        State.pageNotFound());
    
    private static PathRoutes<State> paths() {
         return concat(path("/articles", db.getArticles().thenApply(articles -> State.ofArticles(articles))),
                       path("/articles/:id", id -> db.getArticle(id).thenApply(article -> State.ofArticle(article)));
    }
```

Use ``match()`` DSL function routing to implement custom matching logic, for example:

```java
    match(req -> req.queryParam("name").isPresent(), req -> CompletableFuture.completedFuture(State.of(req.queryParam("name"))))
```

The ``any()`` route matches every request.

### HTML markup Java DSL

rsp provides a Java internal domain-specific language (DSL) for declarative definition of HTML templates as a composition of functions.

For example, to re-write the HTML fragment:

```html
<!DOCTYPE html>
<html>    
    <body>
        <h1>This is a heading</h1>
        <div class="par">
            <p>This is a paragraph</p>
            <p>Some dynamic text</p>
        </div>
    </body>
</html> 
```

provide the DSL Java code as below:

```java
    import static rsp.html.HtmlDsl.*;
    ...
    final View<State> view = state -> html(
                                          body(
                                               h1("This is a heading"),
                                               div(attr("class", "par"), 
                                                   p("This is a paragraph"),
                                                   p(state.text)) // adds a paragraph with a text from the state object
                                              ) 
                                        );
```
where:
- HTML tags are represented by the ``rsp.html.HtmlDsl`` class' methods with same names, e.g. ``<div></div>``  translates to ``div()``
- HTML attributes are represented by the ``rsp.html.HtmlDsl.attr(name, value)`` function, e.g. ``class="par"`` translates to ``attr("class", "par")``
- the lambda's parameter `state` is the current state object

The utility ``of()`` DSL function renders a ``Stream<T>`` of objects, e.g. a list, or a table rows:
```java
    import static rsp.html.HtmlDsl.*;
    ...
    state -> ul(of(state.items.stream().map(item -> li(item.name))))
```

An overloaded variant of ``of()`` accepts a ``CompletableFuture<S>``:
```java
    final Function<Long, CompletableFuture<String>> lookupService = userDetailsByIdService(); 
    ...
     // consider that at this moment we know the current user's Id
    state -> newState -> div(of(lookupService.apply(state.user.id).map(str -> text(str))))
```

Another overloaded ``of()`` function takes a ``Supplier<S>`` as its argument and allows inserting code fragments
with imperative logic.
```java
    import static rsp.html.HtmlDsl.*;
    ...
    state -> of(() -> {
                     if (state.showInfo) {
                         return p(state.info);
                     } else {
                         return p("none");
                     }       
                 })
```

The ``when()`` DSL function conditionally renders (or not) an element:

```java
    state -> when(state.showLabel, span("This is a label"))
```

### SPAs, plain pages and the head tag

The page's ``<head>`` tag DSL determines if this page is an SPA or plain.

The ``head(...)`` or ``head(PageType.SPA, ...)`` function creates an HTML page ``<head>`` tag for an SPA.
If the ``head()`` is not present in the page's markup, the simple SPA-type header is added automatically.
This type of head injects a script, which establishes a WebSocket connection between the browser's page and the server
and enables reacting to the browser events.

Using ``head(HeadType.PLAIN, ...)`` renders the markup with the ``<head>`` tag without injecting of init script
to establish a connection with server and enable server side events handling for SPA.
This results in rendering of a plain detached HTML page.

### Page HTTP status code and HTTP headers

The ``statusCode()`` and ``addHeaders()`` methods enable to change result response HTTP status code and headers.
For example:

```java
    __ -> html(   
                  head(HeadType.PLAIN, title("404 page not found")),
                  body(
                       div(
                           p("404 page not found")
                      ) 
                    )
                ).statusCode(404);
```
### UI components

Pages are composed of components of two kinds:
- Views
- Stateful components

A view is a pure function from an input state to a DOM fragment's definition.

```java
    public static View<State> appView = state -> 
        switch (state) {
            case NotFoundState nf -> statelessComponent(nf, notFoundStatelessView);
            case UserState   user -> component(user, userComponentView);
            case UsersState users -> component(users, usersComponentView);
        }
        
     appView.apply(new UserState("Username"));
```

A stateful component has its own state, represented by a snapshot of an immutable class or a record and managed by
the framework.
A component's state is modelled as a finite state machine (FSM) and can be changed by invoking 
the stages of a component's lifecycle:
- component's state initialization during its first render and mount to the components tree
- events handling
- restoring state after mounting again to the same position

An initial state can be provided is the following ways:
- set in its initiation function to some value
- mapped from an HTTP request, browser's address bar path and query

Any state transition must be initiated by invoking of one of the provided parameter's ``NewState`` interface methods, like ``set()`` and ``apply()``.
State transitions are can be triggered by browser events, notifications or timer events.

The root of its page's components tree is a top-level stateful component. 

### How to use components

Include a new instance of component definition class to the DSL alongside HTML tags.

```java
    div(span("Two counters"),
        new Counter(1),
        new Counter(2))
```

Warning: note that a component's code is executed on the server and in the browser, use only components you trust.

### How to write your own component

To create you own component inherit from one of the base component definition classes or use one of the ComponentDsl functions.

```java
    import static rsp.component.ComponentDsl.*;

    static ComponentView<String> buttonView = state -> newState -> input(attr("type", "button"),
                                                                         attr("value", state),      
                                                                         on("click", ctx -> newState.set("Clicked")));
    static SegmentDefinition buttonComponent(String text) {
            return component(text,        // the component's initial state
                             buttonView)
    }
    
    ...
    div(
        span("Click the button below"),
        buttonComponent("Ready");
    )   
    ...
```

The following example shows how a component's state can be modelled using records and a sealed interface:

```java
    sealed interface State permits NotFoundState, UserState, UsersState {}

    record NotFoundState() implements State {};
    record UserState(User user) implements State {}
    record UsersState(List<User> users) implements State {}
    
    record User(long id, String name) {}
```

### DOM events

To respond to browser events, register a DOM event handler by adding an ``on(eventType, handler)`` to an HTML tag in the Java DSL:

```java
    var view = state -> newState-> a("#","Click me",on("click", ctx-> {
                                                        System.out.println("Clicked " + state.counter() + " times");
                                                        newState.set(new State(s.counter() + 1));
    }));

    record State(int counter) {}
```

This is how it works when an event occurs:
- the browser's page sends the event data message to the server via WebSocket
- the system fires its registered event handler's Java code
- an event handler's code sets its component's state snapshot, calling the ``NewState.set()`` method.

A new set state snapshot triggers the following sequence of actions:
- the page's virtual DOM re-rendered on the server
- the difference between the current and the previous DOM trees is calculated  
- the diff commands sent to the browser
- the page's JS code updates the presentation

Some types of browser events, like a mouse move, may fire a lot of events' invocations. 
Sending all these notifications over the network and processing them on the server side may cause the system's overload.
To filter the events before sending use the following event object's methods:
- ``throttle(int timeFrameMs)``
- ``debounce(int waitMs, boolean immediate)``

```java
    html(window().on("scroll", ctx -> {
            System.out.println("A throtteld page scroll event");
            }).throttle(500),
        ...
        )
```

The context's ``eventObject()`` method reads the event's object as a JSON data structure:

```java
    form(on("submit", ctx -> {
            // Prints the submitted form's input field value
            System.out.println(ctx.eventObject());
         }),
        input(attr("type", "text"), attr("name", "val")),
        input(attr("type", "button"), attr("value", "Submit"))
    )
```
The ``window().on(eventType, handler)`` DSL function registers a browser's window object event handler:

```java
    html(window().on("click", ctx -> {
            System.out.println("window clicked");
        }),
        ...
        )
```

### Navigation bar URL path and components state mapping

A component's state can be mapped to the browser's navigation bar path. 
With that, the current navigation path will be converted to a component's state and vis-a-versa,
the state transition will cause the navigation path to be updated accordingly.

The "Back" and "Forward" browser's history buttons clicks initiate state transitions.

This is an example, where the first element of a path is mapped to an integer id, and id is mapped to the first element of the path:

```java
    import static rsp.component.ComponentDsl.*;

    static SegmentDefinition component() {
        return component(new Routing<>(path("/:id(^\\d+$)/*", id -> CompletableFuture.completedFuture(Integer.parseInt(id))), -1),
                         (id, path) -> Path.of("/" + id + "/" + path.get(0)),
                         componentView());
    }

```
If not configured, the default state-to-path mapping sets an empty path for any state.

### DOM elements references

The ``propertiesByRef()`` method of the event context object's allows access to client-side document elements properties values providing elements references.

```java
    final ElementRef inputRef = createElementRef();
    ...
    input(elementId(inputRef),
          attr("type", "text")),
    a("#", "Click me", on("click", ctx -> {
            var props = ctx.propertiesByRef(inputRef);
            props.getString("value").thenAccept(value -> { 
                                                  System.out.println("Input element's value: " + value);
                                                  props.set("value", "new text");           
            });     
    }))
```

A reference to an object also can be created on-the-fly using ``RefDefinition.withKey()`` method.

There is also the special ``window().ref()`` reference for the page's window object.


### Evaluating JS code on client-side

To invoke arbitrary EcmaScript code in the browser use the ``evalJs()`` method of an event's context object.
``evalJs()`` sends a code fragment to the client and returns its evaluation result as a  ``CompletableFuture<JsonDataType>``.

```java
    ...
        button(attr("type", "button"),
               text("Alert"),
               on("click",
                  ctx -> ctx.evalJs("alert('Hello from the server')"))),
        button(attr("type", "button"),
               text("Calculate"),
               on("click",
                  ctx -> ctx.evalJs("1+1").whenComplete((r,e) -> System.out.println("1+1=" + r)))
   ...
```

### Page lifecycle events

To listen to an SPA page's lifecycle events, provide an implementation of the ``PageLifecycle`` interface as a parameter
of the application's object constructor.
This allows to run some specific code
- after the page is created
- after the page is closed

```java
    final PageLifeCycle<Integer> plc = new PageLifeCycle.Default<Integer>() {
        @Override
        public void pageCreated(QualifiedSessionId sid, Integer initialState, NewState<Integer> newState) {
            final Thread t = new Thread(() -> {
                try {
                    Thread.sleep(10_000);
                    newState.apply(s -> s + 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            t.start();
        }
    };
    
    final App<Optional<FullName>> app = new App<>(route(), pages()).withPagelifeCycle(plc);
    ...
```
Use these listeners to subscribe to some messages stream on a page live session creation and unsubscribing when the page closes.

### Application server's configuration

Use an instance of the ``AppConfig`` class as the parameter to the ``withConfig()`` method of an ``App`` object:

```java
    final var app = new App(routing(), rootCreateViewFunction()).withConfig(AppConfig.DEFAULT);
```
A web server's ``rsp.jetty.JettyServer`` class' constructor accepts extra parameters like an optional static resources' handler 
and a TLS/SSL configuration:

```java
    final var staticResources = new StaticResources(new File("src/main/java/rsp/tetris"), "/res/*");
    final var sslConfig = SslConfiguration("/keysore/path", "changeit");
    final var server = new JettyServer(8080, app, staticResources, sslConfig);
    server.start();
    server.join();
```

### Logging

This project's uses ``System.Logger`` for server-side logging.

On the client-side, to enable detailed diagnostic data exchange logging, enter in the browser console:

```javascript
  RSP.setProtocolDebugEnabled(true)
```

### Schedules

The framework provides the ``EventContext.schedule()`` and ``EventContext.scheduleAtFixedRate()`` methods 
which allows to submit a delayed or periodic action that can be cancelled.
Provide a timer's reference parameter when creating a new schedule, later use this reference for the schedule cancellation.

```java
    final static TimerRef TIMER_0 = TimerRef.createTimerRef();
    ...
    button(attr("type", "button"),
           text("Start"),
           on("click", c -> c.scheduleAtFixedRate(() -> System.out.println("Timer event")), TIMER_0, 0, 1, TimeUnit.SECONDS))),
    button(attr("type", "button"),
           text("Stop"),
           on("click", c -> c.cancelSchedule(TIMER_0)))
```

### How to build the project and run tests

To build the project from the sources, run:

```shell script
$ mvn clean package
```

Run all the system tests:

```shell script

$ mvn clean test -Ptest-all
```