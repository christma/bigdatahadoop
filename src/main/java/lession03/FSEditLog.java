package lession03;

import java.util.LinkedList;
import java.util.List;

public class FSEditLog {
    private long txid = 0L;
    private DoubleBuffer editLogBuffer = new DoubleBuffer();
    //当前是否正在往磁盘里面刷写数据
    private volatile Boolean isSyncRunning = false;
    private volatile Boolean isWaitSync = false;

    private volatile Long syncMaxTxid = 0L;
    /**
     * 一个线程 就会有自己一个ThreadLocal的副本
     */
    private ThreadLocal<Long> localTxid = new ThreadLocal<Long>();


    /**
     * 客户端高并发写元数据信息的时候，就是调用的这个方法
     * <p>
     * // HDFS的源码 90% 5% 5%
     *
     * @param content
     */
    public void logEdit(String content) {//mkdir /data
        //线程1
        //线程2
        //线程3

        //线程4
        //线程5
        synchronized (this) {//锁里面的代码 速度很快，应该都是纯内存操作。
            //加锁了保证 顺序性和独一无二
            //1
            //2
            //3
            //4
            //5
            txid++;
            //线程1 -> 1
            //线程2 -> 2
            //线程3 -> 3
            //线程4 -> 4
            //线程5 -> 5
            localTxid.set(txid);
            //创建一个元数据日志对象
            //
            EditLog log = new EditLog(txid, content);
            //把数据写入到双缓冲的当前内存
            //
            editLogBuffer.write(log);
        } //释放锁 分段加锁

        //线程一
        //线程二进来
        //线程三进来
        //线程四进来
        //线程五进来
        logSync();
    }

    private void logSync() {
        //又要开始加锁
        //线程1
        //线程2
        //线程3

        //线程4
        //线程5
        synchronized (this) {
            //看是否正在刷写当前数据 false
            //线程2看到的时候，true
            //线程3 true
            //线程5 true
            if (isSyncRunning) {
                //获取自己线程的 副本里面的一个ID号  2
                //获取自己的线程的 副本的ID  3

                //获取自己的线程的副本的ID 4
                //获取自己的线程的副本的ID 5
                long txid = localTxid.get();
                //线程2   2 <= 3
                //线程3   3 <= 3

                //线程4   4 <= 3
                //线程5   5 <= 3 =false
                if (txid <= syncMaxTxid) {
                    //当前线程就是return，不往下执行了。
                    return;
                }

                //看前面是否有线程在等待
                if (isWaitSync) {
                    //所以线程5进来的时候，会直接进行return
                    return;
                }
                //线程4
                isWaitSync = true;

                //只要还在同步元数据
                while (isSyncRunning) {
                    try {
                        //当前线程4就要wait(释放锁：不会影响其他线程正常执行)

                        //什么时候代码会往下执行
                        //1： 时间到了
                        //2： 被被人唤醒

                        //此时此刻，线程4就在这儿等着。

                        //等了几毫秒以后，被线程1唤醒了。
                        wait(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //重新赋值
                isWaitSync = false;
            }

            //直接就交换内存
            //真实源码里面不是直接交换内存，而是要达到一定地步的时候
            //才会交换内存。

            editLogBuffer.setReadyToSync();

            if (editLogBuffer.syncBuffer.size() > 0) {
                //获取当前同步内存里面的最大的事务ID号。

                syncMaxTxid = editLogBuffer.getSyncMaxTxid();
                System.out.println(syncMaxTxid);

            }

            //说明接下来就要往磁盘上面写元数据日志信息了。
            isSyncRunning = true;
        } //释放锁

        //线程一
        //把内存里面的数据写到磁盘上。
        //这个操作肯定是一个耗时的操作，会耗费，几十毫秒，甚至是几百毫秒。
        //这个方法上面没有加锁。
        //.......

        //线程4在干活 ...
        editLogBuffer.flush();

        //重新加锁
        synchronized (this) {
            //重新复制
            isSyncRunning = false;
            //线程1 唤醒等待的线程（线程4）
            notify();
        }

    }


    /**
     * 首先用面向对象的思想，把每一条元数据信息，都看做一个对象。
     */
    class EditLog {
        long txid;//每一条元数据都有编号，递增
        String content;//元数据的内容 mkdir /data

        //构造函数
        public EditLog(long txid, String content) {
            this.txid = txid;
            this.content = content;
        }

        //方便我们打印日志
        @Override
        public String toString() {
            return "EditLog{" +
                    "txid=" + txid +
                    ", content='" + content + '\'' +
                    '}';
        }
    }

    /**
     * 双缓冲的方案
     */
    class DoubleBuffer {
        //内存1 当前内存
        LinkedList<EditLog> currentBuffer = new LinkedList<EditLog>();

        //内存2 同步内存
        LinkedList<EditLog> syncBuffer = new LinkedList<EditLog>();


        /**
         * 把元数据写入到当前内存
         *
         * @param log 元数据日志
         */
        public void write(EditLog log) {
            currentBuffer.add(log);
        }


        /**
         * 内存交换
         */
        public void setReadyToSync() {
            LinkedList<EditLog> tmp = currentBuffer;
            currentBuffer = syncBuffer;
            syncBuffer = tmp;
        }


        /**
         * 获取当前刷写磁盘的内存里面的最大的一个元数据编号。
         *
         * @return
         */
        public Long getSyncMaxTxid() {
            return syncBuffer.getLast().txid;
        }


        /**
         * 把数据写到磁盘
         */
        public void flush() {
            for (EditLog log : syncBuffer) {
                //正常情况下，我就是把数据写到磁盘上
                //但是这儿为了观察结果，我们就把数据打印出来。
                System.out.println("存入磁盘日志信息：" + log);
            }
            //清空数据
            syncBuffer.clear();
        }
    }


}
