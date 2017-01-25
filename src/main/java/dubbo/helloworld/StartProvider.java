package dubbo.helloworld;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * 启动服务提供方
 */
public class StartProvider {
    public static void main(String[] args) throws IOException {
        System.out.println("启动");
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/helloDubbo/provider.xml");
        context.start();
        System.in.read(); // 按任意键退出
    }
}
