package zookeeper.CreateGroup;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 连接的观察者，封装了zk的创建等
 * Created by yaosheng on 2017/1/17.
 *
 */
public class ConnectionWatcher implements Watcher {
    private static final int SESSION_TIMEOUT = 5000;

    protected ZooKeeper zk = null;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public void process(WatchedEvent event) {
        Event.KeeperState state = event.getState();

        if(state == Event.KeeperState.SyncConnected){
            countDownLatch.countDown();
        }
    }

    /**
     * 连接资源
     * @param hosts
     * @throws IOException
     * @throws InterruptedException
     */
    public void connection(String hosts) throws IOException, InterruptedException {
        zk = new ZooKeeper(hosts, SESSION_TIMEOUT, this);
        countDownLatch.await();
    }

    /**
     * 释放资源
     * @throws InterruptedException
     */
    public void close() throws InterruptedException {
        if (null != zk) {
            try {
                zk.close();
            } catch (InterruptedException e) {
                throw e;
            }finally{
                zk = null;
                System.gc();
            }
        }
    }
}