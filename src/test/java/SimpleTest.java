import com.test4x.penknife.PenKnife;

import java.util.AbstractMap;
import java.util.HashMap;

public class SimpleTest {

    public static void main(String[] args) throws InterruptedException {
//        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");//简单设置日志

        HashMap<String, Integer> books = new HashMap<>();

        PenKnife.INSTANCE
                .get("index", (req, res) -> res.bodyText("hello,world")) //index
                .post("book", (req, res) -> { //添加书
                    final String name = req.body("name").toString();
                    final Integer price = Integer.valueOf(req.body("price").toString());
                    books.put(name, price);
                    res.body(new AbstractMap.SimpleImmutableEntry<>("status", 1));
                })
                .delete("book", (req, res) -> { //删除书
                    final String name = req.query("name").get(0);
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
                    final Integer price = Integer.valueOf((String) req.body("price"));
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
}
