package lession03;

public class TestNameNode {
    public static void main(String[] args) {
        final  FSEditLog02 fsEditLog = new FSEditLog02();

        for (int i =0; i < 50; i++){
            new Thread(new Runnable() {
                public void run() {

                    for (int j = 0; j < 1000; j++) {
                        fsEditLog.logEdit("日志信息");
                    }

                }
            }).start();

        }
    }

}
