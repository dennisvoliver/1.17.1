package net.minecraft.data.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

public class RegistryDumpProvider implements DataProvider {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private final DataGenerator generator;

   public RegistryDumpProvider(DataGenerator generator) {
      this.generator = generator;
   }

   public void run(DataCache cache) throws IOException {
      JsonObject jsonObject = new JsonObject();
      Registry.REGISTRIES.getIds().forEach((identifier) -> {
         jsonObject.add(identifier.toString(), toJson((Registry)Registry.REGISTRIES.get(identifier)));
      });
      Path path = this.generator.getOutput().resolve("reports/registries.json");
      DataProvider.writeToPath(GSON, cache, jsonObject, path);
   }

   private static <T> JsonElement toJson(Registry<T> registry) {
      JsonObject jsonObject = new JsonObject();
      if (registry instanceof DefaultedRegistry) {
         Identifier identifier = ((DefaultedRegistry)registry).getDefaultId();
         jsonObject.addProperty("default", identifier.toString());
      }

      int i = Registry.REGISTRIES.getRawId(registry);
      jsonObject.addProperty("protocol_id", (Number)i);
      JsonObject jsonObject2 = new JsonObject();
      Iterator var4 = registry.getIds().iterator();

      while(var4.hasNext()) {
         Identifier identifier2 = (Identifier)var4.next();
         T object = registry.get(identifier2);
         int j = registry.getRawId(object);
         JsonObject jsonObject3 = new JsonObject();
         jsonObject3.addProperty("protocol_id", (Number)j);
         jsonObject2.add(identifier2.toString(), jsonObject3);
      }

      jsonObject.add("entries", jsonObject2);
      return jsonObject;
   }

   public String getName() {
      return "Registry Dump";
   }
}
