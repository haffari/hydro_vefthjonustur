package is.hafro.rest.hydro.domain;

import is.codion.framework.domain.entity.Attribute;
import is.codion.framework.domain.entity.Entity;
import is.codion.framework.domain.entity.EntityType;

import java.time.LocalDateTime;

public class Hitamaelar {

  public static final EntityType<Entity> TYPE = HydroREST.DOMAIN.entityType("hydro.hitamaelar");
  public static final Attribute<String> STADUR = TYPE.stringAttribute("stadur");
  public static final Attribute<LocalDateTime> TIMI = TYPE.localDateTimeAttribute("timi");
  public static final Attribute<Double> HITASTIG = TYPE.doubleAttribute("hitastig");

  private String stadur;
  private LocalDateTime timi;
  private Double hitastig;

  public Hitamaelar() {
  }

  public Hitamaelar(final Entity hitamaelar) {
    setStadur(hitamaelar.get(STADUR));
    setTimi(hitamaelar.get(TIMI));
    setHitastig(hitamaelar.get(HITASTIG));
  }

  public String getStadur() {
    return stadur;
  }

  public void setStadur(final String stadur) {
    this.stadur = stadur;
  }

  public LocalDateTime getTimi() {
    return timi;
  }

  public void setTimi(final LocalDateTime timi) {
    this.timi = timi;
  }

  public Double getHitastig() {
    return hitastig;
  }

  public void setHitastig(final Double hitastig) {
    this.hitastig = hitastig;
  }
}
