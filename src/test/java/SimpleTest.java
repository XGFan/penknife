import com.fasterxml.jackson.databind.JsonNode;
import com.test4x.penknife.PenKnife;
import net.dongliu.requests.Requests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;

@Test
public class SimpleTest {

    @BeforeClass
    public void startServer() {
HashMap<String, Integer> books = new HashMap<>();
PenKnife.INSTANCE
        .get("index", (req, res) -> res.bodyText("hello,world")) //index
        .post("book", (req, res) -> { //添加书
            try {
                final JsonNode json = req.readAsJson(JsonNode.class);
                final String name = json.get("name").asText();
                final Integer price = json.get("price").asInt();
                books.put(name, price);
                res.body(new AbstractMap.SimpleImmutableEntry<>("status", 1));
            } catch (IOException e) {
                e.printStackTrace();
            }
        })
        .delete("book", (req, res) -> { //删除书
            final String name = req.query("name");
            if (books.get(name) != null) {
                books.remove(name);
                res.body(new AbstractMap.SimpleImmutableEntry<>("status", 1));
            } else {
                res.body(new AbstractMap.SimpleImmutableEntry<>("status", -1));
            }
        })
        .get("book/:name", (req, res) -> { //查看书
            final String name = req.path(":name");
            final Integer price = books.get(name);
            if (price != null) {
                res.body(price);
            } else {
                res.body(new AbstractMap.SimpleImmutableEntry<>("status", -1));
            }
        })
        .put("book/:name", (req, res) -> { //修改书
            final String name = req.path(":name");
            final Integer price = Integer.valueOf(req.form("price"));
            if (books.get(name) == null) {
                res.body(new AbstractMap.SimpleImmutableEntry<>("status", -1));
            } else {
                books.put(name, price);
                res.body(new AbstractMap.SimpleImmutableEntry<>("status", 1));
            }
        })
        .start(8080);
System.out.println("started");
    }

    public void testIndex() {
        final String s = Requests.get("http://localhost:8080/index").send().readToText();
        assert s.equals("hello,world");
    }

    public void testPost() {
        HashMap<String, Object> json = new HashMap<>();
        json.put("name", "长袜子皮皮");
        json.put("price", 10);
        final JsonNode jsonNode = Requests.post("http://localhost:8080/book")
                .jsonBody(json)
                .send().readToJson(JsonNode.class);
        assert jsonNode.get("status").asInt() == 1;
    }

    public void testQuery() {
        HashMap<String, Object> json = new HashMap<>();
        json.put("name", "长袜子皮皮");
        final JsonNode jsonNode = Requests.delete("http://localhost:8080/book")
                .params(json)
                .send().readToJson(JsonNode.class);
        assert jsonNode.get("status").asInt() == 1 || jsonNode.get("status").asInt() == -1;
    }
}
