package net.minecraft.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifierDefault;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

/**
 * Specifies that all methods in the annotated package/class return nonnull
 * values unless nullability is specified with a separate annotation.
 * 
 * <p>While this annotation is meant to be a package/class annotation, it can
 * be applied to anything as it does not restrict its targets.
 * 
 * <p>This annotation is used on the client exclusively.
 * 
 * @see MethodsReturnNonnullByDefault
 * @see MathMethodsReturnNonnullByDefault
 */
@NotNull
@TypeQualifierDefault({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Environment(EnvType.CLIENT)
public @interface ClientMethodsReturnNonnullByDefault {
}
