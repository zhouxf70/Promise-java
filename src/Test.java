public class Test {

    public static void main(String[] args) {
//        sync();
        async();
    }

    private static void async() {
        new Promise(handler -> {
            handler.resolve("resolve1");
//            handler.reject("error");
        }).then(it -> {
            printThread(it);
            sleep(1000);
            return it + ":then1";
        }).then(it -> {
            printThread(it);
            return new Promise(handler -> {
                new Thread(() -> {
                    sleep(1000);
                    handler.resolve(it + ":resolve2");
//                    handler.reject(it + ":error");
                }).start();
            });
        }).then(it -> {
            printThread(it);
            return new Promise(handler -> {
                new Thread(() -> {
                    handler.resolve(it + ":resolve3");
//                    handler.reject(it + ":error");
                }).start();
            });
        }).then(it -> {
            printThread(it);
            return it + ":then2";
        }).then(it -> {
            printThread(it);
            return new Promise(handler -> {
                new Thread(() -> {
                    sleep(1000);
//                    handler.resolve(it + ":resolve3");
                    handler.reject(it + ":error");
                }).start();
            });
        }).error(e -> {
            printThread("catch error : " + e);
        });
    }

    private static void sync() {
        new Promise(handler -> {
            handler.resolve("resolve1");
//            handler.reject("error");
        }).then(it -> {
            printThread(it);
            sleep(1000);
            return it + ":then1";
        }).then(it -> {
            printThread(it);
            return new Promise(handler -> {
                sleep(500);
                handler.resolve(it + ":resolve2");
//                handler.reject(it + ":error");
            });
        }).then(it -> {
            printThread(it);
            return new Promise(handler -> {
                handler.resolve(it + ":resolve3");
            });
        }).then(it -> {
            printThread(it);
            return it + ":then2";
        }).error(e -> {
            printThread("catch error : " + e);
        });
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void printThread(Object o) {
        System.out.println("[" + Thread.currentThread().getName() + "]::" + o);
    }
}
