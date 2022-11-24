package test;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.spotify.Playlist;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class PlaylistTests {

	RequestSpecification requestSpecification;
	ResponseSpecification responseSpecification;

	public static String token = "BQAZ0KnAtzH9cceOb-Votz29w-4WFrOsXGOm1lGaJcCuo7STc8zDZ8A4Ty-3d69ZbTOSkwEUTuu_UHY7e3nObfnQT2M1dd_zriOP1XRo4MFkxdbXyTQlK1aXIRBAWQdRCVp4f_BgVsbnxdQp6TsWkC-tme5x-EABoR1yBQTmKlxcpozdoDYK_pgHye-vMsSwmYdNYYKDZzOMdYKvR-KOj18SSI2KcVvYxLlounXh9UpmEQu--uNlypN9icI6Vm_-ZdSdM1kVZQoqzuaz";

	String generatedId = null;

	@BeforeClass
	public void setup() {

		RequestSpecBuilder rsb = new RequestSpecBuilder();
		rsb.setBaseUri("https://api.spotify.com");
		rsb.setBasePath("/v1");
		rsb.addHeader("Authorization", "Bearer " + token + "");
		rsb.log(LogDetail.ALL);
		rsb.setContentType(ContentType.JSON);
		requestSpecification = rsb.build();

		ResponseSpecBuilder responseSpecBuilder = new ResponseSpecBuilder();
		responseSpecBuilder.log(LogDetail.ALL);

		responseSpecification = responseSpecBuilder.build();
	}

	@Test
	public void createPlaylist() {
		String payload = "{\r\n" + "  \"name\": \"New Playlist 101\",\r\n"
				+ "  \"description\": \"New playlist description\",\r\n" + "  \"public\": false\r\n" + "}";

		Response response = RestAssured.given().spec(requestSpecification).body(payload).when()
				.post("/users/31pu4ya3datgyc76yxvxilxvydm4/playlists").then().spec(responseSpecification).assertThat()
				.statusCode(201).body("name", Matchers.equalTo("Appu 1")).extract().response();

		JsonPath jsonPath = new JsonPath(response.asString());
		generatedId = jsonPath.getString("id");
	}

	@Test
	public void createPlaylistUsingPojo() {

		Playlist playlist = new Playlist();
		playlist.setName("Appu 2");
		playlist.setDescription("Appu test");
		playlist.setPublic(false);

		Playlist response = RestAssured.given().spec(requestSpecification).body(playlist).when()
				.post("/users/31pu4ya3datgyc76yxvxilxvydm4/playlists").then().spec(responseSpecification).assertThat()
				.statusCode(201).extract().as(Playlist.class);

		Assert.assertEquals(response.getName(), "Appu 2");
		Assert.assertEquals(response.getDescription(), "Appu test");
		
	}

	@Test(dependsOnMethods = "createPlaylist")
	public void getPlaylist() {

		RestAssured.given().spec(requestSpecification).when().get("/playlists/" + generatedId + "").then()
				.spec(responseSpecification).assertThat().statusCode(200).body("id", Matchers.equalTo(generatedId));

	}

	@Test
	public void updatePlaylist() {
		String payload = "{\r\n" + "  \"name\": \"Sad Playlist 1\",\r\n"
				+ "  \"description\": \"Updated for testing purpose\",\r\n" + "  \"public\": false\r\n" + "}";

		RestAssured.given().spec(requestSpecification).body(payload).when().put("/playlists/5fEM8cICnch84LytSoUxIK")
				.then().spec(responseSpecification).assertThat().statusCode(200);

	}

	@Test
	public void createPlaylistWithoutName() {
		String payload = "{\r\n" + "  \"name\": \"\",\r\n" + "  \"description\": \"New playlist description\",\r\n"
				+ "  \"public\": false\r\n" + "}";

		Response response = RestAssured.given().spec(requestSpecification).body(payload).when()
				.post("/users/31pu4ya3datgyc76yxvxilxvydm4/playlists").then().spec(responseSpecification).assertThat()
				.statusCode(400).body("error.message", Matchers.equalTo("Missing required field: name")).extract()
				.response();

	}

	@Test
	public void createPlaylistWithExpiredAccessToken() {
		String payload = "{\r\n" + "  \"name\": \"Test \",\r\n" + "  \"description\": \"New playlist description\",\r\n"
				+ "  \"public\": false\r\n" + "}";

		String token = "BQBkEAX7liNRLTsPjB4POgobm3cR52sysk3AYgYLV4mxtDux6oe5xxli3gHu2zcI1HfhEcvwG4c_I5gWMx-wAYJ0uHSd3fcFsT5lS3DJiBBkx-eQntFkl4WQD3U2o2hOATm7OGco5AfiNexjI0zt7HDxgLrsUiiyXK3RtJkyEuXNw2JETXNdPgbyE4fp1Gvz5UiHOtQCyU1hvgYVTHTwTeI6n3awVn65xP4b7-lFDgKZN2qkWyrj0o_1r4mSlo05MwVTd2OeMF69vN6P";

		Response response = RestAssured.given().header("Authorization", "Bearer " + token + "")
				.spec(requestSpecification).body(payload).when().post("/users/31pu4ya3datgyc76yxvxilxvydm4/playlists")
				.then().spec(responseSpecification).assertThat().statusCode(401)
				.body("error.message", Matchers.equalTo("The access token expired")).extract().response();

	}

}
