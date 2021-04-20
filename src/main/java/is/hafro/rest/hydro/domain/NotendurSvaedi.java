package is.hafro.rest.hydro.domain;

import is.codion.framework.domain.entity.Attribute;
import is.codion.framework.domain.entity.Entity;
import is.codion.framework.domain.entity.EntityType;
import is.codion.framework.domain.entity.ForeignKey;

import java.time.LocalDate;

public interface NotendurSvaedi {
  EntityType<Entity> TYPE = HydroREST.DOMAIN.entityType("hydro.hitamaelar");

  Attribute<Integer> ID = TYPE.integerAttribute("id");
  Attribute<Integer> KERFISNOTANDI_ID = TYPE.integerAttribute("kerfisnotandi_id");
  Attribute<LocalDate> GILDIR_FRA = TYPE.localDateAttribute("gildir_fra");
  Attribute<LocalDate> GILDIR_TIL = TYPE.localDateAttribute("gildir_til");
}
