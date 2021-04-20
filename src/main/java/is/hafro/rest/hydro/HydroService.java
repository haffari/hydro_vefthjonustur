package is.hafro.rest.hydro;

import is.codion.common.db.exception.DatabaseException;
import is.codion.common.user.User;
import is.codion.dbms.oracle.OracleDatabaseFactory;
import is.codion.framework.db.EntityConnection;
import is.codion.framework.db.local.LocalEntityConnection;
import is.hafro.rest.hydro.domain.Hitamaelar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Path("/")
public class HydroService {

  private static final Logger LOG = LoggerFactory.getLogger(HydroService.class);

  private static final String JDBC_RESOURCE = "jdbc/hydro";

  @GET
  @Path("/hiti")
  @Produces({MediaType.APPLICATION_JSON})
  public List<Hitamaelar> getHitamaelar(final HttpHeaders headers, @QueryParam("stadur") final String stadur,
                                        @QueryParam("dagsFra") final String dagsFra,
                                        @QueryParam("dagsTil") final String dagsTil) {
    try (final EntityConnection connection = getConnection()) {
      authenticate(headers, connection);

      Objects.requireNonNull(dagsFra, "dagsFra");
      Objects.requireNonNull(dagsTil, "dagsTil");

      final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

      return HydroUtil.getHitamaelar(connection, stadur, LocalDate.parse(dagsFra, dateTimeFormatter),
              LocalDate.parse(dagsTil, dateTimeFormatter));
    }
    catch (Exception ex) {
      LOG.error("Villa kom upp þegar reynt var að sækja hitamæla", ex);
      throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  private static EntityConnection getConnection() throws DatabaseException {
    try {
      final Connection connection = HydroUtil.lookupConnection(JDBC_RESOURCE);

      return LocalEntityConnection.localEntityConnection(HydroUtil.DOMAIN,
              new OracleDatabaseFactory().createDatabase(connection.getMetaData().getURL()), connection);
    }
    catch (final SQLException e) {
      throw new DatabaseException(e, e.getMessage());
    }
  }

  private static void authenticate(final HttpHeaders headers, final EntityConnection connection) {
    final User user = HydroUtil.parseUser(headers);
    try {
      if (!HydroUtil.authenticateUser(user, connection)) {
        throw new WebApplicationException("Rangt notendanafn eða lykilorð", Response.Status.UNAUTHORIZED);
      }
    }
    catch (final DatabaseException e) {
      LOG.error("Villa kom upp við að auðkenna notanda", e);
      throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }
}
