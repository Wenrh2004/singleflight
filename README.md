# SingleFlight


English | [Chinese](./README_zh.md)

A Java utility class for suppressing duplicate requests, inspired by Go's [singleflight](https://golang.org/x/sync/singleflight) package.

## Introduction

SingleFlight is a lightweight concurrency tool designed to prevent multiple thread from performing duplicate operations for the same key. When multiple concurrent requests use the same key, only the first request actually executes the operation, while other requests wait and share the result of the first request. This is particularly useful for preventing cache stampedes, reducing database load, and optimizing performance.

## Features

- Generic Support: Supports any type of key and return value
- Synchronous and Asynchronous API: Provides both blocking and CompletableFuture-based non-blocking APIs
- Operation Cancellation: Supports forced cancellation of ongoing operations
- Thread Safety: Completely thread-safe, suitable for high-concurrency environments
- Lightweight: No external dependencies, concise code

## Installation

### Maven

```xml
<dependency>
    <groupId>com.qit.softwarestudio</groupId>
    <artifactId>singleflight</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Basic Usage

```java
// Create a SingleFlight instance
SingleFlight<String, String> sf = new SingleFlight<>();

// Execute an operation; if an operation with the same key is in progress, it will wait for it to complete and return the result
String result = sf.doOnce("cache-key", () -> {
    // This is the actual operation, such as loading data from a database
    return loadDataFromDatabase();
});
```

### Asynchronous Usage

```java
// Create a SingleFlight instance
SingleFlight<String, String> sf = new SingleFlight<>();

// Execute an operation asynchronously
CompletableFuture<String> future = sf.doOnceFuture("cache-key", () -> {
    // This is the actual operation
    return loadDataFromDatabase();
});

// Process the result
future.thenAccept(result -> {
    System.out.println("Got result: " + result);
});
```

### Cancelling Operations

```java
// Create a SingleFlight instance
SingleFlight<String, String> sf = new SingleFlight<>();

// Start a long-running task in another thread
CompletableFuture.runAsync(() -> {
    sf.doOnceFuture("long-task", () -> {
        // Long-running task
        try {
            Thread.sleep(10000);
            return "completed";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    });
});

// Cancel the task later
boolean cancelled = sf.forget("long-task");
if (cancelled) {
    System.out.println("Task has been cancelled");
}
```

### Checking the Number of Ongoing Operations

```java
// Create a SingleFlight instance
SingleFlight<String, String> sf = new SingleFlight<>();

// Check the current number of ongoing operations
int count = sf.pendingCount();
System.out.println("Current number of ongoing operations: " + count);
```

## Use Cases

### Cache Stampede Protection

When a hot item in the cache expires, there might be a large number of requests hitting the database simultaneously, known as a cache stampede. Using SingleFlight ensures that only one request queries the database, while other requests wait and share the result.

```java
String data = cache.get(key);
if (data == null) {
    // Cache miss, use SingleFlight to ensure only one request queries the database
    data = sf.doOnce(key, () -> {
        String result = database.query(key);
        cache.put(key, result);
        return result;
    });
}
return data;
```

### API Rate Limiting

When multiple microservices need to call the same external API, SingleFlight can be used to merge requests and reduce the number of API calls.

```java
public CompletableFuture<ApiResponse> callExternalApi(String requestId, ApiRequest request) {
    // Use request parameters as the key to merge identical API calls
    String key = request.generateUniqueKey();
    return sf.doOnceFuture(key, () -> {
        return apiClient.call(request);
    });
}
```

## Notes

- After an operation completes, the corresponding key is automatically removed from the internal map, so subsequent uses of the same key will trigger a new operation
- If an operation throws an exception, the exception is propagated to all callers waiting for the result
- When using the `forget` method to cancel an operation, all callers waiting for that operation will receive an exception

## License

[Apache 2.0 License](LICENSE)

## Contributing

[Contributing Guide](./CONTRIBUTING.md)
Issues and pull requests are welcome!