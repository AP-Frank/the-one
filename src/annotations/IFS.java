package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to mark fields of a {@link movement.MovementModel} for initialization by the settings file.
 * The value given to this annotation is the name of the setting in the class context. The initial value of the field
 * will be used as a default if the settings is not found.
 * <br><br>
 * {@link IFSProcessor} must be called in the constructor for this to work.
 * <br><br>
 * Example usage:
 * <br><br>
 * &#64;IFS("my_setting") <br>
 * int my_index = 5;
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IFS {
    String value();
}
