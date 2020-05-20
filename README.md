# open feign logging 小工具
1. InvocationHandlerFactoryImpl, FeignInvocationHandler, @LogInvocation: 基于动态代理，对input、output输出日志。不涉及http request和response
2. LoggingClient: 对http request及response的日志输出