package net.minecraft.resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.DummyProfiler;

/**
 * A simple implementation of resource reload.
 * 
 * @param <S> the result type for each reloader in the reload
 */
public class SimpleResourceReload<S> implements ResourceReload {
   /**
    * The weight of either prepare or apply stages' progress in the total progress
    * calculation. Has value {@value}.
    */
   private static final int FIRST_PREPARE_APPLY_WEIGHT = 2;
   /**
    * The weight of either prepare or apply stages' progress in the total progress
    * calculation. Has value {@value}.
    */
   private static final int SECOND_PREPARE_APPLY_WEIGHT = 2;
   /**
    * The weight of reloaders' progress in the total progress calculation. Has value {@value}.
    */
   private static final int RELOADER_WEIGHT = 1;
   protected final ResourceManager manager;
   protected final CompletableFuture<Unit> prepareStageFuture = new CompletableFuture();
   protected final CompletableFuture<List<S>> applyStageFuture;
   final Set<ResourceReloader> waitingReloaders;
   private final int reloaderCount;
   private int toApplyCount;
   private int appliedCount;
   private final AtomicInteger toPrepareCount = new AtomicInteger();
   private final AtomicInteger preparedCount = new AtomicInteger();

   /**
    * Creates a simple resource reload without additional results.
    */
   public static SimpleResourceReload<Void> create(ResourceManager manager, List<ResourceReloader> reloaders, Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage) {
      return new SimpleResourceReload(prepareExecutor, applyExecutor, manager, reloaders, (synchronizer, resourceManager, reloader, prepare, apply) -> {
         return reloader.reload(synchronizer, resourceManager, DummyProfiler.INSTANCE, DummyProfiler.INSTANCE, prepareExecutor, apply);
      }, initialStage);
   }

   protected SimpleResourceReload(Executor prepareExecutor, final Executor applyExecutor, ResourceManager manager, List<ResourceReloader> reloaders, SimpleResourceReload.Factory<S> factory, CompletableFuture<Unit> initialStage) {
      this.manager = manager;
      this.reloaderCount = reloaders.size();
      this.toPrepareCount.incrementAndGet();
      AtomicInteger var10001 = this.preparedCount;
      Objects.requireNonNull(var10001);
      initialStage.thenRun(var10001::incrementAndGet);
      List<CompletableFuture<S>> list = Lists.newArrayList();
      final CompletableFuture<?> completableFuture = initialStage;
      this.waitingReloaders = Sets.newHashSet((Iterable)reloaders);

      CompletableFuture completableFuture3;
      for(Iterator var9 = reloaders.iterator(); var9.hasNext(); completableFuture = completableFuture3) {
         final ResourceReloader resourceReloader = (ResourceReloader)var9.next();
         completableFuture3 = factory.create(new ResourceReloader.Synchronizer() {
            public <T> CompletableFuture<T> whenPrepared(T preparedObject) {
               applyExecutor.execute(() -> {
                  SimpleResourceReload.this.waitingReloaders.remove(resourceReloader);
                  if (SimpleResourceReload.this.waitingReloaders.isEmpty()) {
                     SimpleResourceReload.this.prepareStageFuture.complete(Unit.INSTANCE);
                  }

               });
               return SimpleResourceReload.this.prepareStageFuture.thenCombine(completableFuture, (unit, object2) -> {
                  return preparedObject;
               });
            }
         }, manager, resourceReloader, (preparation) -> {
            this.toPrepareCount.incrementAndGet();
            prepareExecutor.execute(() -> {
               preparation.run();
               this.preparedCount.incrementAndGet();
            });
         }, (application) -> {
            ++this.toApplyCount;
            applyExecutor.execute(() -> {
               application.run();
               ++this.appliedCount;
            });
         });
         list.add(completableFuture3);
      }

      this.applyStageFuture = Util.combine(list);
   }

   public CompletableFuture<Unit> whenComplete() {
      return this.applyStageFuture.thenApply((results) -> {
         return Unit.INSTANCE;
      });
   }

   public float getProgress() {
      int i = this.reloaderCount - this.waitingReloaders.size();
      float f = (float)(this.preparedCount.get() * 2 + this.appliedCount * 2 + i * 1);
      float g = (float)(this.toPrepareCount.get() * 2 + this.toApplyCount * 2 + this.reloaderCount * 1);
      return f / g;
   }

   public boolean isComplete() {
      return this.applyStageFuture.isDone();
   }

   public void throwException() {
      if (this.applyStageFuture.isCompletedExceptionally()) {
         this.applyStageFuture.join();
      }

   }

   /**
    * A factory that creates a completable future for each reloader in the
    * resource reload.
    */
   protected interface Factory<S> {
      CompletableFuture<S> create(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, ResourceReloader reloader, Executor prepareExecutor, Executor applyExecutor);
   }
}
