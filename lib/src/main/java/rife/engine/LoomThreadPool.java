package rife.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jetty.util.thread.ThreadPool;

public class LoomThreadPool implements ThreadPool {

  private final ExecutorService executorService;

  public LoomThreadPool() {
    try {
      var clazz = Executors.class;
      var virtualMethod = clazz.getDeclaredMethod("newVirtualThreadPerTaskExecutor");
      executorService = (ExecutorService) virtualMethod.invoke(clazz);
    } catch (Exception e) {
      throw new IllegalStateException("no virtual threads here");
    }
  }

  @Override
  public void join() throws InterruptedException {}

  @Override
  public int getThreads() {
    return 1;
  }

  @Override
  public int getIdleThreads() {
    return 1;
  }

  @Override
  public boolean isLowOnThreads() {
    return false;
  }

  @Override
  public void execute(Runnable command) {
    executorService.submit(command);
  }
}
