package eu.urbreathdsjobs.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class Stazione {
	String codice;
	double longitudine;
	double latitudine;
	double altitudine;
	String nome;
	String provincia;
	String paese;

	public Stazione(String codice, double longitudine, double latitudine, double altitudine, String nome,
			String provincia, String paese) {
		this.codice = codice;
		this.longitudine = longitudine;
		this.latitudine = latitudine;
		this.altitudine = altitudine;
		this.nome = nome;
		this.provincia = provincia;
		this.paese = paese;
	}

	@Override
	public String toString() {
		return codice + " | " + nome + " | (" + latitudine + ", " + longitudine + ")";
	}
}

public class SensorImportTest {

	private final String ENDPOINT = "https://minio-api-dev.urbreath.tech/";

	private final String BUCKET = "urbreath-public-repo";

	private S3Client buildClient() {
		return S3Client.builder().endpointOverride(URI.create(ENDPOINT)).region(Region.US_EAST_1)
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials
						.create(System.getenv().get("ACCESS_KEY"), System.getenv().get("SECRET_KEY"))))
				.forcePathStyle(true).build();
	}

//	@Test
	public void testImportTemperatureStation() throws SQLException {

		String path = "Madrid/Climate Information/Observations/Temperature_observatories.txt";

		// TODO ENV
		String url = "jdbc:postgresql://188.34.141.13:57708/urbreath_dev";
		String user = "external_user";
		String password = "external_pass";

		Snowflake snowflake = IdUtil.getSnowflake(1, 1);

		int idCity = 1;
		int idCountry = 67; // Spain
		String timezone = "Europe/Madrid";
		int idParam = 9996; // Temperature
		String displayName = "(*C)";

		try (Connection conn = DriverManager.getConnection(url, user, password)) {
			conn.setAutoCommit(false);
			List<Stazione> stazioni = parseStazioni(path);

			for (Stazione s : stazioni) {
				long id = snowflake.nextId();
				insertLocation(conn, id, s.codice, s.nome, timezone, s.latitudine, s.longitudine, idCountry, null,
						idCity);
				insertSensor(conn, id, "", idParam, s.latitudine, s.longitudine, displayName, id);
			}
			
			conn.commit();

		}

	}
	
	@Test
	public void testImportPrecipitationStation() throws SQLException {

		String path = "Madrid/Climate Information/Observations/Precipitation_observatories.txt";

		// TODO ENV
		String url = "jdbc:postgresql://188.34.141.13:57708/urbreath_dev";
		String user = "external_user";
		String password = "external_pass";

		Snowflake snowflake = IdUtil.getSnowflake(1, 1);

		int idCity = 1;
		int idCountry = 67; // Spain
		String timezone = "Europe/Madrid";
		int idParam = 19861; // Temperature
		String displayName = "mm";

		try (Connection conn = DriverManager.getConnection(url, user, password)) {
			conn.setAutoCommit(false);
			List<Stazione> stazioni = parseStazioni(path);

			for (Stazione s : stazioni) {
				long id = snowflake.nextId();
				insertLocation(conn, id, s.codice, s.nome, timezone, s.latitudine, s.longitudine, idCountry, null,
						idCity);
				insertSensor(conn, id, s.codice, idParam, s.latitudine, s.longitudine, displayName, id);
			}
			
			conn.commit();

		}

	}

	public List<Stazione> parseStazioni(String path) {
		List<Stazione> stazioni = new ArrayList<>();
		GetObjectRequest request = GetObjectRequest.builder().bucket(BUCKET).key(path).build();

		InputStream inputStream = buildClient().getObject(request);

		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
			String linea;

			while ((linea = br.readLine()) != null) {
				if (linea.trim().isEmpty())
					continue;

				String[] campi = linea.split("\\t");

				try {
					String codice = campi[0];
					double longitudine = Double.parseDouble(campi[1]);
					double latitudine = Double.parseDouble(campi[2]);
					double altitudine = Double.parseDouble(campi[3]);
					String nome = campi[4];
					String provincia = campi[5];
					String paese = campi[6];

					Stazione stazione = new Stazione(codice, longitudine, latitudine, altitudine, nome, provincia,
							paese);
					stazioni.add(stazione);

					// stampa di test
					System.out.println(stazione);

				} catch (Exception e) {
					System.err.println("Errore parsing linea: " + linea);
				}
			}

		} catch (IOException e) {
			System.err.println("Errore lettura file: " + e.getMessage());
		}

		return stazioni;
	}

	public void insertLocation(Connection conn, long idLocation, String name, String locality, String timezone,
			double latitude, double longitude, int idCountry, Integer idZone, Integer idCity) throws SQLException {

		String sql = "INSERT INTO public.\"location\" "
				+ "(id_location, \"name\", locality, timezone, latitude, longitude, id_country, id_zone, id_city) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setLong(1, idLocation);
			ps.setString(2, name);
			ps.setString(3, locality);
			ps.setString(4, timezone);
			ps.setDouble(5, latitude);
			ps.setDouble(6, longitude);
			ps.setInt(7, idCountry);

			if (idZone != null) {
				ps.setInt(8, idZone);
			} else {
				ps.setNull(8, java.sql.Types.INTEGER);
			}

			if (idCity != null) {
				ps.setInt(9, idCity);
			} else {
				ps.setNull(9, java.sql.Types.INTEGER);
			}

			ps.executeUpdate();
		}
	}

	public static void insertSensor(Connection conn, long idSensor, String name, int idParam, double latitude,
			double longitude, String displayName, long idLocation) throws SQLException {

		String sql = "INSERT INTO public.sensor "
				+ "(id_sensor, \"name\", id_param, latitude, longitude, display_name, id_location) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setLong(1, idSensor);
			ps.setString(2, name);
			ps.setInt(3, idParam);
			ps.setDouble(4, latitude);
			ps.setDouble(5, longitude);
			ps.setString(6, displayName);
			ps.setLong(7, idLocation);

			ps.executeUpdate();
		}
	}

}
