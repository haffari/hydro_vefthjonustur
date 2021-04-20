package is.hafro.rest.hydro.domain;

import is.codion.framework.domain.entity.Attribute;
import is.codion.framework.domain.entity.Entity;
import is.codion.framework.domain.entity.EntityType;

public interface Notandi {
  EntityType<Entity> TYPE = HydroREST.DOMAIN.entityType("veidibok.notendur_v");
  Attribute<Integer> ID = TYPE.integerAttribute("id");
  Attribute<String> NOTENDANAFN = TYPE.stringAttribute("notendanafn");
}
