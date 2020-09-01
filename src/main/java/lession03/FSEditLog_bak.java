package lession03;

import java.util.LinkedList;

public class FSEditLog_bak {

    private long txid = 0L;
    private DoubleBuffer doubleBuffer = new DoubleBuffer();
    private volatile Boolean isSyncRunning = false;
    private volatile Boolean isWaitSync = false;


    private volatile Long syncMaxTxid = 0L;
    private ThreadLocal<Long> localTxid = new ThreadLocal<Long>();


    public void logEdit(String context) {
        synchronized (this) {
            txid++;
            localTxid.set(txid);
            EditLog log = new EditLog(txid, context);
            doubleBuffer.writer(log);
        }
        logSync();
    }

    private void logSync() {
        synchronized (this) {
            if (isSyncRunning) {
                long txid = localTxid.get();
                if (txid <= syncMaxTxid) {
                    return;
                }
                if (isWaitSync) {
                    return;
                }
                isWaitSync = true;
                while (isSyncRunning) {
                    try {
                        wait(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isWaitSync = false;
            }
            doubleBuffer.setReadyToSync();
            if (doubleBuffer.syncList.size() > 0) {
                syncMaxTxid = doubleBuffer.getSyncMaxTxid();
            }

            isSyncRunning = true;
        }


        doubleBuffer.flush();

        synchronized (this) {
            isSyncRunning = false;
            notify();
        }
    }


    class EditLog {
        long txid;
        String context;

        public EditLog(long txid, String context) {
            this.txid = txid;
            this.context = context;
        }

        @Override
        public String toString() {
            return "EditLog{" +
                    "txid=" + txid +
                    ", context='" + context + '\'' +
                    '}';
        }
    }

    class DoubleBuffer {
        LinkedList<EditLog> curList = new LinkedList<EditLog>();
        LinkedList<EditLog> syncList = new LinkedList<EditLog>();


        public void writer(EditLog editLog) {
            curList.add(editLog);
        }

        public void setReadyToSync() {
            LinkedList<EditLog> temp = curList;
            curList = syncList;
            syncList = temp;
        }

        public Long getSyncMaxTxid() {
            return syncList.getLast().txid;
        }

        public void flush() {
            for (EditLog log : syncList) {
                System.out.println("编号： " + log.txid + " 的日志写入磁盘：" + log.context);
            }
            syncList.clear();
        }

    }


}
