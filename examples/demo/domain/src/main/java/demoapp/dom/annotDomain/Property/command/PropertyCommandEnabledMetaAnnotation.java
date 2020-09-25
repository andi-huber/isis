package demoapp.dom.annotDomain.Property.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.CommandReification;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;

//tag::class[]
@Property(command = CommandReification.ENABLED) // <.>
@Inherited
@Target({
        ElementType.FIELD, ElementType.METHOD   // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyCommandEnabledMetaAnnotation {

}
//end::class[]