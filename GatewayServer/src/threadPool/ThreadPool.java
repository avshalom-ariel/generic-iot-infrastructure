package threadPool;

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPool implements Executor {
    private final WaitablePQ<Task<?>> taskPQ = new WaitablePQ<>();
    private volatile int threadsInPool;
    private final int HIGHEST = Priority.HIGH.getPriority() + 2;
    private final int LOWEST = Priority.LOW.getPriority() - 1;
    private final Lock shutdownLock = new ReentrantLock();
    private final Lock pauseLock = new ReentrantLock();
    private volatile boolean isPaused  = false;
    private volatile boolean isShutdown = false;
    private Future<Void> LAST_POISON_PILL;

    //ctor
    // Default numberOfThreads depend on number of cores
    public ThreadPool() {
        this(2 * Runtime.getRuntime().availableProcessors());
    }

    //receives the original number of threads
    public ThreadPool(int numberOfThreads) {
        if (0 >= numberOfThreads) {
            throw new IllegalArgumentException("Number of threads must be greater than 0");
        }
        threadsInPool = numberOfThreads;

        for (int i = 0; i < threadsInPool; i++) {
            Thread thread = new Thread(this::threadRoutine);
            thread.start();
        }
    }

    private void threadRoutine() {
        boolean poisonPill = false;
        while (!poisonPill) {
            try {
                Task<?> task = taskPQ.dequeue();
                System.out.println("Thread: " + Thread.currentThread().getName() + " dequeued task");
                poisonPill = task.getPill();
                task.taskExec();

            } catch (Exception e) {
                System.out.println("error: exception in thread: " + Thread.currentThread().getName());
            }
        }
    }

    @Override
    public void execute(Runnable runnable) { submit(runnable); }

    public Future<Void> submit(Runnable command){
        if(null == command){
            throw new NullPointerException("command is null!!");
        }
        return submit(()->{command.run(); return null;});
    }

    public Future<Void> submit(Runnable command, Priority p){
        if(null == command){
            throw new NullPointerException("command is null!!");
        }
        return submit(()->{command.run(); return null;}, p);
    }

    public <T> Future<T> submit(Runnable command, Priority p, T value){
        if(null == command){
            throw new NullPointerException("command is null!!");
        }
        return submit(()->{command.run(); return value;}, p);
    }

    public <T> Future<T> submit(Callable<T> command){
        return submit(command, Priority.MEDIUM);
    }

    public <T> Future<T> submit(Callable<T> command, Priority p){
        if (isShutdown) {
            throw new RejectedExecutionException("ThreadPool is shutdown!!");
        }
        if(null == command){
            throw new NullPointerException("command is null!!");
        }
        Task<T> task = new Task<>(command, p.getPriority());
        taskPQ.enqueue(task);
        return task.future;
    }

    // if threads are removed, they should be the first threads that not running
    public void setNumOfThreads(int numOfThreads){
        if(0 > numOfThreads){
            throw new IllegalArgumentException();
        }

        for (int i = threadsInPool; i < numOfThreads; ++i) {
            Thread thread = new Thread(this::threadRoutine);
            if (isPaused) {
                taskPQ.enqueue(new Task<Void>(new SleepingPill(), HIGHEST));
            }
            thread.start();
        }
        for (int i = numOfThreads; i < threadsInPool; ++i) {
            Task<Void> task = new Task<>(new PoisonPill(), HIGHEST);
            task.poisonPill();
            taskPQ.enqueue(task);
        }
        threadsInPool = numOfThreads;
    }

    //operations
    public void pause(){
        isPaused = true;
        for (int i = 0; i < threadsInPool; i++) {
            Task<Void> task = new Task<>(new SleepingPill(), HIGHEST);
            taskPQ.enqueue(task);
        }
    }

    public void resume(){
        isPaused = false;
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

    public void shutdown(){
        isShutdown = true;

        for (int i = 0; i < threadsInPool - 1; ++i) {
            Task<Void> task = new Task<>(new PoisonPill(), LOWEST);
            task.poisonPill();
            taskPQ.enqueue(task);
        }

        Task<Void> lastTask = new Task<>(new LastPoisonPill(), LOWEST - 1);
        lastTask.poisonPill();
        taskPQ.enqueue(lastTask);

        LAST_POISON_PILL = lastTask.future;
    }

    public void awaitTermination() throws InterruptedException {
        try {
            LAST_POISON_PILL.get();
        } catch (ExecutionException e) {
            throw new RuntimeException("awaitTermination runtime exception", e);
        }
    }
    public boolean awaitTermination(long timeout,TimeUnit unit) throws InterruptedException {
        long deadline = System.currentTimeMillis() + unit.toMillis(timeout);

        try {
            LAST_POISON_PILL.get(timeout, unit);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            return false;
        }
        return deadline > System.currentTimeMillis();
    }

    private class Task<E> implements Comparable<Task<E>> {
        private final Callable<E> command;
        private final int priority;
        private final Future<E> future;
        private Thread execThread;
        private E result;
        private boolean isDone = false;
        private boolean poisonPill = false;
        private Exception e = null;
        private boolean isCancelled = false;
        private final Object futureGetLock = new Object();

        public Task(Callable<E> command, int priority) {
            this.command = command;
            this.priority = priority;
            future = new TaskFuture();
            execThread = null;
        }

        public boolean getPill() {
            return poisonPill;
        }

        public void poisonPill() {
            poisonPill = true;
        }


        @Override
//        public int compareTo(Task<E> task) {return priority - task.priority;}
        public int compareTo(Task<E> task) {return task.priority - this.priority;}

        public void taskExec() {
            execThread = Thread.currentThread();

            if (!isCancelled) {
                try {
                    result = command.call();
                } catch (Exception e) {
                    this.e = e;
                    System.out.println(e.getMessage());
                    isDone = false;
                } finally {
                    synchronized (futureGetLock) {
                        isDone = true;
                        futureGetLock.notifyAll();
                    }

                }
            }
        }

        private class TaskFuture implements Future<E> {
            @Override
            public boolean cancel(boolean b) {
                synchronized (future) {
                    if (b && execThread != null) {
                        execThread.interrupt();
                    }
                    if (isDone || isCancelled || execThread != null) return false;

                    taskPQ.remove(this);
                    isCancelled = true;
                    return true;
                }
            }

            @Override
            public boolean isCancelled() {
                return isCancelled;
            }

            @Override
            public boolean isDone() {
                return isDone;
            }

            @Override
            public E get() throws InterruptedException, ExecutionException {
                if (isCancelled) {
                    throw new CancellationException();
                }

                if (null != Task.this.e) {
                    try {
                        throw e;
                    } catch (Exception ex) {
                        throw new ExecutionException(ex);
                    }
                }
                synchronized (futureGetLock) {
                    while (!isDone && Task.this.e == null && !isCancelled) {
                        futureGetLock.wait();
                    }
                }
                if (null != Task.this.e) {
                    try {
                        throw e;
                    } catch (Exception ex) {
                        throw new ExecutionException(ex);
                    }
                }
                return result;
            }

            @Override
            public E get(long timeOut, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

                if (isCancelled) {
                    throw new CancellationException();
                }

                long deadline = System.currentTimeMillis() + unit.toMillis(timeOut);  // Calculate the deadline

                synchronized (futureGetLock) {
                    while (!isDone && e == null && !isCancelled && deadline >= System.currentTimeMillis()) {
                        futureGetLock.wait(unit.toMillis(deadline - System.currentTimeMillis()));
                        if(Thread.currentThread().isInterrupted()){
                            throw new InterruptedException("interrupt in get");
                        }
                        if (deadline <= System.currentTimeMillis()) {
                            throw new TimeoutException();
                        }
                    }
                }
                if (null != Task.this.e) {
                    try {
                        throw e;
                    } catch (Exception ex) {
                        throw new ExecutionException(ex);
                    }
                }

                return result;
            }
        }
    }

    private class SleepingPill implements Callable<Void> {
        @Override
        public Void call() throws InterruptedException {
            synchronized (pauseLock) {
                while (isPaused) {
                    pauseLock.wait();
                }
            }
            return null;
        }
    }

    private class PoisonPill implements Callable<Void> {
        @Override
        public Void call() throws InterruptedException {
            return null;
        }
    }

    private class LastPoisonPill implements Callable<Void> {
        @Override
        public Void call() throws InterruptedException {
            synchronized (shutdownLock) {
                threadsInPool = 0;
                shutdownLock.notifyAll();
            }
            return null;
        }
    }
}

