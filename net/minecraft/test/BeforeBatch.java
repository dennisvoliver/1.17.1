package net.minecraft.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code BeforeBatch} methods are ran before the batch specified has started.
 * 
 * <p>{@code BeforeBatch} methods must take 1 parameter of {@link net.minecraft.server.world.ServerWorld}.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeBatch {
   String batchId();
}
