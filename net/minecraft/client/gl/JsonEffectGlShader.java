package net.minecraft.client.gl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class JsonEffectGlShader implements EffectGlShader, AutoCloseable {
   private static final String PROGRAM_DIRECTORY = "shaders/program/";
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Uniform DEFAULT_UNIFORM = new Uniform();
   private static final boolean field_32683 = true;
   private static JsonEffectGlShader activeShader;
   private static int activeProgramRef = -1;
   private final Map<String, IntSupplier> samplerBinds = Maps.newHashMap();
   private final List<String> samplerNames = Lists.newArrayList();
   private final List<Integer> samplerShaderLocs = Lists.newArrayList();
   private final List<GlUniform> uniformData = Lists.newArrayList();
   private final List<Integer> uniformLocs = Lists.newArrayList();
   private final Map<String, GlUniform> uniformByName = Maps.newHashMap();
   private final int programRef;
   private final String name;
   private boolean uniformStateDirty;
   private final GlBlendState blendState;
   private final List<Integer> attribLocs;
   private final List<String> attribNames;
   private final EffectProgram vertexShader;
   private final EffectProgram fragmentShader;

   public JsonEffectGlShader(ResourceManager resource, String name) throws IOException {
      Identifier identifier = new Identifier("shaders/program/" + name + ".json");
      this.name = name;
      Resource resource2 = null;

      try {
         String string2;
         try {
            resource2 = resource.getResource(identifier);
            JsonObject jsonObject = JsonHelper.deserialize((Reader)(new InputStreamReader(resource2.getInputStream(), StandardCharsets.UTF_8)));
            String string = JsonHelper.getString(jsonObject, "vertex");
            string2 = JsonHelper.getString(jsonObject, "fragment");
            JsonArray jsonArray = JsonHelper.getArray(jsonObject, "samplers", (JsonArray)null);
            if (jsonArray != null) {
               int i = 0;

               for(Iterator var10 = jsonArray.iterator(); var10.hasNext(); ++i) {
                  JsonElement jsonElement = (JsonElement)var10.next();

                  try {
                     this.addSampler(jsonElement);
                  } catch (Exception var24) {
                     ShaderParseException shaderParseException = ShaderParseException.wrap(var24);
                     shaderParseException.addFaultyElement("samplers[" + i + "]");
                     throw shaderParseException;
                  }
               }
            }

            JsonArray jsonArray2 = JsonHelper.getArray(jsonObject, "attributes", (JsonArray)null);
            Iterator var31;
            if (jsonArray2 != null) {
               int j = 0;
               this.attribLocs = Lists.newArrayListWithCapacity(jsonArray2.size());
               this.attribNames = Lists.newArrayListWithCapacity(jsonArray2.size());

               for(var31 = jsonArray2.iterator(); var31.hasNext(); ++j) {
                  JsonElement jsonElement2 = (JsonElement)var31.next();

                  try {
                     this.attribNames.add(JsonHelper.asString(jsonElement2, "attribute"));
                  } catch (Exception var23) {
                     ShaderParseException shaderParseException2 = ShaderParseException.wrap(var23);
                     shaderParseException2.addFaultyElement("attributes[" + j + "]");
                     throw shaderParseException2;
                  }
               }
            } else {
               this.attribLocs = null;
               this.attribNames = null;
            }

            JsonArray jsonArray3 = JsonHelper.getArray(jsonObject, "uniforms", (JsonArray)null);
            if (jsonArray3 != null) {
               int k = 0;

               for(Iterator var33 = jsonArray3.iterator(); var33.hasNext(); ++k) {
                  JsonElement jsonElement3 = (JsonElement)var33.next();

                  try {
                     this.addUniform(jsonElement3);
                  } catch (Exception var22) {
                     ShaderParseException shaderParseException3 = ShaderParseException.wrap(var22);
                     shaderParseException3.addFaultyElement("uniforms[" + k + "]");
                     throw shaderParseException3;
                  }
               }
            }

            this.blendState = deserializeBlendState(JsonHelper.getObject(jsonObject, "blend", (JsonObject)null));
            this.vertexShader = loadEffect(resource, Program.Type.VERTEX, string);
            this.fragmentShader = loadEffect(resource, Program.Type.FRAGMENT, string2);
            this.programRef = GlProgramManager.createProgram();
            GlProgramManager.linkProgram(this);
            this.finalizeUniformsAndSamplers();
            if (this.attribNames != null) {
               var31 = this.attribNames.iterator();

               while(var31.hasNext()) {
                  String string3 = (String)var31.next();
                  int l = GlUniform.getAttribLocation(this.programRef, string3);
                  this.attribLocs.add(l);
               }
            }
         } catch (Exception var25) {
            if (resource2 != null) {
               string2 = " (" + resource2.getResourcePackName() + ")";
            } else {
               string2 = "";
            }

            ShaderParseException shaderParseException4 = ShaderParseException.wrap(var25);
            String var10001 = identifier.getPath();
            shaderParseException4.addFaultyFile(var10001 + string2);
            throw shaderParseException4;
         }
      } finally {
         IOUtils.closeQuietly((Closeable)resource2);
      }

      this.markUniformsDirty();
   }

   public static EffectProgram loadEffect(ResourceManager resourceManager, Program.Type type, String name) throws IOException {
      Program program = (Program)type.getProgramCache().get(name);
      if (program != null && !(program instanceof EffectProgram)) {
         throw new InvalidClassException("Program is not of type EffectProgram");
      } else {
         EffectProgram effectProgram3;
         if (program == null) {
            Identifier identifier = new Identifier("shaders/program/" + name + type.getFileExtension());
            Resource resource = resourceManager.getResource(identifier);

            try {
               effectProgram3 = EffectProgram.createFromResource(type, name, resource.getInputStream(), resource.getResourcePackName());
            } finally {
               IOUtils.closeQuietly((Closeable)resource);
            }
         } else {
            effectProgram3 = (EffectProgram)program;
         }

         return effectProgram3;
      }
   }

   public static GlBlendState deserializeBlendState(JsonObject json) {
      if (json == null) {
         return new GlBlendState();
      } else {
         int i = 32774;
         int j = 1;
         int k = 0;
         int l = 1;
         int m = 0;
         boolean bl = true;
         boolean bl2 = false;
         if (JsonHelper.hasString(json, "func")) {
            i = GlBlendState.getFuncFromString(json.get("func").getAsString());
            if (i != 32774) {
               bl = false;
            }
         }

         if (JsonHelper.hasString(json, "srcrgb")) {
            j = GlBlendState.getComponentFromString(json.get("srcrgb").getAsString());
            if (j != 1) {
               bl = false;
            }
         }

         if (JsonHelper.hasString(json, "dstrgb")) {
            k = GlBlendState.getComponentFromString(json.get("dstrgb").getAsString());
            if (k != 0) {
               bl = false;
            }
         }

         if (JsonHelper.hasString(json, "srcalpha")) {
            l = GlBlendState.getComponentFromString(json.get("srcalpha").getAsString());
            if (l != 1) {
               bl = false;
            }

            bl2 = true;
         }

         if (JsonHelper.hasString(json, "dstalpha")) {
            m = GlBlendState.getComponentFromString(json.get("dstalpha").getAsString());
            if (m != 0) {
               bl = false;
            }

            bl2 = true;
         }

         if (bl) {
            return new GlBlendState();
         } else {
            return bl2 ? new GlBlendState(j, k, l, m, i) : new GlBlendState(j, k, i);
         }
      }
   }

   public void close() {
      Iterator var1 = this.uniformData.iterator();

      while(var1.hasNext()) {
         GlUniform glUniform = (GlUniform)var1.next();
         glUniform.close();
      }

      GlProgramManager.deleteProgram(this);
   }

   public void disable() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GlProgramManager.useProgram(0);
      activeProgramRef = -1;
      activeShader = null;

      for(int i = 0; i < this.samplerShaderLocs.size(); ++i) {
         if (this.samplerBinds.get(this.samplerNames.get(i)) != null) {
            GlStateManager._activeTexture('蓀' + i);
            GlStateManager._disableTexture();
            GlStateManager._bindTexture(0);
         }
      }

   }

   public void enable() {
      RenderSystem.assertThread(RenderSystem::isOnGameThread);
      this.uniformStateDirty = false;
      activeShader = this;
      this.blendState.enable();
      if (this.programRef != activeProgramRef) {
         GlProgramManager.useProgram(this.programRef);
         activeProgramRef = this.programRef;
      }

      for(int i = 0; i < this.samplerShaderLocs.size(); ++i) {
         String string = (String)this.samplerNames.get(i);
         IntSupplier intSupplier = (IntSupplier)this.samplerBinds.get(string);
         if (intSupplier != null) {
            RenderSystem.activeTexture('蓀' + i);
            RenderSystem.enableTexture();
            int j = intSupplier.getAsInt();
            if (j != -1) {
               RenderSystem.bindTexture(j);
               GlUniform.uniform1((Integer)this.samplerShaderLocs.get(i), i);
            }
         }
      }

      Iterator var5 = this.uniformData.iterator();

      while(var5.hasNext()) {
         GlUniform glUniform = (GlUniform)var5.next();
         glUniform.upload();
      }

   }

   public void markUniformsDirty() {
      this.uniformStateDirty = true;
   }

   @Nullable
   public GlUniform getUniformByName(String name) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return (GlUniform)this.uniformByName.get(name);
   }

   public Uniform getUniformByNameOrDummy(String name) {
      RenderSystem.assertThread(RenderSystem::isOnGameThread);
      GlUniform glUniform = this.getUniformByName(name);
      return (Uniform)(glUniform == null ? DEFAULT_UNIFORM : glUniform);
   }

   private void finalizeUniformsAndSamplers() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      IntList intList = new IntArrayList();

      int k;
      for(k = 0; k < this.samplerNames.size(); ++k) {
         String string = (String)this.samplerNames.get(k);
         int j = GlUniform.getUniformLocation(this.programRef, string);
         if (j == -1) {
            LOGGER.warn((String)"Shader {} could not find sampler named {} in the specified shader program.", (Object)this.name, (Object)string);
            this.samplerBinds.remove(string);
            intList.add(k);
         } else {
            this.samplerShaderLocs.add(j);
         }
      }

      for(k = intList.size() - 1; k >= 0; --k) {
         this.samplerNames.remove(intList.getInt(k));
      }

      Iterator var6 = this.uniformData.iterator();

      while(var6.hasNext()) {
         GlUniform glUniform = (GlUniform)var6.next();
         String string2 = glUniform.getName();
         int l = GlUniform.getUniformLocation(this.programRef, string2);
         if (l == -1) {
            LOGGER.warn((String)"Shader {} could not find uniform named {} in the specified shader program.", (Object)this.name, (Object)string2);
         } else {
            this.uniformLocs.add(l);
            glUniform.setLoc(l);
            this.uniformByName.put(string2, glUniform);
         }
      }

   }

   private void addSampler(JsonElement json) {
      JsonObject jsonObject = JsonHelper.asObject(json, "sampler");
      String string = JsonHelper.getString(jsonObject, "name");
      if (!JsonHelper.hasString(jsonObject, "file")) {
         this.samplerBinds.put(string, (Object)null);
         this.samplerNames.add(string);
      } else {
         this.samplerNames.add(string);
      }
   }

   public void bindSampler(String samplerName, IntSupplier intSupplier) {
      if (this.samplerBinds.containsKey(samplerName)) {
         this.samplerBinds.remove(samplerName);
      }

      this.samplerBinds.put(samplerName, intSupplier);
      this.markUniformsDirty();
   }

   private void addUniform(JsonElement json) throws ShaderParseException {
      JsonObject jsonObject = JsonHelper.asObject(json, "uniform");
      String string = JsonHelper.getString(jsonObject, "name");
      int i = GlUniform.getTypeIndex(JsonHelper.getString(jsonObject, "type"));
      int j = JsonHelper.getInt(jsonObject, "count");
      float[] fs = new float[Math.max(j, 16)];
      JsonArray jsonArray = JsonHelper.getArray(jsonObject, "values");
      if (jsonArray.size() != j && jsonArray.size() > 1) {
         throw new ShaderParseException("Invalid amount of values specified (expected " + j + ", found " + jsonArray.size() + ")");
      } else {
         int k = 0;

         for(Iterator var9 = jsonArray.iterator(); var9.hasNext(); ++k) {
            JsonElement jsonElement = (JsonElement)var9.next();

            try {
               fs[k] = JsonHelper.asFloat(jsonElement, "value");
            } catch (Exception var13) {
               ShaderParseException shaderParseException = ShaderParseException.wrap(var13);
               shaderParseException.addFaultyElement("values[" + k + "]");
               throw shaderParseException;
            }
         }

         if (j > 1 && jsonArray.size() == 1) {
            while(k < j) {
               fs[k] = fs[0];
               ++k;
            }
         }

         int l = j > 1 && j <= 4 && i < 8 ? j - 1 : 0;
         GlUniform glUniform = new GlUniform(string, i + l, j, this);
         if (i <= 3) {
            glUniform.setForDataType((int)fs[0], (int)fs[1], (int)fs[2], (int)fs[3]);
         } else if (i <= 7) {
            glUniform.setForDataType(fs[0], fs[1], fs[2], fs[3]);
         } else {
            glUniform.set(fs);
         }

         this.uniformData.add(glUniform);
      }
   }

   public Program getVertexShader() {
      return this.vertexShader;
   }

   public Program getFragmentShader() {
      return this.fragmentShader;
   }

   public void attachReferencedShaders() {
      this.fragmentShader.attachTo(this);
      this.vertexShader.attachTo(this);
   }

   public String getName() {
      return this.name;
   }

   public int getProgramRef() {
      return this.programRef;
   }
}
