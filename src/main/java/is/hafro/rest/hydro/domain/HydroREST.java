package is.hafro.rest.hydro.domain;

import is.codion.common.item.Item;
import is.codion.framework.domain.DefaultDomain;
import is.codion.framework.domain.DomainType;

import java.util.Arrays;

import static is.codion.framework.domain.property.Properties.*;

public final class HydroREST extends DefaultDomain {

  static final DomainType DOMAIN = DomainType.domainType(HydroREST.class);

  public HydroREST() {
    super(DOMAIN);
    hitamaaelar();
    notandi();
    notendurSvaedi();
    notendaadgangur();
  }


  void hitamaaelar() {
    define(Hitamaelar.TYPE,
            primaryKeyProperty(Hitamaelar.STADUR),
            columnProperty(Hitamaelar.TIMI),
            columnProperty(Hitamaelar.HITASTIG))
            .readOnly();
  }

  void notandi() {
    define(Notandi.TYPE,
            primaryKeyProperty(Notandi.ID),
            columnProperty(Notandi.NOTENDANAFN))
            .readOnly();
  }

  void notendurSvaedi() {
    define(NotendurSvaedi.TYPE,
            primaryKeyProperty(NotendurSvaedi.ID),
            columnProperty(NotendurSvaedi.KERFISNOTANDI_ID),
            columnProperty(NotendurSvaedi.GILDIR_FRA)
                    .nullable(false),
            columnProperty(NotendurSvaedi.GILDIR_TIL)
                    .nullable(false))
            .readOnly();
  }

  private void notendaadgangur() {
    define(Notendaadgangur.TYPE, "notendur.vefadgangur_v",
            primaryKeyProperty(Notendaadgangur.KERFISHEITI).primaryKeyIndex(0),
            primaryKeyProperty(Notendaadgangur.NOTENDANAFN).primaryKeyIndex(1),
            columnProperty(Notendaadgangur.LYKILORD))
            .readOnly();
  }
}
