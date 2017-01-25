package zookeeper.DistributedQueue;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

import java.io.IOException;

/**
 * 链接：http://blog.fens.me/zookeeper-queue/
 *
 * 基于ZooKeeper现实的一种同步的分步式队列，当一个队列的成员都聚齐时，这个队列才可用，否则一直等待所有成员到达。
 *
 * 设计思路
    创建一个父目录 /queue，每个成员都监控(Watch)标志位目录/queue/start 是否存在，然后每个成员都加入这个队列，
    加入队列的方式就是创建 /queue/x(i)的临时目录节点，然后每个成员获取 /queue 目录的所有目录节点，也就是 x(i)。
    判断 i 的值是否已经是成员的个数，如果小于成员个数等待 /queue/start 的出现，如果已经相等就创建 /queue/start。

 */
public class DistributedQueue {
    public static void main(String[] args) throws Exception {
        //模拟app1通过zk1提交x1,app2通过zk2提交x2,app3通过zk3提交x3
        doAction(1);
        doAction(2);
        doAction(3);
    }

    //以此在集群的3台机器上加入某成员
    public static void doAction(int client) throws Exception {
        String host1 = "121.42.8.85:2181";
        String host2 = "121.42.8.85:2182";
        String host3 = "121.42.8.85:2183";
        ZooKeeper zk = null;
        switch (client) {
            case 1:
                zk = connection(host1);
                initQueue(zk);
                joinQueue(zk, 1);
                break;
            case 2:
                zk = connection(host2);
                initQueue(zk);
                joinQueue(zk, 2);
                break;
            case 3:
                zk = connection(host3);
                initQueue(zk);
                joinQueue(zk, 3);
                break;
        }
    }

    // 创建一个与服务器的连接
    public static ZooKeeper connection(String host) throws IOException {
        ZooKeeper zk = new ZooKeeper(host, 60000, new Watcher() {
            // 监控所有被触发的事件
            public void process(WatchedEvent event) {
                if (event.getType() == Event.EventType.NodeCreated && event.getPath().equals("/queue/start")) {
                    System.out.println("Queue has Completed.Finish testing!!!");
                }
            }
        });
        return zk;
    }

    //初始化队列
    public static void initQueue(ZooKeeper zk) throws KeeperException, InterruptedException {

        System.out.println("WATCH => /queue/start");

        //当这个znode节点被改变时，将会触发当前Watcher
        zk.exists("/queue/start", true);

        //如果/queue目录为空，创建此节点
        if (zk.exists("/queue", false) == null) {
            System.out.println("create /queue task-queue");
            zk.create("/queue", "task-queue".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } else {
            System.out.println("/queue is exist!");
        }
    }

    //成员加入队列
    public static void joinQueue(ZooKeeper zk, int x) throws KeeperException, InterruptedException {
        System.out.println("create /queue/x" + x + " x" + x);
        zk.create("/queue/x" + x, ("x" + x).getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        isCompleted(zk);
    }

    //判断队列是否已满
    public static void isCompleted(ZooKeeper zk) throws KeeperException, InterruptedException {
        //规定队列大小
        int size = 3;
        //查询成员数
        int length = zk.getChildren("/queue", true).size();
        System.out.println("Queue Complete:" + length + "/" + size);
        if (length >= size) {
            System.out.println("create /queue/start start");
            zk.create("/queue/start", "start".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }
}
