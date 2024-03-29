package net.minecraft.text;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Unit;

/**
 * An object that can supply strings to a visitor,
 * with or without a style context.
 */
public interface StringVisitable {
   /**
    * Convenience object indicating the termination of a string visit.
    */
   Optional<Unit> TERMINATE_VISIT = Optional.of(Unit.INSTANCE);
   /**
    * An empty visitable that does not call the visitors.
    */
   StringVisitable EMPTY = new StringVisitable() {
      public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
         return Optional.empty();
      }

      public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> styledVisitor, Style style) {
         return Optional.empty();
      }
   };

   /**
    * Supplies this visitable's literal content to the visitor.
    * 
    * @return {@code Optional.empty()} if the visit finished, or a terminating
    * result from the {@code visitor}
    * 
    * @param visitor the visitor
    */
   <T> Optional<T> visit(StringVisitable.Visitor<T> visitor);

   /**
    * Supplies this visitable's literal content and contextual style to
    * the visitor.
    * 
    * @return {@code Optional.empty()} if the visit finished, or a terminating
    * result from the {@code visitor}
    * 
    * @param styledVisitor the visitor
    * @param style the contextual style
    */
   <T> Optional<T> visit(StringVisitable.StyledVisitor<T> styledVisitor, Style style);

   /**
    * Creates a visitable from a plain string.
    * 
    * @param string the plain string
    */
   static StringVisitable plain(final String string) {
      return new StringVisitable() {
         public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
            return visitor.accept(string);
         }

         public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> styledVisitor, Style style) {
            return styledVisitor.accept(style, string);
         }
      };
   }

   /**
    * Creates a visitable from a plain string and a root style.
    * 
    * @param string the plain string
    * @param style the root style
    */
   static StringVisitable styled(final String string, final Style style) {
      return new StringVisitable() {
         public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
            return visitor.accept(string);
         }

         public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> styledVisitor, Style stylex) {
            return styledVisitor.accept(style.withParent(stylex), string);
         }
      };
   }

   /**
    * Concats multiple string visitables by the order they appear in the array.
    * 
    * @param visitables an array or varargs of visitables
    */
   static StringVisitable concat(StringVisitable... visitables) {
      return concat((List)ImmutableList.copyOf((Object[])visitables));
   }

   /**
    * Concats multiple string visitables by the order they appear in the list.
    * 
    * @param visitables a list of visitables
    */
   static StringVisitable concat(final List<? extends StringVisitable> visitables) {
      return new StringVisitable() {
         public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
            Iterator var2 = visitables.iterator();

            Optional optional;
            do {
               if (!var2.hasNext()) {
                  return Optional.empty();
               }

               StringVisitable stringVisitable = (StringVisitable)var2.next();
               optional = stringVisitable.visit(visitor);
            } while(!optional.isPresent());

            return optional;
         }

         public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> styledVisitor, Style style) {
            Iterator var3 = visitables.iterator();

            Optional optional;
            do {
               if (!var3.hasNext()) {
                  return Optional.empty();
               }

               StringVisitable stringVisitable = (StringVisitable)var3.next();
               optional = stringVisitable.visit(styledVisitor, style);
            } while(!optional.isPresent());

            return optional;
         }
      };
   }

   default String getString() {
      StringBuilder stringBuilder = new StringBuilder();
      this.visit((string) -> {
         stringBuilder.append(string);
         return Optional.empty();
      });
      return stringBuilder.toString();
   }

   /**
    * A visitor for string content.
    */
   public interface Visitor<T> {
      /**
       * Visits a literal string.
       * 
       * <p>When a {@link Optional#isPresent() present optional} is returned,
       * the visit is terminated before visiting all text. Can return {@link
       * StringVisitable#TERMINATE_VISIT} for convenience.
       * 
       * @return {@code Optional.empty()} to continue, a non-empty result to terminate
       * 
       * @param asString the literal string
       */
      Optional<T> accept(String asString);
   }

   /**
    * A visitor for string content and a contextual {@link Style}.
    */
   public interface StyledVisitor<T> {
      /**
       * Visits a string's content with a contextual style.
       * 
       * <p>A contextual style is obtained by calling {@link Style#withParent(Style)}
       * on the current's text style, passing the previous contextual style or
       * the starting style if it is the beginning of a visit.
       * 
       * <p>When a {@link Optional#isPresent() present optional} is returned,
       * the visit is terminated before visiting all text. Can return {@link
       * StringVisitable#TERMINATE_VISIT} for convenience.
       * 
       * @return {@code Optional.empty()} to continue, a non-empty result to terminate
       * 
       * @param style the current style
       * @param asString the literal string
       */
      Optional<T> accept(Style style, String asString);
   }
}
