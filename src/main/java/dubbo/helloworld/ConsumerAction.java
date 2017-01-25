package dubbo.helloworld;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 消费者消费服务
 */
public class ConsumerAction {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/helloDubbo/consumer.xml");
        DemoService demoService = (DemoService) context.getBean("demoService"); // 获取远程服务代理
        String hello = demoService.sayHello("KKys!"); // 执行远程方法
        System.out.println(hello); // 显示调用结果
    }
}
