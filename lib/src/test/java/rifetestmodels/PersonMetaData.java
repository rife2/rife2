package rifetestmodels;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

public class PersonMetaData extends MetaData {
    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("firstname")
            .maxLength(10)
            .notNull(true));
        addConstraint(new ConstrainedProperty("lastname")
            .inList("Smith", "Jones", "Ronda"));
    }
}
