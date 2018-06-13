### Penknife

基于Netty的Web Framework

小巧，简单

适合玩具级别的应用使用；）



#### Usage

```java
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
```

