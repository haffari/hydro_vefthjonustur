package is.hafro.rest.hydro.domain;

import is.codion.framework.domain.entity.Attribute;
import is.codion.framework.domain.entity.Entity;
import is.codion.framework.domain.entity.EntityType;

public interface Notendaadgangur {
  EntityType<Entity> TYPE = HydroREST.DOMAIN.entityType("is.fiskistofa.rest.veidibok.vefadgangur_v");
  Attribute<String> KERFISHEITI = TYPE.stringAttribute("kerfi_heiti");
  Attribute<String> NOTENDANAFN = TYPE.stringAttribute("notendanafn");
  Attribute<String> LYKILORD = TYPE.stringAttribute("lykilord");
}
