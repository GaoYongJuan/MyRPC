package service;

import common.User;

public interface UserService {

    // 客户端通过接口调用服务端的实现类
    User getUserByUserId(Integer id);
    Integer insertUserId(User user);
}
