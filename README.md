short-url
=============

## Overview

A simple HTTP service to shorten an arbitrary URL to exactly 10 symbols with Redis as a storage.

For each new unique URL a unique hash of `[A-Za-z0-9_]{10}` is generated. The hash is a representation of Long in Base63 (26 for `A-Z`, 26 for `a-z`, 10 for `0-9`, 1 for `_`) according to following schema:

```scala
val Base63Schema = ('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9') :+ '_'
```

Hashes are created according to global counter of unique requests and left padded by `Base63Schema(0)` symbol to have a
stable length of 10. E.g.

```
0 -> AAAAAAAAAA 
1 -> AAAAAAAAAB
...
63^10 -> __________ 
```

Global counter is backed by Redis atomic [INCR](https://redis.io/commands/INCR) which uses CAS internally. Global
counter is `unsigned long` with `2^64` cardinality, in Scala it's positive `Long` `(2^63 > 63^10)`, therefore it's enough to cover
all possible cases.

Links are modeled as pair of `(UrlHash -> FullUrl, FullUrl -> UrlHash)` and persisted in Redis. Links are set
via [MSETNX](https://redis.io/commands/msetnx) command which ensures atomicity and uniquness.
`GET` commands have `O(1)` complexity. 

For persistence, Redis should be run with [AOF](https://redis.io/topics/persistence#aof-advantages) enabled
via `--append-only=yes` option.

## HTTP
To create a short link:
```bash
> curl -v localhost:8080/ -d https://github.com
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> POST / HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.55.1
> Accept: */*
> Content-Length: 18
> Content-Type: application/x-www-form-urlencoded
>
* upload completely sent off: 18 out of 18 bytes
< HTTP/1.1 201 Created
< Date: Tue, 08 Jun 2021 19:15:49 GMT
< Connection: keep-alive
< Content-Type: text/plain; charset=UTF-8
< Content-Length: 10
<
AAAAAAAABC
```

To redirect to full url:
```bash 
> curl -v localhost:8080/AAAAAAAABC
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET /AAAAAAAABC HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.55.1
> Accept: */*
>
< HTTP/1.1 301 Moved Permanently
< Date: Tue, 08 Jun 2021 19:17:14 GMT
< Connection: keep-alive
< Content-Type: text/plain; charset=UTF-8
< Content-Length: 18
<
https://github.com
```

To explore OpenAPI doc (yaml):
```bash
> curl -v localhost:8080/docs/openapi
````

## Tests

To run Unit Tests:

```
sbt test
```

To run Integration Tests:

```
sbt it:test
```

To run all tests with [scoverage](https://github.com/scoverage/sbt-scoverage):

```
sbt clean coverage test it: test coverageReport coverageOff
```

## Build Docker image

To build image use:

```
sbt docker:publishLocal
```

To run application using Docker image with composed Redis and Swagger-UI:

```
docker compose up
```

Then navigate to `http://localhost:8080/docs` in your favorite browser to play with API.