# reactor-acquire-cancel-issue-demo

When the pending queue is full and `SimpleDequePool` culls pending borrowers with a `PoolAcquirePendingLimitException`, the borrower countdown does not appear to be stopped via `AbstractPool.Borrower#fail`. As a result, a `PoolAcquireTimeoutException` is later triggered after `ConnectionProvider#pendingAcquireTimeout (45s)` elapses.

### Steps to reproduce

1. Start `Application.java (:8080)`
2. Send at least 3 parallel requests to `/test`.
    * Used [`ab`](https://linux.die.net/man/1/ab) for testing. `ab -c 3 -n 3 localhost:8080/test`
    * Increase number of parallel requests if this did not trigger the result from step 3. 
3. Expect a `PoolAcquirePendingLimitException` to be thrown, along with a logged `PoolAcquireTimeoutException` from `Hooks.onOperatorError` after 45 seconds.

### Logs

```
 .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.5.4)

2025-12-15T17:52:05.011+01:00  INFO 29147 --- [demo-3.5.4] [           main] com.example.demo.Application             : Starting Application using Java 21.0.3 with PID 29147 (/Users/dfilipov/projects/misc/demo-3.5.4/target/classes started by dfilipov in /Users/dfilipov/projects/misc/demo-3.5.4)
2025-12-15T17:52:05.012+01:00  INFO 29147 --- [demo-3.5.4] [           main] com.example.demo.Application             : No active profile set, falling back to 1 default profile: "default"
2025-12-15T17:52:05.361+01:00  INFO 29147 --- [demo-3.5.4] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2025-12-15T17:52:05.365+01:00  INFO 29147 --- [demo-3.5.4] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-12-15T17:52:05.365+01:00  INFO 29147 --- [demo-3.5.4] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.43]
2025-12-15T17:52:05.379+01:00  INFO 29147 --- [demo-3.5.4] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-12-15T17:52:05.380+01:00  INFO 29147 --- [demo-3.5.4] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 354 ms
2025-12-15T17:52:05.646+01:00  INFO 29147 --- [demo-3.5.4] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2025-12-15T17:52:05.666+01:00  INFO 29147 --- [demo-3.5.4] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
2025-12-15T17:52:05.672+01:00  INFO 29147 --- [demo-3.5.4] [           main] com.example.demo.Application             : Started Application in 0.803 seconds (process running for 1.097)
2025-12-15T17:52:05.790+01:00  INFO 29147 --- [demo-3.5.4] [)-192.168.0.152] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-12-15T17:52:05.790+01:00  INFO 29147 --- [demo-3.5.4] [)-192.168.0.152] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-12-15T17:52:05.791+01:00  INFO 29147 --- [demo-3.5.4] [)-192.168.0.152] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
2025-12-15T17:52:13.417+01:00 DEBUG 29147 --- [demo-3.5.4] [nio-8080-exec-2] r.netty.resources.DefaultLoopIOUring     : Default io_uring support : false
2025-12-15T17:52:13.420+01:00 DEBUG 29147 --- [demo-3.5.4] [nio-8080-exec-2] r.netty.resources.DefaultLoopEpoll       : Default Epoll support : false
2025-12-15T17:52:13.420+01:00 DEBUG 29147 --- [demo-3.5.4] [nio-8080-exec-2] r.netty.resources.DefaultLoopKQueue      : Default KQueue support : false
2025-12-15T17:52:13.442+01:00 ERROR 29147 --- [demo-3.5.4] [nio-8080-exec-2] i.n.r.d.DnsServerAddressStreamProviders  : Unable to load io.netty.resolver.dns.macos.MacOSDnsServerAddressStreamProvider, fallback to system defaults. This may result in incorrect DNS resolutions on MacOS. Check whether you have a dependency on 'io.netty:netty-resolver-dns-native-macos'. Use DEBUG level to see the full stack: java.lang.UnsatisfiedLinkError: failed to load the required native library
2025-12-15T17:52:13.444+01:00 DEBUG 29147 --- [demo-3.5.4] [nio-8080-exec-2] r.n.resources.PooledConnectionProvider   : Creating a new [default] client pool [PoolFactory{evictionInterval=PT0S, leasingStrategy=fifo, maxConnections=1, maxIdleTime=55000, maxLifeTime=-1, metricsEnabled=true, pendingAcquireMaxCount=1, pendingAcquireTimeout=45000}] for [google.com/<unresolved>:443]
2025-12-15T17:52:13.465+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.resources.PooledConnectionProvider   : [82822adc] Created a new pooled channel, now: 0 active connections, 0 inactive connections 0 pending acquire requests.
2025-12-15T17:52:13.534+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] Registering pool release on close event for channel
2025-12-15T17:52:13.534+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.resources.PooledConnectionProvider   : [82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] Channel connected, now: 1 active connections, 0 inactive connections 0 pending acquire requests.
2025-12-15T17:52:13.659+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] onStateChange(PooledConnection{channel=[id: 0x82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443]}, [connected])
2025-12-15T17:52:13.663+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc-1, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] onStateChange(GET{uri=/, connection=PooledConnection{channel=[id: 0x82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443]}}, [configured])
2025-12-15T17:52:13.663+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc-1, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] onStateChange(GET{uri=/, connection=PooledConnection{channel=[id: 0x82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443]}}, [request_prepared])
2025-12-15T17:52:13.665+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc-1, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] onStateChange(GET{uri=/, connection=PooledConnection{channel=[id: 0x82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443]}}, [request_sent])
2025-12-15T17:52:13.722+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc-1, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] onStateChange(GET{uri=/, connection=PooledConnection{channel=[id: 0x82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443]}}, [response_received])
2025-12-15T17:52:13.728+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] onStateChange(GET{uri=/, connection=PooledConnection{channel=[id: 0x82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443]}}, [response_completed])
2025-12-15T17:52:13.728+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] onStateChange(GET{uri=/, connection=PooledConnection{channel=[id: 0x82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443]}}, [disconnecting])
2025-12-15T17:52:13.728+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] Releasing channel
2025-12-15T17:52:13.729+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.resources.PooledConnectionProvider   : [82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] Channel cleaned, now: 0 active connections, 1 inactive connections 0 pending acquire requests.
2025-12-15T17:52:13.732+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.resources.PooledConnectionProvider   : [82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] Channel acquired, now: 1 active connections, 0 inactive connections 1 pending acquire requests.
2025-12-15T17:52:13.732+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc-2, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] onStateChange(GET{uri=/, connection=PooledConnection{channel=[id: 0x82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443]}}, [request_prepared])
2025-12-15T17:52:13.732+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc-2, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] onStateChange(GET{uri=/, connection=PooledConnection{channel=[id: 0x82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443]}}, [request_sent])
2025-12-15T17:52:13.735+01:00 ERROR 29147 --- [demo-3.5.4] [nio-8080-exec-3] com.example.demo.Application             : onOperatorError:

reactor.netty.internal.shaded.reactor.pool.PoolAcquirePendingLimitException: Pending acquire queue has reached its maximum size of 1
	at reactor.netty.internal.shaded.reactor.pool.SimpleDequePool.pendingOffer(SimpleDequePool.java:612) ~[reactor-netty-core-1.2.8.jar:1.2.8]
	at reactor.netty.internal.shaded.reactor.pool.SimpleDequePool.doAcquire(SimpleDequePool.java:306) ~[reactor-netty-core-1.2.8.jar:1.2.8]
	at reactor.netty.internal.shaded.reactor.pool.AbstractPool$Borrower.request(AbstractPool.java:425) ~[reactor-netty-core-1.2.8.jar:1.2.8]
	at reactor.core.publisher.FluxContextWrite$ContextWriteSubscriber.request(FluxContextWrite.java:136) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.netty.resources.DefaultPooledConnectionProvider$DisposableAcquire.onSubscribe(DefaultPooledConnectionProvider.java:218) ~[reactor-netty-core-1.2.8.jar:1.2.8]
	at reactor.core.publisher.FluxContextWrite$ContextWriteSubscriber.onSubscribe(FluxContextWrite.java:101) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.netty.internal.shaded.reactor.pool.SimpleDequePool$QueueBorrowerMono.subscribe(SimpleDequePool.java:743) ~[reactor-netty-core-1.2.8.jar:1.2.8]
	at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:76) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.netty.resources.PooledConnectionProvider.lambda$acquire$3(PooledConnectionProvider.java:196) ~[reactor-netty-core-1.2.8.jar:1.2.8]
	at reactor.core.publisher.MonoCreate.subscribe(MonoCreate.java:61) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.netty.http.client.HttpClientConnect$MonoHttpConnect.lambda$subscribe$0(HttpClientConnect.java:287) ~[reactor-netty-http-1.2.8.jar:1.2.8]
	at reactor.core.publisher.MonoCreate.subscribe(MonoCreate.java:61) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.FluxRetryWhen.subscribe(FluxRetryWhen.java:81) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.MonoRetryWhen.subscribeOrReturn(MonoRetryWhen.java:46) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:63) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.netty.http.client.HttpClientConnect$MonoHttpConnect.subscribe(HttpClientConnect.java:290) ~[reactor-netty-http-1.2.8.jar:1.2.8]
	at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:76) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:53) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:76) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.MonoDeferContextual.subscribe(MonoDeferContextual.java:55) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.Mono.subscribe(Mono.java:4576) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.Mono.block(Mono.java:1778) ~[reactor-core-3.7.8.jar:3.7.8]
	at com.example.demo.TestController.test(TestController.java:18) ~[classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:258) ~[spring-web-6.2.9.jar:6.2.9]
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:191) ~[spring-web-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:991) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:896) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:903) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:564) ~[tomcat-embed-core-10.1.43.jar:6.0]
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658) ~[tomcat-embed-core-10.1.43.jar:6.0]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:195) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51) ~[tomcat-embed-websocket-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-6.2.9.jar:6.2.9]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.9.jar:6.2.9]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-6.2.9.jar:6.2.9]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.9.jar:6.2.9]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.springframework.web.filter.ServerHttpObservationFilter.doFilterInternal(ServerHttpObservationFilter.java:110) ~[spring-web-6.2.9.jar:6.2.9]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.9.jar:6.2.9]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201) ~[spring-web-6.2.9.jar:6.2.9]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.9.jar:6.2.9]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:483) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:116) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:398) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1769) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1189) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:658) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]

2025-12-15T17:52:13.738+01:00 ERROR 29147 --- [demo-3.5.4] [nio-8080-exec-4] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.web.reactive.function.client.WebClientRequestException: Pending acquire queue has reached its maximum size of 1] with root cause

reactor.netty.internal.shaded.reactor.pool.PoolAcquirePendingLimitException: Pending acquire queue has reached its maximum size of 1
	at reactor.netty.internal.shaded.reactor.pool.SimpleDequePool.pendingOffer(SimpleDequePool.java:612) ~[reactor-netty-core-1.2.8.jar:1.2.8]
	at reactor.netty.internal.shaded.reactor.pool.SimpleDequePool.doAcquire(SimpleDequePool.java:306) ~[reactor-netty-core-1.2.8.jar:1.2.8]
	at reactor.netty.internal.shaded.reactor.pool.AbstractPool$Borrower.request(AbstractPool.java:425) ~[reactor-netty-core-1.2.8.jar:1.2.8]
	at reactor.core.publisher.FluxContextWrite$ContextWriteSubscriber.request(FluxContextWrite.java:136) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.netty.resources.DefaultPooledConnectionProvider$DisposableAcquire.onSubscribe(DefaultPooledConnectionProvider.java:218) ~[reactor-netty-core-1.2.8.jar:1.2.8]
	at reactor.core.publisher.FluxContextWrite$ContextWriteSubscriber.onSubscribe(FluxContextWrite.java:101) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.netty.internal.shaded.reactor.pool.SimpleDequePool$QueueBorrowerMono.subscribe(SimpleDequePool.java:743) ~[reactor-netty-core-1.2.8.jar:1.2.8]
	at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:76) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.netty.resources.PooledConnectionProvider.lambda$acquire$3(PooledConnectionProvider.java:196) ~[reactor-netty-core-1.2.8.jar:1.2.8]
	at reactor.core.publisher.MonoCreate.subscribe(MonoCreate.java:61) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.netty.http.client.HttpClientConnect$MonoHttpConnect.lambda$subscribe$0(HttpClientConnect.java:287) ~[reactor-netty-http-1.2.8.jar:1.2.8]
	at reactor.core.publisher.MonoCreate.subscribe(MonoCreate.java:61) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.FluxRetryWhen.subscribe(FluxRetryWhen.java:81) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.MonoRetryWhen.subscribeOrReturn(MonoRetryWhen.java:46) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:63) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.netty.http.client.HttpClientConnect$MonoHttpConnect.subscribe(HttpClientConnect.java:290) ~[reactor-netty-http-1.2.8.jar:1.2.8]
	at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:76) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.MonoDefer.subscribe(MonoDefer.java:53) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.InternalMonoOperator.subscribe(InternalMonoOperator.java:76) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.MonoDeferContextual.subscribe(MonoDeferContextual.java:55) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.Mono.subscribe(Mono.java:4576) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.publisher.Mono.block(Mono.java:1778) ~[reactor-core-3.7.8.jar:3.7.8]
	at com.example.demo.TestController.test(TestController.java:18) ~[classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:258) ~[spring-web-6.2.9.jar:6.2.9]
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:191) ~[spring-web-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:991) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:896) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:903) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:564) ~[tomcat-embed-core-10.1.43.jar:6.0]
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885) ~[spring-webmvc-6.2.9.jar:6.2.9]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658) ~[tomcat-embed-core-10.1.43.jar:6.0]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:195) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51) ~[tomcat-embed-websocket-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-6.2.9.jar:6.2.9]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.9.jar:6.2.9]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-6.2.9.jar:6.2.9]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.9.jar:6.2.9]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.springframework.web.filter.ServerHttpObservationFilter.doFilterInternal(ServerHttpObservationFilter.java:110) ~[spring-web-6.2.9.jar:6.2.9]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.9.jar:6.2.9]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201) ~[spring-web-6.2.9.jar:6.2.9]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.9.jar:6.2.9]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:483) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:116) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:398) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1769) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1189) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:658) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]

2025-12-15T17:52:13.923+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc-2, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] onStateChange(GET{uri=/, connection=PooledConnection{channel=[id: 0x82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443]}}, [response_received])
2025-12-15T17:52:13.923+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] onStateChange(GET{uri=/, connection=PooledConnection{channel=[id: 0x82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443]}}, [response_completed])
2025-12-15T17:52:13.923+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] onStateChange(GET{uri=/, connection=PooledConnection{channel=[id: 0x82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443]}}, [disconnecting])
2025-12-15T17:52:13.923+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.r.DefaultPooledConnectionProvider    : [82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] Releasing channel
2025-12-15T17:52:13.923+01:00 DEBUG 29147 --- [demo-3.5.4] [ctor-http-nio-2] r.n.resources.PooledConnectionProvider   : [82822adc, L:/192.168.0.152:55082 - R:google.com/216.58.212.46:443] Channel cleaned, now: 0 active connections, 1 inactive connections 0 pending acquire requests.
2025-12-15T17:52:58.739+01:00 ERROR 29147 --- [demo-3.5.4] [     parallel-1] com.example.demo.Application             : onOperatorError:

reactor.netty.internal.shaded.reactor.pool.PoolAcquireTimeoutException: Pool#acquire(Duration) has been pending for more than the configured timeout of 45000ms
	at reactor.netty.internal.shaded.reactor.pool.AbstractPool$Borrower.run(AbstractPool.java:417) ~[reactor-netty-core-1.2.8.jar:1.2.8]
	at reactor.core.scheduler.SchedulerTask.call(SchedulerTask.java:68) ~[reactor-core-3.7.8.jar:3.7.8]
	at reactor.core.scheduler.SchedulerTask.call(SchedulerTask.java:28) ~[reactor-core-3.7.8.jar:3.7.8]
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:304) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642) ~[na:na]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]


```
