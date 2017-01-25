package dubbo.helloworld;

/**
 * 定义服务接口: (该接口需单独打包，在服务提供方和消费方共享)
 */
public interface DemoService {
    String sayHello(String name);
}
