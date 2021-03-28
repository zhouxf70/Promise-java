1、同步
```
new Promise(handler -> {
    handler.resolve("resolve1");
//    handler.reject("error");
}).then(it -> {
    printThread(it);
    sleep(1000);
    return it + ":then1";
}).then(it -> {
    printThread(it);
    return new Promise(handler -> {
        sleep(500);
        handler.resolve(it + ":resolve2");
//        handler.reject(it + ":error");
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
```
```
// 日志输出
[main]::resolve1
[main]::resolve1:then1
[main]::resolve1:then1:resolve2
[main]::resolve1:then1:resolve2:resolve3
```
2、异步
```
new Promise(handler -> {
    handler.resolve("resolve1");
//    handler.reject("error");
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
//            handler.reject(it + ":error");
        }).start();
    });
}).then(it -> {
    printThread(it);
    return new Promise(handler -> {
        new Thread(() -> {
            handler.resolve(it + ":resolve3");
//            handler.reject(it + ":error");
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
//            handler.resolve(it + ":resolve3");
            handler.reject(it + ":error");
        }).start();
    });
}).error(e -> {
    printThread("catch error : " + e);
});
```
```
// 日志输出
[main]::resolve1
[main]::resolve1:then1
[Thread-0]::resolve1:then1:resolve2
[Thread-1]::resolve1:then1:resolve2:resolve3
[Thread-1]::resolve1:then1:resolve2:resolve3:then2
[Thread-2]::catch error : resolve1:then1:resolve2:resolve3:then2:error
```
