package dubbo.helloworld;

/**
 * 在服务提供方实现接口：(对服务消费方隐藏实现)
 */
public class DemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        return "Hello dubbo : " + name;
    }
}
