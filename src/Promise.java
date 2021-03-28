public class Promise {

    private final static String PENDING = "pending";
    private final static String FULFILL = "fulfill";
    private final static String REJECTED = "rejected";

    // 日志打印参数
    private static int count = 0;
    private String name;

    private String state = PENDING;
    private Chain chain;
    private Object value, error;

    public Promise(Executor executor) {

        synchronized (Promise.class) {
            name = "promise_" + count;
            count++;
        }

        printThread("new");

        executor.execute(new Handler() {

            @Override
            public void resolve(Object o) {
                printThread("resolve");
                if (PENDING.equals(state)) {
                    state = FULFILL;
                    value = o;
                    handle(chain);
                }
            }

            @Override
            public void reject(Object o) {
                printThread("reject");
                if (PENDING.equals(state)) {
                    state = REJECTED;
                    error = o;
                    handle(chain);
                }
            }
        });
    }

    public Promise then(Fulfill fulfill) {
        printThread("then1");

        // 一旦处于REJECTED状态，直接跳转到末尾的error方法
        if (state.equals(REJECTED))
            return this;

        return new Promise(handler -> {
            // lambda表达式中,this的指向和匿名内部类中的this指向不一致，这里的this指向不是新new的Promise
            // 在js中也是如此，箭头函数的指向也和普通函数的this指向不一致
            printThread("then2");
            this.handle(new Chain(handler, fulfill, null));
        });
    }

    private void handle(Chain chain) {
        printThread("handle");

        if (chain == null) {
            return;
        }

        if (PENDING.equals(state)) {
            this.chain = chain;
        } else if (FULFILL.equals(state)) {
            if (chain.fulfill != null) {
                Object onResolved = chain.fulfill.onResolved(this.value);
                if (onResolved instanceof Promise)
                    // 当((Promise) onResolved)的resolve是在子线程被调用时，这里的((Promise) onResolved).value只能拿到null
                    // chain.handler.resolve(((Promise) onResolved).value);

                    // 因此只能把主线程的Promise（即then方法中创建的）的handler传递给onResolved，
                    // 这样，当onResolved的resolve->handle方法被调用时，可以把它的value通过这个handler传递给主线程
                    ((Promise) onResolved).handle(new Chain(chain.handler, null, null));
                else
                    chain.handler.resolve(onResolved);
            } else if (chain.handler != null)
                chain.handler.resolve(this.value);
        } else if (REJECTED.equals(state)) {
            if (chain.rejected != null)
                chain.rejected.onRejected(this.error);
            else if (chain.handler != null)
                chain.handler.reject(this.error);
        }
    }

    public void error(Rejected rejected) {
        printThread("error");
        this.handle(new Chain(null, null, rejected));
    }

    public interface Executor {
        void execute(Handler handler);
    }

    interface Handler {

        void resolve(Object t);

        void reject(Object o);
    }

    public interface Fulfill {
        Object onResolved(Object t);
    }

    public interface Rejected {
        void onRejected(Object o);
    }

    private static class Chain {
        Handler handler;
        Fulfill fulfill;
        Rejected rejected;

        Chain(Handler handler, Fulfill fulfill, Rejected rejected) {
            this.handler = handler;
            this.fulfill = fulfill;
            this.rejected = rejected;
        }

    }

    private void printThread(Object o) {
//        System.out.println("[" + Thread.currentThread().getName() + "]" + name + "::" + o);
    }
}
