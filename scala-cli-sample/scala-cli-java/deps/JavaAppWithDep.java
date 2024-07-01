
//> using jvm 17
//> using dep com.google.code.gson:gson:2.8.9
//> 
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import com.google.gson.JsonDeserializer;
import com.google.gson.GsonBuilder;

record CountryRecord(String name, String code, String capital, List<String> languages) {

}

public class JavaAppWithDep {

    public static void main(String args[]) throws Exception {

        // Custom deserializer for CountryRecord
        JsonDeserializer<CountryRecord> deserializer = (json, typeOfT, context) -> {
            var jsonObject = json.getAsJsonObject();
            String name = jsonObject.get("name").getAsString();
            String code = jsonObject.get("code").getAsString();
            String capital = jsonObject.get("capital").getAsString();
            List<String> languages = context.deserialize(jsonObject.get("languages"), new TypeToken<List<String>>() {
            }.getType());
            return new CountryRecord(name, code, capital, languages);
        };

        HttpClient httpClient = HttpClient.newHttpClient();
        URI uri = URI.create("https://raw.githubusercontent.com/yadavan88/blog-code-samples/main/countries.json");
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CountryRecord.class, deserializer)
                .create();
        List<CountryRecord> countries = gson.fromJson(httpResponse.body(), new TypeToken<List<CountryRecord>>() {
        }.getType());
        countries.forEach(System.out::println);
    }
}
