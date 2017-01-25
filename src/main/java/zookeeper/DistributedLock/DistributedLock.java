package zookeeper.DistributedLock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * zookeeper实现分布式锁
 */
public class DistributedLock implements Watcher{
    private int threadId;
    private ZooKeeper zk = null;
    private String selfPath;
    private String waitPath;
    private String PREFIX_OF_THREAD;
    private static final int SESSION_TIMEOUT = 10000;
    private static final String GROUP_PATH = "/lock";
    private static final String SUB_PATH = "/lock/sub";
    private static final String CONNECTION_STRING = "xxx:2181";

    private static final int THREAD_NUM = 10;
    //确保连接zk成功；
    private CountDownLatch connectedSemaphore = new CountDownLatch(1);
    //确保所有线程运行结束；
    private static final CountDownLatch threadSemaphore = new CountDownLatch(THREAD_NUM);
    public DistributedLock(int id) {
        this.threadId = id;
        PREFIX_OF_THREAD = "【第"+threadId+"个线程】";
    }
    public static void main(String[] args) {
        for(int i=0; i < THREAD_NUM; i++){
            final int threadId = i+1;
            new Thread(){
                @Override
                public void run() {
                    try{
                        DistributedLock dc = new DistributedLock(threadId);
                        dc.createConnection(CONNECTION_STRING, SESSION_TIMEOUT);
                        //GROUP_PATH不存在的话，由一个线程创建即可；
                        synchronized (threadSemaphore){
                            dc.createPath(GROUP_PATH, "该节点由线程" + threadId + "创建", true);
                        }
                        dc.getLock();
                    } catch (Exception e){
                        System.out.println(("【第"+threadId+"个线程】 抛出的异常："));
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        try {
            threadSemaphore.await();
            System.out.println("所有线程运行结束!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取锁
     * @return
     */
    private void getLock() throws KeeperException, InterruptedException {
        selfPath = zk.create(SUB_PATH,null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println(PREFIX_OF_THREAD+"创建锁路径:"+selfPath);
        if(checkMinPath()){
            getLockSuccess();
        }
    }

    /**
     * 创建锁节点
     * @param path 锁节点path
     * @param data 初始数据内容
     * @return
     */
    public boolean createPath( String path, String data, boolean needWatch) throws KeeperException, InterruptedException {
        if(zk.exists(path, needWatch)==null){
            System.out.println(PREFIX_OF_THREAD + "节点创建成功, Path: "
                    + this.zk.create( path,
                    data.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT)
                    + ", content: " + data );
        }
        return true;
    }

    /**
     * 创建ZK连接
     * @param connectString  ZK服务器地址列表
     * @param sessionTimeout Session超时时间
     */
    public void createConnection( String connectString, int sessionTimeout ) throws IOException, InterruptedException {
        zk = new ZooKeeper( connectString, sessionTimeout, this);
        connectedSemaphore.await();
    }

    /**
     * 获取锁成功
     */
    public void getLockSuccess() throws KeeperException, InterruptedException {
        if(zk.exists(this.selfPath,false) == null){
            System.out.println(PREFIX_OF_THREAD+"本节点已不在了...");
            return;
        }
        System.out.println(PREFIX_OF_THREAD + "获取锁成功，开始干活！");
        Thread.sleep(2000);
        releaseConnection();
        threadSemaphore.countDown();
    }
    /**
     * 关闭ZK连接
     */
    public void releaseConnection() {
        if ( this.zk !=null ) {
            try {
                this.zk.close();
            } catch ( InterruptedException e ) {}
        }
        System.out.println(PREFIX_OF_THREAD + "工作完毕，释放锁");
    }

    /**
     * 检查自己是不是最小的节点
     * @return
     */
    public boolean checkMinPath() throws KeeperException, InterruptedException {
        List<String> subNodes = zk.getChildren(GROUP_PATH, false);
        Collections.sort(subNodes);
        //判断此节点
        int index = subNodes.indexOf( selfPath.substring(GROUP_PATH.length()+1));
        switch (index){
            case -1:{
                System.out.println(PREFIX_OF_THREAD+"本节点已不在了..."+selfPath);
                return false;
            }
            case 0:{
                System.out.println(PREFIX_OF_THREAD+"子节点中，我最小，可以获得锁了！哈哈"+selfPath);
                return true;
            }
            default:{
                this.waitPath = GROUP_PATH +"/"+ subNodes.get(index - 1);
                System.out.println(PREFIX_OF_THREAD+"排在我前面的节点是 "+waitPath);
                try{
                    zk.getData(waitPath, true, new Stat());
                    return false;
                }catch(KeeperException e){
                    if(zk.exists(waitPath,false) == null){
                        System.out.println(PREFIX_OF_THREAD+"排在我前面的"+waitPath+"已消失 ");
                        return checkMinPath();
                    }else{
                        throw e;
                    }
                }
            }
        }
    }
    @Override
    public void process(WatchedEvent event) {
        if(event == null){
            return;
        }
        Event.KeeperState keeperState = event.getState();
        Event.EventType eventType = event.getType();
        //已经是连接到zookeeper的状态
        if ( Event.KeeperState.SyncConnected == keeperState) {
            if ( Event.EventType.None == eventType ) {
                System.out.println(PREFIX_OF_THREAD + "成功连接上ZK服务器" );
                connectedSemaphore.countDown();
            }else if (event.getType() == Event.EventType.NodeDeleted && event.getPath().equals(waitPath)) {
                //监听节点被删除状态
                System.out.println(PREFIX_OF_THREAD + "watch说我前面那个节点挂了，我可以获取锁啦！！");
                try {
                    //判断当前节点是否为最小节点
                    if(checkMinPath()){
                        getLockSuccess();
                    }
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else if ( Event.KeeperState.Disconnected == keeperState ) {
            //监听断开
            System.out.println(PREFIX_OF_THREAD + "与ZK服务器断开连接" );
        } else if ( Event.KeeperState.AuthFailed == keeperState ) {
            //监听权限失败
            System.out.println(PREFIX_OF_THREAD + "权限检查失败" );
        } else if ( Event.KeeperState.Expired == keeperState ) {
            //监听会话失效
            System.out.println(PREFIX_OF_THREAD + "会话失效" );
        }
    }
}
