package net.minecraft.util.profiler;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.util.thread.ExecutorSampling;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class ServerSamplerSource implements SamplerSource {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Set<Sampler> samplers = new ObjectOpenHashSet();
   private final SamplerFactory factory = new SamplerFactory();

   public ServerSamplerSource(LongSupplier nanoTimeSupplier, boolean includeSystem) {
      this.samplers.add(createTickTimeTracker(nanoTimeSupplier));
      if (includeSystem) {
         this.samplers.addAll(createSystemSamplers());
      }

   }

   public static Set<Sampler> createSystemSamplers() {
      Builder builder = ImmutableSet.builder();

      try {
         ServerSamplerSource.CpuUsageFetcher cpuUsageFetcher = new ServerSamplerSource.CpuUsageFetcher();
         Stream var10000 = IntStream.range(0, cpuUsageFetcher.logicalProcessorCount).mapToObj((index) -> {
            return Sampler.create("cpu#" + index, SampleType.CPU, () -> {
               return cpuUsageFetcher.getCpuUsage(index);
            });
         });
         Objects.requireNonNull(builder);
         var10000.forEach(builder::add);
      } catch (Throwable var2) {
         LOGGER.warn("Failed to query cpu, no cpu stats will be recorded", var2);
      }

      builder.add((Object)Sampler.create("heap MiB", SampleType.JVM, () -> {
         return (double)((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576.0F);
      }));
      builder.addAll((Iterable)ExecutorSampling.INSTANCE.createSamplers());
      return builder.build();
   }

   public Set<Sampler> getSamplers(Supplier<ReadableProfiler> profilerSupplier) {
      this.samplers.addAll(this.factory.createSamplers(profilerSupplier));
      return this.samplers;
   }

   public static Sampler createTickTimeTracker(final LongSupplier nanoTimeSupplier) {
      Stopwatch stopwatch = Stopwatch.createUnstarted(new Ticker() {
         public long read() {
            return nanoTimeSupplier.getAsLong();
         }
      });
      ToDoubleFunction<Stopwatch> toDoubleFunction = (watch) -> {
         if (watch.isRunning()) {
            watch.stop();
         }

         long l = watch.elapsed(TimeUnit.NANOSECONDS);
         watch.reset();
         return (double)l;
      };
      Sampler.RatioDeviationChecker ratioDeviationChecker = new Sampler.RatioDeviationChecker(2.0F);
      return Sampler.builder("ticktime", SampleType.TICK_LOOP, toDoubleFunction, stopwatch).startAction(Stopwatch::start).deviationChecker(ratioDeviationChecker).build();
   }

   static class CpuUsageFetcher {
      private final SystemInfo systemInfo = new SystemInfo();
      private final CentralProcessor processor;
      public final int logicalProcessorCount;
      private long[][] loadTicks;
      private double[] loadBetweenTicks;
      private long lastCheckTime;

      CpuUsageFetcher() {
         this.processor = this.systemInfo.getHardware().getProcessor();
         this.logicalProcessorCount = this.processor.getLogicalProcessorCount();
         this.loadTicks = this.processor.getProcessorCpuLoadTicks();
         this.loadBetweenTicks = this.processor.getProcessorCpuLoadBetweenTicks(this.loadTicks);
      }

      public double getCpuUsage(int index) {
         long l = System.currentTimeMillis();
         if (this.lastCheckTime == 0L || this.lastCheckTime + 501L < l) {
            this.loadBetweenTicks = this.processor.getProcessorCpuLoadBetweenTicks(this.loadTicks);
            this.loadTicks = this.processor.getProcessorCpuLoadTicks();
            this.lastCheckTime = l;
         }

         return this.loadBetweenTicks[index] * 100.0D;
      }
   }
}
