package register;

import loadbalance.LoadBalance;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import loadbalance.RoundLoadBalance;

import java.net.InetSocketAddress;
import java.util.List;

public class ZkServiceRegister implements ServiceRegister {
    // curator 提供的 zookeeper 客户端
    private CuratorFramework client;
    // zookeeper 根路径节点
    private static final String ROOT_PATH = "MyRPC";
    // 初始化负载均衡器，这里用的是随机，一般通过构造函数传入
    private LoadBalance loadBalance = new RoundLoadBalance();

    // 负责 zookeeper 客户端的初始化，并与 zookeeper 服务端建立连接
    public ZkServiceRegister(){
        // 指数时间重试
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        // zookeeper 的地址固定，不管是服务提供者还是消费者，都要与之建立连接
        // sessionTimeoutMs 与 zoo.cfg 中的 tickTime 有关系
        // zk 还会根据 minSessionTimeout 与 maxSessionTimeout 两个参数重新调整最后的超时值。默认分别为 tickTime 的 2 倍和 20 倍
        // 使用心跳监听状态
        this.client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .sessionTimeoutMs(40000).retryPolicy(policy).namespace(ROOT_PATH).build();
        this.client.start();
        System.out.println("zookeeper 连接成功");
    }
    @Override
    public void register(String serviceName, InetSocketAddress serverAddress) {
        try{
            // serviceName 创建成永久节点，服务提供者下线时，不删服务名，只删除地址
            if (client.checkExists().forPath("/" +serviceName) == null){
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + serviceName);
            }
            // 路径地址，一个 / 代表一个节点
            String path = "/" + serviceName + "/" + getServiceAddress(serverAddress);
            // 临时节点，服务器下线就删除节点
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        }catch (Exception e){
            System.out.println("此服务已存在");
        }
    }

    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try{
            List<String> strings = client.getChildren().forPath("/" + serviceName);
            // 负载均衡器，选一个
            String string = loadBalance.balance(strings);
            return parseAddress(string);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    // 地址 -> xxx.xxx.xxx.xxx:port 字符串
    private String getServiceAddress(InetSocketAddress serverAddress){
        return serverAddress.getHostName() +
                ":" +
                serverAddress.getPort();
    }

    // 字符串解析为地址
    private InetSocketAddress parseAddress(String address){
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }
}
