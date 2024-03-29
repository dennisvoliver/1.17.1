package net.minecraft.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

/**
 * The lightmap texture manager maintains a texture containing the RGBA overlay for each of the 16&times;16 sky and block light combinations.
 * <p>
 * Also contains some utilities to pack and unpack lightmap coordinates from sky and block light values,
 * and some lightmap coordinates constants.
 */
@Environment(EnvType.CLIENT)
public class LightmapTextureManager implements AutoCloseable {
   /**
    * Represents the maximum lightmap coordinate, where both sky light and block light equals {@code 15}.
    * The value of this maximum lightmap coordinate is {@value}.
    */
   public static final int MAX_LIGHT_COORDINATE = 15728880;
   /**
    * Represents the maximum sky-light-wise lightmap coordinate whose value is {@value}.
    * This is equivalent to a {@code 15} sky light and {@code 0} block light.
    */
   public static final int MAX_SKY_LIGHT_COORDINATE = 15728640;
   /**
    * Represents the maximum block-light-wise lightmap coordinate whose value is {@value}.
    * This is equivalent to a {@code 0} sky light and {@code 15} block light.
    */
   public static final int MAX_BLOCK_LIGHT_COORDINATE = 240;
   private final NativeImageBackedTexture texture;
   private final NativeImage image;
   private final Identifier textureIdentifier;
   private boolean dirty;
   private float flickerIntensity;
   private final GameRenderer renderer;
   private final MinecraftClient client;

   public LightmapTextureManager(GameRenderer renderer, MinecraftClient client) {
      this.renderer = renderer;
      this.client = client;
      this.texture = new NativeImageBackedTexture(16, 16, false);
      this.textureIdentifier = this.client.getTextureManager().registerDynamicTexture("light_map", this.texture);
      this.image = this.texture.getImage();

      for(int i = 0; i < 16; ++i) {
         for(int j = 0; j < 16; ++j) {
            this.image.setPixelColor(j, i, -1);
         }
      }

      this.texture.upload();
   }

   public void close() {
      this.texture.close();
   }

   public void tick() {
      this.flickerIntensity = (float)((double)this.flickerIntensity + (Math.random() - Math.random()) * Math.random() * Math.random() * 0.1D);
      this.flickerIntensity = (float)((double)this.flickerIntensity * 0.9D);
      this.dirty = true;
   }

   public void disable() {
      RenderSystem.setShaderTexture(2, 0);
   }

   public void enable() {
      RenderSystem.setShaderTexture(2, this.textureIdentifier);
      this.client.getTextureManager().bindTexture(this.textureIdentifier);
      RenderSystem.texParameter(3553, 10241, 9729);
      RenderSystem.texParameter(3553, 10240, 9729);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void update(float delta) {
      if (this.dirty) {
         this.dirty = false;
         this.client.getProfiler().push("lightTex");
         ClientWorld clientWorld = this.client.world;
         if (clientWorld != null) {
            float f = clientWorld.method_23783(1.0F);
            float h;
            if (clientWorld.getLightningTicksLeft() > 0) {
               h = 1.0F;
            } else {
               h = f * 0.95F + 0.05F;
            }

            float i = this.client.player.getUnderwaterVisibility();
            float l;
            if (this.client.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
               l = GameRenderer.getNightVisionStrength(this.client.player, delta);
            } else if (i > 0.0F && this.client.player.hasStatusEffect(StatusEffects.CONDUIT_POWER)) {
               l = i;
            } else {
               l = 0.0F;
            }

            Vec3f vec3f = new Vec3f(f, f, 1.0F);
            vec3f.lerp(new Vec3f(1.0F, 1.0F, 1.0F), 0.35F);
            float m = this.flickerIntensity + 1.5F;
            Vec3f vec3f2 = new Vec3f();

            for(int n = 0; n < 16; ++n) {
               for(int o = 0; o < 16; ++o) {
                  float p = this.getBrightness(clientWorld, n) * h;
                  float q = this.getBrightness(clientWorld, o) * m;
                  float s = q * ((q * 0.6F + 0.4F) * 0.6F + 0.4F);
                  float t = q * (q * q * 0.6F + 0.4F);
                  vec3f2.set(q, s, t);
                  float w;
                  Vec3f vec3f5;
                  if (clientWorld.getSkyProperties().shouldBrightenLighting()) {
                     vec3f2.lerp(new Vec3f(0.99F, 1.12F, 1.0F), 0.25F);
                  } else {
                     Vec3f vec3f3 = vec3f.copy();
                     vec3f3.scale(p);
                     vec3f2.add(vec3f3);
                     vec3f2.lerp(new Vec3f(0.75F, 0.75F, 0.75F), 0.04F);
                     if (this.renderer.getSkyDarkness(delta) > 0.0F) {
                        w = this.renderer.getSkyDarkness(delta);
                        vec3f5 = vec3f2.copy();
                        vec3f5.multiplyComponentwise(0.7F, 0.6F, 0.6F);
                        vec3f2.lerp(vec3f5, w);
                     }
                  }

                  vec3f2.clamp(0.0F, 1.0F);
                  float v;
                  if (l > 0.0F) {
                     v = Math.max(vec3f2.getX(), Math.max(vec3f2.getY(), vec3f2.getZ()));
                     if (v < 1.0F) {
                        w = 1.0F / v;
                        vec3f5 = vec3f2.copy();
                        vec3f5.scale(w);
                        vec3f2.lerp(vec3f5, l);
                     }
                  }

                  v = (float)this.client.options.gamma;
                  Vec3f vec3f6 = vec3f2.copy();
                  vec3f6.modify(this::easeOutQuart);
                  vec3f2.lerp(vec3f6, v);
                  vec3f2.lerp(new Vec3f(0.75F, 0.75F, 0.75F), 0.04F);
                  vec3f2.clamp(0.0F, 1.0F);
                  vec3f2.scale(255.0F);
                  int y = true;
                  int z = (int)vec3f2.getX();
                  int aa = (int)vec3f2.getY();
                  int ab = (int)vec3f2.getZ();
                  this.image.setPixelColor(o, n, -16777216 | ab << 16 | aa << 8 | z);
               }
            }

            this.texture.upload();
            this.client.getProfiler().pop();
         }
      }
   }

   /**
    * Represents an easing function.
    * <p>
    * In this class, it's also used to brighten colors,
    * then the result is used to lerp between the normal and brightened color
    * with the gamma value.
    * 
    * @see <a href="https://easings.net/#easeOutQuart">https://easings.net/#easeOutQuart</a>
    * 
    * @param x represents the absolute progress of the animation in the bounds of 0 (beginning of the animation) and 1 (end of animation)
    */
   private float easeOutQuart(float x) {
      float f = 1.0F - x;
      return 1.0F - f * f * f * f;
   }

   private float getBrightness(World world, int lightLevel) {
      return world.getDimension().getBrightness(lightLevel);
   }

   public static int pack(int block, int sky) {
      return block << 4 | sky << 20;
   }

   public static int getBlockLightCoordinates(int light) {
      return light >> 4 & (MAX_BLOCK_LIGHT_COORDINATE | '／');
   }

   public static int getSkyLightCoordinates(int light) {
      return light >> 20 & (MAX_BLOCK_LIGHT_COORDINATE | '／');
   }
}
