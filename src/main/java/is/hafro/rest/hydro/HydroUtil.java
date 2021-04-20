package is.hafro.rest.hydro;

import is.codion.common.Conjunction;
import is.codion.common.db.database.Database;
import is.codion.common.db.exception.DatabaseException;
import is.codion.common.user.User;
import is.codion.dbms.oracle.OracleDatabaseFactory;
import is.codion.framework.db.EntityConnection;
import is.codion.framework.db.condition.Condition;
import is.codion.framework.db.condition.Conditions;
import is.codion.framework.domain.Domain;
import is.codion.framework.domain.entity.Entity;
import is.hafro.rest.hydro.domain.Hitamaelar;
import is.hafro.rest.hydro.domain.HydroREST;
import is.hafro.rest.hydro.domain.Notandi;
import is.hafro.rest.hydro.domain.Notendaadgangur;
import is.hafro.rest.hydro.domain.NotendurSvaedi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static is.codion.framework.db.condition.Conditions.condition;
import static is.codion.framework.domain.entity.OrderBy.orderBy;
import static java.util.stream.Collectors.toList;

public final class HydroUtil {

  private static final Logger LOG = LoggerFactory.getLogger(HydroUtil.class);

  private static final String AUTHORIZATION = "Authorization";
  private static final String HYDRO = "HydroWS";
  private static final String JDBC_RESOURCE = "jdbc/hydro";
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  static final Domain DOMAIN = new HydroREST();

  static boolean authenticateUser(final User user, final EntityConnection connection) throws DatabaseException {
    final Condition kerfiCriteria = condition(Notendaadgangur.KERFISHEITI).equalTo(HYDRO);
    final Condition notendanafnCriteria = condition(Notendaadgangur.NOTENDANAFN).equalTo(user.getUsername());
    final Condition lykilordCriteria = condition(Notendaadgangur.LYKILORD).equalTo(String.valueOf(user.getPassword()));

    return connection.rowCount(Conditions.combination(Conjunction.AND, kerfiCriteria, notendanafnCriteria, lykilordCriteria)) > 0;
  }

  static User parseUser(final HttpHeaders headers) {
    final List<String> basic = headers.getRequestHeader(AUTHORIZATION);
    if (basic == null || basic.isEmpty()) {
      throw new WebApplicationException("Auðkenningarupplýsingar vantar", Response.Status.UNAUTHORIZED);
    }

    String auth = basic.get(0);
    if (!auth.toLowerCase().startsWith("basic ")) {
      throw new WebApplicationException("Auðkenningarupplýsingar eru á röngu sniði", Response.Status.UNAUTHORIZED);
    }

    auth = auth.replaceFirst("[B|b]asic ", "");
    final String[] credentials = new String(Base64.getDecoder().decode(auth)).split(":", 2);

    return User.user(credentials[0], credentials[1].toCharArray());
  }

  static boolean hefurAdgang(final EntityConnection connection, final Hitamaelar veidilist) throws DatabaseException, ParseException {
    try {
/*      final List<Entity> veidistadir = connection.select(Veidistadur.ID,
              veidilist.stream().map(Veidi::getVeidistadurId).distinct().collect(toList()));
      final Map<Entity, List<Entity>> veidisvaediVeidistadir = Entity.mapToValue(Veidistadur.VEIDISVAEDI_FK, veidistadir);
      for (final Map.Entry<Entity, List<Entity>> svaediStadurEntry : veidisvaediVeidistadir.entrySet()) {
        final Set<Integer> stadirIds = svaediStadurEntry.getValue().stream().map(stadur ->
                stadur.get(Veidistadur.ID)).collect(Collectors.toSet());
        final List<Veidi> veidistadurVeidi = veidilist.stream().filter(veidi ->
                stadirIds.contains(veidi.getVeidistadurId())).collect(toList());
        final Set<LocalDate> dates = new HashSet<>();
        for (final Veidi veidi : veidistadurVeidi) {
          dates.add(LocalDate.parse(veidi.getDags(), DATE_FORMAT));
        }
        final Optional<LocalDate> minDate = dates.stream().min(LocalDate::compareTo);
        final Optional<LocalDate> maxDate = dates.stream().max(LocalDate::compareTo);
        if (!minDate.isPresent() || !maxDate.isPresent()) {
          throw new IllegalArgumentException("Engar dagsetningar fundust við aðgangsstýringu");
        }
        if (!hefurAdgang(connection, minDate.get(), maxDate.get(),
                Collections.singletonList(svaediStadurEntry.getKey().get(Veidisvaedi.ID)))) {
          return false;
        }
      }*/

      return true;
    }

//    catch (final DatabaseException | DateTimeParseException e) {
    catch (final Exception e) {
      LOG.error("Villa kom upp við að aðgangsstýra notanda " + connection.getUser(), e);
      throw e;
    }
  }

