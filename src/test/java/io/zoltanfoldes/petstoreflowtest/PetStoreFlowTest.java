package io.zoltanfoldes.petstoreflowtest;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import static io.restassured.RestAssured.*;
import io.restassured.common.mapper.TypeRef;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;
import io.qameta.allure.*;

@Epic("Pet Store API")
@Feature("Pet Flow Management")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PetStoreFlowTest {
    private Long petId = System.currentTimeMillis();
    private String petName = "Mancs";
    private Map<String, Object> petCategory = Map.of(
            "id", 887,
            "name", "dog");
    private Map<String, Object> petGetResponse;
    private String photoUrl = "https://placedog.net/400/555?id=136";

    @BeforeAll
    public void setup() {
        baseURI = "https://petstore.swagger.io/v2";
        RestAssured.filters(new AllureRestAssured());
    }

    @Test
    @Order(1)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Kisállat létrehozása")
    @Description("Új állat létrehozása az adatbázisban a /pet végponttal.")
    @Step("Post kérés küldése a /pet végpontra")
    public void testCreatePet() {
        Map<String, Object> body = Map.of(
                "id", petId,
                "name", petName,
                "category", petCategory,
                "status", "available"
        );

        petId = given()
                .contentType("application/json")
                .body(body)
                .when()
                .post("/pet")
                .then()
                .statusCode(200)
                .body("name", equalTo(petName))
                .body("id", equalTo(petId))
                .body("category.name", equalTo("dog"))
                .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(2)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Kisállat lekérdezése")
    @Description("Létrehozott állat adatainak verifikálása")
    @Step("Get kérés küldése a /pet/{petId} végpontra")
    public void testGetPet(){
        petGetResponse = given()
                .when()
                .get("/pet/" + petId)
                .then()
                .statusCode(200)
                .body("name", equalTo(petName))
                .body("category.name", equalTo("dog"))
                .extract().as(new TypeRef<Map<String, Object>>() {});
    }

    @Test
    @Order(3)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Kisállat adatainak módosítása")
    @Description("Már létrehozott állat adatainak módosítása")
    @Step("Put kérés küldése a /pet végpontra")
    public void testUpdatePet(){
        var updateBody = new HashMap<>(petGetResponse);
        updateBody.put("photoUrls", java.util.List.of(photoUrl));
        updateBody.put("tags", java.util.List.of(
                Map.of("id", 154, "name", "szobatiszta"),
                Map.of("id", 12, "name", "barátságos")
        ));

        given()
                .contentType("application/json")
                .body(updateBody)
                .when()
                .put("/pet")
                .then()
                .statusCode(200)
                .body("id", equalTo(petId))
                .body("name", equalTo(petName))
                .body("category.name", equalTo("dog"));

    }

    @Test
    @Order(4)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Adatmódosítás ellenőrzése")
    @Description("Adatmódosítás verifikálása")
    @Step("Get kérés küldése a /pet/{petId} végpontra")
    public void testVerifyUpdate() {
        given()
                .when()
                .get("/pet/" + petId)
                .then()
                .statusCode(200)
                .body("photoUrls[0]", equalTo(photoUrl))
                .body("tags[0].name", equalTo("szobatiszta"))
                .body("tags[1].name", equalTo("barátságos"));
    }

    @Test
    @Order(5)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Kisállat törlése")
    @Description("Állat törlése az adatbázisból a /pet/{petiD} végponttal.")
    @Step("Delete kérés küldése a /pet/{petId} végpontra")
    public void testDeletePet() {
        given()
                .when()
                .delete("/pet/" + petId)
                .then()
                .statusCode(200)
                .body("message", equalTo(petId.toString()));
    }
}