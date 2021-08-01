package net.minecraft.util.profiler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.CsvWriter;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecordDumper {
   public static final Path DEBUG_PROFILING_DIRECTORY = Paths.get("debug/profiling");
   public static final String METRICS_DIRECTORY = "metrics";
   public static final String DEVIATIONS_DIRECTORY = "deviations";
   public static final String FILE_NAME = "profiling.txt";
   private static final Logger LOGGER = LogManager.getLogger();
   private final String type;

   public RecordDumper(String type) {
      this.type = type;
   }

   public Path createDump(Set<Sampler> samplers, Map<Sampler, List<Deviation>> deviations, ProfileResult result) {
      try {
         Files.createDirectories(DEBUG_PROFILING_DIRECTORY);
      } catch (IOException var8) {
         throw new UncheckedIOException(var8);
      }

      try {
         Path path = Files.createTempDirectory("minecraft-profiling");
         path.toFile().deleteOnExit();
         Files.createDirectories(DEBUG_PROFILING_DIRECTORY);
         Path path2 = path.resolve(this.type);
         Path path3 = path2.resolve("metrics");
         this.writeSamplers(samplers, path3);
         if (!deviations.isEmpty()) {
            this.writeDeviations(deviations, path2.resolve("deviations"));
         }

         this.save(result, path2);
         return path;
      } catch (IOException var7) {
         throw new UncheckedIOException(var7);
      }
   }

   private void writeSamplers(Set<Sampler> samplers, Path directory) {
      if (samplers.isEmpty()) {
         throw new IllegalArgumentException("Expected at least one sampler to persist");
      } else {
         Map<SampleType, List<Sampler>> map = (Map)samplers.stream().collect(Collectors.groupingBy(Sampler::getType));
         map.forEach((type, sampler) -> {
            this.writeSamplersInType(type, sampler, directory);
         });
      }
   }

   private void writeSamplersInType(SampleType type, List<Sampler> samplers, Path directory) {
      String var10001 = type.getName();
      Path path = directory.resolve(Util.replaceInvalidChars(var10001, Identifier::isPathCharacterValid) + ".csv");
      BufferedWriter writer = null;

      try {
         Files.createDirectories(path.getParent());
         writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
         CsvWriter.Header header = CsvWriter.makeHeader();
         header.addColumn("@tick");
         Iterator var7 = samplers.iterator();

         while(var7.hasNext()) {
            Sampler sampler = (Sampler)var7.next();
            header.addColumn(sampler.getName());
         }

         CsvWriter csvWriter = header.startBody(writer);
         List<Sampler.Data> list = (List)samplers.stream().map(Sampler::collectData).collect(Collectors.toList());
         int i = list.stream().mapToInt(Sampler.Data::getStartTick).summaryStatistics().getMin();
         int j = list.stream().mapToInt(Sampler.Data::getEndTick).summaryStatistics().getMax();

         for(int k = i; k <= j; ++k) {
            Stream<String> stream = list.stream().map((data) -> {
               return String.valueOf(data.getValue(k));
            });
            Object[] objects = Stream.concat(Stream.of(String.valueOf(k)), stream).toArray((ix) -> {
               return new String[ix];
            });
            csvWriter.printRow(objects);
         }

         LOGGER.info((String)"Flushed metrics to {}", (Object)path);
      } catch (Exception var18) {
         LOGGER.error((String)"Could not save profiler results to {}", (Object)path, (Object)var18);
      } finally {
         IOUtils.closeQuietly((Writer)writer);
      }

   }

   private void writeDeviations(Map<Sampler, List<Deviation>> deviations, Path deviationsDirectory) {
      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss.SSS", Locale.UK).withZone(ZoneId.systemDefault());
      deviations.forEach((sampler, sampleDeviations) -> {
         sampleDeviations.forEach((deviation) -> {
            String string = dateTimeFormatter.format(deviation.instant);
            Path path2 = deviationsDirectory.resolve(Util.replaceInvalidChars(sampler.getName(), Identifier::isPathCharacterValid)).resolve(String.format(Locale.ROOT, "%d@%s.txt", deviation.ticks, string));
            deviation.result.save(path2);
         });
      });
   }

   private void save(ProfileResult result, Path directory) {
      result.save(directory.resolve("profiling.txt"));
   }
}