  static boolean hefurAdgang(final EntityConnection connection, final LocalDate dagsFra, final LocalDate dagsTil,
                             final List<Integer> veidisvaediId) throws DatabaseException {
    try {
      final Entity notandi = connection.selectSingle(Notandi.NOTENDANAFN, connection.getUser().getUsername().toUpperCase());
      final Condition condition = Conditions.combination(Conjunction.AND,
              condition(NotendurSvaedi.GILDIR_FRA).lessThanOrEqualTo(dagsFra),
              condition(NotendurSvaedi.GILDIR_TIL).greaterThanOrEqualTo(dagsTil));

      return connection.rowCount(condition) == veidisvaediId.size();
    }
    catch (final DatabaseException e) {
      LOG.error("Villa kom upp við aðgangsstýringu fyrir notanda " + connection.getUser(), e);
      throw e;
    }
  }

  static Database initializeDatabase() throws DatabaseException {
    Connection connection = null;
    try {
      connection = lookupConnection(JDBC_RESOURCE);

      return new OracleDatabaseFactory().createDatabase(connection.getMetaData().getURL());
    }
    catch (final SQLException e) {
      throw new DatabaseException(e, e.getMessage());
    }
    finally {
      Database.closeSilently(connection);
    }
  }

  static Connection lookupConnection(final String jdbcResource) {
    try {
      final DataSource ds = (DataSource) new InitialContext().lookup(jdbcResource);
      if (ds == null) {
        throw new RuntimeException("DataSource is null");
      }

      return ds.getConnection();
    }
    catch (final javax.naming.NamingException jnn) {
      LOG.error("Villa kom upp þegar reynt var að nálgast initial context (java:comp/env, " + jdbcResource + ")", jnn);
      throw new RuntimeException(jnn);
    }
    catch (final SQLException jss) {
      LOG.error("Villa kom upp þegar reynt var að nálgast connection úr initial context", jss);
      throw new RuntimeException(jss);
    }
  }

  public static List<Hitamaelar> getHitamaelar(final EntityConnection connection, final String stadur, final LocalDate dagsFra, final LocalDate dagsTil) throws DatabaseException {
    final List<Hitamaelar> hitamaelar;
    final Condition.Combination combination = Conditions.combination(Conjunction.AND);
    final Condition dagsFraCriteria = condition(Hitamaelar.TIMI).greaterThanOrEqualTo(LocalDateTime.from(dagsFra));
    final Condition dagsTilCriteria = condition(Hitamaelar.TIMI).lessThanOrEqualTo(LocalDateTime.from(dagsTil));

    if (stadur != null) {
      combination.add(condition(Hitamaelar.STADUR).equalTo(stadur));
    }
    else {
      hitamaelar = connection.select(Hitamaelar.STADUR, stadur).stream().map(Hitamaelar::new).collect(toList());
    }
    return connection.select(combination.select().orderBy(orderBy().descending(Hitamaelar.TIMI))).stream().map(Hitamaelar::new).collect(toList());
  }
}
