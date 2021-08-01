package net.minecraft.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;

@Environment(EnvType.CLIENT)
public class SpriteTexturedVertexConsumer implements VertexConsumer {
   private final VertexConsumer parent;
   private final Sprite sprite;

   public SpriteTexturedVertexConsumer(VertexConsumer parent, Sprite sprite) {
      this.parent = parent;
      this.sprite = sprite;
   }

   public VertexConsumer vertex(double x, double y, double z) {
      return this.parent.vertex(x, y, z);
   }

   public VertexConsumer color(int red, int green, int blue, int alpha) {
      return this.parent.color(red, green, blue, alpha);
   }

   public VertexConsumer texture(float u, float v) {
      return this.parent.texture(this.sprite.getFrameU((double)(u * 16.0F)), this.sprite.getFrameV((double)(v * 16.0F)));
   }

   public VertexConsumer overlay(int u, int v) {
      return this.parent.overlay(u, v);
   }

   public VertexConsumer light(int u, int v) {
      return this.parent.light(u, v);
   }

   public VertexConsumer normal(float x, float y, float z) {
      return this.parent.normal(x, y, z);
   }

   public void next() {
      this.parent.next();
   }

   public void fixedColor(int red, int green, int blue, int alpha) {
      this.parent.fixedColor(red, green, blue, alpha);
   }

   public void unfixColor() {
      this.parent.unfixColor();
   }

   public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
      this.parent.vertex(x, y, z, red, green, blue, alpha, this.sprite.getFrameU((double)(u * 16.0F)), this.sprite.getFrameV((double)(v * 16.0F)), overlay, light, normalX, normalY, normalZ);
   }
}
