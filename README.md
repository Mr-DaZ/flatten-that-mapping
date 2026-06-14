# flatten-that-mapping

An Elasticsearch plugin that exposes flattened views of index mappings — useful when you want a flat list of field
paths (`user.address.city`) instead of nested `properties` trees.

Currently targets **Elasticsearch 9.4.2**.

## Install

```sh
mvn clean package
elasticsearch-plugin install file:///path/to/target/elasticsearch-mapping-flattner-1.0-SNAPSHOT.zip
# restart Elasticsearch
```

The descriptor pins `elasticsearch.version=9.4.2`. To target a different patch version, bump `<es.version>` in `pom.xml`
and rebuild.

## API

Three new endpoints, mirroring the standard mapping/index endpoints under the `/_flatten/` prefix:

| Endpoint                         | Mirrors                 | Returns                                                               |
|----------------------------------|-------------------------|-----------------------------------------------------------------------|
| `GET /_flatten/_mapping`         | `GET /_mapping`         | All indices' mappings, flattened                                      |
| `GET /_flatten/{index}/_mapping` | `GET /{index}/_mapping` | Single (or comma-separated) index mappings, flattened                 |
| `GET /_flatten/{index}`          | `GET /{index}`          | Full index info (aliases, settings, mappings) with mappings flattened |

### Query parameters

| Param            | Values             | Default    | Meaning                      |
|------------------|--------------------|------------|------------------------------|
| `flatten`        | `leaves`, `dotted` | `leaves`   | Output shape (see below)     |
| `master_timeout` | duration           | ES default | Standard master-node timeout |

Unknown `flatten` values return `400 Bad Request`.

### Flatten modes

Given this mapping:

```json
{
  "users": {
    "mappings": {
      "properties": {
        "user": {
          "properties": {
            "address": {
              "properties": {
                "city": {
                  "type": "keyword"
                }
              }
            },
            "email": {
              "type": "keyword"
            }
          }
        },
        "timestamp": {
          "type": "date"
        }
      }
    }
  }
}
```

**`?flatten=leaves`** (default) emits only leaf fields:

```json
{
  "users": {
    "mappings": {
      "properties": {
        "user.address.city": {
          "type": "keyword"
        },
        "user.email": {
          "type": "keyword"
        },
        "timestamp": {
          "type": "date"
        }
      }
    }
  }
}
```

**`?flatten=dotted`** also includes intermediate object/nested wrappers:

```json
{
  "users": {
    "mappings": {
      "properties": {
        "user": {
          "type": "object"
        },
        "user.address": {
          "type": "object"
        },
        "user.address.city": {
          "type": "keyword"
        },
        "user.email": {
          "type": "keyword"
        },
        "timestamp": {
          "type": "date"
        }
      }
    }
  }
}
```

## Examples

```sh
# all indices, leaf paths only
curl -k 'https://localhost:9200/_flatten/_mapping'

# one index, include interior nodes
curl -k 'https://localhost:9200/_flatten/users/_mapping?flatten=dotted'

# comma-separated indices, full index info
curl -k 'https://localhost:9200/_flatten/users,orders?flatten=leaves'
```

## License

Apache License 2.0 — see [LICENSE](LICENSE).
