package server;

import service.BlogService;
import service.BlogServiceImpl;
import service.UserService;
import service.UserServiceImpl;

public class TestServer {
    public static void main(String[] args) {

        UserService userService = new UserServiceImpl();
        BlogService blogService = new BlogServiceImpl();

//        Map<String, Object> serviceProvide = new HashMap<>();
//        serviceProvide.put("com.ganghuan.myRPCVersion2.service.UserService",userService);
//        serviceProvide.put("com.ganghuan.myRPCVersion2.service.BlogService",blogService);
        ServiceProvider serviceProvide = new ServiceProvider("127.0.0.1", 8898);
        serviceProvide.provideServiceInterface(userService);
        serviceProvide.provideServiceInterface(blogService);

        RPCServer RPCServer = new NettyRPCServer(serviceProvide);
        RPCServer.start(8898);
    }
}
