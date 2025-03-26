# SingleFlight

中文 ｜ [English](./README.md)

一个用于抑制重复请求的Java工具类，灵感来源于Go语言的 [singleflight](https://golang.org/x/sync/singleflight) 包。

## 简介

SingleFlight是一个轻量级的并发工具，用于防止多个线程针对同一个key重复执行操作。当多个并发请求使用相同的key时，只有第一个请求会实际执行操作，其他请求会等待并共享第一个请求的结果。这对于防止缓存击穿、减轻数据库负载以及优化性能非常有用。

## 特性

- 泛型支持：支持任意类型的key和返回值
- 同步和异步API：提供阻塞式和基于 `CompletableFuture` 的非阻塞式API
- 取消操作：支持强制取消正在进行的操作
- 线程安全：完全线程安全，适用于高并发环境
- 轻量级：无外部依赖，代码简洁

## 安装

### Maven

```xml
<dependency>
    <groupId>com.qit.softwarestudio</groupId>
    <artifactId>singleflight</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## 使用方法

### 基本用法

```java
// 创建SingleFlight实例
SingleFlight<String, String> sf = new SingleFlight<>();

// 执行操作，如果有相同key的操作正在进行，会等待其完成并返回结果
String result = sf.doOnce("cache-key", () -> {
    // 这里是实际的操作，例如从数据库加载数据
    return loadDataFromDatabase();
});
```

### 异步用法

```java
// 创建SingleFlight实例
SingleFlight<String, String> sf = new SingleFlight<>();

// 异步执行操作
CompletableFuture<String> future = sf.doOnceFuture("cache-key", () -> {
    // 这里是实际的操作
    return loadDataFromDatabase();
});

// 处理结果
future.thenAccept(result -> {
    System.out.println("Got result: " + result);
});
```

### 取消操作

```java
// 创建SingleFlight实例
SingleFlight<String, String> sf = new SingleFlight<>();

// 在另一个线程中启动长时间运行的任务
CompletableFuture.runAsync(() -> {
    sf.doOnceFuture("long-task", () -> {
        // 长时间运行的任务
        try {
            Thread.sleep(10000);
            return "completed";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    });
});

// 稍后取消任务
boolean cancelled = sf.forget("long-task");
if (cancelled) {
    System.out.println("任务已取消");
}
```

### 检查进行中的操作数量

```java
// 创建SingleFlight实例
SingleFlight<String, String> sf = new SingleFlight<>();

// 检查当前正在进行的操作数量
int count = sf.pendingCount();
System.out.println("当前进行中的操作数量: " + count);
```

## 应用场景

### 缓存击穿保护

当缓存中的某个热点数据过期时，可能会有大量请求同时打到数据库，这就是所谓的缓存击穿。使用SingleFlight可以确保只有一个请求去数据库查询，其他请求等待并共享结果。

```java
String data = cache.get(key);
if (data == null) {
    // 缓存未命中，使用SingleFlight确保只有一个请求去数据库查询
    data = sf.doOnce(key, () -> {
        String result = database.query(key);
        cache.put(key, result);
        return result;
    });
}
return data;
```

### API限流

当多个微服务需要调用同一个外部API时，可以使用SingleFlight来合并请求，减少API调用次数。

```java
public CompletableFuture<ApiResponse> callExternalApi(String requestId, ApiRequest request) {
    // 使用请求参数作为key，合并相同的API调用
    String key = request.generateUniqueKey();
    return sf.doOnceFuture(key, () -> {
        return apiClient.call(request);
    });
}
```

## 注意事项

- 操作完成后，相应的key会自动从内部映射中移除，因此后续使用相同的key会触发新的操作
- 如果操作抛出异常，异常会传播给所有等待结果的调用者
- 使用`forget`方法取消操作时，所有等待该操作的调用者都会收到异常

## 许可证

[Apache 2.0 License](LICENSE)

## 贡献

[贡献指南](./CONTRIBUTING_zh.md)
欢迎提交问题和拉取请求！
